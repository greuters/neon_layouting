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
scout.TabItem = function() {
  scout.TabItem.parent.call(this);
  this.$tabContainer;
  this._tabRendered = false;
  this._tabActive = false;
};
scout.inherits(scout.TabItem, scout.GroupBox);

scout.TabItem.prototype._init = function(model) {
  scout.TabItem.parent.prototype._init.call(this, model);
  this._syncStatusVisible(this.statusVisible);
  this._syncMenusVisible(this.menusVisible);
};

scout.TabItem.prototype._render = function($parent) {
  scout.TabItem.parent.prototype._render.call(this, $parent);
  // LogicalGridData.isValidateRoot would always return true which is wrong if the data has not been validated yet.
  // Since the tabbox does not use the logical grid layout, noone validates the griddata -> isValidateRoot is never correct.
  // Because there is no logical grid layout, there is no use for the logical grid data.
  // TODO CGU [6.0] we should consider removing new scout.LogicalGridData(this) from addContainer and set it explicitly in the composites which use a logical grid layout
  this.htmlComp.layoutData = null;
};

scout.TabItem.prototype._createLayout = function() {
  return new scout.TabItemLayout(this);
};

/**
 * This method has nothing to do with the regular rendering of the GroupBox. It is an additional method
 * to render a single tab for this tab-item. Since tab and tab-item share the same model.
 */
scout.TabItem.prototype.renderTab = function($parent) {
  if (this._tabRendered) {
    throw new Error('Tab already rendered');
  }
  this.$tabContainer = $parent.appendDiv('tab-item')
    .data('tabItem', this)
    .on('mousedown', this._onTabMouseDown.bind(this));

  this.addLabel();
  this.addStatus();
  this._renderTabActive();
  this._renderLabel();
  this._renderMarked();
  this._renderVisible();
  this._renderTooltipText();
  this._tabRendered = true;
};

scout.TabItem.prototype._onTabMouseDown = function(event) {
  if (this._preventTabActivation) {
    this._preventTabActivation = false;
    return;
  }
  this.parent._selectTab(this);
};

scout.TabItem.prototype._onStatusMousedown = function(event) {
  scout.TabItem.parent.prototype._onStatusMousedown.call(this, event);
  // Prevent switching tabs when status gets clicked
  // Don't use event.preventDefault, otherwise other mouse listener (like tooltip mouse down) will not be executed as well
  this._preventTabActivation = true;
  // Prevent focusing the tab
  event.preventDefault();
};

scout.TabItem.prototype.focusTab = function() {
  if (this._tabRendered) {
    this.$tabContainer.focus();
  }
};

scout.TabItem.prototype.setTabActive = function(active) {
  var oldTabActive = this._tabActive;
  this._tabActive = active;
  if (this._tabRendered && oldTabActive !== active) {
    this._renderTabActive();
  }
};

scout.TabItem.prototype._renderTabActive = function() {
  this.$tabContainer.select(this._tabActive);
  if (this._tabActive) {
    this.$tabContainer.setTabbable(true);
  } else {
    this.$tabContainer.attr('tabindex', -1);
  }
};

/**
 * It's allowed to call removeTab() even when the tab is _not_ rendered.
 * This may be the case, when a tab is placed in the overflow-menu of the tab-area.
 * Thus it happens that some tabs are rendered and some are not.
 */
scout.TabItem.prototype.removeTab = function() {
  if (this._tabRendered) {
    this.$tabContainer.remove();
    this.$tabContainer = null;
    this._removeStatus();
    this._removeLabel();
    this._tabRendered = false;
  }
};

scout.TabItem.prototype._syncMarked = function(marked) {
  this.marked = marked;
  // Marked affects the tab item -> it needs to be rendered even if groupox is not
  if (this._tabRendered) {
    this._renderMarked();
    return false;
  }
};

scout.TabItem.prototype._renderMarked = function(marked) {
  this.$tabContainer.toggleClass('marked', this.marked);
};

scout.TabItem.prototype._syncVisible = function(visible) {
  this.visible = visible;
  // Visible affects the tab item -> it needs to be rendered even if groupox is not
  if (this._tabRendered) {
    this._renderVisible();
    return false;
  }
};

scout.TabItem.prototype._renderVisible = function(visible) {
  // Call super if it is rendered or it is rendering
  if (this.$container) {
    scout.TabItem.parent.prototype._renderVisible.call(this, visible);
  }
  this.$tabContainer.setVisible(this.visible);
};

scout.TabItem.prototype._syncLabel = function(label) {
  this.label = label;
  // Label affects the tab item -> it needs to be rendered even if groupox is not
  if (this._tabRendered) {
    this._renderLabel();
    return false;
  }
};

scout.TabItem.prototype._renderLabel = function() {
  this.$label.textOrNbsp(scout.strings.removeAmpersand(this.label));
};

scout.TabItem.prototype.addLabel = function() {
  if (this.$label) {
    return;
  }
  this.$label = this.$tabContainer.appendSpan('label');
};

scout.TabItem.prototype.addStatus = function() {
  if (this.$status) {
    return;
  }
  this.$status = this.$tabContainer
    .appendSpan('status')
    .on('mousedown', this._onStatusMousedown.bind(this));
};

scout.TabItem.prototype._syncStatusVisible = function() {
  // Always invisible to not waste space, icon will be visible if status needs to be shown
  this.statusVisible = false;
};

scout.TabItem.prototype._renderTooltipText = function() {
  if (this.$container) {
    scout.TabItem.parent.prototype._renderTooltipText.call(this);
  } else {
    // Normally done by renderTooltipText, but since it is not called -> call explicitly
    this._updateStatusVisible();
  }
  var hasTooltipText = scout.strings.hasText(this.tooltipText);
  this.$tabContainer.toggleClass('has-tooltip', hasTooltipText);
};

scout.TabItem.prototype._syncMenusVisible = function() {
  // Always invisible because menus are displayed in menu bar and not with status icon
  // Actually not needed at the moment because only value fields have menus (at least at the java model).
  // But actually we should change this so that menus are possible for every form field
  // TODO CGU [6.0] remove this comment if java model supports form field menus
  this.menusVisible = false;
};

/**
 * @override Widgets.js
 */
scout.TabItem.prototype._attach = function() {
  this._$parent.append(this.$container);
  this.session.detachHelper.afterAttach(this.$container);
  scout.TabItem.parent.prototype._attach.call(this);
};

/**
 * @override Widgets.js
 */
scout.TabItem.prototype._detach = function() {
  this.session.detachHelper.beforeDetach(this.$container, {
    storeFocus: false
  });
  this.$container.detach();
  scout.TabItem.parent.prototype._detach.call(this);
};
