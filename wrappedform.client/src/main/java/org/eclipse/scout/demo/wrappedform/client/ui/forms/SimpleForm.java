package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.rt.client.ui.form.IForm;
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
	protected AbstractDetachableForm createFormCopy() {
		SimpleForm form = new SimpleForm(getField1().getLabel());
		form.getField1().setValue(getField1().getValue());
		return form;
	}

	@Override
	public void initForm() {
		super.initForm();
		getField1().setLabel(m_label);
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("SimpleForm");
	}

	public Field1 getField1() {
		return getFieldByClass(Field1.class);
	}

	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class Field1 extends AbstractStringField {
	}

}
