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
package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.action.tool.AbstractToolButtonExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.FormToolButtonChains.FormToolButtonInitFormChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;

public abstract class AbstractFormToolButtonExtension<FORM extends IForm, OWNER extends AbstractFormToolButton<FORM>> extends AbstractToolButtonExtension<OWNER> implements IFormToolButtonExtension<FORM, OWNER> {

  public AbstractFormToolButtonExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execInitForm(FormToolButtonInitFormChain<FORM> chain, FORM form) {
    chain.execInitForm(form);
  }
}
