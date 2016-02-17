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
scout.TableNavigationHomeKeyStroke = function(table) {
  scout.TableNavigationHomeKeyStroke.parent.call(this, table);
  this.which = [scout.keys.HOME];
  this.renderingHints.text = 'Home';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo();
    if (viewport.firstRow) {
      return viewport.firstRow.$row;
    }
  }.bind(this);
};
scout.inherits(scout.TableNavigationHomeKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationHomeKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    rows = table.filteredRows(),
    firstRow = scout.arrays.first(rows),
    selectedRows = table.selectedRows,
    newSelectedRows = [],
    lastActionRow = table.selectionHandler.lastActionRow,
    lastActionRowIndex = -1;

  if (event.shiftKey && selectedRows.length > 0) {
    if (lastActionRow) {
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }
    // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
    if (lastActionRowIndex < 0) {
      lastActionRow = scout.arrays.first(selectedRows);
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }
    newSelectedRows = rows.slice(0, lastActionRowIndex);
    newSelectedRows = scout.arrays.union(newSelectedRows, selectedRows);
  } else {
    newSelectedRows = firstRow;
  }
  table.selectionHandler.lastActionRow = firstRow;
  table.selectRows(newSelectedRows);
  table.scrollTo(firstRow);
};
