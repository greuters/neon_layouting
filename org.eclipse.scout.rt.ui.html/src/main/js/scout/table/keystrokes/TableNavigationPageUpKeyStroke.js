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
scout.TableNavigationPageUpKeyStroke = function(table) {
  scout.TableNavigationPageUpKeyStroke.parent.call(this, table);
  this.which = [scout.keys.PAGE_UP];
  this.renderingHints.text = 'PgUp';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo();
    if (viewport.firstRow) {
      return viewport.firstRow.$row;
    }
  }.bind(this);
};
scout.inherits(scout.TableNavigationPageUpKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationPageUpKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    viewport = this._viewportInfo(),
    rows = table.filteredRows(),
    selectedRows = table.selectedRows,
    firstSelectedRow = scout.arrays.first(selectedRows),
    lastActionRow = table.selectionHandler.lastActionRow,
    lastActionRowIndex = -1,
    newSelectedRows;

  if (!viewport.firstRow) {
    return;
  }

  if (lastActionRow) {
    lastActionRowIndex = rows.indexOf(lastActionRow);
  }
  // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
  if (lastActionRowIndex < 0) {
    lastActionRow = firstSelectedRow;
    lastActionRowIndex = rows.indexOf(lastActionRow);
  }

  // If first row in viewport already is selected -> scroll a page up
  // Don't do it if multiple rows are selected and user only presses page up without shift
  if (selectedRows.length > 0 && lastActionRow === viewport.firstRow && !(selectedRows.length > 1 && !event.shiftKey)) {
    table.scrollPageUp();
    viewport = this._viewportInfo();
  }

  if (event.shiftKey && selectedRows.length > 0) {
    newSelectedRows = rows.slice(rows.indexOf(viewport.firstRow), lastActionRowIndex);
    newSelectedRows = scout.arrays.union(selectedRows, newSelectedRows);
  } else {
    newSelectedRows = [viewport.firstRow];
  }

  table.selectionHandler.lastActionRow = viewport.firstRow;
  table.selectRows(newSelectedRows, true, true);
};
