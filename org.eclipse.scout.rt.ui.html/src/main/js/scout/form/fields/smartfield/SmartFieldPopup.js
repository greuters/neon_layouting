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
scout.SmartFieldPopup = function() {
  scout.SmartFieldPopup.parent.call(this);
};
scout.inherits(scout.SmartFieldPopup, scout.Popup);

scout.SmartFieldPopup.prototype._init = function(options) {
  options.installFocusContext = false;
  scout.SmartFieldPopup.parent.prototype._init.call(this, options);
  this._field = options.field;
};

scout.SmartFieldPopup.prototype._createLayout = function() {
  return new scout.SmartFieldPopupLayout(this);
};

scout.SmartFieldPopup.prototype._render = function($parent) {
  this.$container = $parent
    .appendDiv('smart-field-popup')
    .on('mousedown', this._onContainerMouseDown.bind(this));

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.htmlComp.validateRoot = true;
};

scout.SmartFieldPopup.prototype._renderProposalChooser = function(proposalChooser) {
  proposalChooser.render(this.$container);
  proposalChooser.setParent(this);
  this.revalidateLayout();
};

/**
 * @override Popup.js
 */
scout.SmartFieldPopup.prototype._onMouseDown = function(event) {
  // when user clicks on SmartField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.SmartFieldPopup.parent.prototype._onMouseDown.call(this, event);
};

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the SmartField popup is closed. It also prevents the focus blur
 * event on the SmartField input-field.
 */
//TODO [5.2] cgu, awe: this is not required by the cell editor anymore, but we cannot remove it either because mouse down on a row would immediately close the popup, why?
scout.SmartFieldPopup.prototype._onContainerMouseDown = function(event) {
  // when user clicks on proposal popup with table or tree (prevent default,
  // so input-field does not lose the focus, popup will be closed by the
  // proposal chooser impl.
  return false;
};

scout.SmartFieldPopup.prototype._onAnchorScroll = function(event) {
  this.position();
};
