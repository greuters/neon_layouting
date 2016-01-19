package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.TabBoxForm.MainBox.TabBox.BookmarksBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.TabBoxForm.MainBox.TabBox.DetachableBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.TabBoxForm.MainBox.TabBox.StationsBox;
import org.eclipse.scout.demo.wrappedform.shared.ui.forms.TabBoxFormData;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@FormData(value = TabBoxFormData.class, sdkCommand = SdkCommand.CREATE)
public class TabBoxForm extends AbstractForm {

	@Override
	public void initForm() {
		super.initForm();
		getDetachableBox().getWrappedFormField().setInnerForm(new SimpleForm("Text in tab"));
	}

	public BookmarksBox getBookmarksBox() {
		return getFieldByClass(BookmarksBox.class);
	}

	public StationsBox getStationsBox() {
		return getFieldByClass(StationsBox.class);
	}

	public DetachableBox getDetachableBox() {
		return getFieldByClass(DetachableBox.class);
	}

	@Order(10)
	public class MainBox extends AbstractGroupBox {
		@Order(10)
		public class TabBox extends AbstractTabBox {

			public abstract class AbstractSampleBox extends AbstractGroupBox {
				@Override
				protected int getConfiguredGridColumnCount() {
					return 1;
				}
			}

			@Order(10)
			public class BookmarksBox extends AbstractSampleBox {
				@Override
				protected String getConfiguredLabel() {
					return "Favorites";
				}

				public FavoriteField1 getFavoriteField1() {
					return getFieldByClass(FavoriteField1.class);
				}

				public FavoriteField2 getFavoriteField2() {
					return getFieldByClass(FavoriteField2.class);
				}

				@Order(10.0)
				public class FavoriteField1 extends AbstractStringField {
					@Override
					protected String getConfiguredLabel() {
						return "Favorite 1";
					}
				}

				@Order(20.0)
				public class FavoriteField2 extends AbstractStringField {
					@Override
					protected String getConfiguredLabel() {
						return "Favorite 2";
					}
				}
			}

			@Order(20)
			public class StationsBox extends AbstractSampleBox {
				@Override
				protected String getConfiguredLabel() {
					return "Stations";
				}

				public StationField1 getStationField1() {
					return getFieldByClass(StationField1.class);
				}

				public StationField2 getStationField2() {
					return getFieldByClass(StationField2.class);
				}

				@Order(10.0)
				public class StationField1 extends AbstractStringField {
					@Override
					protected String getConfiguredLabel() {
						return "Station 1";
					}
				}

				@Order(20.0)
				public class StationField2 extends AbstractStringField {
					@Override
					protected String getConfiguredLabel() {
						return "Station 2";
					}
				}
			}

			@Order(30)
			public class DetachableBox extends AbstractSampleBox {
				@Override
				protected String getConfiguredLabel() {
					return "Detachable Tab";
				}

				public WrappedFormField getWrappedFormField() {
					return getFieldByClass(WrappedFormField.class);
				}

				@Order(10.0)
				@ClassId("50b63105-4c66-4fbe-abaa-3bf1ef81a833")
				public class WrappedFormField extends AbstractWrappedFormField<IForm> {
				}
			}
		}
	}
}
