package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.SimpleForm.MainBox.Field1;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.TEXTS;

public class SimpleForm extends AbstractForm implements IForm {

	public SimpleForm() {
		super();
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("SimpleForm");
	}

	public MainBox getMainBox() {
		return getFieldByClass(MainBox.class);
	}
	
	public Field1 getField1() {
		return getFieldByClass(Field1.class);
	}

	@Order(10)
	public class MainBox extends AbstractGroupBox {
		
		@Order(10)
		@ClassId("ed0594c6-5f57-4bc9-86f4-2b0ba7989d41")
		public class Field1 extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return "Dummy";
			}
		}
	}

}
