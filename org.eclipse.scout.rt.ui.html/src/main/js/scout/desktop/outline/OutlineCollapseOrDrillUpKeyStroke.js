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
scout.OutlineCollapseOrDrillUpKeyStroke = function(tree) {
  scout.OutlineCollapseOrDrillUpKeyStroke.parent.call(this, tree);
  this.which = [scout.keys.SUBTRACT];
  this.renderingHints.text = '-';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return (event._treeCurrentNode.expanded ? event._$treeCurrentNode : null);
  }.bind(this);
};
scout.inherits(scout.OutlineCollapseOrDrillUpKeyStroke, scout.AbstractOutlineNavigationKeyStroke);

scout.OutlineCollapseOrDrillUpKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TreeCollapseOrDrillUpKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && event._treeCurrentNode;
};

scout.OutlineCollapseOrDrillUpKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if (currentNode.expanded) {
    this.field.collapseNode(currentNode);
    return null;
  } else if (currentNode.parentNode) {
    return currentNode.parentNode;
  }
};
