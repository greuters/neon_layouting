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
scout.TableTextUserFilter = function() {
  scout.TableTextUserFilter.parent.call(this);
  this.filterType = scout.TableTextUserFilter.Type;
};
scout.inherits(scout.TableTextUserFilter, scout.TableUserFilter);

scout.TableTextUserFilter.Type = 'text';

/**
 * @override TableUserFilter.js
 */
scout.TableTextUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.ColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
  data.text = this.text;
  return data;
};

scout.TableTextUserFilter.prototype.createLabel = function() {
  return this.text;
};

scout.TableTextUserFilter.prototype.accept = function(row) {
  var rowText = '';
  for (var i = 0; i < this.table.columns.length; i++) {
    var column = this.table.columns[i];
    rowText += column.cellTextForTextFilter(row) + ' ';
  }
  rowText = rowText.trim().toLowerCase();
  return rowText.indexOf(this.text) > -1;
};
