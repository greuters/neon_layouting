package org.eclipse.scout.demo.wrappedform.shared.ui.forms;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.eclipse.scout.demo.wrappedform.client.ui.forms.TabBoxForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class TabBoxFormData extends AbstractDetachableFormData {

	private static final long serialVersionUID = 1L;

	public TabBoxFormData() {
	}

	public FavoriteField1 getFavoriteField1() {
		return getFieldByClass(FavoriteField1.class);
	}

	public FavoriteField2 getFavoriteField2() {
		return getFieldByClass(FavoriteField2.class);
	}

	public StationField1 getStationField1() {
		return getFieldByClass(StationField1.class);
	}

	public StationField2 getStationField2() {
		return getFieldByClass(StationField2.class);
	}

	public static class FavoriteField1 extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;

		public FavoriteField1() {
		}
	}

	public static class FavoriteField2 extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;

		public FavoriteField2() {
		}
	}

	public static class StationField1 extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;

		public StationField1() {
		}
	}

	public static class StationField2 extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;

		public StationField2() {
		}
	}
}
