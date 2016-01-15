package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.LeftBox.BottomLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.LeftBox.MiddleLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.LeftBox.TopLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.RightBox.RightWrappedFormField;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.TEXTS;

public class WrappingFormFieldForm extends AbstractDetachableForm implements IForm {
	public WrappingFormFieldForm() {
		super();
	}

	@Override
	protected AbstractDetachableForm createFormCopy() {
		return new WrappingFormFieldForm(); // TODO [sgr]: copy state if
											// necessary
	}

	@Override
	public void initForm() {
		MainBox form = getMainBox();
		form.setGridColumnCountHint(4);
		getTopLeftWrappedFormField().setInnerForm(new CalendarFieldForm());
		TableFieldForm tableForm = new TableFieldForm();
		tableForm.getTableField().addRandomRows(2);
		getMiddleLeftWrappedFormField().setInnerForm(tableForm);
		getBottomLeftWrappedFormField().setInnerForm(new TabBoxForm());
		getRightWrappedFormField().setInnerForm(new SimpleForm("Simple Text"));
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("WrappingFormField");
	}

	public LeftBox getLeftBox() {
		return getFieldByClass(LeftBox.class);
	}

	public RightBox getRightBox() {
		return getFieldByClass(RightBox.class);
	}

	public TopLeftWrappedFormField getTopLeftWrappedFormField() {
		return getFieldByClass(TopLeftWrappedFormField.class);
	}

	public MiddleLeftWrappedFormField getMiddleLeftWrappedFormField() {
		return getFieldByClass(MiddleLeftWrappedFormField.class);
	}

	public BottomLeftWrappedFormField getBottomLeftWrappedFormField() {
		return getFieldByClass(BottomLeftWrappedFormField.class);
	}

	public RightWrappedFormField getRightWrappedFormField() {
		return getFieldByClass(RightWrappedFormField.class);
	}

	@Order(10.0)
	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class LeftBox extends AbstractGroupBox {

		@Override
		protected int getConfiguredGridColumnCount() {
			return 1;
		}

		@Override
		protected int getConfiguredGridW() {
			return 3;
		}

		@Order(10.0)
		public class TopLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
			@Override
			protected int getConfiguredGridH() {
				return 2; // TODO [sgr]: seems not to work
			}
		}

		@Order(20.0)
		public class MiddleLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
			@Override
			protected int getConfiguredGridH() {
				return 1;
			}
		}

		@Order(30.0)
		public class BottomLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
			@Override
			protected int getConfiguredGridH() {
				return 1;
			}
		}

	}

	@Order(20.0)
	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class RightBox extends AbstractGroupBox {
		@Override
		protected int getConfiguredGridW() {
			return 1;
		}

		@Order(10.0)
		public class RightWrappedFormField extends AbstractWrappedFormField<IForm> {
		}

	}
}
