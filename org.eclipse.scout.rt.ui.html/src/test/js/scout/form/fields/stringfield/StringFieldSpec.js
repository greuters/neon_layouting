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
describe("StringField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createField(model) {
    var field = new scout.StringField();
    field.init(model);
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  describe("Check if field is switched to password field if inputMasked is true", function() {
    var field;

    beforeEach(function() {
      field = createField(createModel());
    });

    it("set input masked", function() {
      field.inputMasked = true;
      field.render(session.$entryPoint);
      expect(field.$field.attr('type')).toBe('password');
    });

    it("set input not masked", function() {
      field.inputMasked = false;
      field.render(session.$entryPoint);
      expect(field.$field.attr('type')).toBe('text');
    });

  });

  describe("insertText", function() {
    var field;

    beforeEach(function() {
      field = createField(createModel());
    });

    it("expects empty field at the beginning", function() {
      field.render(session.$entryPoint);
      expect(field.$field[0].value).toBe('');
    });

    it("inserts text into an empty field", function() {
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('Test1');
    });

    it("appends text to the previous value (if no text is selected)", function() {
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'ABC2'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('Test1ABC2');
    });

    it("replaces selection #1 (if part of the text is selected, selection does not start at the beginning)", function() {
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      field.$field[0].selectionStart = 2;
      field.$field[0].selectionEnd = 4;
      message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'sten2'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('Testen21');
    });

    it("replaces selection #2 (if part of the text is selected, start at the beginning)", function() {
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      field.$field[0].selectionStart = 0;
      field.$field[0].selectionEnd = 4;
      message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'ABC2'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('ABC21');
    });

    it("replaces selection #3 (if whole content is selected)", function() {
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      field.$field[0].select();
      message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'ABC2'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('ABC2');
    });

    it("sends display text changed to server using accept text", function() {
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      var event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);

      message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'ABC2'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('Test1ABC2');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(2);
      event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1ABC2', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("sends display text changed to server using accept text, twice, if updateDisplayTextOnModify=true", function() {
      field.updateDisplayTextOnModify = true;
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      var events = [];
      // displayTextChanged needs to be sent twice, with whileTyping = true and = false
      events[0] = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1', whileTyping: true
      });
      events[1] = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);

      message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'ABC2'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('Test1ABC2');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(2);
      events = [];
      events[0] = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1ABC2', whileTyping: true
      });
      events[1] = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1ABC2', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
    });

  });

  describe("displayTextChanged must always be sent to server at the end of input, if at least one change has been was made", function() {
    var field;

    beforeEach(function() {
      field = createField(createModel());
    });

    it("updateDisplayTextOnModify = true, with changed text", function() {
      field.updateDisplayTextOnModify = true;
      field.render(session.$entryPoint);
      field.$field.val('Test1');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      var event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1', whileTyping: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
      field.$field.triggerBlur();
      sendQueuedAjaxCalls();
      event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test1', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("updateDisplayTextOnModify = false, with changed text", function() {
      field.updateDisplayTextOnModify = false;
      field.render(session.$entryPoint);
      field.$field.val('Test2');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      var event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test2', whileTyping: true
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
      field.$field.triggerBlur();
      sendQueuedAjaxCalls();
      event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test2', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("updateDisplayTextOnModify = true, then property change to updateDisplayTextOnModify = false, with changed text", function() {
      field.updateDisplayTextOnModify = true;
      field.render(session.$entryPoint);
      field.$field.val('Test3');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      var event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test3', whileTyping: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
      event = createPropertyChangeEvent(field, {
        "updateDisplayTextOnModify": false
      });
      field.onModelPropertyChange(event);
      field.$field.triggerBlur();
      sendQueuedAjaxCalls();
      event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test3', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("updateDisplayTextOnModify = true, w/o changed text", function() {
      field.updateDisplayTextOnModify = true;
      field.render(session.$entryPoint);
      field.displayText = 'Test4'; // fake previous display text
      field.$field.val('Test4');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      var event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test4', whileTyping: true
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
      field.$field.triggerBlur();
      sendQueuedAjaxCalls();
      event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test4', whileTyping: false
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
    });

    it("updateDisplayTextOnModify = false, w/o changed text", function() {
      field.updateDisplayTextOnModify = false;
      field.render(session.$entryPoint);
      field.displayText = 'Test5'; // fake previous display text
      field.$field.val('Test5');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      var event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test5', whileTyping: true
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
      field.$field.triggerBlur();
      sendQueuedAjaxCalls();
      event = new scout.Event(field.id, 'displayTextChanged', {
        displayText: 'Test5', whileTyping: false
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
    });
  });

});
