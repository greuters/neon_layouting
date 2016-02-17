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
/* global FormSpecHelper */
describe("WrappedForm", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(model) {
    var field = new scout.WrappedFormField();
    field.init(model);
    return field;
  }

  function createModel() {
    return helper.createFieldModel('WrappedFormField');
  }

  describe("mandatory indicator", function() {

    // Must not contain an indicator to prevent a double indicator if the first field is mandatory too
    it("does not exist", function() {
      var model = createModel();
      model.mandatory = true;
      var field = createField(model);
      field.render(session.$entryPoint);

      expect(field.$mandatory).toBeUndefined();
    });

  });

});
