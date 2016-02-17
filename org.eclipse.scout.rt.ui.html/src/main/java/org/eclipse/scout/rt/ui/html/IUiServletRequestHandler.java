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
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Interface for handlers contributing to the {@link UiServlet}.
 */
@ApplicationScoped
public interface IUiServletRequestHandler {

  /**
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  boolean handlePost(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

  /**
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  boolean handleGet(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
