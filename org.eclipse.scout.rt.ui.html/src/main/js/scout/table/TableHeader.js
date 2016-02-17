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
scout.TableHeader = function() {
  scout.TableHeader.parent.call(this);

  this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
  this._tableAddRemoveFilterHandler = this._onTableAddRemoveFilter.bind(this);
  this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
  this._tableColumnMovedHandler = this._onTableColumnMoved.bind(this);
  this.dragging = false;
  this._renderedColumns = [];
};
scout.inherits(scout.TableHeader, scout.Widget);

scout.TableHeader.prototype._init = function(options) {
  scout.TableHeader.parent.prototype._init.call(this, options);

  this.table = options.table;
  this.enabled = options.enabled;
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  this.menuBar.tabbable = false;
  this.menuBar.bottom();
};

scout.TableHeader.prototype._render = function($parent) {
  this.$container = this.table.$data.beforeDiv('table-header');

  // Filler is necessary to make sure the header is always as large as the table data, otherwise horizontal scrolling does not work correctly
  this.$filler = this.$container.appendDiv('table-header-item filler').css('visibility', 'hidden');

  if (!this.enabled) {
    this.menuBar.hiddenByUi = true;
  }
  // Required to make "height: 100%" rule work
  this.$menuBarContainer = this.$container.appendDiv('menubar-container');
  this.menuBar.render(this.$menuBarContainer);
  this._$window = this.$container.window();
  this._$body = this.$container.body();

  this.updateMenuBar();

  this._renderColumns();

  this.table.$data.on('scroll', this._tableDataScrollHandler);
  this.table.on('addFilter', this._tableAddRemoveFilterHandler);
  this.table.on('removeFilter', this._tableAddRemoveFilterHandler);
  this.table.on('columnResized', this._tableColumnResizedHandler);
  this.table.on('columnMoved', this._tableColumnMovedHandler);
};

scout.TableHeader.prototype._remove = function() {
  this.table.$data.off('scroll', this._tableDataScrollHandler);
  this.table.off('addFilter', this._tableAddRemoveFilterHandler);
  this.table.off('removeFilter', this._tableAddRemoveFilterHandler);
  this.table.off('columnResized', this._tableColumnResizedHandler);
  this.table.off('columnMoved', this._tableColumnMovedHandler);

  this._removeColumns();

  scout.TableHeader.parent.prototype._remove.call(this);
};

scout.TableHeader.prototype.rerenderColumns = function() {
  this._removeColumns();
  this._renderColumns();
};

scout.TableHeader.prototype._renderColumns = function() {
  this.table.columns.forEach(this._renderColumn, this);
  if (this.table.columns.length === 0) {
    // If there are no columns, make the filler visible and make sure the header is as large as normally using nbsp
    this.$filler.css('visibility', 'visible').html('&nbsp;').addClass('empty');
  }
  this._reconcileScrollPos();
};

scout.TableHeader.prototype._renderColumn = function(column, index) {
  var columnWidth = column.width,
    marginLeft = '',
    marginRight = '',
    isFirstColumn = (index === 0),
    isLastColumn = (index === this.table.columns.length - 1);

  if (isFirstColumn) {
    marginLeft = this.table.rowBorderLeftWidth;
  } else if (isLastColumn) {
    marginRight = this.table.rowBorderRightWidth;
  }

  var $header = this.$filler.beforeDiv('table-header-item')
    .setEnabled(this.enabled)
    .data('column', column)
    .css('min-width', columnWidth + 'px')
    .css('max-width', columnWidth + 'px')
    .css('margin-left', marginLeft)
    .css('margin-right', marginRight);
  if (this.enabled) {
    $header
      .on('click', this._onHeaderItemClick.bind(this))
      .on('mousedown', this._onHeaderItemMousedown.bind(this));
  }

  scout.inspector.applyInfo(column, $header);

  if (isFirstColumn) {
    $header.addClass('first');
  } else if (isLastColumn) {
    $header.addClass('last');
  }

  column.$header = $header;

  scout.tooltips.install($header, {
    parent: this,
    text: this._headerItemTooltipText.bind(this),
    arrowPosition: 50,
    arrowPositionUnit: '%',
    nativeTooltip: !scout.device.isCustomEllipsisTooltipPossible()
  });

  this._decorateHeader(column);
  $header.addClass('halign-' + scout.Table.parseHorizontalAlignment(column.horizontalAlignment));

  var showSeparator = column.showSeparator;
  if (isLastColumn && !this.enabled) {
    showSeparator = false;
  }
  if (showSeparator) {
    var $separator = this.$filler.beforeDiv('table-header-resize');
    if (column.fixedWidth || !this.enabled) {
      $separator.setEnabled(false);
    } else {
      $separator.on('mousedown', '', this._onSeparatorMousedown.bind(this));
    }
    column.$separator = $separator;
  }
  this._renderedColumns.push(column);
};

scout.TableHeader.prototype._removeColumns = function() {
  this._renderedColumns.slice().forEach(this._removeColumn, this);
};

scout.TableHeader.prototype._removeColumn = function(column) {
  if (column.$header) {
    column.$header.remove();
    column.$header = null;
  }
  if (column.$separator) {
    column.$separator.remove();
    column.$separator = null;
  }
  scout.arrays.remove(this._renderedColumns, column);
};

scout.TableHeader.prototype.resizeHeaderItem = function(column) {
  if (!column) {
    //May be undefined if there are no columns
    return;
  }
  var remainingHeaderSpace, adjustment,
    $header = column.$header,
    $headerResize,
    columnWidth = column.width,
    marginLeft = '',
    marginRight = '',
    menuBarWidth = (this.menuBar.visible ? this.$menuBarContainer.outerWidth(true) : 0),
    isFirstColumn = this.table.columns.indexOf(column) === 0,
    isLastColumn = this.table.columns.indexOf(column) === this.table.columns.length - 1;

  if (isFirstColumn) {
    marginLeft = this.table.rowBorderLeftWidth;
  } else if (isLastColumn) {
    marginRight = this.table.rowBorderRightWidth;
    remainingHeaderSpace = this.$container.width() - this.table.rowWidth + scout.graphics.getInsets(this.table.$data).right;
    $headerResize = $header.next('.table-header-resize');

    if (remainingHeaderSpace < menuBarWidth) {
      adjustment = menuBarWidth;
      adjustment += $headerResize.width();
      if (remainingHeaderSpace > 0) {
        adjustment -= remainingHeaderSpace;
      }

      var origColumnWidth = columnWidth;
      columnWidth = Math.max(columnWidth - adjustment, column.minWidth - adjustment);
      this.$filler.cssWidth(origColumnWidth - columnWidth);
    }
  }

  $header
    .css('min-width', columnWidth)
    .css('max-width', columnWidth)
    .css('margin-left', marginLeft)
    .css('margin-right', marginRight);
};

scout.TableHeader.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll header as well
  var scrollLeft = this.table.$data.scrollLeft(),
    lastColumn = this.table.columns[this.table.columns.length - 1];

  this.resizeHeaderItem(lastColumn);
  this.$container.scrollLeft(scrollLeft);
  this.$menuBarContainer.cssRight(-1 * scrollLeft);
};

scout.TableHeader.prototype._arrangeHeaderItems = function($headers) {
  var that = this;
  $headers.each(function() {
    // move to old position and then animate
    $(this).css('left', $(this).data('old-pos') - $(this).offset().left)
      .animate({
        left: 0
      }, {
        progress: function(animation, progress, remainingMs) {
          var $headerItem = $(this);
          if (!$headerItem.isSelected()) {
            return;
          }
          // make sure selected header item is visible
          scout.scrollbars.scrollHorizontalTo(that.table.$data, $headerItem);

          // move menu
          if (that._tableHeaderMenu && that._tableHeaderMenu.rendered) {
            that._tableHeaderMenu.position();
          }
        }
      });
  });
};

scout.TableHeader.prototype._headerItemTooltipText = function($col) {
  var column = $col.data('column');
  if (column && scout.strings.hasText(column.headerTooltip)) {
    return column.headerTooltip;
  } else if ($col.isContentTruncated() || ($col.width() + $col.position().left) > $col.parent().width()) {
    $col = $col.clone();
    $col.children('.table-header-item-state').remove();
    return $col.text();
  }
};

scout.TableHeader.prototype.openTableHeaderMenu = function(column) {
  var $header = column.$header;
  this._tableHeaderMenu = scout.create('TableHeaderMenu', {
    parent: this,
    column: $header.data('column'),
    tableHeader: this,
    $anchor: $header,
    focusableContainer: true
  });
  this._tableHeaderMenu.open();
};

scout.TableHeader.prototype.closeTableHeaderMenu = function() {
  this._tableHeaderMenu.remove();
  this._tableHeaderMenu = null;
};


scout.TableHeader.prototype.onColumnActionsChanged = function(event) {
  if (this._tableHeaderMenu) {
    this._tableHeaderMenu.onColumnActionsChanged(event);
  }
};

scout.TableHeader.prototype.findHeaderItems = function() {
  return this.$container.find('.table-header-item:not(.filler)');
};

/**
 * Updates the column headers visualization of the text, sorting and styling state
 */
scout.TableHeader.prototype.updateHeader = function(column, oldColumnState) {
  this._decorateHeader(column, oldColumnState);
};

scout.TableHeader.prototype._decorateHeader = function(column, oldColumnState) {
  var $header = column.$header;
  if (oldColumnState) {
    $header.removeClass(oldColumnState.headerCssClass);
  }
  $header.addClass(column.headerCssClass);
  if (column.disallowHeaderMenu) {
    $header.addClass('disabled');
  }
  this._renderColumnText(column);
};

scout.TableHeader.prototype._renderColumnText = function(column) {
  var text = column.text,
    $header = column.$header;

  if (!text) {
    // Make sure empty header is as height as the others to make it properly clickable
    $header.html('&nbsp;');
    $header.addClass('empty');
  } else {
    $header.html(scout.strings.nl2br(text));
    $header.removeClass('empty');
  }

  this._renderColumnState(column);
};

scout.TableHeader.prototype._renderColumnState = function(column) {
  var sortDirection, $state,
    $header = column.$header,
    filtered = this.table.getFilter(column.id);

  $header.children('.table-header-item-state').remove();
  $state = $header.appendSpan('table-header-item-state');
  $state.empty();
  $header.removeClass('sort-asc sort-desc sorted group-asc group-desc grouped filtered');
  $state.removeClass('sort-asc sort-desc sorted group-asc group-desc grouped filtered');

  if (column.sortActive) {
    sortDirection = column.sortAscending ? 'asc' : 'desc';
    if (column.grouped) {
      $header.addClass('group-' + sortDirection);
    }
    $header.addClass('sorted sort-' + sortDirection);
    $state.addClass('sorted sort-' + sortDirection);
  }

  if (column.grouped || filtered) {
    // contains group and filter symbols
    var $left = $state.appendDiv('left');
    if (column.grouped) {
      $header.addClass('grouped');
      $state.addClass('grouped');
      $left.appendDiv().text('G');
    }
    if (filtered) {
      $header.addClass('filtered');
      $state.addClass('filtered');
      $left.appendDiv().text('F');
    }
  }
  // Contains sort arow
  $state.appendDiv('right');
};

scout.TableHeader.prototype.updateMenuBar = function() {
  var menuItems = this.table._filterMenus(this.table.menus, scout.MenuDestinations.HEADER);
  this.menuBar.updateItems(menuItems);
};

scout.TableHeader.prototype._onTableColumnResized = function(event) {
  var column = event.column,
    lastColumn = this.table.columns[this.table.columns.length - 1];
  this.resizeHeaderItem(column);
  if (lastColumn !== column) {
    this.resizeHeaderItem(lastColumn);
  }
};

scout.TableHeader.prototype.onSortingChanged = function() {
  for (var i = 0; i < this.table.columns.length; i++) {
    var column = this.table.columns[i];
    this._renderColumnState(column);
  }
};

scout.TableHeader.prototype._onTableColumnMoved = function(event) {
  var column = event.column,
    oldPos = event.oldPos,
    newPos = event.newPos,
    $header = column.$header,
    $headers = this.findHeaderItems(),
    $moveHeader = $headers.eq(oldPos),
    $moveResize = $moveHeader.next(),
    lastColumnPos = this.table.columns.length - 1;

  // store old position of header
  $headers.each(function() {
    $(this).data('old-pos', $(this).offset().left);
  });

  // change order in dom of header
  if (newPos < oldPos) {
    $headers.eq(newPos).before($moveHeader);
    $headers.eq(newPos).before($moveResize);
  } else {
    $headers.eq(newPos).after($moveHeader);
    $headers.eq(newPos).after($moveResize);
  }

  // Update first/last markers
  if ($headers.length > 0) {
    $headers.eq(0).removeClass('first');
    $headers.eq($headers.length - 1).removeClass('last');
  }
  if (this.table.columns.length > 0) {
    this.table.columns[0].$header.addClass('first');
    this.table.columns[this.table.columns.length - 1].$header.addClass('last');
  }

  // Update header size due to header menu items if moved from or to last position
  if (oldPos === lastColumnPos || newPos === lastColumnPos) {
    this.table.columns.forEach(function(column) {
      this.resizeHeaderItem(column);
    }.bind(this));
  }

  // move to old position and then animate
  if (event.dragged) {
    $header.css('left', parseInt($header.css('left'), 0) + $header.data('old-pos') - $header.offset().left)
      .animateAVCSD('left', 0);
  } else {
    this._arrangeHeaderItems($headers);
  }
};

scout.TableHeader.prototype.onOrderChanged = function(oldColumnOrder) {
  var column, i, $header, $headerResize;
  var $headers = this.findHeaderItems();

  // store old position of headers
  $headers.each(function() {
    $(this).data('old-pos', $(this).offset().left);
  });

  // change order in dom of header
  for (i = 0; i < this.table.columns.length; i++) {
    column = this.table.columns[i];
    $header = column.$header;
    $headerResize = $header.next('.table-header-resize');

    this.$container.append($header);
    this.$container.append($headerResize);
  }

  this._arrangeHeaderItems($headers);
};

scout.TableHeader.prototype._onHeaderItemClick = function(event) {
  var $headerItem = $(event.currentTarget),
    column = $headerItem.data('column');

  if (column.disallowHeaderMenu) {
    return;
  }

  if (this.dragging || this.columnMoved) {
    this.dragging = false;
    this.columnMoved = false;
  } else if (this.table.sortEnabled && (event.shiftKey || event.ctrlKey)) {
    this.table.removeColumnGrouping();
    this.table.sort(column, $headerItem.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
  } else if (this._tableHeaderMenu && this._tableHeaderMenu.isOpenFor($headerItem)) {
    this.closeTableHeaderMenu();
  } else {
    this.openTableHeaderMenu(column);
  }

  return false;
};

scout.TableHeader.prototype._onHeaderItemMousedown = function(event) {
  var diff = 0,
    that = this,
    startX = Math.floor(event.pageX),
    $header = $(event.currentTarget),
    column = $header.data('column'),
    oldPos = this.table.columns.indexOf(column),
    newPos = oldPos,
    move = $header.outerWidth(),
    $otherHeaders = $header.siblings('.table-header-item:not(.filler)');

  if (column.fixedPosition) {
    // Don't allow moving a column with fixed position
    return;
  }

  this.dragging = false;
  // firefox fires a click action after a column has been droped at the new location, chrome doesn't -> we need a hint to avoid menu gets opened after drop
  this.columnMoved = false;

  // start drag & drop events
  this._$window
    .on('mousemove.tableheader', '', dragMove)
    .one('mouseup', '', dragEnd);

  function dragMove(event) {
    diff = Math.floor(event.pageX) - startX;
    if (diff === 0) {
      return;
    }

    that.dragging = true;

    // change css of dragged header
    $header.addClass('header-move');
    that.$container.addClass('header-move');

    // move dragged header
    $header.css('left', diff);

    // find other affected headers
    var middle = realMiddle($header);

    $otherHeaders.each(function(i) {
      var m = realMiddle($(this));

      if (middle < m && i < oldPos) {
        $(this).css('left', move);
      } else if (middle > m && i >= oldPos) {
        $(this).css('left', -move);
      } else {
        $(this).css('left', 0);
      }
    });

    if (that._tableHeaderMenu && that._tableHeaderMenu.rendered) {
      that._tableHeaderMenu.remove();
      that._tableHeaderMenu = null;
    }
  }

  function realWidth($div) {
    var html = $div.html(),
      width = $div.html('<span>' + html + '</span>').find('span:first').width();

    $div.html(html);
    return width;
  }

  function realMiddle($div) {
    if ($div.hasClass('halign-right')) {
      return $div.offset().left + $div.outerWidth() - realWidth($div) / 2;
    } else {
      return $div.offset().left + realWidth($div) / 2;
    }
  }

  function dragEnd(event) {
    that._$window.off('mousemove.tableheader');

    // in case of no movement: return
    if (!that.dragging) {
      return true;
    }

    // find new position of dragged header
    var h = (diff < 0) ? $otherHeaders : $($otherHeaders.get().reverse());
    h.each(function(i) {
      if ($(this).css('left') !== '0px') {
        newPos = that.table.columns.indexOf(($(this).data('column')));
        return false;
      }
    });

    // move column
    if (newPos > -1 && oldPos !== newPos) {
      that.table.moveColumn($header.data('column'), oldPos, newPos, true);
      that.dragging = false;
      that.columnMoved = true;
    } else {
      $header.animateAVCSD('left', '', function() {
        that.dragging = false;
      });
    }

    // reset css of dragged header
    $otherHeaders.each(function() {
      $(this).css('left', '');
    });

    $header.css('background', '')
      .removeClass('header-move');
    that.$container.removeClass('header-move');
  }
};

scout.TableHeader.prototype._onSeparatorMousedown = function(event) {
  var startX = Math.floor(event.pageX),
    $header = $(event.target).prev(),
    column = $header.data('column'),
    that = this,
    headerWidth = column.width;

  column.resizingInProgress = true;

  // Install resize helpers. Those helpers make sure the header and the data element keep their
  // current width until the resizing has finished. Otherwise, make a column smaller while the
  // table has been horizontally scrolled to the right would behave very strange.
  this.$headerColumnResizeHelper = this.$container
    .appendDiv('table-column-resize-helper')
    .css('width', this.table.rowWidth + this.table.rowBorderWidth);
  this.$dataColumnResizeHelper = this.table.$data
    .appendDiv('table-column-resize-helper')
    .css('width', this.table.rowWidth);

  this._$window
    .on('mousemove.tableheader', resizeMove)
    .one('mouseup', resizeEnd);
  this._$body.addClass('col-resize');

  // Prevent text selection in a form, don't stop propagation to allow others (e.g. cell editor) to react
  event.preventDefault();

  function resizeMove(event) {
    var diff = Math.floor(event.pageX) - startX,
      wHeader = headerWidth + diff;

    wHeader = Math.max(wHeader, column.minWidth);
    if (wHeader !== column.width) {
      that.table.resizeColumn(column, wHeader);
    }
  }

  function resizeEnd(event) {
    delete column.resizingInProgress;

    // Remove resize helpers
    that.$headerColumnResizeHelper.remove();
    that.$headerColumnResizeHelper = null;
    that.$dataColumnResizeHelper.remove();
    that.$dataColumnResizeHelper = null;

    that._$window.off('mousemove.tableheader');
    that._$body.removeClass('col-resize');

    that.table.resizeColumn(column, column.width);
  }
};

scout.TableHeader.prototype._onTableDataScroll = function() {
  scout.scrollbars.fix(this.$menuBarContainer);
  this._reconcileScrollPos();
  this._fixTimeout = scout.scrollbars.unfix(this.$menuBarContainer, this._fixTimeout);
};

scout.TableHeader.prototype._onTableAddRemoveFilter = function(event) {
  var column = event.filter.column;
  // Check for column.$header because column may have been removed in the mean time due to a structure changed event -> don't try to render state
  if (event.filter.filterType === scout.ColumnUserFilter.Type && column.$header) {
    this._renderColumnState(column);
  }
};
