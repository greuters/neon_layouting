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
scout.ContextMenuPopup = function() {
  scout.ContextMenuPopup.parent.call(this);

  // Make sure head won't be rendered, there is a css selector which is applied only if there is a head
  this._headVisible = false;
};
scout.inherits(scout.ContextMenuPopup, scout.PopupWithHead);

scout.ContextMenuPopup.prototype._init = function(options) {
  options.focusableContainer = true; // In order to allow keyboard navigation, the popup must gain focus. Because menu-items are not focusable, make the container focusable instead.
  scout.ContextMenuPopup.parent.prototype._init.call(this, options);

  this.menuItems = options.menuItems;
  this.menuFilter = options.menuFilter;
  this.options = $.extend({
    cloneMenuItems: true
  }, options);
};

/**
 * @override Popup.js
 */
scout.ContextMenuPopup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.ContextMenuPopup.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  scout.menuNavigationKeyStrokes.registerKeyStrokes(keyStrokeContext, this, 'menu-item');
};

scout.ContextMenuPopup.prototype._render = function($parent) {
  scout.ContextMenuPopup.parent.prototype._render.call(this, $parent);
  scout.scrollbars.install(this.$body, {
    parent: this
  });
  this._renderMenuItems();

};

scout.ContextMenuPopup.prototype.removeSubMenuItems = function(parentMenu, animated) {
  var duration = 300;

  this.$body = parentMenu.parentMenu.$subMenuBody;
  // move new body to back
  this.$body.insertBefore(parentMenu.$subMenuBody);

  if (parentMenu.parentMenu._doActionTogglesSubMenu) {
    parentMenu.parentMenu._doActionTogglesSubMenu();
  }

  var displayBackup = parentMenu.$subMenuBody.css('display');
  parentMenu.$subMenuBody.css({
    width: 'auto',
    height: 'auto',
    display: 'none'
  });

  var actualBounds = this.htmlComp.getBounds();
  var actualSize = this.htmlComp.getSize();

  this.revalidateLayout();
  this.position();

  parentMenu.$subMenuBody.css('display', displayBackup);
  var position;
  position = parentMenu.$placeHolder.position();

  if (animated && this.rendered) {
    this.bodyAnimating = true;
    parentMenu.$subMenuBody.css({
      width: 'auto',
      height: 'auto'
    });
    var targetSize = this.htmlComp.getSize();
    parentMenu.$subMenuBody.css('box-shadow', 'none');
    this.htmlComp.setBounds(actualBounds);
    if (this.openingDirectionY !== 'up') {
      // set container to element
      parentMenu.$subMenuBody.cssTop();
    }
    // move new body to top of popup
    parentMenu.$subMenuBody.cssHeightAnimated(actualSize.height, parentMenu.$container.cssHeight(), {
      duration: duration,
      queue: false
    });

    var endTopposition = position.top - this.$body.cssHeight(),
      startTopposition = 0 - actualSize.height;

    parentMenu.$subMenuBody.cssTopAnimated(startTopposition, endTopposition, {
      duration: duration,
      queue: false,
      complete: function() {
        scout.scrollbars.uninstall(parentMenu.$subMenuBody, this.session);
        if (parentMenu.$container) { //check if $container is not removed before by closing operation.
          parentMenu.$placeHolder.replaceWith(parentMenu.$container);
          parentMenu.$container.toggleClass('expanded', false);
          this._updateFirstLastClass();
          this.updateNextToSelected('menu-item', parentMenu.$container);
        }
        parentMenu.$subMenuBody.detach();
        scout.scrollbars.install(this.$body, {
          parent: this
        });
        this.$body.css('box-shadow', "");
        this.bodyAnimating = false;
      }.bind(this)
    });

    this.$body.cssWidthAnimated(actualSize.width, targetSize.width, {
      duration: duration,
      progress: this.revalidateLayout.bind(this),
      queue: false
    });

    if (targetSize.height !== actualSize.height) {
      this.$body.cssHeightAnimated(actualSize.height, targetSize.height, {
        duration: duration,
        queue: false
      });
    }
  }
};

scout.ContextMenuPopup.prototype.renderSubMenuItems = function(parentMenu, menus, animated, initialSubMenuRendering) {
  if (!this.session.desktop.rendered && !initialSubMenuRendering) {
    this.initialSubmenusToRender = {
      parentMenu: parentMenu,
      menus: menus
    };
    return;
  }
  var actualBounds = this.htmlComp.getBounds();
  var actualSize = this.htmlComp.getSize();

  parentMenu.parentMenu.$subMenuBody = this.$body;

  var $all = this.$body.find('.' + 'menu-item');
  $all.toggleClass('next-to-selected', false);

  if (!parentMenu.$subMenuBody) {
    var textPaddingLeft = parentMenu.$container.find('.text').css('padding-left'),
      iconOffset = 0;
    if (parentMenu.iconId && parentMenu.$container.data('$icon').cssWidth() > iconOffset) {
      iconOffset = parentMenu.$container.data('$icon').cssWidth();
    }
    if (textPaddingLeft) {
      textPaddingLeft = textPaddingLeft.replace('px', '');
      textPaddingLeft = Number(textPaddingLeft);
    }
    this.$body = this._$createNewBody();
    parentMenu.$subMenuBody = this.$body;
    this._renderMenuItems(menus, initialSubMenuRendering, Math.max(textPaddingLeft, iconOffset));
  } else {
    // append $body
    this.$body = parentMenu.$subMenuBody;
  }
  var $insertAfterElement = parentMenu.$container.prev();
  var position = parentMenu.$container.position();
  parentMenu.$placeHolder = parentMenu.$container.clone();
  if ($insertAfterElement.length) {
    parentMenu.$placeHolder.insertAfter($insertAfterElement);
  } else {
    parentMenu.parentMenu.$subMenuBody.prepend(parentMenu.$placeHolder);
  }

  this.$body.insertAfter(parentMenu.parentMenu.$subMenuBody);
  this.$body.prepend(parentMenu.$container);
  parentMenu.$container.toggleClass('expanded');

  // sets this.animationBounds;
  this.revalidateLayout();
  this.position();

  this.updateNextToSelected();

  if (animated && this.rendered) {
    var duration = 300;
    this.bodyAnimating = true;
    parentMenu.parentMenu.$subMenuBody.css({
      width: 'auto',
      height: 'auto'
    });
    var targetBounds = this.htmlComp.getBounds();
    var targetSize = this.htmlComp.getSize();
    this.$body.css('box-shadow', 'none');
    // set container to element
    this.$body.cssWidthAnimated(actualSize.width, targetSize.width, {
      duration: duration,
      progress: this.revalidateLayout.bind(this),
      complete: function() {
        this.bodyAnimating = false;
      }.bind(this),
      queue: false
    });

    this.$body.cssHeightAnimated(parentMenu.$container.cssHeight(), targetSize.height, {
      duration: duration,
      queue: false
    });

    var endTopposition = 0 - targetSize.height,
      startTopposition = position.top - parentMenu.parentMenu.$subMenuBody.cssHeight(),
      topMargin = 0;

    // move new body to top of popup.
    this.$body.cssTopAnimated(startTopposition, endTopposition, {
      duration: duration,
      queue: false,
      complete: function() {
        scout.scrollbars.uninstall(parentMenu.parentMenu.$subMenuBody, this.session);
        parentMenu.parentMenu.$subMenuBody.detach();
        this.$body.cssTop(topMargin);
        scout.scrollbars.install(this.$body, {
          parent: this
        });
        this._updateFirstLastClass();
        this.$body.css('box-shadow', "");
      }.bind(this)
    });

    if (actualSize.height !== targetSize.height) {
      parentMenu.parentMenu.$subMenuBody.cssHeightAnimated(actualSize.height, targetSize.height, {
        duration: duration,
        queue: false
      });
      this.$container.cssHeight(actualSize.height, targetSize.height, {
        duration: duration,
        queue: false
      });
    }
    if (this.openingDirectionY === 'up') {
      this.$container.cssTopAnimated(actualBounds.y, targetBounds.y, {
        duration: duration,
        queue: false
      }).css('overflow', 'visible');
      // ajust top of head and deco
      this.$head.cssTopAnimated(actualSize.height, targetSize.height, {
        duration: duration,
        queue: false
      });
      this.$deco.cssTopAnimated(actualSize.height - 1, targetSize.height - 1, {
        duration: duration,
        queue: false
      });
    }
  } else {
    if (!initialSubMenuRendering) {
      scout.scrollbars.uninstall(parentMenu.parentMenu.$subMenuBody, this.session);
    }
    parentMenu.parentMenu.$subMenuBody.detach();
    scout.scrollbars.install(this.$body, {
      parent: this
    });
    this._updateFirstLastClass();
  }
};

scout.ContextMenuPopup.prototype._renderMenuItems = function(menus, initialSubMenuRendering, iconOffset) {
  menus = menus ? menus : this._getMenuItems();
  if (this.menuFilter) {
    menus = this.menuFilter(menus, scout.MenuDestinations.CONTEXT_MENU);
  }

  if (!menus || menus.length === 0) {
    return;
  }

  iconOffset = iconOffset ? iconOffset : 0;
  menus.some(function(menu) {
    // Invisible menus are rendered as well because their visibility might change dynamically
    if (menu.separator) {
      return;
    }
    // prevent loosing original parent
    var parentMenu = menu.parent;
    if (this.options.cloneMenuItems && !menu.cloneOf) {
      menu = menu.cloneAdapter({
        parent: this
      });
      menu.on('propertyChange', function(event) {
        if (event.selected) {
          var propertyChangeEvent = {
            properties: {
              selected: event.selected
            }
          };
          menu.cloneOf.onModelPropertyChange(propertyChangeEvent);
        }
      });
    } else {
      var oldParent = menu.parent;
      menu.setParent(this);
    }
    var menuRemoveHandler = function(event) {
      var removedMenu = event.eventOn;
      if (removedMenu.cloneOf) {
        this.session.unregisterAllAdapterClones(removedMenu.cloneOf);
      }
      removedMenu.setParent(oldParent);
    }.bind(this);
    menu.on('remove', menuRemoveHandler);
    // just set once because on second execution of this menu.parent is set to a popup
    if (!menu.parentMenu) {
      menu.parentMenu = parentMenu;
    }
    menu.render(this.$body);
    menu.afterSendDoAction = this.close.bind(this);
    menu.on('propertyChange', this._onMenuItemPropertyChange.bind(this));
    if (menu.iconId && menu.$container.data('$icon').cssWidth() > iconOffset) {
      iconOffset = menu.$container.data('$icon').cssWidth();
      // update already rendered menu-items
      this.$body.children().each(function(index, element) {
        var $element = $(element);
        var $icon = $element.data('$icon');
        if ($icon && $icon.cssWidth() < iconOffset) {
          $element.find('.text').css('padding-left', iconOffset - $icon.cssWidth());
        } else if (element !== menu.$container[0]) {
          $element.find('.text').css('padding-left', iconOffset);
        }
      });
    } else if (iconOffset && !menu.iconId) {
      menu.$container.find('.text').css('padding-left', iconOffset);
    } else if (menu.$container.data('$icon') && menu.$container.data('$icon').cssWidth() < iconOffset) {
      menu.$container.find('.text').css('padding-left', iconOffset - menu.$container.data('$icon').cssWidth());
    }
  }, this);
  while (this.initialSubmenusToRender && !initialSubMenuRendering) {
    var parentMenu = this.initialSubmenusToRender.parentMenu,
      subMenus = this.initialSubmenusToRender.menus;
    this.initialSubmenusToRender = undefined;
    this.renderSubMenuItems(parentMenu, subMenus, false, true);
  }
  this._updateFirstLastClass();
};

/**
 * When cloneMenuItems is true, it means the menu instance is also used elsewhere (for instance in a menu-bar).
 * When cloneMenuItems is false, it means the menu instance is only used in this popup.
 * In the first case we must _not_ call the remove() method, since the menu is still in use outside of the
 * popup. In the second case we must call remove(), because the menu is only used in the popup and no one
 * else would remove the widget otherwise.
 *
 * @override Widget.js
 */
scout.ContextMenuPopup.prototype._remove = function() {
  this._getMenuItems().forEach(function(menu) {
    if (this.options.cloneMenuItems) {
      if (this.session.hasClones(menu)) {
        this.session.unregisterAllAdapterClones(menu);
      }
    } else {
      menu.remove();
    }
  }, this);
  scout.scrollbars.uninstall(this.$body, this.session);
  scout.ContextMenuPopup.parent.prototype._remove.call(this);
};

scout.ContextMenuPopup.prototype.close = function(event) {
  if ((event && this.openEvent && event.originalEvent !== this.openEvent.originalEvent) || !event || !this.openEvent) {
    if (this.$container) {
      this.$container.stop(true);
    }
    scout.ContextMenuPopup.parent.prototype.close.call(this, event);
  }
};

/**
 * @override PopupWithHead.js
 */
scout.ContextMenuPopup.prototype._modifyBody = function() {
  this.$body.addClass('context-menu');
};

/**
 * Override this method to return menu items or actions used to render menu items.
 */
scout.ContextMenuPopup.prototype._getMenuItems = function() {
  return this.menuItems;
};

/**
 * Updates the first and last visible menu items with the according css classes.
 * Necessary because invisible menu-items are rendered.
 */
scout.ContextMenuPopup.prototype._updateFirstLastClass = function(event) {
  var $firstMenuItem, $lastMenuItem;

  // TODO [5.2] cgu: after refactoring of menu-item to context-menu-item we can use last/first instead of a fully qualified name. We also could move this function to jquery-scout to make it reusable.
  this.$body.children('.menu-item').each(function() {
    var $menuItem = $(this);
    $menuItem.removeClass('context-menu-item-first context-menu-item-last');

    if ($menuItem.isVisible()) {
      if (!$firstMenuItem) {
        $firstMenuItem = $menuItem;
      }
      $lastMenuItem = $menuItem;
    }
  });
  if ($firstMenuItem) {
    $firstMenuItem.addClass('context-menu-item-first');
  }
  if ($lastMenuItem) {
    $lastMenuItem.addClass('context-menu-item-last');
  }
};

scout.ContextMenuPopup.prototype.updateNextToSelected = function(menuItemClass, $selectedItem) {
  menuItemClass = menuItemClass ? menuItemClass : 'menu-item';
  var $all = this.$body.find('.' + menuItemClass);
  $selectedItem = $selectedItem ? $selectedItem : this.$body.find('.' + menuItemClass + '.selected');

  $all.toggleClass('next-to-selected', false);
  if ($selectedItem.hasClass('selected')) {
    $selectedItem.nextAll(':visible').first().toggleClass('next-to-selected', true);
  }
};

scout.ContextMenuPopup.prototype._onMenuItemPropertyChange = function(event) {
  if (!this.rendered) {
    return;
  }
  if (event.changedProperties.indexOf('visible') !== -1) {
    this._updateFirstLastClass();
  }
  // Make sure menu is positioned correctly afterwards (if it is opened upwards hiding/showing a menu item makes it necessary to reposition)
  this.position();
};
