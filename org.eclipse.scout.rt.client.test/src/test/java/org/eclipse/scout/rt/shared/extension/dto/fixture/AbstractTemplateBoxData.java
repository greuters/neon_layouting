package org.eclipse.scout.rt.shared.extension.dto.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "org.eclipse.scout.rt.shared.extension.dto.fixture.AbstractTemplateBox", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractTemplateBoxData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  public FirstStringInTemplate getFirstStringInTemplate() {
    return getFieldByClass(FirstStringInTemplate.class);
  }

  public SecondStringInTemplate getSecondStringInTemplate() {
    return getFieldByClass(SecondStringInTemplate.class);
  }

  public ThirdStringInTemplate getThirdStringInTemplate() {
    return getFieldByClass(ThirdStringInTemplate.class);
  }

  public static class FirstStringInTemplate extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class SecondStringInTemplate extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class ThirdStringInTemplate extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }
}
