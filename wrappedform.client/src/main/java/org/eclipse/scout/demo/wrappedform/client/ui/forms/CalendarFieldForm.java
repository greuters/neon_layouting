package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.TEXTS;

public class CalendarFieldForm extends AbstractDetachableForm implements IForm {

	public CalendarFieldForm() {
		super();
	}

	@Override
	protected AbstractDetachableForm createFormCopy() {
		return new CalendarFieldForm(); // TODO [sgr]: copy state if necessary
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("CalendarFieldForm");
	}

	public CalendarField getCalendarField() {
		return getFieldByClass(CalendarField.class);
	}

	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class CalendarField extends AbstractCalendarField<AbstractCalendar> {

		public class Calendar extends AbstractCalendar {

		}

		@Override
		protected boolean getConfiguredLabelVisible() {
			return false;
		}
	}
}
