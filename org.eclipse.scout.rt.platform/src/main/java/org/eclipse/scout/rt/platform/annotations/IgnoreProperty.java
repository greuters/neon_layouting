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
package org.eclipse.scout.rt.platform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.reflect.FastBeanInfo;

/**
 * This annotation is used to mark bean properties to be ignored in certain translations. This annotation must be placed
 * on the getter method of the bean property. Classes like {@link FastBeanInfo} do not have any knowledge about this
 * annotation. The user of {@link FastBeanInfo} and similar must check the annotation on the reader (getter) method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreProperty {
  public enum Context {
    GUI
  }

  Context value() default Context.GUI;

}
