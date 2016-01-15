package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.shared.ui.forms.CalendarFieldFormData;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

@FormData(value = CalendarFieldFormData.class, sdkCommand = SdkCommand.CREATE)
public class CalendarFieldForm extends AbstractDetachableForm implements IForm {

	public CalendarFieldForm() {
		super();
	}

	@Override
	protected AbstractDetachableForm createNewForm() {
		return new CalendarFieldForm();
	}

	@Override
	protected AbstractFormData createNewFormData() {
		return new CalendarFieldFormData();
	}

	@Override
	protected String getConfiguredTitle() {
		return "Calendar Field Form";
	}

	public CalendarField getCalendarField() {
		return getFieldByClass(CalendarField.class);
	}

	@Order(30.0)
	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class CalendarField extends AbstractCalendarField<AbstractCalendar> {

		public class Calendar extends AbstractCalendar {

			@Override
			public void initCalendar() {
				super.initCalendar();
				setDisplayMode(2); // DISPLAY_MODE_WEEK
			}
		}

		@Override
		protected boolean getConfiguredLabelVisible() {
			return false;
		}
	}
}
