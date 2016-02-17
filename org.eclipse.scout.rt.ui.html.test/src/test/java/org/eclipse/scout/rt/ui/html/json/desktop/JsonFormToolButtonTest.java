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
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.FormToolButton;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonFormToolButtonTest {
  private IUiSession m_uiSession;

  @Before
  public void before() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Form disposal is controlled by the model and must not be triggered by the parent
   */
  @Test
  public void testPreventFormDisposal() {
    FormToolButton button = new FormToolButton();
    FormWithOneField form = new FormWithOneField();
    form.start();
    button.setForm(form);

    JsonFormToolButton<IFormToolButton<IForm>> jsonFormToolButton = m_uiSession.createJsonAdapter(button, null);

    assertNotNull(jsonFormToolButton.getAdapter(form));
    jsonFormToolButton.dispose();

    // Form has not been closed yet -> must still be registered
    assertNotNull(jsonFormToolButton.getAdapter(form));

    form.doClose();
    assertNull(jsonFormToolButton.getAdapter(form));
  }
}
