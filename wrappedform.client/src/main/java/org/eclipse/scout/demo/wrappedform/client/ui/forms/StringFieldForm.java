/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;

public class StringFieldForm extends AbstractForm implements IForm {

	public StringFieldForm() {
		super();
	}

	@Override
	protected boolean getConfiguredAskIfNeedSave() {
		return false;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("StringField");
	}

	public MainBox getMainBox() {
		return getFieldByClass(MainBox.class);
	}

	@Order(10)
	public class MainBox extends AbstractGroupBox {

		@Order(10)
		public class ExamplesBox extends AbstractGroupBox {

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Examples");
			}

			@Order(10)
			public class DefaultField extends AbstractStringField {

				@Override
				protected String getConfiguredLabel() {
					return "&Default";
				}
			}

			@Order(20)
			public class MandatoryField extends AbstractStringField {

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Mandatory");
				}

				@Override
				protected boolean getConfiguredMandatory() {
					return true;
				}

				@Override
				protected String execValidateValue(String rawValue) {
					if (StringUtility.isNullOrEmpty(rawValue)) {
						throw new VetoException("Field content must not be empty");
					}
					return rawValue;
				}
			}

			@Order(25)
			public class InsertText1Button extends AbstractButton {
				@Override
				protected void execInitField() {
					super.execInitField();
				}

				@Override
				protected boolean getConfiguredProcessButton() {
					return false;
				}

				@Override
				protected int getConfiguredDisplayStyle() {
					return DISPLAY_STYLE_LINK;
				}

				@Override
				protected void execClickAction() {
				}
			}

			@Order(30)
			public class DisabledField extends AbstractStringField {

				@Override
				protected boolean getConfiguredEnabled() {
					return false;
				}

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Disabled");
				}

				@Override
				protected void execInitField() {
					setValue("Text in disabled Field");
				}
			}

			@Order(40)
			public class StyledField extends AbstractStringField {

				@Override
				protected String getConfiguredBackgroundColor() {
					return "FDFFAA";
				}

				@Override
				protected String getConfiguredFont() {
					return "BOLD";
				}

				@Override
				protected String getConfiguredForegroundColor() {
					return "0080C0";
				}

				@Override
				protected String getConfiguredFormat() {
					return "j";
				}

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Styled");
				}
			}

			@Order(50)
			public class OnFieldLabelField extends AbstractStringField {

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("OnFieldLabel");
				}

				@Override
				protected int getConfiguredLabelHorizontalAlignment() {
					return -1;
				}

				@Override
				protected int getConfiguredLabelPosition() {
					return LABEL_POSITION_ON_FIELD;
				}
			}

			@Order(60)
			public class LabelLeftField extends AbstractStringField {

				@Override
				protected int getConfiguredLabelHorizontalAlignment() {
					return -1;
				}

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("LabelLeft");
				}
			}

			@Order(70)
			public class LabelCenterField extends AbstractStringField {

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("LabelCenter");
				}

				@Override
				protected int getConfiguredLabelHorizontalAlignment() {
					return 0;
				}
			}

			@Order(80)
			public class LabelRightField extends AbstractStringField {

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("LabelRight");
				}

				@Override
				protected int getConfiguredLabelHorizontalAlignment() {
					return 1;
				}
			}
		}

		@Order(20)
		public class ConfigurationBox extends AbstractGroupBox {

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Configure");
			}

			@Order(60)
			public class TextInputField extends AbstractStringField {

				@Override
				protected void execChangedDisplayText() {
				}

				@Override
				protected int getConfiguredGridH() {
					return 5;
				}

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("MultilineText");
				}

				@Override
				protected boolean getConfiguredSelectionTrackingEnabled() {
					return true;
				}

				@Override
				protected boolean getConfiguredMultilineText() {
					return true;
				}

			}

			@Order(61)
			public class CharCountBox extends AbstractSequenceBox {

				@Order(1000)
				public class NumCharsField extends AbstractIntegerField {

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("NumChars");
					}

					@Override
					protected boolean getConfiguredEnabled() {
						return false;
					}
				}

				@Order(2000)
				public class CountWhileTypingField extends AbstractBooleanField {

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("CountWhileTyping");
					}

					@Override
					protected void execInitField() {
					}

					@Override
					protected void execChangedValue() {
					}
				}
			}

			@Order(70)
			public class FontNameField extends AbstractStringField {

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("Font");
				}

				@Override
				protected String getConfiguredLabelFont() {
					return "ITALIC";
				}

				@Override
				protected void execChangedValue() {
				}
			}

			@Order(150)
			public class InputMaskedField extends AbstractStringField {

				@Override
				protected boolean getConfiguredInputMasked() {
					return true;
				}

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("InputMasked");
				}
			}

			@Order(154)
			public class HasActionField extends AbstractStringField {

				@Override
				protected boolean getConfiguredHasAction() {
					return true;
				}

				@Override
				protected void execAction() {
					super.execAction();
					MessageBoxes.createOk().withHeader(getValue()).show();
				}

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("HasAction");
				}
			}

			@Order(40)
			public class CloseButton extends AbstractCloseButton {
			}
		}

	}
	public class PageFormHandler extends AbstractFormHandler {
	}
}