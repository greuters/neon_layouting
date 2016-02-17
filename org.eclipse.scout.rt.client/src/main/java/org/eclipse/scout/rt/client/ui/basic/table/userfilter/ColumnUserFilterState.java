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
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import java.util.Set;

import org.eclipse.scout.rt.client.services.common.bookmark.internal.BookmarkUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.AbstractUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class ColumnUserFilterState extends AbstractUserFilterState implements IUserFilterState {
  private static final long serialVersionUID = 1L;
  public static final String TYPE = "column";

  private transient IColumn<?> m_column;
  private String m_columnId;
  private Set<Object> m_selectedValues;

  public ColumnUserFilterState(IColumn<?> column) {
    setColumn(column);
    setType(TYPE);
  }

  public IColumn<?> getColumn() {
    return m_column;
  }

  public void setColumn(IColumn<?> column) {
    m_column = column;
    m_columnId = column.getColumnId();
  }

  public Set<Object> getSelectedValues() {
    return m_selectedValues;
  }

  public void setSelectedValues(Set<Object> selectedValues) {
    m_selectedValues = selectedValues;
  }

  @Override
  public Object createKey() {
    return getColumn();
  }

  @Override
  public String getDisplayText() {
    return ScoutTexts.get("Column") + " \"" + getColumn().getHeaderCell().getText() + "\"";
  }

  @Override
  public void notifyDeserialized(Object obj) {
    ITable table = (ITable) obj;
    IColumn<?> col = BookmarkUtility.resolveColumn(table.getColumns(), m_columnId);
    if (col == null) {
      throw new ProcessingException("Column could not be restored. " + m_columnId);
    }
    m_column = col;
  }

}
