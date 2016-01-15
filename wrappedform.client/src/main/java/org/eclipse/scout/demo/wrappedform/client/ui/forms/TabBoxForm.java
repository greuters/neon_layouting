package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import org.eclipse.scout.demo.wrappedform.client.ui.forms.TabBoxForm.TabBox.BookmarksBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.TabBoxForm.TabBox.PeopleBox;
import org.eclipse.scout.demo.wrappedform.client.ui.forms.TabBoxForm.TabBox.StationsBox;
import org.eclipse.scout.demo.wrappedform.shared.ui.forms.TabBoxFormData;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

@FormData(value = TabBoxFormData.class, sdkCommand = SdkCommand.CREATE)
public class TabBoxForm extends AbstractDetachableForm {

	@Override
	protected AbstractDetachableForm createNewForm() {
		return new TabBoxForm();
	}

	@Override
	protected AbstractFormData createNewFormData() {
		return new TabBoxFormData();
	}

	public BookmarksBox getBookmarksBox() {
		return getFieldByClass(BookmarksBox.class);
	}

	public StationsBox getStationsBox() {
		return getFieldByClass(StationsBox.class);
	}

	public PeopleBox getPeopleBox() {
		return getFieldByClass(PeopleBox.class);
	}

	@Order(30.0)
	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
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
				return ScoutTexts.get("Bookmarks");
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
		public class PeopleBox extends AbstractSampleBox {
			@Override
			protected String getConfiguredLabel() {
				return "People";
			}

			public PersonField1 getPersonField1() {
				return getFieldByClass(PersonField1.class);
			}

			public PersonField2 getPersonField2() {
				return getFieldByClass(PersonField2.class);
			}

			@Order(10.0)
			public class PersonField1 extends AbstractStringField {
				@Override
				protected String getConfiguredLabel() {
					return "Person 1";
				}
			}

			@Order(20.0)
			public class PersonField2 extends AbstractStringField {
				@Override
				protected String getConfiguredLabel() {
					return "Person 2";
				}
			}
		}

	}
}
