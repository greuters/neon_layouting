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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableEvent extends java.util.EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;
  private static Logger LOG = LoggerFactory.getLogger(TableEvent.class);
  /**
   * Column visibility and/or order and/or width changed
   */
  public static final int TYPE_COLUMN_STRUCTURE_CHANGED = 1;
  /**
   * Some rows have been added
   * <p>
   * valid properties: rows, firstRow, lastRow
   */
  public static final int TYPE_ROWS_INSERTED = 100;
  /**
   * Some rows have been updated
   * <p>
   * valid properties: rows, firstRow, lastRow
   */
  public static final int TYPE_ROWS_UPDATED = 101;
  /**
   * Some rows have been deleted
   * <p>
   * valid properties: rows, firstRow, lastRow
   */
  public static final int TYPE_ROWS_DELETED = 102;
  /**
   * Some rows have been selected
   * <p>
   * valid properties: rows, firstRow, lastRow
   */
  public static final int TYPE_ROWS_SELECTED = 103;
  /**
   * A row has been activated
   * <p>
   * valid properties: rows, firstRow, lastRow
   */
  public static final int TYPE_ROW_ACTION = 104;
  /**
   * All rows have been deleted
   * <p>
   * valid properties: rows, firstRow, lastRow
   */
  public static final int TYPE_ALL_ROWS_DELETED = 105;
  /**
   * Sorting has been performed
   */
  public static final int TYPE_ROW_ORDER_CHANGED = 200;
  /**
   * Filter has changed
   */
  public static final int TYPE_ROW_FILTER_CHANGED = 210;

  /**
   * Broadcast request to get drag object
   * <p>
   * valid properties: rows, firstRow, lastRow set property: dragObject
   */
  public static final int TYPE_ROWS_DRAG_REQUEST = 730;
  /**
   * Drop action was received
   * <p>
   * valid properties: rows, firstRow, lastRow, dropAction
   */
  public static final int TYPE_ROW_DROP_ACTION = 740;

  /**
   * Copy-To-Clipboard action was received
   * <p>
   * valid properties: rows, firstRow, lastRow, copyAction
   */
  public static final int TYPE_ROWS_COPY_REQUEST = 760;

  /**
   * Column order changed
   * <p>
   * valid properties:
   */
  public static final int TYPE_COLUMN_ORDER_CHANGED = 770;
  /**
   * Column headers were changed, sort status changed
   * <p>
   * valid properties: columns
   */
  public static final int TYPE_COLUMN_HEADERS_UPDATED = 780;

  /**
   * Request ui to set focus to the table
   */
  public static final int TYPE_REQUEST_FOCUS = 800;

  /**
   * Request ui to activate editing of the cell and set focus to it
   * <p>
   * Only for editable cells
   */
  public static final int TYPE_REQUEST_FOCUS_IN_CELL = 805;

  /**
   * A row has been clicked (by the mouse)
   * <p>
   * valid properties: rows, firstRow, lastRow
   */
  public static final int TYPE_ROW_CLICK = 810;

  public static final int TYPE_TABLE_POPULATED = 820;

  /**
   * Advise to scroll to selection
   */
  public static final int TYPE_SCROLL_TO_SELECTION = 830;

  /**
   * A rows has been checked or unchecked
   */
  public static final int TYPE_ROWS_CHECKED = 850;

  public static final int TYPE_USER_FILTER_ADDED = 900;
  public static final int TYPE_USER_FILTER_REMOVED = 910;

  public static final int TYPE_COLUMN_AGGREGATION_CHANGED = 950;
  public static final int TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED = 960;

  private final int m_type;
  private List<? extends ITableRow> m_rows;
  private List<IMenu> m_popupMenus;
  private boolean m_consumed;
  private TransferObject m_dragObject;
  private TransferObject m_dropObject;
  private TransferObject m_copyObject;
  private Collection<? extends IColumn<?>> m_columns;
  private Map<ITableRow, Set<IColumn<?>>> m_updatedColumns;
  private boolean m_sortInMemoryAllowed;
  private IUserFilterState m_userFilter;

  public TableEvent(ITable source, int type) {
    this(source, type, null);
  }

  public TableEvent(ITable source, int type, List<? extends ITableRow> rows) {
    super(source);
    m_type = type;
    m_rows = CollectionUtility.arrayList(rows);
  }

  public ITable getTable() {
    return (ITable) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public List<ITableRow> getRows() {
    return CollectionUtility.arrayList(m_rows);
  }

  protected void setRows(List<? extends ITableRow> rows) {
    m_rows = CollectionUtility.arrayList(rows);
  }

  public int getRowCount() {
    return m_rows.size();
  }

  public ITableRow getFirstRow() {
    return CollectionUtility.firstElement(m_rows);
  }

  public ITableRow getLastRow() {
    return CollectionUtility.lastElement(m_rows);
  }

  /**
   * used by TYPE_ROW_POPUP and TYPE_HEADER_POPUP to add actions
   */
  public void addPopupMenu(IMenu menu) {
    if (menu != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.add(menu);
    }
  }

  /**
   * used by TYPE_ROW_POPUP to add actions
   */
  public void addPopupMenus(List<IMenu> menus) {
    if (menus != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.addAll(menus);
    }
  }

  /**
   * used by TYPE_ROW_POPUP to add actions
   */
  public List<IMenu> getPopupMenus() {
    return CollectionUtility.arrayList(m_popupMenus);
  }

  /**
   * used by TYPE_ROW_POPUP to add actions
   */
  public int getPopupMenuCount() {
    if (m_popupMenus != null) {
      return m_popupMenus.size();
    }
    else {
      return 0;
    }
  }

  public boolean isConsumed() {
    return m_consumed;
  }

  public void consume() {
    m_consumed = true;
  }

  /**
   * used by TYPE_ROW_DRAG_REQUEST
   */
  public TransferObject getDragObject() {
    return m_dragObject;
  }

  public void setDragObject(TransferObject t) {
    m_dragObject = t;
  }

  /**
   * used by TYPE_ROW_DROP_ACTION
   */
  public TransferObject getDropObject() {
    return m_dropObject;
  }

  protected void setDropObject(TransferObject t) {
    m_dropObject = t;
  }

  /**
   * used by {@link TableEvent#TYPE_ROWS_COPY_REQUEST}
   */
  public TransferObject getCopyObject() {
    return m_copyObject;
  }

  protected void setCopyObject(TransferObject t) {
    m_copyObject = t;
  }

  /**
   * used by TYPE_COLUMN_ORDER_CHANGED,TYPE_SORT_REQUEST,TYPE_COLUMN_HEADERS_CHANGED
   */
  public Collection<IColumn<?>> getColumns() {
    return CollectionUtility.arrayList(m_columns);
  }

  public IColumn getFirstColumn() {
    return CollectionUtility.firstElement(m_columns);
  }

  protected void setColumns(Collection<? extends IColumn<?>> columns) {
    m_columns = columns;
  }

  /**
   * Used by {@link #TYPE_ROWS_UPDATED}
   */
  protected void setUpdatedColumns(ITableRow row, Set<IColumn<?>> updateColumns) {
    if (m_updatedColumns == null) {
      m_updatedColumns = new HashMap<>();
    }
    m_updatedColumns.put(row, updateColumns);
  }

  /**
   * Used by {@link #TYPE_ROWS_UPDATED}. Return value is never <code>null</code>.
   */
  public Map<ITableRow, Set<IColumn<?>>> getUpdatedColumns() {
    if (m_updatedColumns == null) {
      return new HashMap<>();
    }
    return new HashMap<>(m_updatedColumns);
  }

  /**
   * Used by {@link #TYPE_ROWS_UPDATED}. Return value is never <code>null</code>.
   */
  public Set<IColumn<?>> getUpdatedColumns(ITableRow row) {
    return CollectionUtility.hashSet(m_updatedColumns == null ? null : m_updatedColumns.get(row));
  }

  public void setUserFilter(IUserFilterState userFilter) {
    m_userFilter = userFilter;
  }

  public IUserFilterState getUserFilter() {
    return m_userFilter;
  }

  /**
   * used by TYPE_SORT_REQUEST
   */
  public boolean isSortInMemoryAllowed() {
    return m_sortInMemoryAllowed;
  }

  protected void setSortInMemoryAllowed(boolean b) {
    m_sortInMemoryAllowed = b;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getClass().getSimpleName() + "[");
    buf.append(getTypeName());
    // rows
    if (CollectionUtility.hasElements(m_rows) && getTable() != null) {
      buf.append(" ");
      if (m_rows.size() == 1) {
        buf.append("row ").append(m_rows.get(0));
      }
      else {
        buf.append("rows {");
        Iterator<? extends ITableRow> rowIt = m_rows.iterator();
        buf.append("" + rowIt.next());
        while (rowIt.hasNext()) {
          buf.append(",");
          buf.append("" + rowIt.next());
        }
        buf.append("}");
      }
    }
    buf.append("]");
    return buf.toString();
  }

  /**
   * decode type
   */
  private String getTypeName() {
    try {
      Field[] f = getClass().getDeclaredFields();
      for (int i = 0; i < f.length; i++) {
        if (Modifier.isPublic(f[i].getModifiers()) && Modifier.isStatic(f[i].getModifiers()) && f[i].getName().startsWith("TYPE_")
            && ((Number) f[i].get(null)).intValue() == m_type) {
          return (f[i].getName());
        }
      }
    }
    catch (IllegalAccessException e) {
      LOG.error("Error Reading fields", e);
    }
    return "#" + m_type;
  }
}
