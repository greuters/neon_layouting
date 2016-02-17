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
package org.eclipse.scout.rt.ui.html.cache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.ui.html.UiServlet;

/**
 * An {@link IHttpResponseInterceptor} that adds a HTTP header from the given key/value pair to the response.
 */
public class HttpResponseHeaderContributor implements IHttpResponseInterceptor {
  private static final long serialVersionUID = 1L;

  private final String m_name;
  private final String m_value;

  public HttpResponseHeaderContributor(String name, String value) {
    m_name = name;
    m_value = value;
  }

  public String getName() {
    return m_name;
  }

  public String getValue() {
    return m_value;
  }

  @Override
  public void intercept(UiServlet servlet, HttpServletRequest httpReq, HttpServletResponse httpResp) {
    httpResp.setHeader(m_name, m_value);
  }
}
