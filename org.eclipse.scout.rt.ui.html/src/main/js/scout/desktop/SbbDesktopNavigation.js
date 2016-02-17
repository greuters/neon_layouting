/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: BSI Business Systems Integration AG - initial API and
 * implementation
 ******************************************************************************/
// FIXME nbu/AWE: inherit from Widget.js? refactor un-/installKeyStroke
scout.SbbDesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;

  this.$container;
  this.$navigationTop;
  this.$navigationBottom;
  this.$viewButtons;
  this.htmlViewButtons;
  this.outline;

  this.viewMenuTab;
};

scout.SbbDesktopNavigation.BREADCRUMB_SWITCH_WIDTH = 240; // Same value as in sizes.css FIXME awe: make dynamic (min. breadcrumb width)
scout.SbbDesktopNavigation.MIN_SPLITTER_SIZE = 49; // not 50px because last pixel is the border (would not look good)

scout.SbbDesktopNavigation.prototype.render = function($parent) {
  this.$container = $parent;
  this.$navigationTop = $parent.appendDiv('desktop-navigation-top');
  this.$viewButtons = this.$navigationTop.appendDiv('view-buttons');
  this.htmlViewButtons = new scout.HtmlComponent(this.$viewButtons, this.session);
  this.htmlViewButtons.setLayout(new scout.ViewButtonsLayout(this.htmlViewButtons));
  this.viewMenuTab = new scout.ViewMenuTab(this._viewButtons('MENU'), this.session);
  this.viewMenuTab.render(this.$viewButtons);

  var i, viewTab, viewTabs = this._viewButtons('TAB');
  for (i = 0; i < viewTabs.length; i++) {
    viewTab = viewTabs[i];
    viewTab.render(this.$viewButtons);
    if (i === viewTabs.length - 1) {
      viewTab.last();
    }
  }

  if (this._breadcrumbEnabled === undefined && this.desktop.outline) {
    // Read initial value from active outline
    this.setBreadcrumbEnabled(this.desktop.outline.breadcrumbEnabled);
  }

  this.$navigationBottom = this.$container.appendDiv('desktop-navigation-bottom');
  this.$navigationBottom.on('mousedown', this._onNavigationMousedown.bind(this));
  new scout.HtmlComponent(this.$navigationBottom, this.session);
};

scout.SbbDesktopNavigation.prototype._viewButtons = function(displayStyle) {
  var viewButtons = [];
  this.desktop.viewButtons.forEach(function(viewButton) {
    if (displayStyle === undefined || displayStyle === viewButton.displayStyle) {
      viewButtons.push(viewButton);
    }
  });
  return viewButtons;
};

scout.SbbDesktopNavigation.prototype.setNavigationBottomVisible = function(visible) {
  this.$navigationBottom.toggleClass('is-hidden', !visible);
}

scout.SbbDesktopNavigation.prototype._getNumSelectedTabs = function() {
  var numSelected = 0;
  if (this.viewMenuTab.selected) {
    numSelected++;
  }
  this._viewButtons('TAB').forEach(function(viewTab) {
    if (viewTab.selected) {
      numSelected++;
    }
  });
  return numSelected;
};

scout.SbbDesktopNavigation.prototype._onNavigationMousedown = function(event) {
  if (this.outline.inBackground) {
    this.desktop.bringOutlineToFront(this.outline);
  }
};

scout.SbbDesktopNavigation.prototype.onOutlineChanged = function(outline, bringToFront) {
  if (this.outline === outline) {
    return;
  }
  if (this.outline) {
    this.outline.remove();
  }
  this.outline = outline;
  this.outline.setBreadcrumbEnabled(this._breadcrumbEnabled);
  this.outline.render(this.$navigationBottom);
  this.outline.handleOutlineContent(bringToFront);
  this._updateViewButtons(outline);
  this.outline.validateFocus();
};

/**
 * This method updates the state of the view-menu-tab and the selected state of
 * outline-view-buttons. This method must also work in offline mode.
 */
scout.SbbDesktopNavigation.prototype._updateViewButtons = function(outline) {
  this.viewMenuTab.onOutlineChanged(outline);
  this._viewButtons('TAB').forEach(function(viewTab) {
    if (viewTab instanceof scout.OutlineViewButton) {
      viewTab.onOutlineChanged(outline);
    }
  });
};

// vertical splitter
scout.SbbDesktopNavigation.prototype.onResize = function(event) {
  var newWidth = Math.max(event.data, scout.SbbDesktopNavigation.MIN_SPLITTER_SIZE); // data = newSize, ensure newSize is not negative
  this.$navigationTop.width(newWidth);
  this.$navigationBottom.width(newWidth);
  this.htmlViewButtons.revalidateLayout();
  this.desktop.navigationWidthUpdated(newWidth);
  this.setBreadcrumbEnabled(newWidth <= scout.SbbDesktopNavigation.BREADCRUMB_SWITCH_WIDTH);
};

scout.SbbDesktopNavigation.prototype.setBreadcrumbEnabled = function(enabled) {
  if (this._breadcrumbEnabled === enabled) {
    return;
  }

  this._breadcrumbEnabled = enabled;
  if (this.outline) {
    this.outline.setBreadcrumbEnabled(enabled);
  }
  this.viewMenuTab.setBreadcrumbEnabled(enabled);
  this._viewButtons('TAB').forEach(function(viewButton) {
    viewButton.setBreadcrumbEnabled(enabled);
  });
  this.$navigationTop.toggleClass('navigation-breadcrumb', enabled);
};

scout.SbbDesktopNavigation.prototype.doViewMenuAction = function(event) {
  this.viewMenuTab.togglePopup(event);
};

scout.SbbDesktopNavigation.prototype.sendToBack = function() {
  this.viewMenuTab.sendToBack();
  this.outline.sendToBack();
};

scout.SbbDesktopNavigation.prototype.bringToFront = function() {
  this.viewMenuTab.bringToFront();
  this.outline.bringToFront();
};

scout.SbbDesktopNavigation.prototype.revalidateLayout = function() {
  // this check here is required because there are multiple property change
  // events while the outline changes. Sometimes we have none at all or two
  // selected tabs at the same time. This makes it impossible to animate the
  // view-buttons properly. With this check here we wait until all property
  // change events have been processed. Assuming that in the end there's always
  // on single selected view-button.
  if (this._getNumSelectedTabs() === 1) {
    this.htmlViewButtons.revalidateLayout();
  }
};
