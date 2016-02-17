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
package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.ui.html.json.AbstractEventFilter;

public class TreeEventFilter extends AbstractEventFilter<TreeEvent, TreeEventFilterCondition> {

  private ITree m_source;

  public TreeEventFilter(ITree source) {
    m_source = source;
  }

  /**
   * Computes whether the event should be returned to the GUI. There are three cases:
   * <ul>
   * <li>No filtering happens: The original event is returned. <br>
   * This is the case if the conditions don't contain an event with the same type as the original event.</li>
   * <li>Partial filtering happens (if condition.checkNodes is true): A new event with a subset of tree nodes is
   * returned.<br>
   * This is the case if the conditions contain a relevant event but has different nodes than the original event.
   * <li>Complete filtering happens: Null is returned.<br>
   * This is the case if the event should be filtered for every node in the original event
   */
  @Override
  public TreeEvent filter(TreeEvent event) {
    for (TreeEventFilterCondition condition : getConditions()) {
      if (condition.getType() == event.getType()) {
        if (!condition.checkNodes()) {
          return null;
        }

        Collection<ITreeNode> nodes = new ArrayList<>(event.getNodes());
        nodes.removeAll(condition.getNodes());
        if (nodes.size() == 0) {
          // Event should be ignored if no nodes remain or if the event contained no nodes at all
          return null;
        }

        return new TreeEvent(m_source, event.getType(), event.getCommonParentNode(), nodes);
      }
    }
    return event;
  }
}
