package org.eclipse.scout.contacts.shared.template;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "org.eclipse.scout.contacts.client.template.AbstractPictureBox", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractPictureBoxData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  /**
   * access method for property PictureUrl.
   */
  public String getPictureUrl() {
    return getPictureUrlProperty().getValue();
  }

  /**
   * access method for property PictureUrl.
   */
  public void setPictureUrl(String pictureUrl) {
    getPictureUrlProperty().setValue(pictureUrl);
  }

  public PictureUrlProperty getPictureUrlProperty() {
    return getPropertyByClass(PictureUrlProperty.class);
  }

  public static class PictureUrlProperty extends AbstractPropertyData<String> {

    private static final long serialVersionUID = 1L;
  }
}
