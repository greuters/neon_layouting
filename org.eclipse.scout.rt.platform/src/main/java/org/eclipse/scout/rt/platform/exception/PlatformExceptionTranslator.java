/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.exception;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Exception translator to work with {@link PlatformException}s.
 * <p>
 * If given a {@link PlatformException}, it is returned as given. For all other exceptions (checked or unchecked), a
 * {@link PlatformException} is returned which wraps the given exception.
 * <p>
 * If given a wrapped exception like {@link UndeclaredThrowableException}, {@link InvocationTargetException} or
 * {@link ExecutionException}, its cause is unwrapped prior translation.
 * <p>
 * If the exception is of the type {@link Error}, it is not translated, but thrown instead. That is because an
 * {@link Error} indicates a serious problem due to a abnormal condition.
 * <p>
 * Typically, this translator is used if you require to add some context-infos via
 * {@link PlatformException#withContextInfo(String, Object, Object...)}.
 */
public class PlatformExceptionTranslator implements IExceptionTranslator<PlatformException> {

  @Override
  public PlatformException translate(final Throwable throwable) {
    final DefaultExceptionTranslator helper = BEANS.get(DefaultExceptionTranslator.class);

    final Throwable t = helper.unwrap(throwable);
    return helper.decorate(translateInternal(t));
  }

  /**
   * Method invoked to translate the given {@link Throwable}.
   */
  protected PlatformException translateInternal(final Throwable t) {
    if (t instanceof Error) {
      throw (Error) t;
    }
    else if (t instanceof PlatformException) {
      return (PlatformException) t;
    }
    else {
      return new PlatformException(StringUtility.nvl(t.getMessage(), t.getClass().getSimpleName()), t)
          .withContextInfo("translator", PlatformExceptionTranslator.class.getName());
    }
  }
}
