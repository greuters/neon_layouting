/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableNavigationPageDownKeyStroke = function(table) {
  scout.TableNavigationPageDownKeyStroke.parent.call(this, table);
  this.which = [scout.keys.PAGE_DOWN];
  this.renderingHints.text = 'PgDn';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo();
    if (viewport.lastRow) {
      return viewport.lastRow.$row;
    }
  }.bind(this);
};
scout.inherits(scout.TableNavigationPageDownKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationPageDownKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    viewport = this._viewportInfo(),
    rows = table.filteredRows(),
    selectedRows = table.selectedRows,
    lastSelectedRow = scout.arrays.last(selectedRows),
    lastActionRow = table.selectionHandler.lastActionRow,
    lastActionRowIndex = -1,
    newSelectedRows;

  if (!viewport.lastRow) {
    return;
  }

  if (lastActionRow) {
    lastActionRowIndex = rows.indexOf(lastActionRow);
  }
  // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
  if (lastActionRowIndex < 0) {
    lastActionRow = lastSelectedRow;
    lastActionRowIndex = rows.indexOf(lastActionRow);
  }

  // If last row in viewport already is selected -> scroll a page down
  // Don't do it if multiple rows are selected and user only presses page down without shift
  if (selectedRows.length > 0 && lastActionRow === viewport.lastRow && !(selectedRows.length > 1 && !event.shiftKey)) {
    table.scrollPageDown();
    viewport = this._viewportInfo();
  }

  if (event.shiftKey && selectedRows.length > 0) {
    newSelectedRows = rows.slice(lastActionRowIndex + 1, rows.indexOf(viewport.lastRow) + 1);
    newSelectedRows = scout.arrays.union(selectedRows, newSelectedRows);
  } else {
    newSelectedRows = [viewport.lastRow];
  }

  table.selectionHandler.lastActionRow = viewport.lastRow;
  table.selectRows(newSelectedRows, true, true);
};
