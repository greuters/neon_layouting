/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.contacts.client.common;

import org.eclipse.scout.contacts.client.common.PictureUrlForm.MainBox.CancelButton;
import org.eclipse.scout.contacts.client.common.PictureUrlForm.MainBox.OkButton;
import org.eclipse.scout.contacts.client.common.PictureUrlForm.MainBox.UrlBox;
import org.eclipse.scout.contacts.client.common.PictureUrlForm.MainBox.UrlBox.PictureUrlField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;

public class PictureUrlForm extends AbstractForm {

  public PictureUrlForm() {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("PictureURL");
  }

  public void startModify() {
    startInternal(new ModifyHandler());
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public PictureUrlField getPictureUrlField() {
    return getFieldByClass(PictureUrlField.class);
  }

  public UrlBox getUrlBox() {
    return getFieldByClass(UrlBox.class);
  }

  @Order(1000)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Order(1000)
    public class UrlBox extends AbstractGroupBox {

      @Order(1000)
      public class PictureUrlField extends AbstractStringField {

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }
      }
    }

    @Order(2000)
    public class OkButton extends AbstractOkButton {
    }

    @Order(3000)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
  }
}
