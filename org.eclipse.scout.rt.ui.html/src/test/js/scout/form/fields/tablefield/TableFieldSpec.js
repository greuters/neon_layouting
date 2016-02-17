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
/* global TableSpecHelper, FormSpecHelper */
describe("TableField", function() {
  var session;
  var helper;
  var tableHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    tableHelper = new TableSpecHelper(session);
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createModel() {
    return helper.createFieldModel('TableField');
  }

  function createTableFieldWithTable() {
    var tableModel = tableHelper.createModelFixture(2, 2);
    return createTableField(tableModel);
  }

  function createTableField(tableModel) {
    var tableFieldModel = createModel();
    if (tableModel) {
      tableFieldModel.table = tableModel.id;
    }
    tableFieldModel.owner = session.rootAdapter.id;
    return createAdapter(tableFieldModel, session, tableModel);
  }

  describe("property table", function() {

    it("shows (renders) the table if the value is set", function() {
      var tableModel = tableHelper.createModelFixture(2, 2);
      var tableField = createTableField();
      tableField.render(session.$entryPoint);

      expect(tableField.table).toBeUndefined();
      var message = {
        adapterData : createAdapterData(tableModel),
        events: [createPropertyChangeEvent(tableField, {table: tableModel.id})]
      };
      session._processSuccessResponse(message);

      expect(tableField.table.rendered).toBe(true);

      //Field is necessary for the FormFieldLayout
      expect(tableField.$field).toBeTruthy();
    });

    it("destroys the table if value is changed to ''", function() {
      var tableField = createTableFieldWithTable();
      var table = tableField.table;
      tableField.render(session.$entryPoint);

      expect(table.rendered).toBe(true);
      var message = {
        events: [createPropertyChangeEvent(tableField, {table: ''})]
      };
      session._processSuccessResponse(message);

      expect(tableField.table).toBeFalsy();
      expect(tableField.$field).toBeFalsy();
      expect(table.rendered).toBe(false);
      expect(session.getModelAdapter(table.id)).toBeFalsy();
    });

    it("if table is global, only removes the table but does not destroy it if value is changed to ''", function() {
      var tableModel = tableHelper.createModelFixture(2, 2);
      tableModel.owner = session.rootAdapter.id;
      var tableField = createTableField(tableModel);
      var table = tableField.table;
      tableField.render(session.$entryPoint);

      expect(table.rendered).toBe(true);
      var message = {
        events: [createPropertyChangeEvent(tableField, {table: ''})]
      };
      session._processSuccessResponse(message);

      // Table is unlinked with table field but still exists
      expect(tableField.table).toBeFalsy();
      expect(tableField.$field).toBeFalsy();
      expect(table.rendered).toBe(false);
      expect(session.getModelAdapter(table.id)).toBeTruthy();
    });

    it("table gets class 'field' to make it work with the form field layout", function() {
      var tableField = createTableFieldWithTable();
      tableField.render(session.$entryPoint);

      expect(tableField.table.$container).toHaveClass('field');
    });

    it("table gets class 'field' to make it work with the form field layout (also when loaded by property change event)", function() {
      var tableModel = tableHelper.createModelFixture(2, 2);
      var tableField = createTableField();
      tableField.render(session.$entryPoint);

      expect(tableField.table).toBeUndefined();
      var message = {
          adapterData : createAdapterData(tableModel),
          events: [createPropertyChangeEvent(tableField, {table: tableModel.id})]
      };
      session._processSuccessResponse(message);

      expect(tableField.table.$container).toHaveClass('field');
    });
  });
});
