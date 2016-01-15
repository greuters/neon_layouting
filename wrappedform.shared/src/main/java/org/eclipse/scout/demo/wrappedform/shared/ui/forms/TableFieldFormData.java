package org.eclipse.scout.demo.wrappedform.shared.ui.forms;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.eclipse.scout.demo.wrappedform.client.ui.forms.TableFieldForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class TableFieldFormData extends AbstractDetachableFormData {

	private static final long serialVersionUID = 1L;

	public TableFieldFormData() {
	}

	public Table getTable() {
		return getFieldByClass(Table.class);
	}

	public static class Table extends AbstractTableFieldBeanData {

		private static final long serialVersionUID = 1L;

		public Table() {
		}

		@Override
		public TableRowData addRow() {
			return (TableRowData) super.addRow();
		}

		@Override
		public TableRowData addRow(int rowState) {
			return (TableRowData) super.addRow(rowState);
		}

		@Override
		public TableRowData createRow() {
			return new TableRowData();
		}

		@Override
		public Class<? extends AbstractTableRowData> getRowType() {
			return TableRowData.class;
		}

		@Override
		public TableRowData[] getRows() {
			return (TableRowData[]) super.getRows();
		}

		@Override
		public TableRowData rowAt(int index) {
			return (TableRowData) super.rowAt(index);
		}

		public void setRows(TableRowData[] rows) {
			super.setRows(rows);
		}

		public static class TableRowData extends AbstractTableRowData {

			private static final long serialVersionUID = 1L;
			public static final String id = "id";
			public static final String name = "name";
			public static final String location = "location";
			private Long m_id;
			private String m_name;
			private String m_location;

			public TableRowData() {
			}

			public Long getId() {
				return m_id;
			}

			public void setId(Long id) {
				m_id = id;
			}

			public String getName() {
				return m_name;
			}

			public void setName(String name) {
				m_name = name;
			}

			public String getLocation() {
				return m_location;
			}

			public void setLocation(String location) {
				m_location = location;
			}
		}
	}
}