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
/**
 * Provides methods to "sync" properties 'keyStrokes' and 'menus' on a model-adapter.
 * This class is basically required because Table, Tree and FormField have no common base-class
 * but all require support for keyStrokes and menus.
 */
scout.KeyStrokeSupport = function(adapter) {
  this._adapter = adapter;
};

scout.KeyStrokeSupport.prototype.syncKeyStrokes = function(newKeyStrokes, oldKeyStrokes) {
  this._updateKeyStrokes(newKeyStrokes, oldKeyStrokes);
  this._adapter.keyStrokes = newKeyStrokes;
};

scout.KeyStrokeSupport.prototype.syncMenus = function(newMenus, oldMenus) {
  this._updateKeyStrokes(newMenus, oldMenus);
  this._adapter.menus = newMenus;
};

scout.KeyStrokeSupport.prototype._updateKeyStrokes = function(newKeyStrokes, oldKeyStrokes) {
  // 1st: unregister all old key-strokes
  if (oldKeyStrokes && Array.isArray(oldKeyStrokes)) {
    oldKeyStrokes.forEach(function(keyStroke) {
      this.keyStrokeContext.unregisterKeyStroke(keyStroke);
    }, this._adapter);
  }
  // 2nd: register all new key-strokes
  if (newKeyStrokes && Array.isArray(newKeyStrokes)) {
    newKeyStrokes.forEach(function(keyStroke) {
      this.keyStrokeContext.registerKeyStroke(keyStroke);
    }, this._adapter);
  }
};
