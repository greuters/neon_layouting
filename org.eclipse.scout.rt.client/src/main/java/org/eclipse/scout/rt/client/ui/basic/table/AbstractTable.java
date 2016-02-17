/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableContentChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCopyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCreateTableRowDataMapperChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateRowChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDisposeTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDragChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDropChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableResetColumnsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsCheckedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsSelectedChain;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.TableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizerProvider;
import org.eclipse.scout.rt.client.ui.basic.table.internal.InternalTableRow;
import org.eclipse.scout.rt.client.ui.basic.table.menus.OrganizeColumnsMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ITableOrganizer;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ITableOrganizerProvider;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.UserTableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilter;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TextTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Columns are defined as inner classes<br>
 * for every inner column class there is a generated getXYColumn method directly on the table
 */
public abstract class AbstractTable extends AbstractPropertyObserver implements ITable, IContributionOwner, IExtensibleObject {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTable.class);

  public interface IResetColumnsOption {
    String VISIBILITY = "visibility";
    String ORDER = "order";
    String SORTING = "sorting";
    String WIDTHS = "widths";
    String BACKGROUND_EFFECTS = "backgroundEffects";
  }

  private boolean m_initialized;
  private final OptimisticLock m_initLock;
  private ColumnSet m_columnSet;
  /**
   * synchronized list
   */
  private final List<ITableRow> m_rows;

  private final Object m_cachedRowsLock;
  private List<ITableRow> m_cachedRows;

  private final Map<CompositeObject, ITableRow> m_deletedRows;
  private List<ITableRow/* ordered by rowIndex */> m_selectedRows = new ArrayList<ITableRow>();
  private Set<ITableRow/* ordered by rowIndex */> m_checkedRows = new LinkedHashSet<ITableRow>();
  private Map<Class<?>, Class<? extends IMenu>> m_menuReplacementMapping;
  private ITableUIFacade m_uiFacade;
  private final List<ITableRowFilter> m_rowFilters;
  private String m_userPreferenceContext;
  // batch mutation
  private boolean m_autoDiscardOnDelete;
  private boolean m_sortValid;
  private boolean m_initialMultiLineText;
  private int m_tableChanging;
  private AbstractEventBuffer<TableEvent> m_eventBuffer;
  private int m_eventBufferLoopDetection;

  private HashSet<ITableRow> m_rowDecorationBuffer = new HashSet<ITableRow>();
  private Map<Integer, Set<ITableRow>> m_rowValueChangeBuffer = new HashMap<>();

  // key stroke buffer for select-as-you-type
  private final KeyStrokeBuffer m_keyStrokeBuffer;
  private final EventListenerList m_listenerList = new EventListenerList();
  //cell editing
  private P_CellEditorContext m_editContext;
  //checkable table
  private IBooleanColumn m_checkableColumn;
  //auto filter
  private final Object m_cachedFilteredRowsLock;
  private List<ITableRow> m_cachedFilteredRows;
  private IEventHistory<TableEvent> m_eventHistory;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractTable, ITableExtension<? extends AbstractTable>> m_objectExtensions;
  // only do one action at a time
  private boolean m_actionRunning;
  private List<ITableControl> m_tableControls;
  private IReloadHandler m_reloadHandler;
  private int m_valueChangeTriggerEnabled = 1;// >=1 is true
//  private IOrganizeColumnsForm m_organizeColumnsForm;
  private ITableOrganizer m_tableOrganizer;

  public AbstractTable() {
    this(true);
  }

  public AbstractTable(boolean callInitializer) {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTable(this);
    }
    m_cachedRowsLock = new Object();
    m_cachedFilteredRowsLock = new Object();
    m_rows = Collections.synchronizedList(new ArrayList<ITableRow>(1));
    m_deletedRows = new HashMap<CompositeObject, ITableRow>();
    m_keyStrokeBuffer = new KeyStrokeBuffer(500L);
    m_rowFilters = new ArrayList<ITableRowFilter>(1);
    m_initLock = new OptimisticLock();
    m_actionRunning = false;
    m_objectExtensions = new ObjectExtensions<AbstractTable, ITableExtension<? extends AbstractTable>>(this);
    //add single observer listener
    addTableListener(new P_TableListener());
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public final List<? extends ITableExtension<? extends AbstractTable>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected ITableExtension<? extends AbstractTable> createLocalExtension() {
    return new LocalTableExtension<AbstractTable>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected void callInitializer() {
    interceptInitConfig();
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    if (getContainer() != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + getContainer().classId();
    }
    return simpleClassId;
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  /*
   * Configuration
   */

  /**
   * Configures the title of this table. The title of the table is rarely used because a table is usually surrounded by
   * an {@link AbstractTableField} having its own title / label.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Title of this table.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  /**
   * Configures the default icon for this table. The default icon is used for each row in the table.
   * <p>
   * This has only an effect, if {@link #getConfiguredRowIconVisible()} is set to true.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(20)
  protected String getConfiguredDefaultIconId() {
    return null;
  }

  /**
   * Configures whether the row icon is visible.
   * <p>
   * If set to true the gui creates a column which contains the row icons. The column has a fixed width, is not moveable
   * and always the first column (resp. the second if the table is checkable). The column is not available in the model.
   * <p>
   * If you need other settings or if you need the icon at another column position, you cannot use the row icons.
   * Instead you have to create a column and use {@link Cell#setIconId(String)} to set the icons on it's cells.
   * <p>
   * Subclasses can override this method. Default is false.
   *
   * @return {@code true} if the row icon is visible, {@code false} otherwise.
   * @see ITableRow#getIconId()
   * @see #getConfiguredDefaultIconId()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(25)
  protected boolean getConfiguredRowIconVisible() {
    return false;
  }

  /**
   * Configures whether only one row can be selected at once in this table.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if more then one row in this table can be selected at once, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredMultiSelect() {
    return true;
  }

  /**
   * Configures whether only one row can be checked in this table. This configuration is only useful if
   * {@link #getConfiguredCheckable()} is {@code true} .
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if more then one row in this table can be checked, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(32)
  protected boolean getConfiguredMultiCheck() {
    return true;
  }

  /**
   * Configures the default menu that is used on the ENTER (action key) or the double click on a table row.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return The default menu to use.
   */
  @ConfigProperty(ConfigProperty.MENU_CLASS)
  @Order(35)
  protected Class<? extends IMenu> getConfiguredDefaultMenu() {
    return null;
  }

  /**
   * Interception method used for customizing the default menu. Should be used by the framework only.
   *
   * @since 3.8.1
   */
  protected Class<? extends IMenu> getDefaultMenuInternal() {
    return getConfiguredDefaultMenu();
  }

  /**
   * Configures whether deleted rows are automatically erased or cached for later processing (service deletion).
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if deleted rows are automatically erased, {@code false} if deleted nodes are cached for later
   *         processing.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredAutoDiscardOnDelete() {
    return false;
  }

  /**
   * Configures whether sort is enabled for this table. If sort is enabled, the table rows are sorted based on their
   * sort index (see {@link AbstractColumn#getConfiguredSortIndex()}) and the user might change the sorting at run time.
   * If sort is disabled, the table rows are not sorted and the user cannot change the sorting.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if sort is enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredSortEnabled() {
    return true;
  }

  /**
   * Configures whether the header row is visible. The header row contains the titles of each column.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the header row is visible, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  /**
   * Configures whether the header row is enabled. In a disabled header, it is not possible to move or resize the
   * columns and the table header menu cannot be opened.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the header row is enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredHeaderEnabled() {
    return true;
  }

  /**
   * Configures whether the columns are auto resized. If true, all columns are resized so that the table never needs
   * horizontal scrolling. This is especially useful for tables inside a form.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the columns are auto resized, {@code false} otherwise.
   * @see {@link AbstractColumn#getConfiguredWidth()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredAutoResizeColumns() {
    return false;
  }

  /**
   * Configures whether the table supports multiline text. If multiline text is supported and a string column has set
   * the {@link AbstractStringColumn#getConfiguredTextWrap()} property to true, the text is wrapped and uses two or more
   * lines.
   * <p>
   * Subclasses can override this method. Default is {@code false}. If the method is not overridden and at least one
   * string column has set the {@link AbstractStringColumn#getConfiguredTextWrap()} to true, the multiline property is
   * set automatically to true.
   *
   * @return {@code true} if the table supports multiline text, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredMultilineText() {
    return false;
  }

  /**
   * Configures the row height hint. This is a hint for the UI if and only if it is not capable of having variable table
   * row height based on cell contents.
   * <p>
   * This property is interpreted in different manner for each GUI port:
   * <ul>
   * <li>Swing: The property is ignored.
   * </ul>
   * This hint defines the table row height in pixels being used as the fixed row height for all table rows of this
   * table.
   * </p>
   * Subclasses can override this method. Default is {@code -1}.
   *
   * @return Table row height hint in pixels.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(92)
  protected int getConfiguredRowHeightHint() {
    return -1;
  }

  /**
   * Configures whether the table is checkable.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the table is checkable, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredCheckable() {
    return false;
  }

  /**
   * Configures the checkable column. The checkable column represents the check state of the row, i.e. if it is checked
   * or not. If no checkable column is configured, only the row itself represents if the row was checked or not.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return A column class extending {@link AbstractBooleanColumn} that represents the row check state.
   */
  @ConfigProperty(ConfigProperty.TABLE_COLUMN)
  @Order(102)
  protected Class<? extends AbstractBooleanColumn> getConfiguredCheckableColumn() {
    return null;
  }

  /**
   * Configures the maximum size for a drop request (in bytes).
   * <p>
   * Subclasses can override this method. Default is defined by {@link IDNDSupport#DEFAULT_DROP_MAXIMUM_SIZE}.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(190)
  protected long getConfiguredDropMaximumSize() {
    return DEFAULT_DROP_MAXIMUM_SIZE;
  }

  /**
   * Configures the drop support of this table.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(190)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * Configures the drag support of this table.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drag support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(190)
  protected int getConfiguredDragType() {
    return 0;
  }

  /**
   * Configures whether the keyboard can be used for navigation in table. When activated, the user can click on a column
   * in the table. Now starting to type some letters, the row matching the typed letters in the column will be selected.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the keyboard navigation is supported, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredKeyboardNavigation() {
    return true;
  }

  /**
   * Configures whether the table always scrolls to the selection. When activated and the selection in a table changes,
   * the table is scrolled to the selection so that the selected row is visible.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the table scrolls to the selection, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(230)
  protected boolean getConfiguredScrollToSelection() {
    return false;
  }

  /**
   * Called after a drag operation was executed on one or several table rows.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param rows
   *          Table rows that were dragged (unmodifiable list).
   * @return A transferable object representing the given rows.
   */
  @ConfigOperation
  @Order(10)
  protected TransferObject execDrag(List<ITableRow> rows) {
    return null;
  }

  /**
   * Called after a drop operation was executed on the table.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param row
   *          Table row on which the transferable object was dropped (row may be null for empty space drop).
   * @param t
   *          Transferable object that was dropped on the row.
   */
  @ConfigOperation
  @Order(20)
  protected void execDrop(ITableRow row, TransferObject t) {
  }

  /**
   * Called by a <code>CTRL-C</code> event on the table to copy the given rows into the clipboard.
   * <p>
   * Subclasses can override this method. The default creates a {@link TextTransferObject} of the table content (HTML
   * table).
   *
   * @param rows
   *          The selected table rows to copy.
   * @return A transferable object representing the given rows or null to not populate the clipboard.
   */
  @ConfigOperation
  @Order(30)
  protected TransferObject execCopy(List<? extends ITableRow> rows) {
    if (!CollectionUtility.hasElements(rows)) {
      return null;
    }

    StringBuilder plainText = new StringBuilder();

    List<IColumn<?>> columns = getColumnSet().getVisibleColumns();

    boolean firstRow = true;
    for (ITableRow row : rows) {
      if (!firstRow) {
        plainText.append(System.getProperty("line.separator"));
      }

      boolean firstColumn = true;
      for (IColumn<?> column : columns) {
        String text;
        if (column instanceof IBooleanColumn) {
          boolean value = BooleanUtility.nvl(((IBooleanColumn) column).getValue(row), false);
          text = value ? "X" : "";
        }
        else {
          text = StringUtility.emptyIfNull(row.getCell(column).getText());
        }

        // special intercept for html
        if (text != null && row.getCell(column).isHtmlEnabled()) {
          text = HTML.plain(text).toPlainText();
        }

        // text/plain
        if (!firstColumn) {
          plainText.append("\t");
        }
        plainText.append(StringUtility.emptyIfNull(StringUtility.unwrapText(text)));

        firstColumn = false;
      }
      firstRow = false;
    }

    TextTransferObject transferObject = new TextTransferObject(plainText.toString());
    return transferObject;
  }

  /**
   * Called after the table content changed, rows were added, removed or changed.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(40)
  protected void execContentChanged() {
  }

  /**
   * Called after {@link AbstractColumn#execDecorateCell(Cell,ITableRow)} on the column to decorate the cell.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(50)
  protected void execDecorateCell(Cell view, ITableRow row, IColumn<?> col) {
  }

  /**
   * Called during initialization of this table, after the columns were initialized.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(60)
  protected void execInitTable() {
  }

  /**
   * Called when this table is disposed, after the columns were disposed.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(70)
  protected void execDisposeTable() {
  }

  /**
   * Called when the user clicks on a row in this table.
   * <p>
   * Subclasses can override this method. The default fires a {@link TableEvent#TYPE_ROW_CLICK} event.
   *
   * @param Row
   *          that was clicked (never null).
   * @param mouseButton
   *          the mouse button ({@link MouseButton}) which triggered this method
   */
  @ConfigOperation
  @Order(80)
  protected void execRowClick(ITableRow row, MouseButton mouseButton) {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_CLICK, CollectionUtility.arrayList(row));
    fireTableEventInternal(e);
  }

  /**
   * Called when the row has been activated.
   * <p>
   * Subclasses can override this method. The default opens the configured default menu or if no default menu is
   * configured, fires a {@link TableEvent#TYPE_ROW_ACTION} event.
   *
   * @param row
   *          that was activated (never null).
   */
  @ConfigOperation
  @Order(90)
  protected void execRowAction(ITableRow row) {
    Class<? extends IMenu> defaultMenuType = getDefaultMenuInternal();
    if (defaultMenuType != null) {
      try {
        runMenu(defaultMenuType);
      }
      catch (Exception ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }
    else {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_ACTION, CollectionUtility.arrayList(row));
      fireTableEventInternal(e);
    }
  }

  /**
   * Called whenever the selection changes.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param rows
   *          an unmodifiable list of the selected rows, may be empty but not null.
   */
  @ConfigOperation
  @Order(100)
  protected void execRowsSelected(List<? extends ITableRow> rows) {
  }

  /**
   * Called when the row is going to be decorated.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param row
   *          that is going to be decorated.
   */
  @ConfigOperation
  @Order(110)
  protected void execDecorateRow(ITableRow row) {
  }

  /**
   * Called when a hyperlink is used within the table. The hyperlink's table row is the selected row and the column is
   * the context column ({@link #getContextColumn()}).
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param url
   *          Hyperlink to process.
   * @param path
   *          Path of URL ({@link URL#getPath()}).
   * @param local
   *          {@code true} if the url is not a valid external url but a local model url (http://local/...)
   * @{@link Deprecated} use {@link #execAppLinkAction(String)} instead
   */
  @ConfigOperation
  @Order(120)
  @Deprecated
  protected void execHyperlinkAction(URL url, String path, boolean local) {
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(120)
  protected void execAppLinkAction(String ref) {
    //FIXME cgu: remove this code when execpHyperlinkAction has been removed
    URL url = null;
    boolean local = false;
    if (ref != null) {
      try {
        url = new URL(ref);
        local = "local".equals(url.getHost());
      }
      catch (MalformedURLException e) {
        LOG.error("Malformed URL", e);
      }
    }
    execHyperlinkAction(url, ref, local);
  }

  /**
   * Called when rows get checked or unchecked.
   * <p>
   * Subclasses can override this method.
   *
   * @param rows
   *          list of rows which have been checked or unchecked (never null).
   */
  @ConfigOperation
  @Order(130)
  protected void execRowsChecked(Collection<? extends ITableRow> rows) {
  }

  /**
   * This method is called during initializing the table and is thought to add header menus to the given collection of
   * menus. Menus added in this method should be of menu type {@link ITableMenu.TableMenuType#Header}.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          a live collection of the menus. Add additional header menus to this list optionally add some separators at
   *          the end.
   */
  protected void addHeaderMenus(OrderedCollection<IMenu> menus) {
    menus.addLast(new OrganizeColumnsMenu(this));
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  protected List<Class<? extends ITableControl>> getConfiguredTableControls() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITableControl>> filtered = ConfigurationUtility.filterClasses(dca, ITableControl.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  private List<Class<? extends IColumn>> getConfiguredColumns() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IColumn>> foca = ConfigurationUtility.filterClasses(dca, IColumn.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  private List<Class<? extends IKeyStroke>> getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IKeyStroke>> fca = ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    m_eventHistory = createEventHistory();
    m_eventBuffer = createEventBuffer();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    m_contributionHolder = new ContributionComposite(this);
    setEnabled(true);
    setTitle(getConfiguredTitle());
    setAutoDiscardOnDelete(getConfiguredAutoDiscardOnDelete());
    setSortEnabled(getConfiguredSortEnabled());
    setDefaultIconId(getConfiguredDefaultIconId());
    setRowIconVisible(getConfiguredRowIconVisible());
    setHeaderVisible(getConfiguredHeaderVisible());
    setHeaderEnabled(getConfiguredHeaderEnabled());
    setAutoResizeColumns(getConfiguredAutoResizeColumns());
    setCheckable(getConfiguredCheckable());
    setMultiCheck(getConfiguredMultiCheck());
    setMultiSelect(getConfiguredMultiSelect());
    setInitialMultilineText(getConfiguredMultilineText());
    setMultilineText(getConfiguredMultilineText());
    setRowHeightHint(getConfiguredRowHeightHint());
    setKeyboardNavigation(getConfiguredKeyboardNavigation());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setScrollToSelection(getConfiguredScrollToSelection());
    setTableStatusVisible(getConfiguredTableStatusVisible());
    if (getTableCustomizer() == null) {
      setTableCustomizer(createTableCustomizer());
    }
    // columns
    createColumnsInternal();
    // table controls
    createTableControlsInternal();
    // menus
    List<Class<? extends IMenu>> ma = getDeclaredMenus();
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    Map<Class<?>, Class<? extends IMenu>> replacements = ConfigurationUtility.getReplacementMapping(ma);
    if (!replacements.isEmpty()) {
      m_menuReplacementMapping = replacements;
    }
    for (Class<? extends IMenu> clazz : ma) {
      IMenu menu = ConfigurationUtility.newInnerInstance(this, clazz);
      menus.addOrdered(menu);
    }
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);

    addHeaderMenus(menus);
    //set container on menus
    for (IMenu menu : menus) {
      menu.setContainerInternal(this);
    }

    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    ITableContextMenu contextMenu = new TableContextMenu(this, menus.getOrderedList());
    setContextMenu(contextMenu);

    // key strokes
    List<Class<? extends IKeyStroke>> ksClasses = getConfiguredKeyStrokes();
    ArrayList<IKeyStroke> ksList = new ArrayList<IKeyStroke>(ksClasses.size());
    for (Class<? extends IKeyStroke> clazz : ksClasses) {
      IKeyStroke ks = ConfigurationUtility.newInnerInstance(this, clazz);
      ks.initAction();
      if (ks.getKeyStroke() != null) {
        ksList.add(ks);
      }
    }
    //add ENTER key stroke when default menu is used or execRowAction has an override
    Class<? extends IMenu> defaultMenuType = getDefaultMenuInternal();
    if (defaultMenuType != null || ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "execRowAction", new Class[]{ITableRow.class}, this.getClass())) {
      ksList.add(new KeyStroke("ENTER") {
        @Override
        protected void execAction() {
          fireRowAction(getSelectedRow());
        }
      });
    }
    // add keystroke contributions
    List<IKeyStroke> contributedKeyStrokes = m_contributionHolder.getContributionsByClass(IKeyStroke.class);
    ksList.addAll(contributedKeyStrokes);
    setKeyStrokes(ksList);
    // FIXME AWE: (organize) austauschbar mit bean
    m_tableOrganizer = BEANS.get(ITableOrganizerProvider.class).createTableOrganizer(this);

    // add Convenience observer for drag & drop callbacks, event history and ui sort possible check
    addTableListener(new TableAdapter() {
      @Override
      public void tableChanged(TableEvent e) {
        //event history
        IEventHistory<TableEvent> h = getEventHistory();
        if (h != null) {
          h.notifyEvent(e);
        }
        //dnd
        switch (e.getType()) {
          case TableEvent.TYPE_ROWS_DRAG_REQUEST: {
            if (e.getDragObject() == null) {
              try {
                e.setDragObject(interceptDrag(e.getRows()));
              }
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ROW_DROP_ACTION: {
            if (e.getDropObject() != null && isEnabled()) {
              try {
                interceptDrop(e.getFirstRow(), e.getDropObject());
              }
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ROWS_COPY_REQUEST: {
            if (e.getCopyObject() == null) {
              try {
                e.setCopyObject(interceptCopy(e.getRows()));
              }
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ALL_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_INSERTED:
          case TableEvent.TYPE_ROWS_UPDATED: {
            if (isValueChangeTriggerEnabled()) {
              try {
                interceptContentChanged();
              }
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ROWS_CHECKED:
            try {
              interceptRowsChecked(e.getRows());
            }
            catch (RuntimeException ex) {
              BEANS.get(ExceptionHandler.class).handle(ex);
            }
            break;
          case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
          case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED:
            checkIfColumnPreventsUiSortForTable();
            break;
        }
      }
    });
  }

  @Override
  public AbstractEventBuffer<TableEvent> createEventBuffer() {
    return new TableEventBuffer();
  }

  protected AbstractEventBuffer<TableEvent> getEventBuffer() {
    return m_eventBuffer;
  }

  private void initColumnsInternal() {
    getColumnSet().initColumns();
  }

  private void disposeColumnsInternal() {
    getColumnSet().disposeColumns();
  }

  private void disposeMenus() {
    for (IMenu menu : getMenus()) {
      menu.dispose();
    }
  }

  // FIXME awe, mvi: make TableControls extensible, check copy/paste code in this class
  private void createTableControlsInternal() {
    List<Class<? extends ITableControl>> tcs = getConfiguredTableControls();
    OrderedCollection<ITableControl> tableControls = new OrderedCollection<ITableControl>();
    for (Class<? extends ITableControl> clazz : tcs) {
      ITableControl tableControl = ConfigurationUtility.newInnerInstance(this, clazz);
      ((AbstractTableControl) tableControl).setTable(this);
      tableControls.addOrdered(tableControl);
    }
    m_tableControls = tableControls.getOrderedList();
  }

  private void createColumnsInternal() {
    List<Class<? extends IColumn>> ca = getConfiguredColumns();
    OrderedCollection<IColumn<?>> columns = new OrderedCollection<IColumn<?>>();

    // configured columns
    for (Class<? extends IColumn> clazz : ca) {
      IColumn<?> column = ConfigurationUtility.newInnerInstance(this, clazz);
      columns.addOrdered(column);
    }

    // contributed columns
    List<IColumn> contributedColumns = m_contributionHolder.getContributionsByClass(IColumn.class);
    for (IColumn c : contributedColumns) {
      columns.addOrdered(c);
    }

    // dynamically injected columns
    injectColumnsInternal(columns);

    // move columns
    ExtensionUtility.moveModelObjects(columns);

    m_columnSet = new ColumnSet(this, columns.getOrderedList());
    if (getConfiguredCheckableColumn() != null) {
      AbstractBooleanColumn checkableColumn = getColumnSet().getColumnByClass(getConfiguredCheckableColumn());
      setCheckableColumn(checkableColumn);
    }

    // add listener to disable ui sort possible property if needed
    PropertyChangeListener columnListener = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        checkIfColumnPreventsUiSortForTable();
      }
    };
    for (IColumn column : m_columnSet.getColumns()) {
      column.addPropertyChangeListener(IColumn.PROP_VISIBLE, columnListener);
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic columns<br>
   * To change the order or specify the insert position use {@link IColumn#setOrder(double)}.
   *
   * @param columns
   *          live and mutable collection of configured columns, not yet initialized
   */
  protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columns) {
    ITableCustomizer c = getTableCustomizer();
    if (c != null) {
      c.injectCustomColumns(columns);
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  protected ITableUIFacade createUIFacade() {
    return new P_TableUIFacade();
  }

  /*
   * Runtime
   */

  @Override
  public String getUserPreferenceContext() {
    return m_userPreferenceContext;
  }

  @Override
  public void setUserPreferenceContext(String context) {
    m_userPreferenceContext = context;
    if (isTableInitialized()) {
      //re-initialize
      try {
        initTable();
      }
      catch (RuntimeException e) {
        LOG.error("Failed re-initializing table {}", getClass().getName(), e);
      }
    }
  }

  /**
   * This is the init of the runtime model after the table and columns are built and configured
   */
  @Override
  public final void initTable() {
    try {
      if (m_initLock.acquire()) {
        try {
          setTableChanging(true);
          //
          initTableInternal();
          ActionUtility.initActions(getMenus());
          interceptInitTable();
        }
        finally {
          setTableChanging(false);
        }
      }
    }
    finally {
      m_initialized = true;
      m_initLock.release();
    }
  }

  protected void initTableInternal() {
    initColumnsInternal();
    if (getUserFilterManager() == null) {
      setUserFilterManager(createUserFilterManager());
    }
  }

  @Override
  public final void disposeTable() {
    try {
      disposeTableInternal();
      interceptDisposeTable();
    }
    catch (Exception e) {
      LOG.error("Could not dispose table [{}]", getClass().getName(), e);
    }
  }

  protected void disposeTableInternal() {
    disposeColumnsInternal();
    disposeMenus();
  }

  @Override
  public void doAppLinkAction(String ref) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        interceptAppLinkAction(ref);
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  @Override
  public List<ITableRowFilter> getRowFilters() {
    return CollectionUtility.arrayList(m_rowFilters);
  }

  @Override
  public void addRowFilter(ITableRowFilter filter) {
    if (filter != null) {
      //avoid duplicate add
      if (!m_rowFilters.contains(filter)) {
        m_rowFilters.add(filter);
        applyRowFilters();
      }
    }
  }

  @Override
  public void removeRowFilter(ITableRowFilter filter) {
    if (filter != null) {
      if (m_rowFilters.remove(filter)) {
        applyRowFilters();
      }
    }
  }

  @Override
  public void applyRowFilters() {
    boolean filterChanged = applyRowFiltersInternal();
    if (filterChanged) {
      fireRowFilterChanged();
    }
  }

  private boolean applyRowFiltersInternal() {
    boolean filterChanged = false;
    for (ITableRow row : m_rows) {
      boolean wasFilterAccepted = row.isFilterAccepted();
      applyRowFiltersInternal((InternalTableRow) row);
      if (row.isFilterAccepted() != wasFilterAccepted) {
        filterChanged = true;
      }
    }
    return filterChanged;
  }

  private void applyRowFiltersInternal(InternalTableRow row) {
    List<ITableRowFilter> rejectingFilters = new ArrayList<ITableRowFilter>();
    row.setFilterAcceptedInternal(true);
    row.setRejectedByUser(false);
    if (m_rowFilters.size() > 0) {
      for (ITableRowFilter filter : m_rowFilters) {
        if (!filter.accept(row)) {
          row.setFilterAcceptedInternal(false);
          /*
           * ticket 95770
           */
          if (isSelectedRow(row)) {
            deselectRow(row);
          }
          rejectingFilters.add(filter);
        }
      }
    }

    // Prefer row.isRejectedByUser to allow a filter to set this flag
    row.setRejectedByUser(row.isRejectedByUser() || rejectingFilters.size() == 1 && rejectingFilters.get(0) instanceof IUserFilter);
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public boolean isAutoResizeColumns() {
    return propertySupport.getPropertyBool(PROP_AUTO_RESIZE_COLUMNS);
  }

  @Override
  public void setAutoResizeColumns(boolean b) {
    propertySupport.setPropertyBool(PROP_AUTO_RESIZE_COLUMNS, b);
  }

  @Override
  public ColumnSet getColumnSet() {
    return m_columnSet;
  }

  @Override
  public int getColumnCount() {
    return getColumnSet().getColumnCount();
  }

  @Override
  public List<IColumn<?>> getColumns() {
    return getColumnSet().getColumns();
  }

  @Override
  public List<String> getColumnNames() {
    List<String> columnNames = new ArrayList<String>(getColumnCount());
    for (IColumn col : getColumns()) {
      columnNames.add(col.getHeaderCell().getText());
    }
    return columnNames;
  }

  @Override
  public int getVisibleColumnCount() {
    return getColumnSet().getVisibleColumnCount();
  }

  @Override
  public IHeaderCell getVisibleHeaderCell(int visibleColumnIndex) {
    return getHeaderCell(getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  @Override
  public IHeaderCell getHeaderCell(int columnIndex) {
    return getHeaderCell(getColumnSet().getColumn(columnIndex));
  }

  @Override
  public IHeaderCell getHeaderCell(IColumn<?> col) {
    return col.getHeaderCell();
  }

  @Override
  public ICell getVisibleCell(int rowIndex, int visibleColumnIndex) {
    return getVisibleCell(getRow(rowIndex), visibleColumnIndex);
  }

  @Override
  public ICell getVisibleCell(ITableRow row, int visibleColumnIndex) {
    return getCell(row, getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  @Override
  public ICell getCell(int rowIndex, int columnIndex) {
    return getCell(getRow(rowIndex), getColumnSet().getColumn(columnIndex));
  }

  @Override
  public ICell getSummaryCell(int rowIndex) {
    return getSummaryCell(getRow(rowIndex));
  }

  @Override
  public ICell getSummaryCell(ITableRow row) {
    List<IColumn<?>> a = getColumnSet().getSummaryColumns();
    if (a.size() == 0) {
      IColumn<?> col = getColumnSet().getFirstDefinedVisibileColumn();
      if (col != null) {
        a = CollectionUtility.<IColumn<?>> arrayList(col);
      }
    }
    if (a.isEmpty()) {
      return new Cell();
    }
    else if (a.size() == 1) {
      Cell cell = new Cell(getCell(row, a.get(0)));
      if (cell.getIconId() == null) {
        // use icon of row
        cell.setIconId(row.getIconId());
      }
      return cell;
    }
    else {
      Cell cell = new Cell(getCell(row, a.get(0)));
      if (cell.getIconId() == null) {
        // use icon of row
        cell.setIconId(row.getIconId());
      }
      StringBuilder b = new StringBuilder();
      for (IColumn<?> c : a) {
        if (b.length() > 0) {
          b.append(" ");
        }
        b.append(getCell(row, c).getText());
      }
      cell.setText(b.toString());
      return cell;
    }
  }

  @Override
  public ICell getCell(ITableRow row, IColumn<?> col) {
    row = resolveRow(row);
    if (row == null || col == null) {
      return null;
    }
    return row.getCell(col.getColumnIndex());
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return isCellEditable(getRow(rowIndex), getColumnSet().getColumn(columnIndex));
  }

  @Override
  public boolean isCellEditable(ITableRow row, IColumn<?> column) {
    return row != null && column != null && column.isCellEditable(row);
  }

  @Override
  public Object getProperty(String name) {
    return propertySupport.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    propertySupport.setProperty(name, value);
  }

  @Override
  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  @Override
  public boolean isCheckable() {
    return propertySupport.getPropertyBool(PROP_CHECKABLE);
  }

  @Override
  public void setCheckable(boolean b) {
    propertySupport.setPropertyBool(PROP_CHECKABLE, b);
  }

  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(PROP_DROP_TYPE);
  }

  @Override
  public void setDropMaximumSize(long dropMaximumSize) {
    propertySupport.setPropertyLong(PROP_DROP_MAXIMUM_SIZE, dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return propertySupport.getPropertyInt(PROP_DROP_MAXIMUM_SIZE);
  }

  @Override
  public boolean isMultilineText() {
    return propertySupport.getPropertyBool(PROP_MULTILINE_TEXT);
  }

  @Override
  public void setMultilineText(boolean on) {
    propertySupport.setPropertyBool(PROP_MULTILINE_TEXT, on);
  }

  @Override
  public int getRowHeightHint() {
    return propertySupport.getPropertyInt(PROP_ROW_HEIGHT_HINT);
  }

  @Override
  public void setRowHeightHint(int h) {
    propertySupport.setPropertyInt(PROP_ROW_HEIGHT_HINT, h);
  }

  @Override
  public boolean isInitialMultilineText() {
    return m_initialMultiLineText;
  }

  @Override
  public void setInitialMultilineText(boolean on) {
    m_initialMultiLineText = on;
  }

  @Override
  public boolean hasKeyboardNavigation() {
    return propertySupport.getPropertyBool(PROP_KEYBOARD_NAVIGATION);
  }

  @Override
  public void setKeyboardNavigation(boolean on) {
    propertySupport.setPropertyBool(PROP_KEYBOARD_NAVIGATION, on);
  }

  @Override
  public boolean isMultiSelect() {
    return propertySupport.getPropertyBool(PROP_MULTI_SELECT);
  }

  @Override
  public void setMultiSelect(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_SELECT, b);
  }

  @Override
  public boolean isMultiCheck() {
    return propertySupport.getPropertyBool(PROP_MULTI_CHECK);
  }

  @Override
  public void setMultiCheck(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_CHECK, b);
  }

  @Override
  public IBooleanColumn getCheckableColumn() {
    return m_checkableColumn;
  }

  @Override
  public void setCheckableColumn(IBooleanColumn checkableColumn) {
    m_checkableColumn = checkableColumn;
  }

  @Override
  public boolean isAutoDiscardOnDelete() {
    return m_autoDiscardOnDelete;
  }

  @Override
  public void setAutoDiscardOnDelete(boolean on) {
    m_autoDiscardOnDelete = on;
  }

  @Override
  public boolean isTableInitialized() {
    return m_initialized;
  }

  @Override
  public boolean isTableChanging() {
    return m_tableChanging > 0;
  }

  @Override
  public void setTableChanging(boolean b) {
    // use a stack counter because setTableChanging might be called in nested
    // loops
    if (b) {
      m_tableChanging++;
      if (m_tableChanging == 1) {
        // 0 --> 1
        propertySupport.setPropertiesChanging(true);
      }
    }
    else {
      // all calls to further methods are wrapped into a try-catch block so that the change counters are adjusted correctly
      if (m_tableChanging > 0) {
        Exception saveEx = null;
        if (m_tableChanging == 1) {
          try {
            //will be going to zero, but process decorations here, so events are added to the event buffer
            processDecorationBuffer();
            if (!m_sortValid) {
              sort();
            }
          }
          catch (Exception t) {
            saveEx = t;
          }
        }
        m_tableChanging--;
        if (m_tableChanging == 0) {
          try {
            processEventBuffer();
          }
          catch (Exception t) {
            if (saveEx == null) {
              saveEx = t;
            }
          }
          try {
            propertySupport.setPropertiesChanging(false);
          }
          catch (Exception t) {
            if (saveEx == null) {
              saveEx = t;
            }
          }
        }
        if (saveEx == null) {
          return;
        }
        else if (saveEx instanceof RuntimeException) {
          throw (RuntimeException) saveEx;
        }
      }
    }
  }

  @Override
  public List<IKeyStroke> getKeyStrokes() {
    return CollectionUtility.arrayList(propertySupport.<IKeyStroke> getPropertyList(PROP_KEY_STROKES));
  }

  @Override
  public void setKeyStrokes(List<? extends IKeyStroke> keyStrokes0) {
    propertySupport.setPropertyList(PROP_KEY_STROKES, CollectionUtility.arrayListWithoutNullElements(keyStrokes0));
  }

  @Override
  public void requestFocus() {
    fireRequestFocus();
  }

  @Override
  public void requestFocusInCell(IColumn<?> column, ITableRow row) {
    if (isCellEditable(row, column)) {
      fireRequestFocusInCell(column, row);
    }
  }

  @Override
  public ITableRowDataMapper createTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) {
    return interceptCreateTableRowDataMapper(rowType);
  }

  /**
   * Creates a {@link TableRowDataMapper} that is used for reading and writing data from the given
   * {@link AbstractTableRowData} type.
   *
   * @param rowType
   * @return
   * @since 3.8.2
   */
  @ConfigOperation
  @Order(130)
  protected ITableRowDataMapper execCreateTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) {
    return new TableRowDataMapper(rowType, getColumnSet());
  }

  @Override
  public void exportToTableBeanData(AbstractTableFieldBeanData target) {
    ITableRowDataMapper rowMapper = createTableRowDataMapper(target.getRowType());
    for (int i = 0, ni = getRowCount(); i < ni; i++) {
      ITableRow row = getRow(i);
      if (rowMapper.acceptExport(row)) {
        AbstractTableRowData rowData = target.addRow();
        rowMapper.exportTableRowData(row, rowData);
      }
    }
    List<ITableRow> deletedRows = getDeletedRows();
    for (ITableRow delRow : deletedRows) {
      if (rowMapper.acceptExport(delRow)) {
        AbstractTableRowData rowData = target.addRow();
        rowMapper.exportTableRowData(delRow, rowData);
        rowData.setRowState(AbstractTableRowData.STATUS_DELETED);
      }
    }
  }

  @Override
  public void importFromTableBeanData(AbstractTableFieldBeanData source) {
    importFromTableRowBeanData(CollectionUtility.arrayList(source.getRows()), source.getRowType());
  }

  public void importFromTableRowBeanData(List<? extends AbstractTableRowData> rowDatas, Class<? extends AbstractTableRowData> rowType) {
    discardAllDeletedRows();
    int deleteCount = 0;
    List<ITableRow> newRows = new ArrayList<ITableRow>(rowDatas.size());
    ITableRowDataMapper mapper = createTableRowDataMapper(rowType);
    for (int i = 0, ni = rowDatas.size(); i < ni; i++) {
      AbstractTableRowData rowData = rowDatas.get(i);
      if (rowData.getRowState() != AbstractTableRowData.STATUS_DELETED && mapper.acceptImport(rowData)) {
        ITableRow newTableRow = new TableRow(getColumnSet());
        mapper.importTableRowData(newTableRow, rowData);
        newRows.add(newTableRow);
      }
      else {
        deleteCount++;
      }
    }
    replaceRows(newRows);
    if (deleteCount > 0) {
      try {
        setTableChanging(true);
        //
        for (int i = 0, ni = rowDatas.size(); i < ni; i++) {
          AbstractTableRowData rowData = rowDatas.get(i);
          if (rowData.getRowState() == AbstractTableRowData.STATUS_DELETED && mapper.acceptImport(rowData)) {
            ITableRow newTableRow = new TableRow(getColumnSet());
            mapper.importTableRowData(newTableRow, rowData);
            newTableRow.setStatus(ITableRow.STATUS_NON_CHANGED);
            ITableRow addedRow = addRow(newTableRow);
            deleteRow(addedRow);
          }
        }
      }
      finally {
        setTableChanging(false);
      }
    }
  }

  @Override
  public void extractTableData(AbstractTableFieldData target) {
    for (int i = 0, ni = getRowCount(); i < ni; i++) {
      ITableRow row = getRow(i);
      int newRowIndex = target.addRow();
      for (int j = 0, nj = row.getCellCount(); j < nj; j++) {
        target.setValueAt(newRowIndex, j, row.getCellValue(j));
      }
      target.setRowState(newRowIndex, row.getStatus());
    }
    for (ITableRow delRow : getDeletedRows()) {
      int newRowIndex = target.addRow();
      for (int j = 0, nj = delRow.getCellCount(); j < nj; j++) {
        target.setValueAt(newRowIndex, j, delRow.getCellValue(j));
      }
      target.setRowState(newRowIndex, AbstractTableFieldData.STATUS_DELETED);
    }
    target.setValueSet(true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void updateTable(AbstractTableFieldData source) {
    if (source.isValueSet()) {
      discardAllDeletedRows();
      int deleteCount = 0;
      List<ITableRow> newRows = new ArrayList<ITableRow>();
      for (int i = 0, ni = source.getRowCount(); i < ni; i++) {
        int importState = source.getRowState(i);
        if (importState != AbstractTableFieldData.STATUS_DELETED) {
          ITableRow newTableRow = new TableRow(getColumnSet());
          for (int j = 0, nj = source.getColumnCount(); j < nj; j++) {
            if (j < getColumnCount()) {
              getColumnSet().getColumn(j).setValue(newTableRow, source.getValueAt(i, j));
            }
            else {
              newTableRow.setCellValue(j, source.getValueAt(i, j));
            }
          }
          newTableRow.setStatus(importState);
          newRows.add(newTableRow);
        }
        else {
          deleteCount++;
        }
      }
      replaceRows(newRows);
      if (deleteCount > 0) {
        try {
          setTableChanging(true);
          //
          for (int i = 0, ni = source.getRowCount(); i < ni; i++) {
            int importState = source.getRowState(i);
            if (importState == AbstractTableFieldData.STATUS_DELETED) {
              ITableRow newTableRow = new TableRow(getColumnSet());
              for (int j = 0, nj = source.getColumnCount(); j < nj; j++) {
                if (j < getColumnCount()) {
                  getColumnSet().getColumn(j).setValue(newTableRow, source.getValueAt(i, j));
                }
                else {
                  newTableRow.setCellValue(j, source.getValueAt(i, j));
                }
              }
              newTableRow.setStatus(ITableRow.STATUS_NON_CHANGED);
              ITableRow addedRow = addRow(newTableRow);
              deleteRow(addedRow);
            }
          }
        }
        finally {
          setTableChanging(false);
        }
      }
    }
  }

  @Override
  public void setMenus(List<? extends IMenu> menus) {
    getContextMenu().setChildActions(menus);
  }

  @Override
  public void addMenu(IMenu menu) {
    List<IMenu> menus = getMenus();
    menus.add(menu);
    setMenus(menus);
  }

  protected void setContextMenu(ITableContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public ITableContextMenu getContextMenu() {
    return (ITableContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public <T extends IMenu> T getMenu(final Class<T> menuType) {
    IContextMenu contextMenu = getContextMenu();
    if (contextMenu != null) {
      final Holder<T> resultHolder = new Holder<T>();
      contextMenu.acceptVisitor(new IActionVisitor() {

        @SuppressWarnings("unchecked")
        @Override
        public int visit(IAction action) {
          if (menuType.isAssignableFrom(action.getClass())) {
            resultHolder.setValue((T) action);
            return CANCEL;
          }
          return CONTINUE;
        }
      });
      return resultHolder.getValue();
    }
    return null;
  }

  @Override
  public boolean runMenu(Class<? extends IMenu> menuType) {
    Class<? extends IMenu> c = getReplacingMenuClass(menuType);
    for (IMenu m : getMenus()) {
      if (m.getClass() == c) {
        if (!m.isEnabledProcessingAction()) {
          return false;
        }
        if ((!m.isInheritAccessibility()) || isEnabled()) {
          if (m.isVisible() && m.isEnabled()) {
            m.doAction();
            return true;
          }
          else {
            return false;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks whether the menu with the given class has been replaced by another menu. If so, the replacing menu's class
   * is returned. Otherwise the given class itself.
   *
   * @param c
   * @return Returns the possibly available replacing menu class for the given class.
   * @see Replace
   * @since 3.8.2
   */
  private <T extends IMenu> Class<? extends T> getReplacingMenuClass(Class<T> c) {
    if (m_menuReplacementMapping != null) {
      @SuppressWarnings("unchecked")
      Class<? extends T> replacingMenuClass = (Class<? extends T>) m_menuReplacementMapping.get(c);
      if (replacingMenuClass != null) {
        return replacingMenuClass;
      }
    }
    return c;
  }

  /**
   * factory to manage user filters
   * <p>
   * default creates a {@link TableUserFilterManager}
   */
  protected TableUserFilterManager createUserFilterManager() {
    return new TableUserFilterManager(this);
  }

  /**
   * factory to manage custom columns
   * <p>
   * default creates null
   */
  protected ITableCustomizer createTableCustomizer() {
    return BEANS.get(ITableCustomizerProvider.class).createTableCustomizer(this);
  }

  /*
   * Row handling methods. Operate on a Row instance.
   */

  @Override
  public ITableRow createRow() {
    return new P_TableRowBuilder().createRow();
  }

  @Override
  public ITableRow createRow(Object rowValues) {
    return new P_TableRowBuilder().createRow(rowValues);
  }

  @Override
  public List<ITableRow> createRowsByArray(Object dataArray) {
    return new P_TableRowBuilder().createRowsByArray(dataArray);
  }

  @Override
  public List<ITableRow> createRowsByArray(Object dataArray, int rowStatus) {
    return new P_TableRowBuilder().createRowsByArray(dataArray, rowStatus);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new AtomicReference
   * <Object>(Object[][]) so that the further processing can set the content of the holder to null while processing.
   */
  @Override
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference) {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new AtomicReference
   * <Object>(Object[][]) so that the further processing can set the content of the holder to null while processing.
   */
  @Override
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference, int rowStatus) {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference, rowStatus);
  }

  @Override
  public List<ITableRow> createRowsByCodes(Collection<? extends ICode<?>> codes) {
    return new P_TableRowBuilder().createRowsByCodes(codes);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new AtomicReference
   * <Object>(Object[][]) so that the further processing can set the content of the holder to null while processing.
   */
  @Override
  public void replaceRowsByMatrix(Object dataMatrixOrReference) {
    replaceRows(createRowsByMatrix(dataMatrixOrReference));
  }

  @Override
  public void replaceRowsByArray(Object dataArray) {
    replaceRows(createRowsByArray(dataArray));
  }

  @Override
  public void replaceRows(List<? extends ITableRow> newRows) {
    /*
     * There are two ways to replace: (1) Completely replace all rows by
     * discarding all rows and adding new rows when - autoDiscardOnDelete=true
     * (2) Replace rows by applying insert/update/delete on existing rows by
     * primary key match when - autoDiscardOnDelete=false
     */
    if (isAutoDiscardOnDelete()) {
      replaceRowsCase1(newRows);
    }
    else {
      replaceRowsCase2(newRows);
    }
  }

  /**
   * Replace rows discarding deleted rows
   */
  private void replaceRowsCase1(List<? extends ITableRow> newRows) {
    try {
      setTableChanging(true);
      //
      List<CompositeObject> selectedKeys = getSelectedKeys();
      discardAllRows();
      addRows(newRows, false);
      restoreSelection(selectedKeys);
    }
    finally {
      setTableChanging(false);
    }
  }

  private List<CompositeObject> getSelectedKeys() {
    ArrayList<CompositeObject> selectedKeys = new ArrayList<CompositeObject>();
    for (ITableRow r : getSelectedRows()) {
      selectedKeys.add(new CompositeObject(getRowKeys(r)));
    }
    return selectedKeys;
  }

  private void restoreSelection(List<CompositeObject> selectedKeys) {
    ArrayList<ITableRow> selectedRows = new ArrayList<ITableRow>();
    if (selectedKeys.size() > 0) {
      for (ITableRow r : m_rows) {
        if (selectedKeys.remove(new CompositeObject(getRowKeys(r)))) {
          selectedRows.add(r);
          if (selectedKeys.size() == 0) {
            break;
          }
        }
      }
    }
    selectRows(selectedRows, false);
  }

  /**
   * Replace rows by applying insert/update/delete on existing rows by primary key match
   */
  private void replaceRowsCase2(List<? extends ITableRow> newRows) {
    try {
      setTableChanging(true);
      //
      int[] oldToNew = new int[getRowCount()];
      int[] newToOld = new int[newRows.size()];
      Arrays.fill(oldToNew, -1);
      Arrays.fill(newToOld, -1);
      HashMap<CompositeObject, Integer> newRowIndexMap = new HashMap<CompositeObject, Integer>();
      for (int i = newRows.size() - 1; i >= 0; i--) {
        newRowIndexMap.put(new CompositeObject(getRowKeys(newRows.get(i))), Integer.valueOf(i));
      }
      int mappedCount = 0;
      for (int i = 0, ni = getRowCount(); i < ni; i++) {
        ITableRow existingRow = m_rows.get(i);
        Integer newIndex = newRowIndexMap.remove(new CompositeObject(getRowKeys(existingRow)));
        if (newIndex != null) {
          oldToNew[i] = newIndex.intValue();
          newToOld[newIndex.intValue()] = i;
          mappedCount++;
        }
      }
      List<ITableRow> updatedRows = new ArrayList<ITableRow>(mappedCount);
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] >= 0) {
          ITableRow existingRow = getRow(i);
          ITableRow newRow = newRows.get(oldToNew[i]);

          replaceRowValues(existingRow, newRow);
          updatedRows.add(existingRow);
        }
      }

      List<ITableRow> deletedRows = new ArrayList<ITableRow>(getRowCount() - mappedCount);
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] < 0) {
          deletedRows.add(m_rows.get(i));
        }
      }
      List<ITableRow> insertedRows = new ArrayList<ITableRow>(newRows.size() - mappedCount);
      int[] insertedRowIndexes = new int[newRows.size() - mappedCount];
      int index = 0;
      for (int i = 0; i < newToOld.length; i++) {
        if (newToOld[i] < 0) {
          insertedRows.add(newRows.get(i));
          insertedRowIndexes[index] = i;
          index++;
        }
      }
      //
      updateRows(updatedRows);
      deleteRows(deletedRows);
      addRows(insertedRows, false, insertedRowIndexes);
    }
    finally {
      setTableChanging(false);
    }
  }

  /**
   * Update existing row with values from new row
   */
  private void replaceRowValues(ITableRow existingRow, ITableRow newRow) {
    try {
      existingRow.setRowChanging(true);
      //
      existingRow.setEnabled(newRow.isEnabled());
      existingRow.setStatus(newRow.getStatus());

      //map values
      for (IColumn<?> col : getColumns()) {

        int columnIndex = col.getColumnIndex();
        Object newValue = null;
        if (columnIndex < newRow.getCellCount()) {
          newValue = newRow.getCellValue(columnIndex);
        }

        col.parseValueAndSet(existingRow, newValue);
      }
    }
    finally {
      existingRow.setRowPropertiesChanged(false);
      existingRow.setRowChanging(false);
    }
  }

  @Override
  public void updateRow(ITableRow row) {
    if (row != null) {
      updateRows(CollectionUtility.arrayList(row));
    }
  }

  @Override
  public void updateAllRows() {
    updateRows(getRows());
  }

  @Override
  public void setRowState(ITableRow row, int rowState) {
    setRowState(CollectionUtility.arrayList(row), rowState);
  }

  @Override
  public void setAllRowState(int rowState) {
    setRowState(getRows(), rowState);
  }

  @Override
  public void setRowState(Collection<? extends ITableRow> rows, int rowState) {
    try {
      setTableChanging(true);
      //
      for (ITableRow row : rows) {
        row.setStatus(rowState);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void updateRows(Collection<? extends ITableRow> rows) {
    try {
      setTableChanging(true);
      //
      List<ITableRow> resolvedRowList = new ArrayList<ITableRow>(rows.size());
      for (ITableRow row : rows) {
        ITableRow resolvedRow = resolveRow(row);
        if (resolvedRow != null) {
          resolvedRowList.add(resolvedRow);
          updateRowImpl(resolvedRow);
        }
      }
      if (resolvedRowList.size() > 0) {
        fireRowsUpdated(resolvedRowList);
      }
      if (getColumnSet().getSortColumnCount() > 0) {
        // restore order of rows according to sort criteria
        if (isTableChanging()) {
          m_sortValid = false;
        }
        else {
          sort();
        }
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  private void updateRowImpl(ITableRow row) {
    if (row != null) {
      /*
       * do NOT use ITableRow#setRowChanging, this might cause a stack overflow
       */
      ensureInvalidColumnsVisible(row);
      Set<Integer> changedColumnValues = row.getUpdatedColumnIndexes(ICell.VALUE_BIT);
      if (!changedColumnValues.isEmpty()) {
        enqueueValueChangeTasks(row, changedColumnValues);
      }
      enqueueDecorationTasks(row);
    }
  }

  @Override
  public void ensureInvalidColumnsVisible() {
    List<ITableRow> rows = getRows();
    for (ITableRow row : rows) {
      ensureInvalidColumnsVisible(row);
    }
  }

  private void ensureInvalidColumnsVisible(ITableRow row) {
    for (IColumn<?> col : getColumns()) {
      col.ensureVisibileIfInvalid(row);
    }
  }

  @Override
  public int getRowCount() {
    return m_rows.size();
  }

  @Override
  public int getDeletedRowCount() {
    return m_deletedRows.size();
  }

  @Override
  public int getSelectedRowCount() {
    return m_selectedRows.size();
  }

  @Override
  public ITableRow getSelectedRow() {
    return CollectionUtility.firstElement(m_selectedRows);
  }

  @Override
  public List<ITableRow> getSelectedRows() {
    return CollectionUtility.arrayList(m_selectedRows);
  }

  @Override
  public boolean isSelectedRow(ITableRow row) {
    row = resolveRow(row);
    if (row == null) {
      return false;
    }
    else {
      return m_selectedRows.contains(row);
    }
  }

  @Override
  public boolean isCheckedRow(ITableRow row) {
    row = resolveRow(row);
    if (row == null) {
      return false;
    }
    else {
      return m_checkedRows.contains(row);
    }
  }

  @Override
  public void selectRow(int rowIndex) {
    selectRow(getRow(rowIndex));
  }

  @Override
  public void selectRow(ITableRow row) {
    selectRow(row, false);
  }

  @Override
  public void selectRow(ITableRow row, boolean append) {
    selectRows(CollectionUtility.arrayList(row), append);
  }

  @Override
  public void selectRows(List<? extends ITableRow> rows) {
    selectRows(rows, false);
  }

  @Override
  public void selectRows(List<? extends ITableRow> rows, boolean append) {
    rows = resolveRows(rows);
    TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
    if (append) {
      newSelection.addAll(m_selectedRows);
      newSelection.addAll(rows);
    }
    else {
      newSelection.addAll(rows);
    }
    // check selection count with multiselect
    if (newSelection.size() > 1 && !isMultiSelect()) {
      ITableRow first = newSelection.first();
      newSelection.clear();
      newSelection.add(first);
    }
    if (!CollectionUtility.equalsCollection(m_selectedRows, newSelection, true)) {
      m_selectedRows = new ArrayList<ITableRow>(newSelection);
      // notify menus
      List<ITableRow> notificationCopy = CollectionUtility.arrayList(m_selectedRows);

      fireRowsSelected(notificationCopy);
    }
  }

  @Override
  public void selectFirstRow() {
    selectRow(getRow(0));
  }

  @Override
  public void selectNextRow() {
    ITableRow row = getSelectedRow();
    if (row != null && row.getRowIndex() + 1 < getRowCount()) {
      selectRow(getRow(row.getRowIndex() + 1));
    }
    else if (row == null && getRowCount() > 0) {
      selectRow(0);
    }
  }

  @Override
  public void selectPreviousRow() {
    ITableRow row = getSelectedRow();
    if (row != null && row.getRowIndex() - 1 >= 0) {
      selectRow(getRow(row.getRowIndex() - 1));
    }
    else if (row == null && getRowCount() > 0) {
      selectRow(getRowCount() - 1);
    }
  }

  @Override
  public void selectLastRow() {
    selectRow(getRow(getRowCount() - 1));
  }

  @Override
  public void deselectRow(ITableRow row) {
    if (row != null) {
      deselectRows(CollectionUtility.arrayList(row));
    }
  }

  @Override
  public void deselectRows(List<? extends ITableRow> rows) {
    rows = resolveRows(rows);
    if (CollectionUtility.hasElements(rows)) {
      TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
      newSelection.addAll(m_selectedRows);
      if (newSelection.removeAll(rows)) {
        m_selectedRows = new ArrayList<ITableRow>(newSelection);
        fireRowsSelected(m_selectedRows);
      }
    }
  }

  @Override
  public void selectAllRows() {
    selectRows(getRows(), false);
  }

  @Override
  public void deselectAllRows() {
    selectRow(null, false);
  }

  @Override
  public void selectAllEnabledRows() {
    List<ITableRow> newList = new ArrayList<ITableRow>();
    for (int i = 0, ni = getRowCount(); i < ni; i++) {
      ITableRow row = getRow(i);
      if (row.isEnabled()) {
        newList.add(row);
      }
      else if (isSelectedRow(row)) {
        newList.add(row);
      }
    }
    selectRows(newList, false);
  }

  @Override
  public void deselectAllEnabledRows() {
    List<ITableRow> selectedRows = getSelectedRows();
    ArrayList<ITableRow> newList = new ArrayList<ITableRow>();
    for (ITableRow selectedRow : selectedRows) {
      if (selectedRow.isEnabled()) {
        newList.add(selectedRow);
      }
    }
    deselectRows(newList);
  }

  @Override
  public List<ITableRow> getCheckedRows() {
    return CollectionUtility.arrayList(m_checkedRows);
  }

  @Override
  public void checkRow(int row, boolean value) {
    checkRow(getRow(row), value);
  }

  @Override
  public void checkRow(ITableRow row, boolean value) {
    checkRows(CollectionUtility.arrayList(row), value);
  }

  @Override
  public void checkRows(Collection<? extends ITableRow> rows, boolean value) {
    checkRows(rows, value, false);
  }

  public void checkRows(Collection<? extends ITableRow> rows, boolean value, boolean enabledRowsOnly) {
    try {
      rows = resolveRows(rows);
      // check checked count with multicheck
      if (!isMultiCheck() && value) {
        ITableRow rowToCheck = null;
        for (ITableRow row : rows) {
          if (row.isChecked() != value && (!enabledRowsOnly || row.isEnabled())) {
            rowToCheck = row;
            break;
          }
        }
        if (rowToCheck != null) {
          if (!enabledRowsOnly) {
            uncheckAllRows();
          }
          else {
            uncheckAllEnabledRows();
          }
          checkRowImpl(rowToCheck, value);
          fireRowsChecked(CollectionUtility.arrayList(rowToCheck));
        }
      }
      else {
        List<ITableRow> rowsUpdated = new ArrayList<ITableRow>();
        for (ITableRow row : rows) {
          if (row.isChecked() != value && (!enabledRowsOnly || row.isEnabled())) {
            checkRowImpl(row, value);
            rowsUpdated.add(row);
          }
        }
        if (rowsUpdated.size() > 0) {
          if (value) {
            // sort checked rows if new checked rows have been added (not necessary if checked rows have been removed)
            sortCheckedRows();
          }
          fireRowsChecked(CollectionUtility.arrayList(rowsUpdated));
        }
      }
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  private void checkRowImpl(ITableRow row, boolean value) {
    if (!(row instanceof InternalTableRow)) {
      return;
    }
    InternalTableRow internalRow = (InternalTableRow) row;
    if (value) {
      m_checkedRows.add(internalRow);
    }
    else {
      m_checkedRows.remove(internalRow);
    }
    if (getCheckableColumn() != null) {
      getCheckableColumn().setValue(internalRow, value);
    }
    else {
      // Do not use setStatus() or setStatusUpdated(), because this would trigger unnecessary UPDATED events
      internalRow.setStatusInternal(ITableRow.STATUS_UPDATED);
    }
  }

  @Override
  public void checkAllRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), true);
    }
    finally {
      setTableChanging(false);
    }
  }

  public void checkAllEnabledRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), true, true);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void uncheckRow(ITableRow row) {
    checkRow(row, false);
  }

  @Override
  public void uncheckRows(Collection<? extends ITableRow> rows) {
    checkRows(rows, false);
  }

  @Override
  public void uncheckAllEnabledRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), false, true);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void uncheckAllRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), false);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public String getDefaultIconId() {
    String iconId = propertySupport.getPropertyString(PROP_DEFAULT_ICON);
    if (iconId != null && iconId.length() == 0) {
      iconId = null;
    }
    return iconId;
  }

  @Override
  public void setDefaultIconId(String iconId) {
    propertySupport.setPropertyString(PROP_DEFAULT_ICON, iconId);
  }

  @Override
  public boolean isRowIconVisible() {
    return propertySupport.getPropertyBool(PROP_ROW_ICON_VISIBLE);
  }

  @Override
  public void setRowIconVisible(boolean rowIconVisible) {
    propertySupport.setPropertyBool(PROP_ROW_ICON_VISIBLE, rowIconVisible);
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public final void setEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_ENABLED, b);
  }

  @Override
  public boolean isScrollToSelection() {
    return propertySupport.getPropertyBool(PROP_SCROLL_TO_SELECTION);
  }

  @Override
  public void setScrollToSelection(boolean b) {
    propertySupport.setPropertyBool(PROP_SCROLL_TO_SELECTION, b);
  }

  @Override
  public void scrollToSelection() {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_SCROLL_TO_SELECTION));
  }

  /**
   * @return a copy of a row<br>
   *         when the row is changed it has to be applied to the table using modifyRow(row);
   */
  @Override
  public ITableRow getRow(int rowIndex) {
    return CollectionUtility.getElement(getRows(), rowIndex);
  }

  @Override
  public List<ITableRow> getRows() {
    //lazy create list in getter, make sure to be thread-safe since getters may be called from "wild" threads
    synchronized (m_cachedRowsLock) {
      if (m_cachedRows == null) {
        //this code must be thread-safe
        m_cachedRows = CollectionUtility.arrayList(m_rows);
      }
      return m_cachedRows;
    }
  }

  @Override
  public List<ITableRow> getFilteredRows() {
    List<ITableRow> rows = getRows();
    if (m_rowFilters.size() > 0) {
      //lazy create list in getter, make sure to be thread-safe since getters may be called from "wild" threads
      synchronized (m_cachedFilteredRowsLock) {
        if (m_cachedFilteredRows == null) {
          //this code must be thread-safe
          if (m_rowFilters.size() > 0) {
            List<ITableRow> filteredRows = new ArrayList<ITableRow>(getRowCount());
            for (ITableRow row : rows) {
              if (row != null && row.isFilterAccepted()) {
                filteredRows.add(row);
              }
            }
            m_cachedFilteredRows = filteredRows;
          }
          else {
            m_cachedFilteredRows = CollectionUtility.emptyArrayList();
          }
        }
        return m_cachedFilteredRows;
      }
    }
    else {
      return rows;
    }
  }

  @Override
  public int getFilteredRowCount() {
    if (m_rowFilters.size() > 0) {
      return getFilteredRows().size();
    }
    else {
      return getRowCount();
    }
  }

  @Override
  public ITableRow getFilteredRow(int index) {
    if (m_rowFilters.size() > 0) {
      ITableRow row = null;
      List<ITableRow> filteredRows = getFilteredRows();
      if (index >= 0 && index < filteredRows.size()) {
        row = filteredRows.get(index);
      }
      return row;
    }
    else {
      return getRow(index);
    }
  }

  @Override
  public int getFilteredRowIndex(ITableRow row) {
    return getFilteredRows().indexOf(row);
  }

  @Override
  public List<ITableRow> getNotDeletedRows() {
    List<ITableRow> notDeletedRows = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.getStatus() != ITableRow.STATUS_DELETED) {
        notDeletedRows.add(row);
      }
    }
    return notDeletedRows;
  }

  @Override
  public int getNotDeletedRowCount() {
    return getNotDeletedRows().size();
  }

  @Override
  public Object[][] getTableData() {
    Object[][] data = new Object[getRowCount()][getColumnCount()];
    for (int r = 0; r < getRowCount(); r++) {
      for (int c = 0; c < getColumnCount(); c++) {
        data[r][c] = getRow(r).getCellValue(c);
      }
    }
    return data;
  }

  @Override
  public Object[][] exportTableRowsAsCSV(List<? extends ITableRow> rows, List<? extends IColumn> columns, boolean includeLineForColumnNames, boolean includeLineForColumnTypes, boolean includeLineForColumnFormat) {
    return TableUtility.exportRowsAsCSV(rows, columns, includeLineForColumnNames, includeLineForColumnTypes, includeLineForColumnFormat);
  }

  @Override
  public List<ITableRow> getRows(int[] rowIndexes) {
    if (rowIndexes == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<ITableRow> result = new ArrayList<ITableRow>(rowIndexes.length);
    for (int rowIndex : rowIndexes) {
      ITableRow row = getRow(rowIndex);
      if (row != null) {
        result.add(row);
      }
    }
    return result;
  }

  /**
   * @return a copy of a deleted row<br>
   *         when the row is changed it has to be applied to the table using modifyRow(row);
   */
  @Override
  public List<ITableRow> getDeletedRows() {
    return CollectionUtility.arrayList(m_deletedRows.values());
  }

  @Override
  public int getInsertedRowCount() {
    int count = 0;
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_INSERTED) {
        count++;
      }
    }
    return count;
  }

  @Override
  public List<ITableRow> getInsertedRows() {
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_INSERTED) {
        rowList.add(row);
      }
    }
    return rowList;
  }

  @Override
  public int getUpdatedRowCount() {
    int count = 0;
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_UPDATED) {
        count++;
      }
    }
    return count;
  }

  @Override
  public List<ITableRow> getUpdatedRows() {
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_UPDATED) {
        rowList.add(row);
      }
    }
    return rowList;
  }

  /**
   * Convenience to add row by data only
   */
  @Override
  public ITableRow addRowByArray(Object dataArray) {
    if (dataArray == null) {
      return null;
    }
    List<ITableRow> result = addRowsByMatrix(new Object[]{dataArray});
    return CollectionUtility.firstElement(result);
  }

  @Override
  public List<ITableRow> addRowsByMatrix(Object dataMatrix) {
    return addRowsByMatrix(dataMatrix, ITableRow.STATUS_INSERTED);
  }

  @Override
  public List<ITableRow> addRowsByMatrix(Object dataMatrix, int rowStatus) {
    return addRows(createRowsByMatrix(dataMatrix, rowStatus));
  }

  @Override
  public List<ITableRow> addRowsByArray(Object dataArray) {
    return addRowsByArray(dataArray, ITableRow.STATUS_INSERTED);
  }

  @Override
  public List<ITableRow> addRowsByArray(Object dataArray, int rowStatus) {
    return addRows(createRowsByArray(dataArray, rowStatus));
  }

  @Override
  public ITableRow addRow() {
    return addRow(true);
  }

  @Override
  public ITableRow addRow(boolean markAsInserted) {
    return addRow(createRow(), markAsInserted);
  }

  @Override
  public ITableRow addRow(ITableRow newRow) {
    return addRow(newRow, false);
  }

  @Override
  public ITableRow addRow(ITableRow newRow, boolean markAsInserted) {
    List<ITableRow> addedRows = addRows(CollectionUtility.arrayList(newRow), markAsInserted);
    return CollectionUtility.firstElement(addedRows);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows) {
    return addRows(newRows, false);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted) {
    return addRows(newRows, markAsInserted, null);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted, int[] insertIndexes) {
    if (newRows == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<InternalTableRow> newIRows = null;
    try {
      setTableChanging(true);
      //
      int oldRowCount = m_rows.size();
      initCells(newRows);
      if (markAsInserted) {
        updateStatus(newRows, ITableRow.STATUS_INSERTED);
      }
      newIRows = createInternalRows(newRows);

      addCellObserver(newIRows);
      // Fire ROWS_INSERTED event before really adding the internal rows to the table, because adding might trigger ROWS_UPDATED events (due to validation)
      fireRowsInserted(newIRows);
      for (ITableRow newIRow : newIRows) {
        addInternalRow((InternalTableRow) newIRow);
      }

      if (getColumnSet().getSortColumnCount() > 0) {
        // restore order of rows according to sort criteria
        if (isTableChanging()) {
          m_sortValid = false;
        }
        else {
          sort();
        }
      }
      else if (insertIndexes != null) {
        ITableRow[] sortArray = createSortArray(newIRows, insertIndexes, oldRowCount);
        sortInternal(Arrays.asList(sortArray));
      }
    }
    finally {
      setTableChanging(false);
    }

    return new ArrayList<ITableRow>(newIRows);
  }

  private ITableRow[] createSortArray(List<InternalTableRow> newIRows, int[] insertIndexes, int oldRowCount) {
    ITableRow[] sortArray = new ITableRow[m_rows.size()];
    // add new rows that have a given sortIndex
    for (int i = 0; i < insertIndexes.length; i++) {
      sortArray[insertIndexes[i]] = newIRows.get(i);
    }
    int sortArrayIndex = 0;
    // add existing rows
    for (int i = 0; i < oldRowCount; i++) {
      // find next empty slot
      while (sortArray[sortArrayIndex] != null) {
        sortArrayIndex++;
      }
      sortArray[sortArrayIndex] = m_rows.get(i);
    }
    // add new rows that have no given sortIndex
    for (int i = insertIndexes.length; i < newIRows.size(); i++) {
      // find next empty slot
      while (sortArray[sortArrayIndex] != null) {
        sortArrayIndex++;
      }
      sortArray[sortArrayIndex] = newIRows.get(i);
    }
    return sortArray;
  }

  /**
   * Add InternalTableRow as an observer to the cell in order to update the row status on changes
   */
  private void addCellObserver(List<InternalTableRow> rows) {
    for (InternalTableRow row : rows) {
      for (int i = 0; i < row.getCellCount(); i++) {
        Cell cell = row.getCellForUpdate(i);
        cell.setObserver(row);
      }
    }
  }

  /**
   * initialize cells with column default values
   */
  private void initCells(List<? extends ITableRow> rows) {
    for (int i = 0; i < getColumnCount(); i++) {
      for (ITableRow row : rows) {
        IColumn<?> col = getColumnSet().getColumn(i);
        col.initCell(row);
      }
    }
  }

  private void updateStatus(List<? extends ITableRow> rows, int status) {
    for (ITableRow newRow : rows) {
      newRow.setStatus(status);
    }
  }

  private List<InternalTableRow> createInternalRows(List<? extends ITableRow> newRows) {
    List<InternalTableRow> newIRows = new ArrayList<>(newRows.size());
    for (ITableRow newRow : newRows) {
      newIRows.add(new InternalTableRow(this, newRow));
    }
    return newIRows;
  }

  private ITableRow addInternalRow(InternalTableRow newIRow) {
    synchronized (m_cachedRowsLock) {
      m_cachedRows = null;
      int newIndex = m_rows.size();
      newIRow.setRowIndex(newIndex);
      newIRow.setTableInternal(this);
      m_rows.add(newIRow);
    }

    Set<Integer> indexes = new HashSet<Integer>();
    for (int idx : getColumnSet().getAllColumnIndexes()) {
      indexes.add(idx);
    }

    enqueueValueChangeTasks(newIRow, indexes);
    enqueueDecorationTasks(newIRow);
    return newIRow;
  }

  @Override
  public void moveRow(int sourceIndex, int targetIndex) {
    moveRowImpl(sourceIndex, targetIndex);
  }

  /**
   * move the movingRow to the location just before the target row
   */
  @Override
  public void moveRowBefore(ITableRow movingRow, ITableRow targetRow) {
    movingRow = resolveRow(movingRow);
    targetRow = resolveRow(targetRow);
    if (movingRow != null && targetRow != null) {
      int sourceIndex = movingRow.getRowIndex();
      int targetIndex = targetRow.getRowIndex();
      if (sourceIndex < targetIndex) {
        moveRowImpl(sourceIndex, targetIndex - 1);
      }
      else {
        moveRowImpl(sourceIndex, targetIndex);
      }
    }
  }

  /**
   * move the movingRow to the location just after the target row
   */
  @Override
  public void moveRowAfter(ITableRow movingRow, ITableRow targetRow) {
    movingRow = resolveRow(movingRow);
    targetRow = resolveRow(targetRow);
    if (movingRow != null && targetRow != null) {
      int sourceIndex = movingRow.getRowIndex();
      int targetIndex = targetRow.getRowIndex();
      if (sourceIndex > targetIndex) {
        moveRowImpl(sourceIndex, targetIndex + 1);
      }
      else {
        moveRowImpl(sourceIndex, targetIndex);
      }
    }
  }

  /**
   * @see {@link List#add(int, Object)}
   * @param sourceIndex
   * @param targetIndex
   */
  private void moveRowImpl(int sourceIndex, int targetIndex) {
    if (sourceIndex < 0) {
      sourceIndex = 0;
    }
    if (sourceIndex >= getRowCount()) {
      sourceIndex = getRowCount() - 1;
    }
    if (targetIndex < 0) {
      targetIndex = 0;
    }
    if (targetIndex >= getRowCount()) {
      targetIndex = getRowCount() - 1;
    }
    if (targetIndex != sourceIndex) {
      synchronized (m_cachedRowsLock) {
        m_cachedRows = null;
        ITableRow row = m_rows.remove(sourceIndex);
        m_rows.add(targetIndex, row);
      }
      // update row indexes
      int min = Math.min(sourceIndex, targetIndex);
      int max = Math.max(sourceIndex, targetIndex);
      ITableRow[] changedRows = new ITableRow[max - min + 1];
      for (int i = min; i <= max; i++) {
        changedRows[i - min] = getRow(i);
        ((InternalTableRow) changedRows[i - min]).setRowIndex(i);
      }
      fireRowOrderChanged();
      // rebuild selection and checked rows
      selectRows(getSelectedRows(), false);
      sortCheckedRows();
    }
  }

  @Override
  public void deleteRow(int rowIndex) {
    deleteRows(new int[]{rowIndex});
  }

  @Override
  public void deleteRows(int[] rowIndexes) {
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int i = 0; i < rowIndexes.length; i++) {
      ITableRow row = getRow(rowIndexes[i]);
      if (row != null) {
        rowList.add(row);
      }
    }
    deleteRows(rowList);
  }

  @Override
  public void deleteRow(ITableRow row) {
    if (row != null) {
      deleteRows(CollectionUtility.arrayList(row));
    }
  }

  @Override
  public void deleteAllRows() {
    deleteRows(getRows());
  }

  @Override
  public void deleteRows(Collection<? extends ITableRow> rows) {

    List<ITableRow> existingRows = getRows();
    //peformance quick-check
    if (rows != existingRows) {
      rows = resolveRows(rows);
    }
    if (CollectionUtility.hasElements(rows)) {
      try {
        setTableChanging(true);
        //
        int rowCountBefore = getRowCount();
        int min = getRowCount();
        int max = 0;
        for (ITableRow row : rows) {
          min = Math.min(min, row.getRowIndex());
          max = Math.max(max, row.getRowIndex());
        }
        List<ITableRow> deletedRows = new ArrayList<ITableRow>(rows);
        // remove from selection
        deselectRows(deletedRows);
        uncheckRows(deletedRows);
        //delete impl
        //peformance quick-check
        if (rows == existingRows) {
          //remove all of them
          synchronized (m_cachedRowsLock) {
            m_rows.clear();
            m_cachedRows = null;
          }
          for (int i = deletedRows.size() - 1; i >= 0; i--) {
            ITableRow candidateRow = deletedRows.get(i);
            if (candidateRow != null) {
              deleteRowImpl(candidateRow);
            }
          }
        }
        else {
          for (int i = deletedRows.size() - 1; i >= 0; i--) {
            ITableRow candidateRow = deletedRows.get(i);
            if (candidateRow != null) {
              // delete regardless if index is right
              boolean removed = false;
              synchronized (m_cachedRowsLock) {
                removed = m_rows.remove(candidateRow);
                if (removed) {
                  m_cachedRows = null;
                }
              }
              if (removed) {
                deleteRowImpl(candidateRow);
              }
            }
          }
        }
        // get affected rows
        List<ITableRow> selectionRows = new ArrayList<ITableRow>(getSelectedRows());
        int minAffectedIndex = Math.max(min - 1, 0);
        ITableRow[] affectedRows = new ITableRow[getRowCount() - minAffectedIndex];
        for (int i = minAffectedIndex; i < getRowCount(); i++) {
          affectedRows[i - minAffectedIndex] = getRow(i);
          ((InternalTableRow) affectedRows[i - minAffectedIndex]).setRowIndex(i);
          selectionRows.remove(getRow(i));
        }
        if (rowCountBefore == deletedRows.size()) {
          fireAllRowsDeleted(deletedRows);
        }
        else {
          fireRowsDeleted(deletedRows);
        }
        //TODO [5.2] cgu: is this necessary? Deleted rows are deselected above
        selectRows(selectionRows, false);
      }
      finally {
        setTableChanging(false);
      }
    }
  }

  private void deleteRowImpl(ITableRow row) {
    if (!(row instanceof InternalTableRow)) {
      return;
    }
    InternalTableRow internalRow = (InternalTableRow) row;
    if (isAutoDiscardOnDelete()) {
      internalRow.setTableInternal(null);
      // don't manage deleted rows any further
    }
    else if (internalRow.getStatus() == ITableRow.STATUS_INSERTED) {
      internalRow.setTableInternal(null);
      // it was new and now it is gone, no further action required
    }
    else {
      internalRow.setStatus(ITableRow.STATUS_DELETED);
      m_deletedRows.put(new CompositeObject(getRowKeys(internalRow)), internalRow);
    }
  }

  @Override
  public void discardRow(int rowIndex) {
    discardRows(new int[]{rowIndex});
  }

  @Override
  public void discardRows(int[] rowIndexes) {
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int rIndex : rowIndexes) {
      ITableRow row = getRow(rIndex);
      if (row != null) {
        rowList.add(row);
      }
    }
    discardRows(rowList);
  }

  @Override
  public void discardRow(ITableRow row) {
    if (row != null) {
      discardRows(CollectionUtility.arrayList(row));
    }
  }

  @Override
  public void discardAllRows() {
    discardRows(getRows());
  }

  /**
   * discard is the same as delete with the exception that discarded rows are not collected in the deletedRows list
   */
  @Override
  public void discardRows(Collection<? extends ITableRow> rows) {
    try {
      setTableChanging(true);
      //
      for (ITableRow row : rows) {
        row.setStatus(ITableRow.STATUS_INSERTED);
      }
      deleteRows(rows);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void discardAllDeletedRows() {
    for (Iterator<ITableRow> it = m_deletedRows.values().iterator(); it.hasNext();) {
      ((InternalTableRow) it.next()).setTableInternal(null);
    }
    m_deletedRows.clear();
  }

  @Override
  public void discardDeletedRow(ITableRow deletedRow) {
    if (deletedRow != null) {
      discardDeletedRows(CollectionUtility.arrayList(deletedRow));
    }
  }

  @Override
  public void discardDeletedRows(Collection<? extends ITableRow> deletedRows) {
    if (deletedRows != null) {
      for (ITableRow row : deletedRows) {
        m_deletedRows.remove(new CompositeObject(getRowKeys(row)));
        ((InternalTableRow) row).setTableInternal(null);
      }
    }
  }

  @Override
  public void setContextColumn(IColumn<?> col) {
    propertySupport.setProperty(PROP_CONTEXT_COLUMN, col);
  }

  @Override
  public IColumn<?> getContextColumn() {
    return (IColumn<?>) propertySupport.getProperty(PROP_CONTEXT_COLUMN);
  }

  @Override
  public List<Object> getRowKeys(int rowIndex) {
    ITableRow row = getRow(rowIndex);
    return getRowKeys(row);
  }

  @Override
  public List<Object> getRowKeys(ITableRow row) {
    if (row != null) {
      return row.getKeyValues();
    }
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public ITableRow findRowByKey(List<?> keys) {
    if (!CollectionUtility.hasElements(keys)) {
      return null;
    }

    List<IColumn<?>> keyColumns = getColumnSet().getKeyColumns();
    if (keyColumns.size() == 0) {
      keyColumns = getColumnSet().getColumns();
    }
    if (keyColumns.size() == 0) {
      return null; // no columns in the table: cannot search by keys
    }

    for (ITableRow row : m_rows) {
      if (areCellsEqual(keys, keyColumns, row)) {
        return row;
      }
    }
    return null;
  }

  /**
   * Gets if the given cell values are equal to the given search values
   *
   * @param searchValues
   *          The values to search in the given cells. Must not be <code>null</code>.
   * @param keyColumns
   *          The columns describing the cells to be searched. Must not be <code>null</code>.
   * @param row
   *          The row holding the cells to be searched. Must not be <code>null</code>.
   * @return <code>true</code> if the cells described by the given columns and row have the same content as the given
   *         searchValues. <code>false</code> otherwise. If the number of columns is different than the number of search
   *         values only the columns are searched for which a search value exists (
   *         <code>min(searchValues.size(), keyColumns.size()</code>).
   */
  protected boolean areCellsEqual(List<?> searchValues, List<IColumn<?>> keyColumns, ITableRow row) {
    int keyIndex = 0;
    int numKeyColumns = keyColumns.size();
    for (Object key : searchValues) {
      if (keyIndex >= numKeyColumns) {
        break;
      }

      Object cellValue = keyColumns.get(keyIndex).getValue(row);
      if (!CompareUtility.equals(key, cellValue)) {
        return false;
      }

      keyIndex++;
    }
    return true;
  }

  @Override
  public TableUserFilterManager getUserFilterManager() {
    return (TableUserFilterManager) propertySupport.getProperty(PROP_USER_FILTER_MANAGER);
  }

  @Override
  public void setUserFilterManager(TableUserFilterManager m) {
    propertySupport.setProperty(PROP_USER_FILTER_MANAGER, m);
  }

  @Override
  public ITableCustomizer getTableCustomizer() {
    return (ITableCustomizer) propertySupport.getProperty(PROP_TABLE_CUSTOMIZER);
  }

  @Override
  public void setTableCustomizer(ITableCustomizer c) {
    propertySupport.setProperty(PROP_TABLE_CUSTOMIZER, c);
  }

  @Override
  public ITypeWithClassId getContainer() {
    return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls an {@link ITable}
   */
  public void setContainerInternal(ITypeWithClassId container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  @Override
  public boolean isSortEnabled() {
    return propertySupport.getPropertyBool(PROP_SORT_ENABLED);
  }

  @Override
  public void setSortEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_SORT_ENABLED, b);
  }

  @Override
  public boolean isUiSortPossible() {
    return propertySupport.getPropertyBool(PROP_UI_SORT_POSSIBLE);
  }

  @Override
  public void setUiSortPossible(boolean b) {
    propertySupport.setPropertyBool(PROP_UI_SORT_POSSIBLE, b);
  }

  public void onGroupedColumnInvisible(IColumn<?> col) {
    if (isTableChanging()) {
      m_sortValid = false;
    }
    else {
      sort();
    }
  }

  @Override
  public void sort() {
    try {
      if (isSortEnabled()) {
        // Consider any active sort-column, not only explicit ones.
        // This is to support reverse (implicit) sorting of columns, meaning that multiple column sort is done
        // without CTRL-key held. In contrast to explicit multiple column sort, the first clicked column
        // is the least significant sort column.
        List<IColumn<?>> sortCols = getColumnSet().getSortColumns();
        if (!sortCols.isEmpty() && !getRows().isEmpty()) {
          // first make sure decorations and lookups are up-to-date
          processDecorationBuffer();
          List<ITableRow> a = new ArrayList<ITableRow>(getRows());
          Collections.sort(a, new TableRowComparator(sortCols));
          sortInternal(a);
        }
      }
    }
    finally {
      m_sortValid = true;
    }
  }

  @Override
  public void sort(List<? extends ITableRow> rowsInNewOrder) {
    List<ITableRow> resolvedRows = resolveRows(rowsInNewOrder);
    if (resolvedRows.size() == rowsInNewOrder.size()) {
      sortInternal(resolvedRows);
    }
    else {
      // check which rows could not be mapped
      ArrayList<ITableRow> list = new ArrayList<ITableRow>();
      list.addAll(m_rows);
      list.removeAll(resolvedRows);
      ArrayList<ITableRow> sortedList = new ArrayList<ITableRow>();
      sortedList.addAll(resolvedRows);
      sortedList.addAll(list);
      sortInternal(sortedList);
    }
  }

  private void sortInternal(List<? extends ITableRow> resolvedRows) {
    int i = 0;
    for (ITableRow row : resolvedRows) {
      ((InternalTableRow) row).setRowIndex(i);
      i++;
    }
    synchronized (m_cachedRowsLock) {
      m_cachedRows = null;
      m_rows.clear();
      m_rows.addAll(resolvedRows);
    }
    //sort selection and checked rows without firing an event
    if (m_selectedRows != null && m_selectedRows.size() > 0) {
      TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
      newSelection.addAll(m_selectedRows);
      m_selectedRows = new ArrayList<ITableRow>(newSelection);
    }
    sortCheckedRows();
    fireRowOrderChanged();
  }

  private void sortCheckedRows() {
    if (m_checkedRows == null || m_checkedRows.size() == 0) {
      return;
    }
    TreeSet<ITableRow> newCheckedRows = new TreeSet<ITableRow>(new RowIndexComparator());
    newCheckedRows.addAll(m_checkedRows);
    m_checkedRows = new LinkedHashSet<>(newCheckedRows);
  }

  @Override
  public void resetColumnConfiguration() {
    discardAllRows();
    //
    try {
      setTableChanging(true);
      // save displayable state
      HashMap<String, Boolean> displayableState = new HashMap<String, Boolean>();
      for (IColumn<?> col : getColumns()) {
        displayableState.put(col.getColumnId(), col.isDisplayable());
      }
      // reset columns
      disposeColumnsInternal();
      createColumnsInternal();
      initColumnsInternal();
      // re-apply displayable
      for (IColumn<?> col : getColumns()) {
        if (displayableState.get(col.getColumnId()) != null) {
          col.setDisplayable(displayableState.get(col.getColumnId()));
        }
      }
      // re link existing filters to new columns
      linkColumnFilters();
      fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED));
    }
    finally {
      setTableChanging(false);
    }
  }

  private void linkColumnFilters() {
    TableUserFilterManager filterManager = getUserFilterManager();
    if (filterManager == null) {
      return;
    }
    for (IColumn<?> col : getColumns()) {
      IUserFilterState filter = getUserFilterManager().getFilter(col);
      if (filter == null) {
        continue;
      }
      if (!(filter instanceof ColumnUserFilterState)) {
        throw new IllegalStateException("Unexpected filter state" + filter.getClass());
      }
      ((ColumnUserFilterState) filter).setColumn(col);
    }
  }

  @Override
  public void resetColumnVisibilities() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.VISIBILITY));
  }

  @Override
  public void resetColumnOrder() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.ORDER));
  }

  @Override
  public void resetColumnSortOrder() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.SORTING));
  }

  @Override
  public void resetColumnWidths() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.WIDTHS));
  }

  @Override
  public void resetColumnBackgroundEffects() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.BACKGROUND_EFFECTS));
  }

  @Override
  public void resetColumns() {
    resetColumns(CollectionUtility.hashSet(
        IResetColumnsOption.VISIBILITY,
        IResetColumnsOption.ORDER,
        IResetColumnsOption.SORTING,
        IResetColumnsOption.WIDTHS,
        IResetColumnsOption.BACKGROUND_EFFECTS));
  }

  protected void resetColumns(Set<String> options) {
    try {
      setTableChanging(true);
      // TODO [5.2] asa: move to internal?
      if (options.contains(IResetColumnsOption.SORTING)) {
        m_sortValid = false;
      }
      resetColumnsInternal(options);
      interceptResetColumns(options);
    }
    finally {
      setTableChanging(false);
    }
  }

  private void resetColumnsInternal(Set<String> options) {

    if (options.contains(IResetColumnsOption.VISIBILITY)) {
      ArrayList<IColumn<?>> list = new ArrayList<IColumn<?>>();
      for (IColumn<?> col : getColumnSet().getAllColumnsInUserOrder()) {
        if (col.isDisplayable()) {
          boolean configuredVisible = ((AbstractColumn<?>) col).isInitialVisible();
          if (configuredVisible) {
            list.add(col);
          }
        }
      }
      getColumnSet().setVisibleColumns(list);
    }

    if (options.contains(IResetColumnsOption.ORDER)) {
      TreeMap<CompositeObject, IColumn<?>> orderMap = new TreeMap<CompositeObject, IColumn<?>>();
      int index = 0;
      for (IColumn<?> col : getColumns()) {
        if (col.isDisplayable() && col.isVisible()) {
          orderMap.put(new CompositeObject(col.getOrder(), index), col);
          index++;
        }
      }
      getColumnSet().setVisibleColumns(orderMap.values());
    }

    if (options.contains(IResetColumnsOption.SORTING)) {
      getColumnSet().resetSortingAndGrouping();
    }

    if (options.contains(IResetColumnsOption.WIDTHS)) {
      for (IColumn<?> col : getColumns()) {
        if (col.isDisplayable()) {
          col.setWidth(col.getInitialWidth());
        }
      }
    }

    if (options.contains(IResetColumnsOption.BACKGROUND_EFFECTS)) {
      for (IColumn<?> col : getColumns()) {
        if (col instanceof INumberColumn) {
          ((INumberColumn) col).setBackgroundEffect(((INumberColumn) col).getInitialBackgroundEffect());
        }
      }
    }
  }

  /**
   * Affects columns with lookup calls or code types<br>
   * cells that have changed values fetch new texts/decorations from the lookup service in one single batch call lookup
   * (performance optimization)
   */
  private void processDecorationBuffer() {
    /*
     * update row decorations
     */
    Map<Integer, Set<ITableRow>> changes = m_rowValueChangeBuffer;
    m_rowValueChangeBuffer = new HashMap<>();
    applyRowValueChanges(changes);

    Set<ITableRow> set = m_rowDecorationBuffer;
    m_rowDecorationBuffer = new HashSet<ITableRow>();
    applyRowDecorations(set);
    /*
     * check row filters
     */
    if (m_rowFilters.size() > 0) {
      boolean filterChanged = false;
      for (ITableRow row : set) {
        if (row.getTable() == AbstractTable.this) {
          if (row instanceof InternalTableRow) {
            InternalTableRow irow = (InternalTableRow) row;
            boolean oldFlag = irow.isFilterAccepted();
            applyRowFiltersInternal(irow);
            boolean newFlag = irow.isFilterAccepted();
            filterChanged = filterChanged || (oldFlag != newFlag);
          }
        }
      }
      if (filterChanged) {
        fireRowFilterChanged();
      }
    }
  }

  private void applyRowValueChanges(Map<Integer, Set<ITableRow>> changes) {
    try {
      for (ITableRow tableRow : getRows()) {
        tableRow.setRowChanging(true);
      }

      Set<Entry<Integer, Set<ITableRow>>> entrySet = changes.entrySet();

      for (Entry<Integer, Set<ITableRow>> e : entrySet) {
        IColumn<?> col = getColumnSet().getColumn(e.getKey());
        col.updateDisplayTexts(CollectionUtility.arrayList(e.getValue()));
      }
    }
    finally {
      for (ITableRow tableRow : getRows()) {
        tableRow.setRowPropertiesChanged(false);
        tableRow.setRowChanging(false);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void applyRowDecorations(Set<ITableRow> rows) {
    try {
      for (ITableRow tableRow : rows) {
        tableRow.setRowChanging(true);
        this.decorateRow(tableRow);
      }

      for (IColumn col : getColumns()) {
        col.decorateCells(CollectionUtility.arrayList(rows));

        // cell decorator on table
        for (ITableRow row : rows) {
          this.decorateCell(row, col);
        }
      }
    }
    catch (Exception ex) {
      LOG.error("Error occured while applying row decoration", ex);
    }
    finally {
      for (ITableRow tableRow : rows) {
        tableRow.setRowPropertiesChanged(false);
        tableRow.setRowChanging(false);
      }
    }
  }

  /**
   * Fires events in form in of one batch <br>
   * Unnecessary events are removed or merged.
   */
  private void processEventBuffer() {
    //loop detection
    try {
      m_eventBufferLoopDetection++;
      if (m_eventBufferLoopDetection > 100) {
        LOG.error("LOOP DETECTION in {}. see stack trace for more details.", getClass(), new Exception("LOOP DETECTION"));
        return;
      }
      //
      if (!getEventBuffer().isEmpty()) {
        List<TableEvent> coalescedEvents = getEventBuffer().consumeAndCoalesceEvents();
        // fire the batch and set tree to changing, otherwise a listener might trigger another events that
        // then are processed before all other listeners received that batch
        try {
          setTableChanging(true);
          //
          fireTableEventBatchInternal(coalescedEvents);
        }
        finally {
          setTableChanging(false);
        }
      }
    }
    finally {
      m_eventBufferLoopDetection--;
    }
  }

  /**
   * do decoration and filtering later
   */
  private void enqueueDecorationTasks(ITableRow row) {
    if (row != null) {
      m_rowDecorationBuffer.add(row);
    }
  }

  private void enqueueValueChangeTasks(ITableRow row, Set<Integer> valueChangedColumns) {
    for (Integer colIndex : valueChangedColumns) {
      Set<ITableRow> rows = m_rowValueChangeBuffer.get(colIndex);
      if (rows == null) {
        rows = new HashSet<ITableRow>();
      }
      rows.add(row);
      m_rowValueChangeBuffer.put(colIndex, rows);
    }
  }

  @Override
  public void tablePopulated() {
    if (getEventBuffer().isEmpty()) {
      synchronized (m_cachedFilteredRowsLock) {
        m_cachedFilteredRows = null;
      }
      fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_TABLE_POPULATED, null));
    }
  }

  @Override
  public ITableRow resolveRow(ITableRow row) {
    if (row == null) {
      return null;
    }
    if (!(row instanceof InternalTableRow)) {
      throw new IllegalArgumentException("only accept InternalTableRow, not " + (row != null ? row.getClass() : null));
    }
    // check owner
    if (row.getTable() == this) {
      return row;
    }
    else {
      return null;
    }
  }

  @Override
  public List<ITableRow> resolveRows(Collection<? extends ITableRow> rows) {
    if (rows == null) {
      rows = CollectionUtility.emptyArrayList();
    }
    List<ITableRow> resolvedRows = new ArrayList<ITableRow>(rows.size());
    for (ITableRow row : rows) {
      if (resolveRow(row) == row) {
        resolvedRows.add(row);
      }
      else {
        LOG.warn("could not resolve row {}", row);
      }
    }
    return resolvedRows;
  }

  @Override
  public boolean isHeaderVisible() {
    return propertySupport.getPropertyBool(PROP_HEADER_VISIBLE);
  }

  @Override
  public void setHeaderVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_HEADER_VISIBLE, b);
  }

  @Override
  public boolean isHeaderEnabled() {
    return propertySupport.getPropertyBool(PROP_HEADER_ENABLED);
  }

  @Override
  public void setHeaderEnabled(boolean headerEnabled) {
    propertySupport.setPropertyBool(PROP_HEADER_ENABLED, headerEnabled);
  }

  @Override
  public final void decorateCell(ITableRow row, IColumn<?> col) {
    Cell cell = row.getCellForUpdate(col.getColumnIndex());
    decorateCellInternal(cell, row, col);
    try {
      interceptDecorateCell(cell, row, col);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  protected void decorateCellInternal(Cell view, ITableRow row, IColumn<?> col) {
  }

  @Override
  public final void decorateRow(ITableRow row) {
    decorateRowInternal(row);
    try {
      interceptDecorateRow(row);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  protected void decorateRowInternal(ITableRow row) {
    // icon
    if (row.getIconId() == null) {
      String s = getDefaultIconId();
      if (s != null) {
        row.setIconId(s);
      }
    }
  }

  /**
   * Called when the columns are reset.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param options
   *          Set of constants of {@link IResetColumnsOption}
   */
  @ConfigOperation
  @Order(90)
  protected void execResetColumns(Set<String> options) {
  }

  /**
   * Model Observer
   */
  @Override
  public void addTableListener(TableListener listener) {
    m_listenerList.add(TableListener.class, listener);
  }

  @Override
  public void removeTableListener(TableListener listener) {
    m_listenerList.remove(TableListener.class, listener);
  }

  @Override
  public void addUITableListener(TableListener listener) {
    m_listenerList.insertAtFront(TableListener.class, listener);
  }

  protected IEventHistory<TableEvent> createEventHistory() {
    return new DefaultTableEventHistory(5000L);
  }

  @Override
  public IEventHistory<TableEvent> getEventHistory() {
    return m_eventHistory;
  }

  private void fireRowsInserted(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_INSERTED, rows));
  }

  private void fireRowsUpdated(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    LOG.debug("fire rows updated {}", rows);
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_UPDATED, rows);
    // For each row, add information about updated columns to the event. (A row may also be updated if
    // not specific column was changed, e.g. when a row's enabled state changes.)
    for (ITableRow row : rows) {
      // Convert column indexes to IColumns
      Set<Integer> columnIndexes = row.getUpdatedColumnIndexes();
      if (!columnIndexes.isEmpty()) {
        Set<IColumn<?>> columns = new HashSet<>();
        for (Integer columnIndex : columnIndexes) {
          IColumn<?> column = getColumns().get(columnIndex);
          if (column != null) {
            columns.add(column);
          }
        }
        // Put updated columns into event
        e.setUpdatedColumns(row, columns);
      }
    }
    fireTableEventInternal(e);
  }

  /**
   * Request to reload/replace table data with refreshed data
   */
  private void fireRowsDeleted(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_DELETED, rows));
  }

  private void fireAllRowsDeleted(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ALL_ROWS_DELETED, rows));
  }

  private void fireRowsSelected(List<? extends ITableRow> rows) {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_SELECTED, rows));
  }

  private void fireRowsChecked(List<? extends ITableRow> rows) {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_CHECKED, rows));
  }

  private void fireRowClick(ITableRow row, MouseButton mouseButton) {
    if (row != null) {
      try {
        interceptRowClickSingleObserver(row, mouseButton);
        interceptRowClick(row, mouseButton);
      }
      catch (Exception ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }
  }

  protected void interceptRowClickSingleObserver(ITableRow row, MouseButton mouseButton) {
    // Only toggle checked state if the table and row are enabled.
    if (!row.isEnabled() || !isEnabled()) {
      return;
    }

    // Only toggle checked state if being fired by the left mousebutton (https://bugs.eclipse.org/bugs/show_bug.cgi?id=453543).
    if (mouseButton != MouseButton.Left) {
      return;
    }

    IColumn<?> ctxCol = getContextColumn();
    if (isCellEditable(row, ctxCol)) {
      //cell-level checkbox
      if (ctxCol instanceof IBooleanColumn) {
        //editable boolean columns consume this click
        IFormField field = ctxCol.prepareEdit(row);
        if (field instanceof IBooleanField) {
          IBooleanField bfield = (IBooleanField) field;
          bfield.setChecked(!bfield.isChecked());
          ctxCol.completeEdit(row, field);
        }
      }
      else {
        //other editable columns have no effect HERE, the ui will open an editor
      }
    }
  }

  private void fireRowAction(ITableRow row) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        if (row != null) {
          try {
            interceptRowAction(row);
          }
          catch (Exception ex) {
            BEANS.get(ExceptionHandler.class).handle(ex);
          }
        }
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  private void fireRowOrderChanged() {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROW_ORDER_CHANGED, getRows()));
  }

  private void fireRequestFocus() {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_REQUEST_FOCUS));
  }

  private void fireRequestFocusInCell(IColumn<?> column, ITableRow row) {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_REQUEST_FOCUS_IN_CELL);
    e.setColumns(CollectionUtility.hashSet(column));
    e.setRows(CollectionUtility.arrayList(row));
    fireTableEventInternal(e);
  }

  private void fireRowFilterChanged() {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROW_FILTER_CHANGED));
  }

  private TransferObject fireRowsDragRequest() {
    List<ITableRow> rows = getSelectedRows();
    if (CollectionUtility.hasElements(rows)) {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_DRAG_REQUEST, rows);
      fireTableEventInternal(e);
      return e.getDragObject();
    }
    else {
      return null;
    }
  }

  private void fireRowDropAction(ITableRow row, TransferObject dropData) {
    List<ITableRow> rows = null;
    if (row != null) {
      rows = CollectionUtility.arrayList(row);
    }
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_DROP_ACTION, rows);
    e.setDropObject(dropData);
    fireTableEventInternal(e);
  }

  private TransferObject fireRowsCopyRequest() {
    List<ITableRow> rows = getSelectedRows();
    if (CollectionUtility.hasElements(rows)) {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_COPY_REQUEST, rows);
      fireTableEventInternal(e);
      return e.getCopyObject();
    }
    else {
      return null;
    }
  }

  // main handler
  public void fireTableEventInternal(TableEvent e) {
    if (isTableChanging()) {
      // buffer the event for later batch firing
      getEventBuffer().add(e);
    }
    else {
      TableListener[] listeners = m_listenerList.getListeners(TableListener.class);
      for (TableListener l : listeners) {
        l.tableChanged(e);
      }
    }
  }

  // batch handler
  private void fireTableEventBatchInternal(List<? extends TableEvent> batch) {
    if (CollectionUtility.hasElements(batch)) {
      EventListener[] listeners = m_listenerList.getListeners(TableListener.class);
      for (EventListener l : listeners) {
        ((TableListener) l).tableChangedBatch(batch);
      }
    }
  }

  protected boolean handleKeyStroke(String keyName, char keyChar) {
    if (keyName == null) {
      return false;
    }
    keyName = keyName.toLowerCase();
    // check if there is no menu keystroke with that name
    for (IMenu m : getMenus()) {
      if (m.getKeyStroke() != null && m.getKeyStroke().equalsIgnoreCase(keyName)) {
        return false;
      }
    }
    // check if there is no keystroke with that name (ticket 78234)
    for (IKeyStroke k : getKeyStrokes()) {
      if (k.getKeyStroke() != null && k.getKeyStroke().equalsIgnoreCase(keyName)) {
        return false;
      }
    }
    if (keyChar > ' ' && (!keyName.contains("control")) && (!keyName.contains("ctrl")) && (!keyName.contains("alt"))) {
      // select first/next line with corresponding character
      String newText = "" + Character.toLowerCase(keyChar);
      m_keyStrokeBuffer.append(newText);
      String prefix = m_keyStrokeBuffer.getText();

      IColumn<?> col = getContextColumn();
      if (col == null) {
        List<IColumn<?>> sortCols = getColumnSet().getSortColumns();
        if (sortCols.size() > 0) {
          col = CollectionUtility.lastElement(sortCols);
        }
        else {
          TreeMap<CompositeObject, IColumn<?>> sortMap = new TreeMap<CompositeObject, IColumn<?>>();
          int index = 0;
          for (IColumn<?> c : getColumnSet().getVisibleColumns()) {
            if (c.getDataType() == String.class) {
              sortMap.put(new CompositeObject(1, index), c);
            }
            else if (c.getDataType() == Boolean.class) {
              sortMap.put(new CompositeObject(3, index), c);
            }
            else {
              sortMap.put(new CompositeObject(2, index), c);
            }
            index++;
          }
          if (sortMap.size() > 0) {
            col = sortMap.get(sortMap.firstKey());
          }
        }
      }
      if (col != null) {
        int colIndex = col.getColumnIndex();
        String pattern = StringUtility.toRegExPattern(prefix.toLowerCase());
        pattern = pattern + ".*";
        if (LOG.isInfoEnabled()) {
          LOG.info("finding regex: '{}' in column '{}'", pattern, getColumnSet().getColumn(colIndex).getHeaderCell().getText());
        }
        // loop over values and find matching one
        int rowCount = getRowCount();
        ITableRow selRow = getSelectedRow();
        int startIndex = 0;
        if (selRow != null) {
          if (prefix.length() <= 1) {
            startIndex = selRow.getRowIndex() + 1;
          }
          else {
            startIndex = selRow.getRowIndex();
          }
        }
        for (int i = 0; i < rowCount; i++) {
          ITableRow row = m_rows.get((startIndex + i) % rowCount);
          String text = row.getCell(colIndex).getText();
          if (text != null && text.toLowerCase().matches(pattern)) {
            // handled
            selectRow(row, false);
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public ITableUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setReloadHandler(IReloadHandler reloadHandler) {
    m_reloadHandler = reloadHandler;
  }

  @Override
  public IReloadHandler getReloadHandler() {
    return m_reloadHandler;
  }

  @Override
  public List<ITableControl> getTableControls() {
    return CollectionUtility.arrayList(m_tableControls);
  }

  @Override
  public void addTableControl(ITableControl control) {
    m_tableControls.add(control);
    addTableControlInternal(control);
  }

  @Override
  public void addTableControl(int index, ITableControl control) {
    m_tableControls.add(index, control);
    addTableControlInternal(control);
  }

  private void addTableControlInternal(ITableControl control) {
    ((AbstractTableControl) control).setTable(this);
    propertySupport.firePropertyChange(PROP_TABLE_CONTROLS, null, getTableControls());
  }

  @Override
  public void removeTableControl(ITableControl control) {
    m_tableControls.remove(control);
    ((AbstractTableControl) control).setTable(null);
    propertySupport.firePropertyChange(PROP_TABLE_CONTROLS, null, getTableControls());
  }

  @Override
  public <T extends ITableControl> T getTableControl(Class<T> controlClass) {
    for (ITableControl control : m_tableControls) {
      if (controlClass.isAssignableFrom(control.getClass())) {
        return controlClass.cast(control);
      }
    }
    return null;
  }

  /**
   * Configures the visibility of the table status.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the table status is visible, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredTableStatusVisible() {
    return false;
  }

  @Override
  public boolean isTableStatusVisible() {
    return propertySupport.getPropertyBool(PROP_TABLE_STATUS_VISIBLE);
  }

  @Override
  public void setTableStatusVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_TABLE_STATUS_VISIBLE, visible);
  }

  @Override
  public IStatus getTableStatus() {
    return (IStatus) propertySupport.getProperty(PROP_TABLE_STATUS);
  }

  @Override
  public void setTableStatus(IStatus status) {
    propertySupport.setProperty(PROP_TABLE_STATUS, status);
  }

  /**
   * Check if this column would prevent an ui sort for table. If it prevents an ui sort,
   * {@link ITable#setUiSortPossible(boolean)} is set to <code>false</code> for all columns of the table.
   */
  protected void checkIfColumnPreventsUiSortForTable() {
    for (IColumn<?> column : m_columnSet.getColumns()) {
      if (!column.isVisible() && column.getSortIndex() != -1) {
        setUiSortPossible(false);
        return;
      }
    }
    setUiSortPossible(true);
  }

  /*
   * UI Notifications
   */
  protected class P_TableUIFacade implements ITableUIFacade {
    private int m_uiProcessorCount = 0;

    protected void pushUIProcessor() {
      m_uiProcessorCount++;
    }

    protected void popUIProcessor() {
      m_uiProcessorCount--;
    }

    @Override
    public boolean isUIProcessing() {
      return m_uiProcessorCount > 0;
    }

    @Override
    public void fireRowClickFromUI(ITableRow row, MouseButton mouseButton) {
      try {
        pushUIProcessor();
        //
        row = resolveRow(row);
        if (row != null) {
          fireRowClick(resolveRow(row), mouseButton);
        }
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireRowActionFromUI(ITableRow row) {
      try {
        pushUIProcessor();
        //
        row = resolveRow(row);
        if (row != null) {
          fireRowAction(row);
        }
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public boolean fireKeyTypedFromUI(String keyStrokeText, char keyChar) {
      try {
        pushUIProcessor();
        //
        return handleKeyStroke(keyStrokeText, keyChar);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireVisibleColumnsChangedFromUI(Collection<IColumn<?>> visibleColumns) {
      try {
        pushUIProcessor();
        //
        getColumnSet().setVisibleColumns(visibleColumns);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireColumnMovedFromUI(IColumn<?> c, int toViewIndex) {
      try {
        pushUIProcessor();
        //
        c = getColumnSet().resolveColumn(c);
        if (c != null) {
          getColumnSet().moveColumnToVisibleIndex(c.getColumnIndex(), toViewIndex);
          ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
        }
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setColumnWidthFromUI(IColumn<?> c, int newWidth) {
      try {
        pushUIProcessor();
        //
        c = getColumnSet().resolveColumn(c);
        if (c != null) {
          c.setWidthInternal(newWidth);
          ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
        }
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireHeaderSortFromUI(IColumn<?> c, boolean multiSort, boolean ascending) {
      try {
        pushUIProcessor();
        //
        if (isSortEnabled()) {
          c = getColumnSet().resolveColumn(c);
          if (c != null) {
            getColumnSet().handleSortEvent(c, multiSort, ascending);
            ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
            sort();
          }
        }
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireHeaderGroupFromUI(IColumn<?> c, boolean multiGroup, boolean ascending) {
      try {
        pushUIProcessor();
        //
        if (isSortEnabled()) {
          c = getColumnSet().resolveColumn(c);
          if (c != null) {
            getColumnSet().handleGroupingEvent(c, multiGroup, ascending);
            ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
            sort();
          }
        }
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireAggregationFunctionChanged(INumberColumn<?> c, String function) {

      try {
        pushUIProcessor();
        getColumnSet().setAggregationFunction(c, function);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setColumnBackgroundEffect(INumberColumn<?> column, String effect) {
      try {
        pushUIProcessor();
        column.setBackgroundEffect(effect);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setCheckedRowsFromUI(List<? extends ITableRow> rows, boolean checked) {
      if (!isEnabled()) {
        return;
      }
      try {
        pushUIProcessor();
        checkRows(rows, checked, true);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setSelectedRowsFromUI(List<? extends ITableRow> rows) {
      try {
        pushUIProcessor();
        //
        Set<ITableRow> requestedRows = new HashSet<ITableRow>(resolveRows(rows));
        List<ITableRow> validRows = new ArrayList<ITableRow>();
        // add existing selected rows that are masked by filter
        for (ITableRow row : getSelectedRows()) {
          if (!row.isFilterAccepted()) {
            validRows.add(row);
          }
        }
        // remove all filtered from requested
        requestedRows.removeAll(validRows);
        // add remainder
        for (ITableRow row : requestedRows) {
          validRows.add(row);
        }
        selectRows(validRows, false);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public TransferObject fireRowsDragRequestFromUI() {
      try {
        pushUIProcessor();
        return fireRowsDragRequest();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireRowDropActionFromUI(ITableRow row, TransferObject dropData) {
      try {
        pushUIProcessor();
        row = resolveRow(row);
        fireRowDropAction(row, dropData);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public TransferObject fireRowsCopyRequestFromUI() {
      try {
        pushUIProcessor();
        return fireRowsCopyRequest();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      try {
        pushUIProcessor();
        //
        doAppLinkAction(ref);
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setContextColumnFromUI(IColumn<?> col) {
      try {
        pushUIProcessor();
        //
        if (col != null && col.getTable() != AbstractTable.this) {
          col = null;
        }
        setContextColumn(col);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public IFormField prepareCellEditFromUI(ITableRow row, IColumn<?> col) {
      if (!isEnabled()) {
        return null;
      }
      try {
        pushUIProcessor();
        //
        m_editContext = null;
        row = resolveRow(row);
        if (row != null && col != null) {
          // ensure the editable row to be selected.
          // This is crucial if the cell's value is changed right away in @{link IColumn#prepareEdit(ITableRow)}, e.g. in @{link AbstractBooleanColumn}
          row.getTable().selectRow(row);
          IFormField f = col.prepareEdit(row);
          if (f != null) {
            m_editContext = new P_CellEditorContext(row, col, f);
          }
          return f;
        }
      }
      finally {
        popUIProcessor();
      }
      return null;
    }

    @Override
    public void completeCellEditFromUI() {
      if (!isEnabled()) {
        return;
      }
      try {
        pushUIProcessor();
        //
        if (m_editContext != null) {
          try {
            m_editContext.getColumn().completeEdit(m_editContext.getRow(), m_editContext.getFormField());
          }
          finally {
            m_editContext = null;
          }
        }
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void cancelCellEditFromUI() {
      try {
        pushUIProcessor();
        //
        m_editContext = null;
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireTableReloadFromUI() {
      if (m_reloadHandler != null) {
        try {
          pushUIProcessor();
          //
          m_reloadHandler.reload();
        }
        finally {
          popUIProcessor();
        }
      }
    }

    @Override
    public void fireTableResetFromUI() {
      try {
        setTableChanging(true);
        resetColumns();
        TableUserFilterManager m = getUserFilterManager();
        if (m != null) {
          m.reset();
        }
        ITableCustomizer cst = getTableCustomizer();
        if (cst != null) {
          cst.removeAllColumns();
        }
      }
      finally {
        setTableChanging(false);
      }
    }

    @Override
    public void fireSortColumnRemovedFromUI(IColumn<?> column) {
      try {
        pushUIProcessor();
        //
        getColumnSet().removeSortColumn(column);
        sort();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireGroupColumnRemovedFromUI(IColumn<?> column) {
      try {
        pushUIProcessor();
        //
        getColumnSet().removeGroupColumn(column);
        sort();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireFilterAddedFromUI(IUserFilterState filter) {
      try {
        pushUIProcessor();
        //
        getUserFilterManager().addFilter(filter);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireFilterRemovedFromUI(IUserFilterState filter) {
      try {
        pushUIProcessor();
        //
        getUserFilterManager().removeFilter(filter);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setFilteredRowsFromUI(List<? extends ITableRow> rows) {
      try {
        pushUIProcessor();
        // Remove existing filter first, so that only one UserTableRowFilter is active
        removeUserRowFilters();

        // Create and add a new filter
        UserTableRowFilter filter = new UserTableRowFilter(rows);

        // Do not use addRowFilter to prevent applyRowFilters
        m_rowFilters.add(filter);
        applyRowFilters();
      }
      finally {
        popUIProcessor();
      }
    }

    protected void removeUserRowFilters() {
      for (ITableRowFilter filter : getRowFilters()) {
        if (filter instanceof UserTableRowFilter) {
          // Do not use removeRowFilter to prevent applyRowFilters
          m_rowFilters.remove(filter);
        }
      }
    }

    @Override
    public void removeFilteredRowsFromUI() {
      try {
        pushUIProcessor();
        removeUserRowFilters();
        applyRowFilters();
      }
      finally {
        popUIProcessor();
      }
    }
  }

  private class P_TableRowBuilder extends AbstractTableRowBuilder<Object> {

    @Override
    protected ITableRow createEmptyTableRow() {
      return new TableRow(getColumnSet());
    }

  }

  private class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROWS_SELECTED: {
          // single observer exec
          try {
            interceptRowsSelected(e.getRows());
          }
          catch (Exception ex) {
            BEANS.get(ExceptionHandler.class).handle(ex);
          }
          break;
        }
      }
    }
  }

  private static class P_CellEditorContext {
    private final ITableRow m_row;
    private final IColumn<?> m_column;
    private final IFormField m_formField;

    public P_CellEditorContext(ITableRow row, IColumn<?> col, IFormField f) {
      m_row = row;
      m_column = col;
      m_formField = f;
    }

    public ITableRow getRow() {
      return m_row;
    }

    public IColumn<?> getColumn() {
      return m_column;
    }

    public IFormField getFormField() {
      return m_formField;
    }
  }

  protected static class LocalTableExtension<TABLE extends AbstractTable> extends AbstractExtension<TABLE> implements ITableExtension<TABLE> {

    public LocalTableExtension(TABLE owner) {
      super(owner);
    }

    @Override
    public void execAppLinkAction(TableAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }

    @Override
    public void execRowAction(TableRowActionChain chain, ITableRow row) {
      getOwner().execRowAction(row);
    }

    @Override
    public void execContentChanged(TableContentChangedChain chain) {
      getOwner().execContentChanged();
    }

    @Override
    public ITableRowDataMapper execCreateTableRowDataMapper(TableCreateTableRowDataMapperChain chain, Class<? extends AbstractTableRowData> rowType) {
      return getOwner().execCreateTableRowDataMapper(rowType);
    }

    @Override
    public void execInitTable(TableInitTableChain chain) {
      getOwner().execInitTable();
    }

    @Override
    public void execResetColumns(TableResetColumnsChain chain, Set<String> options) {
      getOwner().execResetColumns(options);
    }

    @Override
    public void execDecorateCell(TableDecorateCellChain chain, Cell view, ITableRow row, IColumn<?> col) {
      getOwner().execDecorateCell(view, row, col);
    }

    @Override
    public void execDrop(TableDropChain chain, ITableRow row, TransferObject t) {
      getOwner().execDrop(row, t);
    }

    @Override
    public void execDisposeTable(TableDisposeTableChain chain) {
      getOwner().execDisposeTable();
    }

    @Override
    public void execRowClick(TableRowClickChain chain, ITableRow row, MouseButton mouseButton) {
      getOwner().execRowClick(row, mouseButton);
    }

    @Override
    public void execDecorateRow(TableDecorateRowChain chain, ITableRow row) {
      getOwner().execDecorateRow(row);
    }

    @Override
    public TransferObject execCopy(TableCopyChain chain, List<? extends ITableRow> rows) {
      return getOwner().execCopy(rows);
    }

    @Override
    public void execRowsSelected(TableRowsSelectedChain chain, List<? extends ITableRow> rows) {
      getOwner().execRowsSelected(rows);
    }

    @Override
    public TransferObject execDrag(TableDragChain chain, List<ITableRow> rows) {
      return getOwner().execDrag(rows);
    }

    @Override
    public void execRowsChecked(TableRowsCheckedChain chain, List<? extends ITableRow> row) {
      getOwner().execRowsChecked(row);
    }
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableAppLinkActionChain chain = new TableAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected final void interceptRowAction(ITableRow row) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowActionChain chain = new TableRowActionChain(extensions);
    chain.execRowAction(row);
  }

  protected final void interceptContentChanged() {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableContentChangedChain chain = new TableContentChangedChain(extensions);
    chain.execContentChanged();
  }

  protected final ITableRowDataMapper interceptCreateTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableCreateTableRowDataMapperChain chain = new TableCreateTableRowDataMapperChain(extensions);
    return chain.execCreateTableRowDataMapper(rowType);
  }

  protected final void interceptInitTable() {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableInitTableChain chain = new TableInitTableChain(extensions);
    chain.execInitTable();
  }

  protected final void interceptResetColumns(Set<String> options) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableResetColumnsChain chain = new TableResetColumnsChain(extensions);
    chain.execResetColumns(options);
  }

  protected final void interceptDecorateCell(Cell view, ITableRow row, IColumn<?> col) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDecorateCellChain chain = new TableDecorateCellChain(extensions);
    chain.execDecorateCell(view, row, col);
  }

  protected final void interceptDrop(ITableRow row, TransferObject t) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDropChain chain = new TableDropChain(extensions);
    chain.execDrop(row, t);
  }

  protected final void interceptDisposeTable() {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDisposeTableChain chain = new TableDisposeTableChain(extensions);
    chain.execDisposeTable();
  }

  protected final void interceptRowClick(ITableRow row, MouseButton mouseButton) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowClickChain chain = new TableRowClickChain(extensions);
    chain.execRowClick(row, mouseButton);
  }

  protected final void interceptDecorateRow(ITableRow row) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDecorateRowChain chain = new TableDecorateRowChain(extensions);
    chain.execDecorateRow(row);
  }

  protected final TransferObject interceptCopy(List<? extends ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableCopyChain chain = new TableCopyChain(extensions);
    return chain.execCopy(rows);
  }

  protected final void interceptRowsSelected(List<? extends ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowsSelectedChain chain = new TableRowsSelectedChain(extensions);
    chain.execRowsSelected(rows);
  }

  protected final void interceptRowsChecked(List<? extends ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowsCheckedChain chain = new TableRowsCheckedChain(extensions);
    chain.execRowsChecked(rows);
  }

  protected final TransferObject interceptDrag(List<ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDragChain chain = new TableDragChain(extensions);
    return chain.execDrag(rows);
  }

  @Override
  public boolean isValueChangeTriggerEnabled() {
    return m_valueChangeTriggerEnabled >= 1;
  }

  @Override
  public void setValueChangeTriggerEnabled(boolean b) {
    if (b) {
      m_valueChangeTriggerEnabled++;
    }
    else {
      m_valueChangeTriggerEnabled--;
    }
  }

  @Override
  public ITableOrganizer getTableOrganizer() {
    return m_tableOrganizer;
  }

  @Override
  public boolean isCustomizable() {
    return getTableCustomizer() != null;
  }

}
