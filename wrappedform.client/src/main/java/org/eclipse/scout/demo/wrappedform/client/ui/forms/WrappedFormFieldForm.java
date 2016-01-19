package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.MainBox.VerticalSplitBox.LeftBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.MainBox.VerticalSplitBox.LeftBox.TopLeftSplitBox.BottomLeftSplitBox.BottomLeftWrappingBox.BottomLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.MainBox.VerticalSplitBox.LeftBox.TopLeftSplitBox.BottomLeftSplitBox.MiddleLeftWrappingBox.MiddleLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.MainBox.VerticalSplitBox.LeftBox.TopLeftSplitBox.TopLeftWrappingBox.TopLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.MainBox.VerticalSplitBox.RightBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappedFormFieldForm.MainBox.VerticalSplitBox.RightBox.RightWrappedFormField;
import org.eclipse.scout.demo.wrappedform.shared.Icons;
import org.eclipse.scout.demo.wrappedform.shared.ui.forms.WrappedFormFieldFormData;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("e55cf14b-8005-42d4-a54d-4fedf7ebec88")
@FormData(value = WrappedFormFieldFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class WrappedFormFieldForm extends AbstractForm implements IForm {

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

	@Order(10.0)
	@ClassId("a0cdddb4-06f1-4670-a06a-43f465fbe1a7")
	public class MainBox extends AbstractGroupBox {
		@Order(10)
		public class UserIconMenu extends AbstractMenu {

			@Override
			protected String getConfiguredIconId() {
				return Icons.User;
			}

			@Override
			protected String getConfiguredTooltipText() {
				return "User Icon Menu";
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected void execAction() {
				MessageBoxes.createOk().withHeader("You clicked me!").show();
			}
		}

		@Order(20)
		public class ScoutIconMenu extends AbstractMenu {

			@Override
			protected String getConfiguredIconId() {
				return Icons.Scout;
			}

			@Override
			protected String getConfiguredTooltipText() {
				return "Scout Icon Menu";
			}

			@Override
			protected void execAction() {
				MessageBoxes.createOk().withHeader("You clicked me!").show();
			}
		}

		@Order(30)
		public class VerticalSplitBox extends AbstractSplitBox {

			@Override
			protected double getConfiguredSplitterPosition() {
				return 0.8;
			}

			@Order(10.0)
			@ClassId("b9b92b68-2cd1-44ee-9491-b185d1c8b51e")
			public class LeftBox extends AbstractGroupBox {

				@Order(10.0)
				@ClassId("30de5b70-d986-42b0-9787-7462be85d437")
				public class TopLeftSplitBox extends AbstractSplitBox {
					@Override
					protected double getConfiguredSplitterPosition() {
						return 0.2;
					}

					@Override
					protected boolean getConfiguredSplitHorizontal() {
						return false;
					}

					@Order(10.0)
					@ClassId("8cc17615-c007-4cb0-b91c-d4da2b4f0de7")
					public class TopLeftWrappingBox extends AbstractGroupBox {

						@Order(10.0)
						@ClassId("5873ecf8-b578-4d6e-a427-6d74dfee7a7b")
						public class TopLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
						}
					}

					@Order(20.0)
					@ClassId("f032d8d7-4a1e-45a4-a4b9-ac7521dbb45b")
					public class BottomLeftSplitBox extends AbstractSplitBox {
						@Override
						protected boolean getConfiguredSplitHorizontal() {
							return false;
						}

						@Override
						protected double getConfiguredSplitterPosition() {
							return 0.6;
						}

						@Order(10.0)
						@ClassId("c745a401-6baa-46db-bfea-66cc05318271")
						public class MiddleLeftWrappingBox extends AbstractGroupBox {
							@Order(20.0)
							@ClassId("c203965c-bafa-4d5a-8f18-850a7de375f2")
							public class MiddleLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
							}
						}

						@Order(20.0)
						@ClassId("59a2bdb1-00ff-4696-b84d-6a1566007a15")
						public class BottomLeftWrappingBox extends AbstractGroupBox {
							@Order(30.0)
							@ClassId("5d154f3a-9415-481f-bbad-2f34cef875d5")
							public class BottomLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
							}
						}
					}
				}

			}

			@Order(20.0)
			@ClassId("62c038a2-98d7-443e-b874-f2507d8e9795")
			public class RightBox extends AbstractGroupBox {
				@Order(10.0)
				@ClassId("f9ed9d21-b0b8-40aa-af1c-7b8a88d3abce")
				public class RightWrappedFormField extends AbstractWrappedFormField<IForm> {
				}

			}
		}
	}
}
