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
package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;

/**
 * The filter accepts all checked rows
 */
class CheckedNodesFilter implements ITreeNodeFilter {

  public CheckedNodesFilter() {
  }

  @Override
  public boolean accept(ITreeNode node, int level) {
    if (node.isChecked()) {
      return true;
    }
    return false;
  }

}
