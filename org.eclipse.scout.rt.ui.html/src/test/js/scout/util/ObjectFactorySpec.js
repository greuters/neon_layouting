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
/* global LocaleSpecHelper*/
describe('ObjectFactory', function() {

  beforeEach(function() {
    // Needed because some model adapters make JSON calls during initialization (e.g. Calendar.js)
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    scout.device.type = scout.Device.Type.DESKTOP;
  });

  /**
   * This function is used to create a special-model when a model-adapter requires one.
   * Normally a generic model with id and objectType is sufficient, but some adapters require a more complex
   * model in order to make this test succeed. Remember you must add the additional adapter models to the
   * adapaterDataCache of the Session.
   */
  function createModel(session, id, objectType) {
    var model = createSimpleModel(objectType, session, id);
    if ('Menu.NavigateUp' === objectType || 'Menu.NavigateDown' === objectType) {
      var outlineId = 'outline' + id;
      model.outline = outlineId;
      session._adapterDataCache[outlineId] = {
        id: outlineId,
        objectType: 'Outline'
      };
    } else if ('Calendar' === objectType) {
      model.displayMode = 3;
      model.selectedDate = '2015-04-06 00:00:00.000';
    } else if ('Form' === objectType) {
      model.displayHint = 'view';
    } else if ('TextColumnUserFilter' === objectType) {
      model.table = {};
      model.column = {};
      model.calculate = function() {};
    } else if ('AggregateTableControl' === objectType) {
      model.table = {
        columns: [],
        on: function(){}
      };
    } else if ('TabBox' === objectType) {
      var tabItemId = 'tabItem' + id;
      model.selectedTab = 0;

      model.tabItems = [tabItemId];
      session._adapterDataCache[tabItemId] = {
        id: tabItemId,
        objectType: 'TabItem',
        getForm: function() {
          return createSimpleModel('Form', session);
        }
      };
    } else if ('ButtonAdapterMenu' === objectType) {
      model.button = {
        on: function() {}
      };
    }

    if ('GroupBox' === objectType || 'TabItem' === objectType) {
      model.getForm = function() {
        return createSimpleModel('Form', session);
      };
    }

    return model;
  }

  it('creates objects which are registered in scout.objectFactories', function() {
    setFixtures(sandbox());
    var session = new scout.Session($('#sandbox'), '1.1');
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);

    // When this test fails with a message like 'TypeError: scout.[ObjectType] is not a constructor...'
    // you should check if the required .js File is registered in SpecRunnerMaven.html.
    var i, model, factory, object, modelAdapter, objectType;
    for (objectType in scout.objectFactories) {
      model = createModel(session, i, objectType);
      object = scout.objectFactories[objectType](model);
      object.init(model);
      session.registerModelAdapter(object);
      modelAdapter = session.getModelAdapter(model.id);
      expect(modelAdapter).toBe(object);
    }
  });

  it('distinguishes between mobile and regular objects', function() {
    scout.device.type = scout.Device.Type.DESKTOP;
    var objectType, object = scout.objectFactories.Desktop();
    expect(object instanceof scout.Desktop).toBe(true);
    scout.device.type = scout.Device.Type.MOBILE;
    object = scout.objectFactories.Desktop();
    expect(object instanceof scout.MobileDesktop).toBe(true);
  });

});
