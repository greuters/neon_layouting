/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.beans.PropertyChangeEvent;

public class PropertyEventFilter extends AbstractEventFilter<PropertyChangeEvent, PropertyChangeEventFilterCondition> {

  public PropertyEventFilter() {
  }

  /**
   * Ignores the event if new value is the same.
   */
  @Override
  public PropertyChangeEvent filter(PropertyChangeEvent event) {
    for (PropertyChangeEventFilterCondition condition : getConditions()) {
      if (condition.getPropertyName().equals(event.getPropertyName())) {
        // Ignore if null == null
        if (condition.getValue() == null) {
          if (event.getNewValue() == null) {
            return null;
          }
        }
        // Ignore if value is the same
        else if (condition.getValue().equals(event.getNewValue())) {
          return null;
        }
        // When value is not ignored, we update the value to filter
        condition.updateValue(event.getNewValue());
      }
    }
    return event;
  }

}
