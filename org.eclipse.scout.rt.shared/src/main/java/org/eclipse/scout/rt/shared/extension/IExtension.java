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
package org.eclipse.scout.rt.shared.extension;

/**
 * Super interface of all Scout model extensions.<br>
 * Extensions can be applied to all {@link IExtensibleObject}s.<br>
 * Use the {@link IExtensionRegistry} service to register your extensions.
 *
 * @since 4.2
 */
public interface IExtension<OWNER extends IExtensibleObject> {

  /**
   * @return the owner of the extension (the object that is extended).
   */
  OWNER getOwner();
}
