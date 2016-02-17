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
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.ui.html.json.AbstractEventFilter;

@Bean
public class DesktopEventFilter extends AbstractEventFilter<DesktopEvent, DesktopEventFilterCondition> {

  public DesktopEventFilter() {
  }

  @Override
  public DesktopEvent filter(DesktopEvent event) {
    for (DesktopEventFilterCondition condition : getConditions()) {
      if (condition.getType() == event.getType()) {
        if (CompareUtility.equals(event.getForm(), condition.getForm())) {
          return null;
        }
        if (condition.isCheckDisplayParents() && checkIfEventOnParent(condition.getForm(), event)) {
          return null;
        }
      }
    }
    return event;
  }

  private boolean checkIfEventOnParent(IForm form, DesktopEvent event) {
    if (CompareUtility.equals(event.getForm(), form)) {
      return true;
    }
    else if (form.getDisplayParent() instanceof IForm) {
      return checkIfEventOnParent((IForm) form.getDisplayParent(), event);
    }
    return false;
  }
}
