package org.eclipse.scout.rt.shared.extension.dto.fixture;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigPageWithTableData.OrigPageWithTableRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Extends(OrigPageWithTableRowData.class)
@Generated(value = "org.eclipse.scout.rt.shared.extension.dto.fixture.MultiColumnExtension", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class MultiColumnExtensionData implements Serializable {

  private static final long serialVersionUID = 1L;
  public static final String thirdLong = "thirdLong";
  public static final String fourthBigDecimal = "fourthBigDecimal";
  private Long m_thirdLong;
  private BigDecimal m_fourthBigDecimal;

  public Long getThirdLong() {
    return m_thirdLong;
  }

  public void setThirdLong(Long newThirdLong) {
    m_thirdLong = newThirdLong;
  }

  public BigDecimal getFourthBigDecimal() {
    return m_fourthBigDecimal;
  }

  public void setFourthBigDecimal(BigDecimal newFourthBigDecimal) {
    m_fourthBigDecimal = newFourthBigDecimal;
  }
}