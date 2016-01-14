package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.MainBox.LeftBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.MainBox.LeftBox.BottomLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.MainBox.LeftBox.MiddleLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.MainBox.LeftBox.TopLeftWrappedFormField;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.MainBox.RightBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.WrappingFormFieldForm.MainBox.RightBox.RightWrappedFormField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;

public class WrappingFormFieldForm extends AbstractForm implements IForm {
	public WrappingFormFieldForm() {
		super();
	}

	  @Override
	public void initForm() {
				  getTopLeftWrappedFormField().setInnerForm(new CalendarFieldForm());
				  getMiddleLeftWrappedFormField().setInnerForm(new SimpleForm());
				  getBottomLeftWrappedFormField().setInnerForm(new StringFieldForm());
				  SimpleForm placeholderForm = new SimpleForm();
				  placeholderForm.getField1().setLabel("Placeholder");
				  getRightWrappedFormField().setInnerForm(placeholderForm);
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("WrappingFormField");
	}

	public MainBox getMainBox() {
		return getFieldByClass(MainBox.class);
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

	  @Order(10)
	public class MainBox extends AbstractGroupBox {

		  @Override
		protected int getConfiguredGridColumnCount() {
			return 4;
		}
		  
		  @Order(10)
		  public class LeftBox extends AbstractGroupBox {
			  
			  @Override
			protected int getConfiguredGridColumnCount() {
				return 1;
			}
			  
			  @Override
			protected int getConfiguredGridW() {
			return 3;
			}
			  
			  @Order(10)
			    public class TopLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
			    }
				
			    @Order(20)
			    public class MiddleLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
			    }

			    @Order(30)
			    public class BottomLeftWrappedFormField extends AbstractWrappedFormField<IForm> {
		}
			  
		  }
		  
		  @Order(20)
		  public class RightBox extends AbstractGroupBox {
			  @Override
			protected int getConfiguredGridW() {
			return 1;
			}

			    @Order(10)
			    public class RightWrappedFormField extends AbstractWrappedFormField<IForm> {
		}

		  }
}
}
