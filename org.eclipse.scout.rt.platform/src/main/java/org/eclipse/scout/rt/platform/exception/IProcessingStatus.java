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
package org.eclipse.scout.rt.platform.exception;

import org.eclipse.scout.rt.platform.status.IStatus;

public interface IProcessingStatus extends IStatus {

  /** a fatal error */
  int FATAL = 0x10000000;

  /**
   * A title that may be used as message header for the status. {@link #getMessage()} is composed of {@link #getTitle()}
   * and {@link #getBody()}.
   */
  String getTitle();

  /**
   * The body of the message. {@link #getMessage()} is composed of {@link #getTitle()} and {@link #getBody()}.
   */
  String getBody();

  Throwable getException();

}
