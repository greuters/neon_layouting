/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;

/**
 *
 */
public abstract class AbstractPositiveIntegerConfigProperty extends AbstractConfigProperty<Integer> {

  private static final Pattern INT_PAT = Pattern.compile("^\\d{1,9}$");

  @Override
  protected Integer parse(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    return Integer.parseInt(value);
  }

  @Override
  protected IProcessingStatus getStatusRaw(String rawValue) {
    // property is not mandatory
    if (!StringUtility.hasText(rawValue)) {
      return ProcessingStatus.OK_STATUS;
    }

    // if specified: it must be a valid integer
    if (INT_PAT.matcher(rawValue).matches()) {
      return ProcessingStatus.OK_STATUS;
    }
    return new ProcessingStatus("Invalid integer value '" + rawValue + "' for property '" + getKey() + "'.", new Exception("origin"), 0, IProcessingStatus.ERROR);
  }
}
