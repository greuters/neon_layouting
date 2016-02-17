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
describe("GroupBox", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(model, parent) {
    var field = new scout.GroupBox();
    field.getForm = function() {
      return createSimpleModel('Form', session);
    };

    model.session = session;
    field.init(model);
    return field;
  }

  describe("_render", function() {
    var groupBox, model = {
        id: '2',
        label: "fooBar",
        gridData: {
          x: 0,
          y: 0
        },
        parent: {
          objectType: 'GroupBox',
          addChild: function() {}
        }
      };

    beforeEach(function() {
      groupBox = createField(model);
    });

    it("adds group-box div when label is set", function() {
      groupBox._render($('#sandbox'));
      expect($('#sandbox')).toContainElement('div.group-box');
      expect($('#sandbox')).toContainElement('div.group-box-title');
    });
  });

  describe("test predefined height and width in pixel", function() {
    var form, formAdapter, formController, rootGroupBox, model = $.extend(createSimpleModel('GroupBox', session), {
        id: '3',
        label: "fooBar",
        gridData: {
          x: 0,
          y: 0,
          widthInPixel: 97,
          heightInPixel: 123
        },
        mainBox: true
      });

    beforeEach(function() {
      form = helper.createFormModel();
      session.desktop = new scout.Desktop();
      formController = new scout.FormController(form.parent, session);
      rootGroupBox = model;
      rootGroupBox.fields = [];
      form.rootGroupBox = rootGroupBox.id;
      form.owner = session.rootAdapter.id;
      formAdapter = createAdapter(form, session, [rootGroupBox]);
      session.desktop.$container = $('#sandbox');
    });

    it("adds group-box div when label is set", function() {
      formController._renderDialog(formAdapter);
      expect(formAdapter.rootGroupBox.$container.cssHeight()).toBe(123);
      expect(formAdapter.rootGroupBox.$container.cssWidth()).toBe(97);
    });
  });

});
