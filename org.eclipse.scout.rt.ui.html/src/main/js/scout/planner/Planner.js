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
scout.Planner = function() {
  scout.Planner.parent.call(this);
  this.resourceMap = [];
  this.activityMap = [];

  // visual
  this._resourceTitleWidth = 20;

  // main elements
  this.$container;
  this.$range;
  this.$modes;
  this.$grid;

  // scale calculator
  this.transformLeft = function(t) {
    return t;
  };
  this.transformWidth = function(t) {
    return t;
  };

  // additional modes; should be stored in model
  this.yearPanelVisible = false;
  this._addAdapterProperties(['menus']);
};
scout.inherits(scout.Planner, scout.ModelAdapter);

scout.Planner.Direction = {
  BACKWARD: -1,
  FORWARD: 1
};

/**
 * Enum providing display-modes for planner (extends calendar).
 * @see IPlannerDisplayMode.java
 */
scout.Planner.DisplayMode = $.extend({
  INTRADAY: 0,
  CALENDAR_WEEK: 5,
  YEAR: 6
}, scout.Calendar.DisplayMode);


scout.Planner.SelectionMode = {
  NONE: 0,
  ACTIVITY: 1,
  SINGLE_RANGE: 2,
  MULTI_RANGE: 3
};

scout.Planner.prototype._init = function(model) {
  scout.Planner.parent.prototype._init.call(this, model);
  this._yearPanel = scout.create('YearPanel', {
    parent: this,
    alwaysSelectFirstDay: true
  });
  this._yearPanel.on('dateSelect', this._onYearPanelDateSelect.bind(this));
  this._header = scout.create('PlannerHeader', {
    parent: this
  });
  this._header.on('todayClick', this._onTodayClick.bind(this));
  this._header.on('yearClick', this._onYearClick.bind(this));
  this._header.on('previousClick', this._onPreviousClick.bind(this));
  this._header.on('nextClick', this._onNextClick.bind(this));
  this._header.on('displayModeClick', this._onDisplayModeClick.bind(this));
  for (var i = 0; i < this.resources.length; i++) {
    this._initResource(this.resources[i]);
  }
  this._syncDisplayMode(this.displayMode);
  this._syncAvailableDisplayModes(this.availableDisplayModes);
  this._syncViewRange(this.viewRange);
  this._syncSelectedResources(this.selectedResources);
  this._syncSelectionRange(this.selectionRange);

  this._tooltipSupport = new scout.TooltipSupport({
    parent: this,
    arrowPosition: 50
  });

  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.PlannerMenuItemsOrder(this.session, 'Planner')
  });
  this.menuBar.bottom();
};

scout.Planner.prototype._initResource = function(resource) {
  scout.defaultValues.applyTo(resource, 'Resource');
  resource.activities.forEach(function(activity) {
    this._initActivity(activity);
  }, this);
  this.resourceMap[resource.id] = resource;
};

scout.Planner.prototype._initActivity = function(activity) {
  activity.beginTime = scout.dates.parseJsonDate(activity.beginTime);
  activity.endTime = scout.dates.parseJsonDate(activity.endTime);
  scout.defaultValues.applyTo(activity, 'Activity');
  this.activityMap[activity.id] = activity;
};

scout.Planner.prototype._render = function($parent) {
  // basics, layout etc.
  this.$container = $parent.appendDiv('planner');
  var layout = new scout.PlannerLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  // main elements
  this._header.render(this.$container);
  this._yearPanel.render(this.$container);
  this.$grid = this.$container.appendDiv('planner-grid')
    .on('mousedown', '.resource-cells', this._onCellMousedown.bind(this))
    .on('mousedown', '.resource-title', this._onResourceTitleMousedown.bind(this))
    .on('contextmenu', '.resource-title', this._onResourceTitleContextMenu.bind(this))
    .on('contextmenu', '.planner-activity', this._onActivityContextMenu.bind(this));
  this.$scale = this.$container.appendDiv('planner-scale');
  this.menuBar.render(this.$container);

  scout.tooltips.install(this.$grid, {
    parent: this,
    selector: '.planner-activity',
    text: function($comp) {
      if (this._activityById($comp.attr('data-id'))) {
        return this._activityById($comp.attr('data-id')).tooltipText;
      } else {
        return undefined;
      }
    }.bind(this)
  });

  scout.scrollbars.install(this.$grid, {
    parent: this
  });
  this._gridScrollHandler = this._onGridScroll.bind(this);
  this.$grid.on('scroll', this._gridScrollHandler);
};

scout.Planner.prototype._renderProperties = function() {
  scout.Planner.parent.prototype._renderProperties.call(this);

  this._renderViewRange();
  this._renderHeaderVisible();
  this._renderMenus();
  this._renderYearPanelVisible(false);
  this._renderResources();
  this._renderSelectedResources();
  // render with setTimeout because the planner needs to be layouted first
  setTimeout(this._renderSelectionRange.bind(this));
};

scout.Planner.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$grid, this.session);
  scout.Planner.parent.prototype._remove.call(this);
};

/* -- basics, events -------------------------------------------- */

scout.Planner.prototype._onPreviousClick = function(event) {
  this._navigateDate(scout.Planner.Direction.BACKWARD);
};

scout.Planner.prototype._onNextClick = function(event) {
  this._navigateDate(scout.Planner.Direction.FORWARD);
};

scout.Planner.prototype._navigateDate = function(direction) {
  var viewRange = new scout.DateRange(this.viewRange.from, this.viewRange.to),
    displayMode = scout.Planner.DisplayMode;

  if (this.displayMode === displayMode.DAY) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, 0, direction);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, 0, direction);
  } else if (scout.isOneOf(this.displayMode, displayMode.WEEK, displayMode.WORK_WEEK)) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, 0, direction * 7);
    viewRange.from = scout.dates.shiftToNextOrPrevMonday(viewRange.from, -1 * direction);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, 0, direction * 7);
  } else if (this.displayMode === displayMode.MONTH) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, direction, 0);
    viewRange.from = scout.dates.shiftToNextOrPrevMonday(viewRange.from, -1 * direction);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode === displayMode.CALENDAR_WEEK) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, direction, 0);
    viewRange.from = scout.dates.shiftToNextOrPrevMonday(viewRange.from, -1 * direction);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode === displayMode.YEAR) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, 3 * direction, 0);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, 3 * direction, 0);
  }

  this.setViewRange(viewRange);
};

scout.Planner.prototype._onTodayClick = function(event) {
  var today = new Date(),
    year = today.getFullYear(),
    month = today.getMonth(),
    date = today.getDate(),
    day = (today.getDay() + 6) % 7,
    displayMode = scout.Planner.DisplayMode;

  if (this.displayMode === displayMode.DAY) {
    today = new Date(year, month, day);
  } else if (this.displayMode === displayMode.YEAR) {
    today = new Date(year, month, 1);
  } else {
    today = new Date(year, month, date - day);
  }

  this.setViewRangeFrom(today);
};

scout.Planner.prototype._onDisplayModeClick = function(event) {
  var displayMode = event.displayMode;
  this.setDisplayMode(displayMode);
};

scout.Planner.prototype._onYearClick = function(event) {
  this.setYearPanelVisible(!this.yearPanelVisible);
};

scout.Planner.prototype._onYearPanelDateSelect = function(event) {
  this.setViewRangeFrom(event.date);
};

scout.Planner.prototype._onResourceTitleMousedown = function(event) {
  var $resource = $(event.target).parent();
  if ($resource.isSelected()) {
    if (event.which === 3 || event.which === 1 && event.ctrlKey) {
      // Right click on an already selected resource must not clear the selection -> context menu will be opened
      return;
    }
  }
  this.startRow = $resource.data('resource');
  this.lastRow = this.startRow;
  this._select();
};

scout.Planner.prototype._onResourceTitleContextMenu = function(event) {
  this._showContextMenu(event, 'Planner.Resource');
};

scout.Planner.prototype._onRangeSelectorContextMenu = function(event) {
  this._showContextMenu(event, 'Planner.Range');
};

scout.Planner.prototype._onActivityContextMenu = function(event) {
  this._showContextMenu(event, 'Planner.Activity');
};

scout.Planner.prototype._showContextMenu = function(event, allowedType) {
  event.preventDefault();
  event.stopPropagation();
  var func = function func(event, allowedType) {
    var filteredMenus = this._filterMenus([allowedType]),
      $part = $(event.currentTarget);
    if (filteredMenus.length === 0) {
      return; // at least one menu item must be visible
    }
    var popup = scout.create('ContextMenuPopup', {
      parent: this,
      menuItems: filteredMenus,
      location: {
        x: event.pageX,
        y: event.pageY
      },
      $anchor: $part
    });
    popup.open();
  }.bind(this);

  scout.menus.showContextMenuWithWait(this.session, func, event, allowedType);
};

scout.Planner.prototype._onGridScroll = function() {
  this._reconcileScrollPos();
};

scout.Planner.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll scale as well
  var scrollLeft = this.$grid.scrollLeft();
  this.$scale.scrollLeft(scrollLeft);
};

scout.Planner.prototype._renderRange = function() {
  if (!this.viewRange.from || !this.viewRange.to) {
    return;
  }
  var text,
    toDate = new Date(this.viewRange.to.valueOf() - 1),
    toText = this.session.text('ui.to'),
    displayMode = scout.Planner.DisplayMode;

  // find range text
  if (scout.dates.isSameDay(this.viewRange.from, toDate)) {
    text = this._dateFormat(this.viewRange.from, 'd. MMMM yyyy');
  } else if (this.viewRange.from.getMonth() === toDate.getMonth() && this.viewRange.from.getFullYear() === toDate.getFullYear()) {
    text = scout.strings.join(' ', this._dateFormat(this.viewRange.from, 'd.'), toText, this._dateFormat(toDate, 'd. MMMM yyyy'));
  } else if (this.viewRange.from.getFullYear() === toDate.getFullYear()) {
    if (this.displayMode === displayMode.YEAR) {
      text = scout.strings.join(' ', this._dateFormat(this.viewRange.from, 'MMMM'), toText, this._dateFormat(toDate, 'MMMM yyyy'));
    } else {
      text = scout.strings.join(' ', this._dateFormat(this.viewRange.from, 'd.  MMMM'), toText, this._dateFormat(toDate, 'd. MMMM yyyy'));
    }
  } else {
    if (this.displayMode === displayMode.YEAR) {
      text = scout.strings.join(' ', this._dateFormat(this.viewRange.from, 'MMMM yyyy'), toText, this._dateFormat(toDate, 'MMMM yyyy'));
    } else {
      text = scout.strings.join(' ', this._dateFormat(this.viewRange.from, 'd.  MMMM yyyy'), toText, this._dateFormat(toDate, 'd. MMMM yyyy'));
    }
  }

  // set text
  $('.planner-select', this._header.$range).text(text);
};

scout.Planner.prototype._renderScale = function() {
  if (!this.viewRange.from || !this.viewRange.to) {
    return;
  }
  var $timeline, $timelineLarge, $timelineSmall, loop, $divLarge, $divSmall, width, newLargeGroup,
    first = true,
    that = this,
    displayMode = scout.Planner.DisplayMode;

  // empty scale
  this.$scale.empty();
  this.$grid.children('.planner-small-scale-item-line').remove();
  this.$grid.children('.planner-large-scale-item-line').remove();

  // append main elements
  this.$scaleTitle = this.$scale.appendDiv('planner-scale-title');
  this._renderLabel();
  this.$timeline = this.$scale.appendDiv('timeline');
  this.$timelineLarge = this.$timeline.appendDiv('timeline-large');
  this.$timelineSmall = this.$timeline.appendDiv('timeline-small');
  $timeline = this.$timeline;
  $timelineLarge = this.$timelineLarge;
  $timelineSmall = this.$timelineSmall;

  // fill timeline large depending on mode
  if (this.displayMode === displayMode.DAY) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getMinutes() === 0) || first) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'HH')).data('count', 0);
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, ':mm'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setMinutes(loop.getMinutes() + 30);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
      first = false;
    }
  } else if (scout.isOneOf(this.displayMode, displayMode.WORK_WEEK, displayMode.WEEK)) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getHours() === 0) || first) {
        if ((loop.getMonth() === 0) || first) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMMM yyyy')).data('count', 0);
        } else if (loop.getDate() === 1) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMMM')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd.')).data('count', 0);
        }
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'HH:mm'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setHours(loop.getHours() + 6);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
      first = false;
    }

  } else if (this.displayMode === displayMode.MONTH) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getDate() === 1) || first) {
        if ((loop.getMonth() === 0) || first) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
        }
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'dd'))
        .data('date-from', new Date(loop.valueOf()));

      if (loop.getDate() % 2 === 1) {
        $divSmall.addClass('label-invisible');
      }

      loop = scout.dates.shift(loop, 0, 0, 1);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
      first = false;
    }

  } else if (this.displayMode === displayMode.CALENDAR_WEEK) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getDate() < 8) || first === true) {
        if ((loop.getMonth() === 0) || first === true) {
          if (loop.getDate() > 11) {
            $divLarge = $timelineLarge.appendDiv('scale-item', '&nbsp').data('count', 0);
            first = 2;
          } else {
            $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
            first = false;
          }
        } else {
          if (first === 2) {
            $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
            first = false;
          } else {
            $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
          }
        }
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', scout.dates.weekInYear(loop))
        .data('date-from', new Date(loop.valueOf()))
        .data('tooltipText', this._scaleTooltipText.bind(this));
      this._tooltipSupport.install($divSmall);

      loop.setDate(loop.getDate() + 7);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === displayMode.YEAR) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getMonth() === 0) || first) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'yyyy')).data('count', 0);
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'MMMM'))
        .data('date-from', new Date(loop.valueOf()));

      loop = scout.dates.shift(loop, 0, 1, 0);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
      first = false;
    }
  }

  // set sizes and append scale lines
  var $smallScaleItems = $timelineSmall.children('.scale-item');
  var $largeScaleItems = $timelineLarge.children('.scale-item');
  width = 100 / $smallScaleItems.length;
  $largeScaleItems.each(function() {
    var $scaleItem = $(this);
    $scaleItem.css('width', $scaleItem.data('count') * width + '%')
      .data('scale-item-line', that.$grid.appendDiv('planner-large-scale-item-line'));
    $scaleItem.appendDiv('planner-large-scale-item-line')
      .css('left', 0);
  });
  $smallScaleItems.each(function(index) {
    var $scaleItem = $(this);
    $scaleItem.css('width', width + '%');
    if (!$scaleItem.data('first')) {
      $scaleItem.data('scale-item-line', that.$grid.appendDiv('planner-small-scale-item-line'));
      $scaleItem.appendDiv('planner-small-scale-item-line')
        .css('left', 0);
    }
  });

  // find transfer function
  this.beginScale = $timelineSmall.children().first().data('date-from').valueOf();
  this.endScale = $timelineSmall.children().last().data('date-to').valueOf();

  this.transformLeft = function(begin, end) {
    return function(t) {
      return (t - begin) / (end - begin) * 100;
    };
  }(this.beginScale, this.endScale);

  this.transformWidth = function(begin, end) {
    return function(t) {
      return t / (end - begin) * 100;
    };
  }(this.beginScale, this.endScale);
};

/* -- scale events --------------------------------------------------- */

scout.Planner.prototype._scaleTooltipText = function($scale) {
  var toText = ' ' + this.session.text('ui.to') + ' ',
    from = new Date($scale.data('date-from').valueOf()),
    to = new Date($scale.data('date-to').valueOf() - 1);

  if (from.getMonth() === to.getMonth()) {
    return this._dateFormat(from, 'd.') + toText + this._dateFormat(to, 'd. MMMM yyyy');
  } else if (from.getFullYear() === to.getFullYear()) {
    return this._dateFormat(from, 'd. MMMM') + toText + this._dateFormat(to, 'd. MMMM yyyy');
  } else {
    return this._dateFormat(from, 'd. MMMM yyyy') + toText + this._dateFormat(to, 'd. MMMM yyyy');
  }
};

/* --  render resources, activities --------------------------------- */

scout.Planner.prototype._removeAllResources = function() {
  this.resources.forEach(function(resource) {
    resource.$resource.remove();
  });
};

scout.Planner.prototype._renderResources = function(resources) {
  var i, $resource, resource,
    resourcesHtml = '';

  resources = resources || this.resources;
  for (i = 0; i < resources.length; i++) {
    resource = resources[i];
    resourcesHtml += this._buildResourceHtml(resource, this.$grid);
  }

  // Append resources to grid
  $(resourcesHtml).appendTo(this.$grid);

  // Match resources
  this.$grid.children('.planner-resource').each(function(index, element) {
    var $element = $(element);
    resource = this._resourceById($element.attr('data-id'));
    this._linkResource($element, resource);
  }.bind(this));
};

scout.Planner.prototype._linkResource = function($resource, resource) {
  $resource.data('resource', resource);
  resource.$resource = $resource;
  resource.$cells = $resource.children('.resource-cells');
};

scout.Planner.prototype._rerenderActivities = function(resources) {
  resources = resources || this.resources;
  resources.forEach(function(resource) {
    this._removeActivititesForResource(resource);
    this._renderActivititesForResource(resource);
  }, this);
};

scout.Planner.prototype._buildResourceHtml = function(resource) {
  var resourceHtml = '<div class="planner-resource" data-id="' + resource.id + '">';
  resourceHtml += '<div class="resource-title">' + scout.strings.encode(resource.resourceCell.text || '') + '</div>';
  resourceHtml += '<div class="resource-cells">' + this._buildActivitiesHtml(resource) + '</div>';
  resourceHtml += '</div>';
  return resourceHtml;
};

scout.Planner.prototype._renderActivititesForResource = function(resource) {
  resource.$cells.html(this._buildActivitiesHtml(resource));
};

scout.Planner.prototype._buildActivitiesHtml = function(resource) {
  var activitiesHtml = '';
  resource.activities.forEach(function(activity) {
    if (activity.beginTime.valueOf() >= this.endScale ||
      activity.endTime.valueOf() <= this.beginScale) {
      // don't add activities which are not in the view range
      return;
    }
    activitiesHtml += this._buildActivityHtml(activity);
  }, this);
  return activitiesHtml;
};

scout.Planner.prototype._removeActivititesForResource = function(resource) {
  resource.activities.forEach(function(activity) {
    if (activity.$activity) {
      activity.$activity.remove();
      activity.$activity = null;
    }
  }, this);
};

scout.Planner.prototype._buildActivityHtml = function(activity) {
  var i, level = 100 - Math.min(activity.level * 100, 100),
    levelColor = scout.styles.modelToCssColor(activity.levelColor),
    begin = activity.beginTime.valueOf(),
    end = activity.endTime.valueOf();

  // Make sure activity fits into scale
  begin = Math.max(begin, this.beginScale);
  end = Math.min(end, this.endScale);

  var activityCssClass = 'planner-activity' + (activity.cssClass ? (' ' + activity.cssClass) : '');
  var activityStyle = 'left: ' + 'calc(' + this.transformLeft(begin) + '% + 2px);';
  activityStyle += ' width: ' + 'calc(' + this.transformWidth(end - begin) + '% - 4px);';

  if (levelColor) {
    activityStyle += ' background-color: ' + levelColor + ';';
    activityStyle += ' border-color: ' + levelColor + ';';
  }

  // the background-color represents the fill level and not the image. This makes it easier to change the color using a css class
  activityStyle += ' background-image: ' + 'linear-gradient(to bottom, #fff 0%, #fff ' + level + '%, transparent ' + level + '%, transparent 100% );';

  var activityHtml = '<div';
  activityHtml += ' class="' + activityCssClass + '"';
  activityHtml += ' style="' + activityStyle + '"';
  activityHtml += ' data-id="' + activity.id + '"';
  activityHtml += '>' + scout.strings.encode(activity.text || '') + '</div>';
  return activityHtml;
};

/* -- selector -------------------------------------------------- */

scout.Planner.prototype._onCellMousedown = function(event) {
  var $activity,
    $resource,
    $target = $(event.target),
    selectionMode = scout.Planner.SelectionMode;

  if (this.selectionMode === selectionMode.NONE) {
    return;
  }

  if (this.selectionMode === selectionMode.ACTIVITY) {
    $activity = this._$elementFromPoint(event.pageX, event.pageY);

    if ($activity.hasClass('planner-activity')) {
      $('.selected', this.$grid).removeClass('selected');
      $activity.addClass('selected');

      $resource = $activity.parent().parent();
      this.selectResources([$resource.data('resource')]);
    }
  } else {
    if ($target.hasClass('selector')) {
      if (event.which === 3 || event.which === 1 && event.ctrlKey) {
        // Right click on the selector must not clear the selection -> context menu will be opened
        return;
      }
    }

    // init selector
    this.startRow = this._findRow(event.pageY);
    this.lastRow = this.startRow;

    // find range on scale
    this.startRange = this._findScale(event.pageX);
    this.lastRange = this.startRange;

    // draw
    this._select(true);

    // event
    $target.document()
      .on('mousemove', this._onCellMousemove.bind(this))
      .one('mouseup', this._onCellMouseup.bind(this));
  }
};

scout.Planner.prototype._onResizeMousedown = function(event) {
  var swap,
    $target = $(event.target);


  // find range on scale
  if (($target.hasClass('selector-resize-right') && this.startRange.to > this.lastRange.to) ||
    ($target.hasClass('selector-resize-left') && this.startRange.to < this.lastRange.to)) {
    swap = this.startRange;
    this.startRange = this.lastRange;
    this.lastRange = swap;
  }

  $target.body().addClass('col-resize');

  $target.document()
    .on('mousemove', this._onResizeMousemove.bind(this))
    .one('mouseup', this._onCellMouseup.bind(this));

  return false;

};

scout.Planner.prototype._onCellMousemove = function(event) {
  this.lastRow = this._findRow(event.pageY);
  this.lastRange = this._findScale(event.pageX);

  this._select(true);
};

scout.Planner.prototype._onResizeMousemove = function(event) {
  this.lastRange = this._findScale(event.pageX);

  this._select(true);
};

scout.Planner.prototype._onCellMouseup = function(event) {
  this._select();
  var $target = $(event.target);
  $target.body().removeClass('col-resize');
  $target.document().off('mousemove');
};

scout.Planner.prototype._select = function(whileSelecting) {
  if (!this.startRow || !this.lastRow) {
    return;
  }
  // If startRange or lastRange are not given, use the existing range selection
  // Happens if the user clicks a resource instead of making a range selection
  if (!this.startRange && !this.lastRange) {
    if (this.selectionRange.from) {
      this.startRange = {};
      this.startRange.from = this.selectionRange.from.getTime();
      this.startRange.to = this.startRange.from;
    }
    if (this.selectionRange.to) {
      this.lastRange = {};
      this.lastRange.from = this.selectionRange.to.getTime();
      this.lastRange.to = this.lastRange.from;
    }
  }
  var rangeSelected = !!(this.startRange && this.lastRange);
  var $startRow = this.startRow.$resource,
    $lastRow = this.lastRow.$resource;

  // in case of single selection
  if (this.selectionMode === scout.Planner.SelectionMode.SINGLE_RANGE) {
    this.lastRow = this.startRow;
    $lastRow = this.startRow.$resource;
  }

  // select rows
  var $upperRow = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow,
    $lowerRow = ($startRow[0].offsetTop > $lastRow[0].offsetTop) ? $startRow : $lastRow,
    resources = $('.planner-resource', this.$grid).toArray(),
    top = $upperRow[0].offsetTop,
    low = $lowerRow[0].offsetTop;

  for (var r = resources.length - 1; r >= 0; r--) {
    var row = resources[r];
    if ((row.offsetTop < top && row.offsetTop < low) || (row.offsetTop > top && row.offsetTop > low)) {
      resources.splice(r, 1);
    }
  }

  this.selectResources(resources.map(function(i) {
    return $(i).data('resource');
  }), !whileSelecting);

  if (rangeSelected) {
    // left and width
    var from = Math.min(this.lastRange.from, this.startRange.from),
      to = Math.max(this.lastRange.to, this.startRange.to);

    var selectionRange = {
      from: new Date(from),
      to: new Date(to)
    };

    this.selectRange(selectionRange, !whileSelecting);
  }
};

scout.Planner.prototype._findRow = function(y) {
  var x = this.$grid.offset().left + 10,
    $row = this._$elementFromPoint(x, y).parent();

  if ($row.hasClass('planner-resource')) {
    return $row.data('resource');
  } else {
    return null;
  }
};

scout.Planner.prototype._findScale = function(x) {
  var y = this.$scale.offset().top + this.$scale.height() * 0.75,
    $scale = this._$elementFromPoint(x, y);

  if ($scale.data('date-from') !== undefined) {
    return {
      from: $scale.data('date-from').valueOf(),
      to: $scale.data('date-to').valueOf()
    };
  } else {
    return null;
  }
};

/* -- helper ---------------------------------------------------- */

scout.Planner.prototype._$elementFromPoint = function(x, y) {
  return $(this.$container.document(true).elementFromPoint(x, y));
};

scout.Planner.prototype._dateFormat = function(date, pattern) {
  var d = new Date(date.valueOf()),
    dateFormat = new scout.DateFormat(this.session.locale, pattern);

  return dateFormat.format(d);
};

scout.Planner.prototype._renderViewRange = function() {
  this._renderRange();
  this._renderScale();
  this.invalidateLayoutTree();
};

scout.Planner.prototype._renderHeaderVisible = function() {
  this._header.setVisible(this.headerVisible);
  this.invalidateLayoutTree();
};

scout.Planner.prototype._renderYearPanelVisible = function(animated) {
  var yearPanelWidth;
  if (this.yearPanelVisible) {
    this._yearPanel.renderContent();
  }

  // show or hide year panel
  $('.calendar-toggle-year', this.$modes).select(this.yearPanelVisible);
  if (this.yearPanelVisible) {
    yearPanelWidth = 210;
  } else {
    yearPanelWidth = 0;
  }
  this._yearPanel.$container.animate({
    width: yearPanelWidth
  }, {
    duration: animated ? 500 : 0,
    progress: this._onYearPanelWidthChange.bind(this),
    complete: this._afterYearPanelWidthChange.bind(this)
  });
};

scout.Planner.prototype._onYearPanelWidthChange = function() {
  if (!this._yearPanel.$container) {
    // If container has been removed in the meantime (e.g. user navigates away while animation is in progress)
    return;
  }
  var yearPanelWidth = this._yearPanel.$container.outerWidth();
  this.$grid.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
  this.$scale.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
};

scout.Planner.prototype._afterYearPanelWidthChange = function() {
  if (!this.yearPanelVisible) {
    this._yearPanel.removeContent();
  }
};

scout.Planner.prototype._renderMenus = function() {
  this._updateMenuBar();
};

scout.Planner.prototype._updateMenuBar = function() {
  var menuItems = this._filterMenus(['Planner.EmptySpace', 'Planner.Resource', 'Planner.Activity', 'Planner.Range'], true);
  this.menuBar.updateItems(menuItems);
};

scout.Planner.prototype._filterMenus = function(allowedTypes, enableDisableKeyStroke) {
  allowedTypes = allowedTypes || [];
  if (allowedTypes.indexOf('Planner.Resource') > -1 && this.selectedResources.length === 0) {
    scout.arrays.remove(allowedTypes, 'Planner.Resource');
  }
  if (allowedTypes.indexOf('Planner.Activity') > -1 && !this.selectedActivity) {
    scout.arrays.remove(allowedTypes, 'Planner.Activity');
  }
  if (allowedTypes.indexOf('Planner.Range') > -1 && !this.selectionRange.from && !this.selectionRange.to) {
    scout.arrays.remove(allowedTypes, 'Planner.Range');
  }
  return scout.menus.filter(this.menus, allowedTypes, true, enableDisableKeyStroke);
};

scout.Planner.prototype._renderWorkDayCount = function() {};

scout.Planner.prototype._renderWorkDaysOnly = function() {};

scout.Planner.prototype._renderFirstHourOfDay = function() {};

scout.Planner.prototype._renderLastHourOfDay = function() {};

scout.Planner.prototype._renderIntradayInterval = function() {};

scout.Planner.prototype._renderAvailableDisplayModes = function() {
  // done by PlannerHeader.js
};

scout.Planner.prototype._renderDisplayMode = function() {
  // done by PlannerHeader.js
};

scout.Planner.prototype._syncViewRange = function(viewRange) {
  this.viewRange = new scout.DateRange(
    scout.dates.parseJsonDate(viewRange.from),
    scout.dates.parseJsonDate(viewRange.to)
  );
  this._yearPanel.setViewRange(this.viewRange);
  this._yearPanel.selectDate(this.viewRange.from);
};

scout.Planner.prototype._syncDisplayMode = function(displayMode) {
  this.displayMode = displayMode;
  this._yearPanel.setDisplayMode(this.displayMode);
  this._header.setDisplayMode(this.displayMode);
};

scout.Planner.prototype._syncAvailableDisplayModes = function(availableDisplayModes) {
  this.availableDisplayModes = availableDisplayModes;
  this._header.setAvailableDisplayModes(this.availableDisplayModes);
};

scout.Planner.prototype._syncSelectionRange = function(selectionRange) {
  this.selectionRange = {
    from: scout.dates.parseJsonDate(selectionRange.from),
    to: scout.dates.parseJsonDate(selectionRange.to)
  };
};

scout.Planner.prototype._syncSelectedResources = function(selectedResources) {
  this.selectedResources = this._resourcesByIds(selectedResources);
};

scout.Planner.prototype._renderSelectedResources = function(newIds, oldSelectedResources) {
  if (oldSelectedResources) {
    oldSelectedResources.forEach(function(resource) {
      resource.$resource.select(false);
    });
  }

  this.selectedResources.forEach(function(resource) {
    resource.$resource.select(true);
  });

  // Only call update menubar on property change, not necessary to call it when initializing
  if (this.rendered) {
    this._updateMenuBar();
  }
};

scout.Planner.prototype._renderSelectionRange = function() {
  var $startRow, $lastRow,
    from = this.selectionRange.from,
    to = this.selectionRange.to,
    startRow = this.selectedResources[0],
    lastRow = this.selectedResources[this.selectedResources.length - 1];

  // remove old selector
  if (this.$selector) {
    this.$selector.remove();
    $('.selected', this.$scale).select(false);
  }

  if (!startRow || !lastRow || !this.selectionRange.from || !this.selectionRange.to) {
    return;
  }
  $startRow = startRow.$resource;
  $lastRow = lastRow.$resource;

  // top and height
  var $parent = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow;
  this.$selector = $parent.children('.resource-cells').appendDiv('selector');
  this.$selector.css('height', $startRow.outerHeight() + Math.abs($lastRow[0].offsetTop - $startRow[0].offsetTop));
  this.$selector.appendDiv('selector-resize-left').mousedown(this._onResizeMousedown.bind(this));
  this.$selector.appendDiv('selector-resize-right').mousedown(this._onResizeMousedown.bind(this));
  this.$selector
    .css('left', 'calc(' + this.transformLeft(from) + '% - 6px)')
    .css('width', 'calc(' + this.transformWidth(to - from) + '% + 12px)')
    .on('contextmenu', this._onRangeSelectorContextMenu.bind(this));

  // colorize scale
  var $scaleItems = $('.timeline-small', this.$scale).children();
  for (var i = 0; i < $scaleItems.length; i++) {
    var $item = $scaleItems.eq(i);
    if ($item.data('date-from') >= from && $item.data('date-to') <= to) {
      $item.addClass('selected');
    }
  }

  // Only call update menubar on property change, not necessary to call it when initializing
  if (this.rendered) {
    this._updateMenuBar();
  }
};

scout.Planner.prototype._renderSelectedActivity = function() {
  // Only call update menubar on property change, not necessary to call it when initializing
  if (this.rendered) {
    this._updateMenuBar();
  }
};

scout.Planner.prototype._renderLabel = function() {
  var label = this.label || '';
  if (this.$scaleTitle) {
    this.$scaleTitle.text(label);
  }
};

scout.Planner.prototype._resourcesByIds = function(ids) {
  return ids.map(this._resourceById.bind(this));
};

scout.Planner.prototype._activityById = function(id) {
  return this.activityMap[id];
};

scout.Planner.prototype._resourceById = function(id) {
  return this.resourceMap[id];
};

scout.Planner.prototype.setDisplayMode = function(displayMode) {
  this.displayMode = displayMode;
  this._yearPanel.setDisplayMode(displayMode);

  this._sendSetDisplayMode(displayMode);
  if (this.rendered) {
    this._renderDisplayMode();
  }

  this.startRange = null;
  this.lastRange = null;
};

scout.Planner.prototype.layoutYearPanel = function() {
  if (this.yearPanelVisible) {
    scout.scrollbars.update(this._yearPanel.$yearList);
    this._yearPanel._scrollYear();
  }
};

scout.Planner.prototype.setYearPanelVisible = function(visible) {
  if (this.yearPanelVisible === visible) {
    return;
  }
  this.yearPanelVisible = visible;
  if (this.rendered) {
    this._renderYearPanelVisible(true);
  }
};

scout.Planner.prototype.setViewRangeFrom = function(date) {
  var diff = this.viewRange.to.getTime() - this.viewRange.from.getTime(),
    viewRange = new scout.DateRange(this.viewRange.from, this.viewRange.to);

  viewRange.from = date;
  viewRange.to = new Date(date.getTime() + diff);
  this.setViewRange(viewRange);
};

scout.Planner.prototype.setViewRange = function(viewRange) {
  this.viewRange = viewRange;
  this._yearPanel.setViewRange(viewRange);
  this._yearPanel.selectDate(this.viewRange.from);
  this._sendSetViewRange(viewRange);

  if (this.rendered) {
    this._renderViewRange();
    this._rerenderActivities();
    this.validateLayoutTree();
  }
};

scout.Planner.prototype.selectRange = function(range, notifyServer) {
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  this.selectionRange = range;
  if (notifyServer) {
    this._sendSetSelection();
  }
  if (this.rendered) {
    this._renderSelectionRange();
  }
};

scout.Planner.prototype.selectResources = function(resources, notifyServer) {
  var oldSelection = this.selectedResources;
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  this.selectedResources = resources;
  if (notifyServer) {
    this._sendSetSelection();
  }
  if (this.rendered) {
    this._renderSelectedResources('', oldSelection);
  }
};

/**
 * Returns true if a deselection happened. False if the given resources were not selected at all.
 */
scout.Planner.prototype.deselectResources = function(resources, notifyServer) {
  var deselected = false;
  resources = scout.arrays.ensure(resources);
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  var selectedResources = this.selectedResources.slice(); // copy
  if (scout.arrays.removeAll(selectedResources, resources)) {
    this.selectResources(selectedResources, notifyServer);
    deselected = true;
  }
  return deselected;
};

scout.Planner.prototype.insertResources = function(resources) {
  // Update model
  resources.forEach(function(resource) {
    this._initResource(resource);
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.resources.push(resource);
  }.bind(this));

  // Update HTML
  if (this.rendered) {
    this._renderResources(resources);
    this.invalidateLayoutTree();
  }
};

scout.Planner.prototype.deleteResources = function(resources) {
  if (this.deselectResources(resources, false)) {
    this.selectRange({}, false);
  }
  resources.forEach(function(resource) {
    // Update model
    scout.arrays.remove(this.resources, resource);
    delete this.resourceMap[resource.id];

    resource.activities.forEach(function(activity) {
      delete this.activityMap[activity.id];
    }.bind(this));

    // Update HTML
    if (this.rendered) {
      resource.$resource.remove();
      delete resource.$resource;
    }
  }.bind(this));

  this.invalidateLayoutTree();
};

scout.Planner.prototype.deleteAllResources = function() {
  // Update HTML
  if (this.rendered) {
    this._removeAllResources();
    this.invalidateLayoutTree();
  }

  // Update model
  this.resources = [];
  this.resourceMap = {};
  this.activityMap = {};
  this.selectResources([], false);
  this.selectRange({}, false);
};

scout.Planner.prototype._updateResources = function(resources) {
  resources.forEach(function(updatedResource) {
    var oldResource = this.resourceMap[updatedResource.id];
    if (!oldResource) {
      throw new Error('Update event received for non existing resource. ResourceId: ' + updatedResource.id);
    }

    // Replace old resource
    this._initResource(updatedResource);
    scout.arrays.replace(this.resources, oldResource, updatedResource);
    scout.arrays.replace(this.selectedResources, oldResource, updatedResource);

    // Replace old $resource
    if (this.rendered && oldResource.$resource) {
      var $updatedResource = $(this._buildResourceHtml(updatedResource));
      oldResource.$resource.replaceWith($updatedResource);
      this._linkResource($updatedResource, updatedResource);
    }
  }.bind(this));
};

scout.Planner.prototype._sendSetDisplayMode = function(displayMode) {
  this._send('setDisplayMode', {
    displayMode: displayMode
  });
};

scout.Planner.prototype._sendSetViewRange = function(viewRange) {
  this._send('setViewRange', {
    viewRange: scout.dates.toJsonDateRange(viewRange)
  });
};

scout.Planner.prototype._sendSetSelection = function() {
  var selectionRange = scout.dates.toJsonDateRange(this.selectionRange),
    resourceIds = this.selectedResources.map(function(r) {
      return r.id;
    });
  this._send('setSelection', {
    resourceIds: resourceIds,
    selectionRange: selectionRange
  });
};

scout.Planner.prototype._onResourcesInserted = function(resources) {
  this.insertResources(resources);
};

scout.Planner.prototype._onResourcesDeleted = function(resourceIds) {
  var resources = this._resourcesByIds(resourceIds);
  this.deleteResources(resources);
};

scout.Planner.prototype._onResourcesSelected = function(resourceIds) {
  var resources = this._resourcesByIds(resourceIds);
  this.selectResources(resources, false);
};

scout.Planner.prototype._onAllResourcesDeleted = function() {
  this.deleteAllResources();
};

scout.Planner.prototype._onResourcesUpdated = function(resources) {
  this._updateResources(resources);
};

scout.Planner.prototype.onModelAction = function(event) {
  if (event.type === 'resourcesInserted') {
    this._onResourcesInserted(event.resources);
  } else if (event.type === 'resourcesDeleted') {
    this._onResourcesDeleted(event.resourceIds);
  } else if (event.type === 'resourcesSelected') {
    this._onResourcesSelected(event.resourceIds);
  } else if (event.type === 'allResourcesDeleted') {
    this._onAllResourcesDeleted();
  } else if (event.type === 'resourcesUpdated') {
    this._onResourcesUpdated(event.resources);
  } else {
    scout.Planner.parent.prototype.onModelAction.call(this, event);
  }
};
