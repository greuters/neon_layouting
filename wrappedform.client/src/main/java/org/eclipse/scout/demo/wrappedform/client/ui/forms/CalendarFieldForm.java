package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.CalendarFieldForm.MainBox.CalendarField;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.TEXTS;

public class CalendarFieldForm extends AbstractForm implements IForm {


	public CalendarFieldForm() {
		super();
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("CalendarFieldForm");
	}

	public MainBox getMainBox() {
		return getFieldByClass(MainBox.class);
	}
	
	public CalendarField getCalendarField() {
		return getFieldByClass(CalendarField.class);
	}

	@Order(10)
	public class MainBox extends AbstractGroupBox {
		
		@Order(10)
		@ClassId("ed0594c6-5f57-4bc9-86f4-2b0ba7989d41")
		public class CalendarField extends AbstractCalendarField<AbstractCalendar> {
			
			public class Calendar extends AbstractCalendar {
				
			}
			
			@Override
			protected String getConfiguredLabel() {
				return "Dummy";
			}
		}
	}
}
