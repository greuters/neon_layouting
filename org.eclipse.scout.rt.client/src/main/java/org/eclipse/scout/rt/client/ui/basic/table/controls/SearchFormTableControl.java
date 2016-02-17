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
package org.eclipse.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * @since 5.1.0
 */
public class SearchFormTableControl extends AbstractTableControl {

  @Override
  protected void initConfig() {
    super.initConfig();
    setIconId(AbstractIcons.Search);
    setTooltipText(TEXTS.get("Search"));
  }

  @Override
  public void setForm(IForm form) {
    setEnabled(form != null);
    super.setForm(form);
  }

  @Override
  protected boolean getConfiguredEnabled() {
    return false;
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.F6;
  }
}
