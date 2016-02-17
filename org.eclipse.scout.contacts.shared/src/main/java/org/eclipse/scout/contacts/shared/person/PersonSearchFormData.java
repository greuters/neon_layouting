package org.eclipse.scout.contacts.shared.person;

import javax.annotation.Generated;

import org.eclipse.scout.contacts.shared.template.AbstractLocationBoxData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "org.eclipse.scout.contacts.client.person.PersonSearchForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class PersonSearchFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public FirstName getFirstName() {
    return getFieldByClass(FirstName.class);
  }

  public LastName getLastName() {
    return getFieldByClass(LastName.class);
  }

  public Location getLocation() {
    return getFieldByClass(Location.class);
  }

  public Organization getOrganization() {
    return getFieldByClass(Organization.class);
  }

  public static class FirstName extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class LastName extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class Location extends AbstractLocationBoxData {

    private static final long serialVersionUID = 1L;
  }

  public static class Organization extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }
}
