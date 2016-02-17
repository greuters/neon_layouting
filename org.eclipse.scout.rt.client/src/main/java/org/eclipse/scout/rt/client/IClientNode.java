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
package org.eclipse.scout.rt.client;

import java.util.UUID;

/**
 * Represents a client node which typically hosts multiple client sessions.
 *
 * @since 5.2
 */
public interface IClientNode {

  /**
   * Unique ID to identify this node.
   */
  String ID = UUID.randomUUID().toString();
}
