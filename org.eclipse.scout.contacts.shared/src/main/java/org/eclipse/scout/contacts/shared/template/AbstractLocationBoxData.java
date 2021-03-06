package org.eclipse.scout.contacts.shared.template;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "org.eclipse.scout.contacts.client.template.AbstractLocationBox", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractLocationBoxData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  public City getCity() {
    return getFieldByClass(City.class);
  }

  public Country getCountry() {
    return getFieldByClass(Country.class);
  }

  public static class City extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class Country extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }
}
