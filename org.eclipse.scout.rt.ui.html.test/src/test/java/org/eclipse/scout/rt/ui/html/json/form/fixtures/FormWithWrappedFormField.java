/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fixtures;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithWrappedFormField.MainBox.WrappedFormField;

public class FormWithWrappedFormField extends AbstractForm {

  public FormWithWrappedFormField() throws ProcessingException {
    super();
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class WrappedFormField extends AbstractWrappedFormField<IForm> {
    }
  }

  @Override
  public void start() throws ProcessingException {
    startInternal(new FormHandler());
  }

  public class FormHandler extends AbstractFormHandler {
    @Override
    protected void execLoad() throws ProcessingException {
    }

    @Override
    protected void execPostLoad() throws ProcessingException {
      FormWithOneField f = new FormWithOneField();
      getFieldByClass(WrappedFormField.class).setInnerForm(f, true);
      getFieldByClass(WrappedFormField.class).setInnerForm(null, true);
    }
  }
}
