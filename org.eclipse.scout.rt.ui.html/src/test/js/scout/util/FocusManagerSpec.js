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
/* global FormSpecHelper, FocusManagerSpecHelper */
jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
describe('scout.Focusmanager', function() {
  var session, formHelper, focusHelper, form;

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    session = sandboxSession();
    session.init();
    formHelper = new FormSpecHelper(session);
    focusHelper = new FocusManagerSpecHelper();
    jasmine.clock().install();
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('isSelectableText', function() {

    it('must return true for disabled text-fields', function() {
      var $textField = $('<input>')
        .attr('type', 'text')
        .attr('disabled', 'disabled');
      expect(scout.focusUtils.isSelectableText($textField)).toBe(true);
    });

  });

  describe('validateFocus', function() {

    it('When nothing else is focusable, focus must be on the Desktop (=sandbox)', function() {
      session.focusManager.validateFocus();
      var sandbox = $('#sandbox')[0];
      expect(document.activeElement).toBe(sandbox);
    });

    describe('with forms:', function() {

      var form;
      beforeEach(function() {
        form = formHelper.createFormXFields(4, false);
        form.render(session.$entryPoint);
      });

      afterEach(function() {
        form.remove();
        form = null;
      });

      /**
       * Because form is not a dialog, it does not install its own focus-context
       * but uses the focus-context of the Desktop (=sandbox) instead.
       */
      it('Focus-context must install listeners on its $container', function() {
        expect(focusHelper.handlersRegistered(session.$entryPoint)).toBe(true);
      });

      it('Focus must be on the 1st form-field when form is rendered', function() {
        var $firstField = form.rootGroupBox.fields[0].$field;
        expect($firstField).toBeFocused();
      });

      it('FocusContext must remember the last focused element', function() {
        var $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus();
        expect($secondField).toBeFocused();

        expect(session.focusManager._findActiveContext()._lastValidFocusedElement).toBe($secondField[0]);
      });

      it('A new FocusContext must be created when a form is opened as dialog', function() {
        var $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus(); // must be remembered by focus-context

        var sandboxContext = session.focusManager._findActiveContext();
        expect(sandboxContext.$container).toBe(session.$entryPoint);

        var dialog = formHelper.createFormXFields(2, true);
        dialog.render(session.$entryPoint);

        expect(session.focusManager._focusContexts.length).toBe(2);

        var dialogContext = session.focusManager._findActiveContext();
        expect(dialogContext.$container).toBe(dialog.$container);

        // focus-context must install handlers on form $container
        expect(focusHelper.handlersRegistered(dialog.$container)).toBe(true);

        // must remember last focused field of first focus-context
        expect(sandboxContext._lastValidFocusedElement).toBe($secondField[0]);
      });

      it('Must focus another valid field if the focused field is removed', function() {
        var $firstField = form.rootGroupBox.fields[0].$field,
          $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.remove();
        expect($secondField).toBeFocused();
      });


      it('Must focus another valid field if the focused field is hidden', function() {
        var $firstField = form.rootGroupBox.fields[0].$field,
        $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.setVisible(false);
        expect($secondField).toBeFocused();
      });

    });

  });

});
