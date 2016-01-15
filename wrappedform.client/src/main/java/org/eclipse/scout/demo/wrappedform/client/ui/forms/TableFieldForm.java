package org.eclipse.scout.demo.wrappedform.client.ui.forms;

import java.util.Random;

import org.eclipse.scout.demo.wrappedform.shared.ui.forms.TableFieldFormData;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

@FormData(value = TableFieldFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldForm extends AbstractDetachableForm {

	private static String[] NAMES = new String[] { "Eclipsecon USA", "Javaland", "BSI" };
	private static String[] LOCATIONS = new String[] { "San Francisco, USA", "Bruehl, Germany", "Täfernstr, Zurich" };

	@Override
	protected AbstractDetachableForm createNewForm() {
		return new TableFieldForm();
	}

	@Override
	protected AbstractFormData createNewFormData() {
		return new TableFieldFormData();
	}

	public ConfigurationBox.TableField getTableField() {
		return getFieldByClass(ConfigurationBox.TableField.class);
	}

	@Order(30.0)
	@InjectFieldTo(AbstractDetachableForm.MainBox.class)
	public class ConfigurationBox extends AbstractGroupBox {

		@Override
		protected String getConfiguredLabel() {
			return "Table Box";
		}

		@Order(10)
		public class AddRowButton extends AbstractButton {
			@Override
			protected String getConfiguredLabel() {
				return "Add new random row";
			}

			@Override
			protected void execClickAction() {
				getTableField().addRandomRows(1);
			}
		}

		@Order(20)
		public class TableField extends AbstractTableField<TableField.Table> {

			private long m_maxId = 0;
			private Random m_random = new Random(System.currentTimeMillis());

			@Override
			protected int getConfiguredGridH() {
				return 5;
			}

			@Override
			protected int getConfiguredGridW() {
				return 2;
			}

			@Override
			protected String getConfiguredLabel() {
				return "Table field";
			}

			private long getNextId() {
				return ++m_maxId;
			}

			public void addRandomRows(int count) {
				Table table = getTable();
				ITableRow r;

				for (int k = 0; k < count; k++) {
					int nameIdx = m_random.nextInt(NAMES.length);
					int locationIdx = m_random.nextInt(LOCATIONS.length);
					r = table.addRow(getTable().createRow());
					table.getIdColumn().setValue(r, getNextId());
					table.getNameColumn().setValue(r, NAMES[nameIdx]);
					table.getLocationColumn().setValue(r, LOCATIONS[locationIdx]);
				}
			}

			public class Table extends AbstractTable {

				public IdColumn getIdColumn() {
					return getColumnSet().getColumnByClass(IdColumn.class);
				}

				public NameColumn getNameColumn() {
					return getColumnSet().getColumnByClass(NameColumn.class);
				}

				public LocationColumn getLocationColumn() {
					return getColumnSet().getColumnByClass(LocationColumn.class);
				}

				@Order(10)
				public class IdColumn extends AbstractLongColumn {

					@Override
					protected boolean getConfiguredDisplayable() {
						return false;
					}

					@Override
					protected boolean getConfiguredPrimaryKey() {
						return true;
					}

					@Override
					protected boolean getConfiguredVisible() {
						return false;
					}
				}

				@Order(20)
				public class NameColumn extends AbstractStringColumn {

					@Override
					protected boolean getConfiguredEditable() {
						return true;
					}

					@Override
					protected boolean getConfiguredMandatory() {
						return true;
					}

					@Override
					protected String getConfiguredHeaderText() {
						return "Name";
					}

					@Override
					protected int getConfiguredWidth() {
						return 120;
					}

				}

				@Order(30)
				public class LocationColumn extends AbstractStringColumn {

					@Override
					protected boolean getConfiguredEditable() {
						return true;
					}

					@Override
					protected String getConfiguredHeaderText() {
						return "Location";
					}

					@Override
					protected int getConfiguredWidth() {
						return 150;
					}

				}
			}
		}
	}
}
