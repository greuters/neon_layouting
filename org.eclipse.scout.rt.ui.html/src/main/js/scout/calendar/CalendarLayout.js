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
scout.CalendarLayout = function(calendar) {
  scout.CalendarLayout.parent.call(this);
  this.calendar = calendar;
};
scout.inherits(scout.CalendarLayout, scout.AbstractLayout);

scout.CalendarLayout.prototype.layout = function($container) {
  var height = 0;

  height += $container.cssMarginTop() + $container.cssMarginBottom();
  $container.css('height', 'calc(100% - ' + height + 'px)');

  this.calendar.layoutSize();
  this.calendar.layoutYearPanel();
};
