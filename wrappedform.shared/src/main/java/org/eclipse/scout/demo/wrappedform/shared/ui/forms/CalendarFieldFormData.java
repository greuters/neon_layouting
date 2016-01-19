package org.eclipse.scout.demo.wrappedform.shared.ui.forms;

import java.util.Date;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.eclipse.scout.demo.wrappedform.client.ui.forms.CalendarFieldForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class CalendarFieldFormData extends AbstractDetachableFormData {

	private static final long serialVersionUID = 1L;

	public CalendarFieldFormData() {
	}

	public Calendar getCalendar() {
		return getFieldByClass(Calendar.class);
	}

	public static class Calendar extends AbstractValueFieldData<Date> {

		private static final long serialVersionUID = 1L;

		public Calendar() {
		}
	}
}
