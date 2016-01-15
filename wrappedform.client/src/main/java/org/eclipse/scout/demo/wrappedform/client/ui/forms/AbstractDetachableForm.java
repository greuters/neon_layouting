package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ClientSession;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.AbstractDetachableForm.MainBox.CloseButton;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.AbstractDetachableForm.MainBox.OpenInANewWindowButton;
import org.eclipse.scout.demo.wrappedform.shared.ui.forms.AbstractDetachableFormData;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

@ClassId("e23dbb9b-3cf6-40da-8baa-065ab7f9f8d6")
@FormData(value = AbstractDetachableFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public abstract class AbstractDetachableForm extends AbstractForm implements IForm {

	private boolean m_isDetached;

	public AbstractDetachableForm() {
		setDetached(false);
	}

	/**
	 * @return a copy of this form; the default implementation exports and
	 *         re-imports the formData if available, copying most values
	 */
	protected AbstractDetachableForm createFormCopy() {
		AbstractDetachableForm form = createNewForm();
		AbstractFormData formData = createNewFormData();
		if (formData != null) {
			exportFormData(formData);
			form.importFormData(formData);
		}
		return form;
	}

	/**
	 * @return a new, blank form of the derived type
	 */
	protected abstract AbstractDetachableForm createNewForm();

	/**
	 * @return a new form data that can be exported to and imported by the form
	 *         returned by createNewForm. if null is returned, the current state
	 *         will not be copied to the new form when detaching it into a
	 *         window
	 */
	protected AbstractFormData createNewFormData() {
		return null;
	};

	public void startPageForm() {
		startInternal(new PageFormHandler());
	}

	public MainBox getMainBox() {
		return getFieldByClass(MainBox.class);
	}

	public CloseButton getCloseButton() {
		return getFieldByClass(CloseButton.class);
	}

	public OpenInANewWindowButton getOpenInANewWindowButton() {
		return getFieldByClass(OpenInANewWindowButton.class);
	}

	public boolean isDetached() {
		return m_isDetached;
	}

	public void setDetached(boolean isDetached) {
		// getCloseButton().setVisible(isDetached); // if this would be the
		// requested behavior, they can just use the close button of the window
		getOpenInANewWindowButton().setVisible(!isDetached);
		this.m_isDetached = isDetached;
	}

	@Order(10.0)
	@ClassId("4a6168f3-1272-4dc8-b811-2151cc293d28")
	public class MainBox extends AbstractGroupBox {

		@Order(10.0)
		@ClassId("720aacdc-2e82-4329-950e-1b11e4b7c810")
		public class CloseButton extends AbstractCloseButton {
		}

		@Order(20.0)
		@ClassId("e15ef4b6-650d-4222-9dbe-8f79914daa7f")
		public class OpenInANewWindowButton extends AbstractButton {

			@Override
			protected String getConfiguredLabel() {
				return "Open in a new window";
			}

			@Override
			protected void execClickAction() {
				AbstractDetachableForm form = createFormCopy();
				form.setDetached(true);
				form.setDisplayParent(ClientSession.get().getDesktop());
				form.setDisplayHint(IForm.DISPLAY_HINT_POPUP_WINDOW);
				form.setAskIfNeedSave(false);
				form.startPageForm();
				form.waitFor();
			}
		}
	}

	public class PageFormHandler extends AbstractFormHandler {
	}

}
