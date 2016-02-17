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
describe('Desktop', function() {

  var session, desktop = new scout.Desktop();

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    desktop.viewButtons = [];
    desktop.session = session;
  });

  describe('_addNullOutline', function() {

    it('should add null-outline when outline of model doesn\'t exist', function() {
      var ovb, outline = null;
      desktop._addNullOutline(outline);
      expect(desktop.viewButtons.length).toBe(1);
      ovb = desktop.viewButtons[0];
      expect(desktop.outline).toBe(ovb.outline);
      expect(ovb.visibleInMenu).toBe(false);
    });

    it('shouldn\'t do anything when model already has an outline', function() {
      var outline = {};
      desktop.outline = outline;
      desktop._addNullOutline(outline);
      expect(desktop.outline).toBe(outline);
      expect(desktop.viewButtons.length).toBe(0);
    });

  });

});
