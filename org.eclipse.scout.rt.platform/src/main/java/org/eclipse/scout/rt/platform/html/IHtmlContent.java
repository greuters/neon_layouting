/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html;

/**
 * Marker Interface for any Html Content that may contain bind variables.
 */
public interface IHtmlContent extends IHtmlBind {

  String toEncodedHtml();

  String toPlainText();

  HtmlBinds getBinds();

  void setBinds(HtmlBinds binds);

}
