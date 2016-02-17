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
scout.CalendarField = function() {
  scout.CalendarField.parent.call(this);
  this._addAdapterProperties(['calendar']);
};
scout.inherits(scout.CalendarField, scout.FormField);

scout.CalendarField.prototype._render = function($parent) {
  this.addContainer($parent, 'calendar-field');
  this.addLabel();
  this.addStatus();
  if (this.calendar) {
    this.calendar.render(this.$container);
    this.addField(this.calendar.$container);
  }
};
