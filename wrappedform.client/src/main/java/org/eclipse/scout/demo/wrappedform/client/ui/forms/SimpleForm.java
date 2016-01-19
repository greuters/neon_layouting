package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.SimpleForm.GroupBox.Field1;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.TEXTS;

public class SimpleForm extends AbstractDetachableForm implements IForm {

	private String m_label;

	public SimpleForm(String label) {
		super();
		m_label = label;
	}

	@Override
	protected AbstractDetachableForm createNewForm() {
		// shallow copy instead of using the default mechanism via formData
		SimpleForm form = new SimpleForm(getField1().getLabel());
		form.getField1().setValue(getField1().getValue());
		return form;
	}

	@Override
	public void initForm() {
		super.initForm();
		getField1().setLabel(m_label);
		getGroupBox().setLabelVisible(isDetached());
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("SimpleForm");
	}

	public Field1 getField1() {
		return getFieldByClass(Field1.class);
	}

	public GroupBox getGroupBox() {
		return getFieldByClass(GroupBox.class);
	}

	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class GroupBox extends AbstractGroupBox {
		@Override
		protected String getConfiguredLabel() {
			return "Simple Box";
		}

		public class Field1 extends AbstractStringField {
		}
	}
}
