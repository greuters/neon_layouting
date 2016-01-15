package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ClientSession;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.AbstractDetachableForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;

public abstract class AbstractDetachableForm extends AbstractForm implements IForm {

	protected abstract AbstractDetachableForm createFormCopy();

	public void startPageForm() {
		startInternal(new PageFormHandler());
	}

	public CloseButton getCloseButton() {
		return getFieldByClass(CloseButton.class);
	}

	public MainBox getMainBox() {
		return getFieldByClass(MainBox.class);
	}

	@Order(10.0)
	public class MainBox extends AbstractGroupBox {

		@Order(9998.0)
		public class CloseButton extends AbstractCloseButton {
		}

		@Order(9999.0)
		public class OpenInANewWindowButton extends AbstractButton {

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("OpenInANewWindow");
			}

			@Override
			protected void execClickAction() {
				AbstractDetachableForm form = createFormCopy();
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
