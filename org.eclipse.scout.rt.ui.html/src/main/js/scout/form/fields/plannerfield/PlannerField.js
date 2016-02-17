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
scout.PlannerField = function() {
  scout.PlannerField.parent.call(this);
  this._addAdapterProperties(['planner']);
};
scout.inherits(scout.PlannerField, scout.FormField);

scout.PlannerField.prototype._render = function($parent) {
  this.addContainer($parent, 'planner-field');
  this.addLabel();
  this.addStatus();
  if (this.planner) {
    this._renderPlanner();
  }
};

/**
 * Will also be called by model adapter on property change event
 */
scout.PlannerField.prototype._renderPlanner = function() {
  this.planner.render(this.$container);
  this.addField(this.planner.$container);
};

scout.PlannerField.prototype._removePlanner = function(oldPlanner) {
  oldPlanner.remove();
  this._removeField();
};

scout.PlannerField.prototype._renderSplitterPosition = function() {};
