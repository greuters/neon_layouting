package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.ContainerBox.LeftBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.ContainerBox.LeftBox.BottomLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.ContainerBox.LeftBox.MiddleLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.ContainerBox.LeftBox.TopLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.ContainerBox.RightBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.ContainerBox.RightBox.RightWrappedFormField;
import org.eclipse.scout.demo.wrappedform.shared.ui.forms.WrappedFormFieldFormData;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

@ClassId("e55cf14b-8005-42d4-a54d-4fedf7ebec88")
@FormData(value = WrappedFormFieldFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class WrappedFormFieldForm extends AbstractDetachableForm implements IForm {

	@Override
	protected AbstractDetachableForm createNewForm() {
		return new WrappedFormFieldForm();
	}

	@Override
	protected AbstractFormData createNewFormData() {
		return new WrappedFormFieldFormData();
	}

	@Override
	public void initForm() {
		getTopLeftWrappedFormField().setInnerForm(new CalendarFieldForm());
		TableFieldForm tableForm = new TableFieldForm();
		tableForm.getTableField().addRandomRows(2);
		getMiddleLeftWrappedFormField().setInnerForm(tableForm);
		getBottomLeftWrappedFormField().setInnerForm(new TabBoxForm());
		getRightWrappedFormField().setInnerForm(new SimpleForm("Simple Text"));
	}

	@Override
	protected String getConfiguredTitle() {
		return "wrapped form field form";
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

	@Order(30.0)
	@ClassId("a0cdddb4-06f1-4670-a06a-43f465fbe1a7")
	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class ContainerBox extends AbstractGroupBox {

		@Override
		protected int getConfiguredGridColumnCount() {
			return 4;
		}

		@Order(10.0)
		@ClassId("b9b92b68-2cd1-44ee-9491-b185d1c8b51e")
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
			@ClassId("5873ecf8-b578-4d6e-a427-6d74dfee7a7b")
			public class TopLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
				@Override
				protected int getConfiguredGridH() {
					return 2;
				}

				@Override
				protected double getConfiguredGridWeightY() {
					return -1;
				}
			}

			@Order(20.0)
			@ClassId("c203965c-bafa-4d5a-8f18-850a7de375f2")
			public class MiddleLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
				@Override
				protected int getConfiguredGridH() {
					return 1;
				}

				@Override
				protected double getConfiguredGridWeightY() {
					return -1;
				}
			}

			@Order(30.0)
			@ClassId("5d154f3a-9415-481f-bbad-2f34cef875d5")
			public class BottomLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
				@Override
				protected int getConfiguredGridH() {
					return 1;
				}

				@Override
				protected double getConfiguredGridWeightY() {
					return -1;
				}
			}

		}

		@Order(20.0)
		@ClassId("62c038a2-98d7-443e-b874-f2507d8e9795")
		public class RightBox extends AbstractGroupBox {
			@Override
			protected int getConfiguredGridW() {
				return 1;
			}

			@Order(10.0)
			@ClassId("f9ed9d21-b0b8-40aa-af1c-7b8a88d3abce")
			public class RightWrappedFormField extends AbstractWrappedFormField<IForm> {
			}

		}
	}
}
