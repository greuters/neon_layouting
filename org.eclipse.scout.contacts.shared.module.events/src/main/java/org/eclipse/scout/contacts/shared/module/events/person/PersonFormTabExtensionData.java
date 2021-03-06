package org.eclipse.scout.contacts.shared.module.events.person;

import java.util.Date;

import javax.annotation.Generated;

import org.eclipse.scout.contacts.shared.person.PersonFormData;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Extends(PersonFormData.class)
@Generated(value = "org.eclipse.scout.contacts.client.module.events.person.PersonFormTabExtension", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class PersonFormTabExtensionData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  public Events getEvents() {
    return getFieldByClass(Events.class);
  }

  public static class Events extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    @Override
    public EventsRowData addRow() {
      return (EventsRowData) super.addRow();
    }

    @Override
    public EventsRowData addRow(int rowState) {
      return (EventsRowData) super.addRow(rowState);
    }

    @Override
    public EventsRowData createRow() {
      return new EventsRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return EventsRowData.class;
    }

    @Override
    public EventsRowData[] getRows() {
      return (EventsRowData[]) super.getRows();
    }

    @Override
    public EventsRowData rowAt(int index) {
      return (EventsRowData) super.rowAt(index);
    }

    public void setRows(EventsRowData[] rows) {
      super.setRows(rows);
    }

    public static class EventsRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String id = "id";
      public static final String title = "title";
      public static final String starts = "starts";
      public static final String city = "city";
      public static final String country = "country";
      private String m_id;
      private String m_title;
      private Date m_starts;
      private String m_city;
      private String m_country;

      public String getId() {
        return m_id;
      }

      public void setId(String newId) {
        m_id = newId;
      }

      public String getTitle() {
        return m_title;
      }

      public void setTitle(String newTitle) {
        m_title = newTitle;
      }

      public Date getStarts() {
        return m_starts;
      }

      public void setStarts(Date newStarts) {
        m_starts = newStarts;
      }

      public String getCity() {
        return m_city;
      }

      public void setCity(String newCity) {
        m_city = newCity;
      }

      public String getCountry() {
        return m_country;
      }

      public void setCountry(String newCountry) {
        m_country = newCountry;
      }
    }
  }
}
