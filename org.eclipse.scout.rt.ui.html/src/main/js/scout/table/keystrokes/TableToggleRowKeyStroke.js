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
scout.TableToggleRowKeyStroke = function(table) {
  scout.TableToggleRowKeyStroke.parent.call(this);
  this.field = table;

  this.which = [scout.keys.SPACE];
  this.stopPropagation = true;
  this.renderingHints.render = false;
};
scout.inherits(scout.TableToggleRowKeyStroke, scout.KeyStroke);

scout.TableToggleRowKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TableToggleRowKeyStroke.parent.prototype._accept.call(this, event);
  return accepted &&
    this.field.checkable &&
    this.field.selectedRows.length;
};

scout.TableToggleRowKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    selection = table.selectedRows;

  var checked = selection[0].checked;
  selection.forEach(function(row) {
    table.checkRow(row, !checked);
  });
};
