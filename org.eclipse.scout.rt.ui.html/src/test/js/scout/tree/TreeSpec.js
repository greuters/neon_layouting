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
describe("Tree", function() {
  var session;

  //TODO NBU-> write test for dynamical keystroke(menu) update.

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
    $.fx.off = true;
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function createModelFixture(nodeCount, depth, expanded) {
    return createModel(createModelNodes(nodeCount, depth, expanded));
  }

  function createModel(nodes) {
    var model = createSimpleModel('Tree', session);

    if (nodes) {
      model.nodes = nodes;
    }
    model.enabled = true;
    return model;
  }

  function createModelNode(id, text, position) {
    return {
      "id": id,
      "text": text,
      "childNodeIndex": position ? position : 0,
      "enabled": true,
      "checked": false
    };
  }

  function createModelNodes(nodeCount, depth, expanded) {
    return createModelNodesInternal(nodeCount, depth, expanded);
  }

  function createModelNodesInternal(nodeCount, depth, expanded, parentNode) {
    if (!nodeCount) {
      return;
    }

    var nodes = [],
      nodeId;
    if (!depth) {
      depth = 0;
    }
    for (var i = 0; i < nodeCount; i++) {
      nodeId = i;
      if (parentNode) {
        nodeId = parentNode.id + '_' + nodeId;
      }
      nodes[i] = createModelNode(nodeId, 'node ' + i);
      nodes[i].expanded = expanded;
      if (depth > 0) {
        nodes[i].childNodes = createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
      }
    }
    return nodes;
  }

  function createTree(model) {
    var tree = new scout.Tree();
    tree.init(model);
    return tree;
  }

  function findAllNodes(tree) {
    return tree.$container.find('.tree-node');
  }

  function createNodeExpandedEvent(model, nodeId, expanded) {
    return {
      target: model.id,
      nodeId: nodeId,
      expanded: expanded,
      type: 'nodeExpanded'
    };
  }

  function createNodesSelectedEvent(model, nodeIds) {
    return {
      target: model.id,
      nodeIds: nodeIds,
      type: 'nodesSelected'
    };
  }

  function createNodesInsertedEvent(model, nodes, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodes: nodes,
      type: 'nodesInserted'
    };
  }

  function createNodesInsertedEventTopNode(model, nodes) {
    return {
      target: model.id,
      nodes: nodes,
      type: 'nodesInserted'
    };
  }

  function createNodesDeletedEvent(model, nodeIds, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodeIds: nodeIds,
      type: 'nodesDeleted'
    };
  }

  function createAllChildNodesDeletedEvent(model, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      type: 'allChildNodesDeleted'
    };
  }

  function createNodeChangedEvent(model, nodeId) {
    return {
      target: model.id,
      nodeId: nodeId,
      type: 'nodeChanged'
    };
  }

  function createNodesUpdatedEvent(model, nodes) {
    return {
      target: model.id,
      nodes: nodes,
      type: 'nodesUpdated'
    };
  }

  function createTreeEnabledEvent(model, enabled) {
    return {
      target: model.id,
      type: 'property',
      properties: {
        enabled: enabled
      }
    };
  }

  describe("creation", function() {
    it("adds nodes", function() {

      var model = createModelFixture(1);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      expect(findAllNodes(tree).length).toBe(1);
    });

    it("does not add notes if no nodes are provided", function() {

      var model = createModelFixture();
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      expect(findAllNodes(tree).length).toBe(0);
    });
  });

  describe("insertNodes", function() {
    var model;
    var tree;
    var node0;
    var node1;
    var node2;

    beforeEach(function() {
      model = createModelFixture(3, 1, true);
      tree = createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
    });

    describe("inserting a child", function() {

      it("updates model", function() {
        var newNode0Child3 = createModelNode('0_3', 'newNode0Child3', 3);
        expect(tree.nodes.length).toBe(3);
        expect(Object.keys(tree.nodesMap).length).toBe(12);

        tree.insertNodes([newNode0Child3], node0);
        expect(node0.childNodes.length).toBe(4);
        expect(node0.childNodes[3].text).toBe(newNode0Child3.text);
        expect(Object.keys(tree.nodesMap).length).toBe(13);
      });

      it("updates html document if parent is expanded", function() {
        tree.render(session.$entryPoint);

        var newNode0Child3 = createModelNode('0_3', 'newNode0Child3', 3);
        expect(findAllNodes(tree).length).toBe(12);

        tree.insertNodes([newNode0Child3], node0);
        expect(findAllNodes(tree).length).toBe(13);
        expect(node0.childNodes[3].$node.text()).toBe(newNode0Child3.text);
      });

      it("updates html document on specific position", function() {
        tree.render(session.$entryPoint);

        var newNode0Child3 = createModelNode('0_3', 'newNode0Child3', 2);
        var newNode0Child4 = createModelNode('0_4', 'newNode0Child4', 3);
        expect(findAllNodes(tree).length).toBe(12);

        tree.insertNodes([newNode0Child3, newNode0Child4], node0);
        expect(findAllNodes(tree).length).toBe(14);
        expect(node0.childNodes[2].$node.text()).toBe(newNode0Child3.text);
        expect(node0.childNodes[3].$node.text()).toBe(newNode0Child4.text);
        expect(node0.childNodes[3].$node.attr('data-level')).toBe('1');
        expect(node0.childNodes[3].$node.next().attr('data-level')).toBe('1');
        expect(node0.childNodes[3].$node.next().text()).toBe('node 2');

        var newNode1Child3 = createModelNode('1_3', 'newNode1Child3', 1);
        var newNode1Child4 = createModelNode('1_4', 'newNode1Child4', 2);

        tree.insertNodes([newNode1Child3, newNode1Child4]);
        expect(findAllNodes(tree).length).toBe(16);
        expect(tree.nodes[1].$node.prev().text()).toBe('node 2');
        expect(tree.nodes[1].$node.prev().attr('data-level')).toBe('1');
        expect(tree.nodes[1].$node.text()).toBe(newNode1Child3.text);
        expect(tree.nodes[1].$node.attr('data-level')).toBe('0');
        expect(tree.nodes[2].$node.text()).toBe(newNode1Child4.text);
        expect(tree.nodes[2].$node.attr('data-level')).toBe('0');
        expect(tree.nodes[2].$node.next().attr('data-level')).toBe('0');
        expect(tree.nodes[2].$node.next().text()).toBe('node 1');
      });
    });

    it("only updates the model if parent is collapsed", function() {
      node0.expanded = false;
      tree.render(session.$entryPoint);

      var newNode0Child3 = createModelNode('0_3', 'newNode0Child3', 3);
      expect(findAllNodes(tree).length).toBe(9);

      tree.insertNodes([newNode0Child3], node0);
      //Check that the model was updated correctly
      expect(node0.childNodes.length).toBe(4);
      expect(node0.childNodes[3].text).toBe(newNode0Child3.text);
      expect(Object.keys(tree.nodesMap).length).toBe(13);

      //Check that no dom manipulation happened
      expect(findAllNodes(tree).length).toBe(9);
      expect(node0.childNodes[3].$node).toBeUndefined();
    });

    it("expands the parent if parent.expanded = true and the new inserted nodes are the first child nodes", function() {
      model = createModelFixture(3, 0, true);
      tree = createTree(model);
      node0 = model.nodes[0];
      node1 = model.nodes[1];
      node2 = model.nodes[2];
      tree.render(session.$entryPoint);

      var newNode0Child3 = createModelNode('0_3', 'newNode0Child3');
      var $node0 = node0.$node;
      // Even tough the nodes were created with expanded=true, the $node should not have
      // been rendered as expanded (because it has no children)
      expect($node0).not.toHaveClass('expanded');
      expect(findAllNodes(tree).length).toBe(3);

      tree.insertNodes([newNode0Child3], node0);
      expect(findAllNodes(tree).length).toBe(4);
      expect(node0.childNodes[0].$node.text()).toBe(newNode0Child3.text);
      expect($node0).toHaveClass('expanded');
    });

  });

  describe("updateNodes", function() {
    var model;
    var tree;
    var node0;
    var child0;

    beforeEach(function() {
      model = createModelFixture(3, 3, false);
      tree = createTree(model);
      node0 = tree.nodes[0];
      child0 = node0.childNodes[0];
    });

    describe("enabled update", function() {
      var child0Update;

      beforeEach(function() {
        child0Update = {
          id: child0.id,
          enabled: false
        };
        tree.checkable = true;
      });

      it("updates the enabled state of the model node", function() {
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);
        expect(child0.enabled).toBe(false);
      });

      it("updates the enabled state of the html node, if visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
        tree.setNodeExpanded(node0, true);
        expect(child0.$node.isEnabled()).toBe(true);
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);

        // Expect node and $node to be disabled
        expect(child0.enabled).toBe(false);
        expect(child0.$node.isEnabled()).toBe(false);
      });

      it("updates the enabled state of the html node after expansion, if not visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
        tree.setNodeExpanded(node0, true);
        expect(child0.$node.isEnabled()).toBe(true);

        // Make sure child0 is not visible anymore
        tree.setNodeExpanded(node0, false);
        expect(child0.$node).toBeFalsy();

        tree.updateNodes([child0Update]);

        // Mode state needs to be updated, $node is still node visible
        expect(child0.enabled).toBe(false);
        expect(child0.$node).toBeFalsy();

        // Expand node -> node gets visible and needs to be disabled
        tree.setNodeExpanded(node0, true);
        expect(child0.$node.isEnabled()).toBe(false);
      });
    });

    describe("enabled update on checkable tree", function() {
      var child0Update;

      function $checkbox(node) {
        return node.$node.children('.tree-node-checkbox')
          .children('.check-box');
      }

      beforeEach(function() {
        child0Update = {
          id: child0.id,
          enabled: false
        };
        tree.checkable = true;
      });

      it("updates the enabled state of the model node", function() {
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);
        expect(child0.enabled).toBe(false);
      });

      it("updates the enabled state of the html node, if visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
        tree.setNodeExpanded(node0, true);
        expect($checkbox(child0).isEnabled()).toBe(true);

        tree.updateNodes([child0Update]);

        // Expect node and $node to be disabled
        expect(child0.enabled).toBe(false);
        expect($checkbox(child0).isEnabled()).toBe(false);
      });

      it("updates the enabled state of the html node after expansion, if not visible", function() {
        // Render tree and make sure child0 is visible
        tree.render(session.$entryPoint);
        tree.setNodeExpanded(node0, true);
        expect($checkbox(child0).isEnabled()).toBe(true);

        // Make sure child0 is not visible anymore
        tree.setNodeExpanded(node0, false);
        expect(child0.$node).toBeFalsy();
        expect(child0.enabled).toBe(true);

        tree.updateNodes([child0Update]);

        // Mode state needs to be updated, $node is still node visible
        expect(child0.enabled).toBe(false);
        expect(child0.$node).toBeFalsy();

        // Expand node -> node gets visible and needs to be disabled
        tree.setNodeExpanded(node0, true);
        expect($checkbox(child0).isEnabled()).toBe(false);
      });
    });
  });

  describe("deleteNodes", function() {
    var model;
    var tree;
    var node0;
    var node1;
    var node2;

    beforeEach(function() {
      // A large tree is used to properly test recursion
      model = createModelFixture(3, 2, true);
      tree = createTree(model);
      node0 = tree.nodes[0];
      node1 = tree.nodes[1];
      node2 = tree.nodes[2];
    });

    describe("deleting a child", function() {

      it("updates model", function() {
        var node2Child0 = node2.childNodes[0];
        var node2Child1 = node2.childNodes[1];
        expect(tree.nodes.length).toBe(3);
        expect(tree.nodes[0]).toBe(node0);
        expect(Object.keys(tree.nodesMap).length).toBe(39);

        tree.deleteNodes([node2Child0], node2);
        expect(tree.nodes[2].childNodes.length).toBe(2);
        expect(tree.nodes[2].childNodes[0]).toBe(node2Child1);
        expect(Object.keys(tree.nodesMap).length).toBe(35);
      });

      it("updates html document", function() {
        tree.render(session.$entryPoint);

        var node2Child0 = node2.childNodes[0];

        expect(findAllNodes(tree).length).toBe(39);
        expect(node2Child0.$node).toBeDefined();

        tree.deleteNodes([node2Child0], node2);
        expect(findAllNodes(tree).length).toBe(35);
        expect(node2Child0.$node).toBeUndefined();

        expect(node0.$node).toBeDefined();
        expect(node0.childNodes[0].$node).toBeDefined();
        expect(node0.childNodes[1].$node).toBeDefined();
        expect(node0.childNodes[2].$node).toBeDefined();
      });

    });

    describe("deleting a root node", function() {
      it("updates model", function() {
        tree.deleteNodes([node0]);
        expect(tree.nodes.length).toBe(2);
        expect(tree.nodes[0]).toBe(node1);
        expect(Object.keys(tree.nodesMap).length).toBe(26);
      });

      it("updates html document", function() {
        tree.render(session.$entryPoint);

        tree.deleteNodes([node0]);
        expect(findAllNodes(tree).length).toBe(26);
        expect(node0.$node).toBeUndefined();
        expect(node0.childNodes[0].$node).toBeUndefined();
        expect(node0.childNodes[1].$node).toBeUndefined();
        expect(node0.childNodes[2].$node).toBeUndefined();
      });

      describe("deleting a collapsed root node", function() {
        it("updates model", function() {
          node0.expanded = false;

          tree.deleteNodes([node0]);
          expect(tree.nodes.length).toBe(2);
          expect(tree.nodes[0]).toBe(node1);
          expect(Object.keys(tree.nodesMap).length).toBe(26);
        });

        it("updates html document", function() {
          node0.expanded = false;
          tree.render(session.$entryPoint);

          tree.deleteNodes([node0]);
          expect(findAllNodes(tree).length).toBe(26);
          expect(node0.$node).toBeUndefined();
          expect(node0.childNodes[0].$node).toBeUndefined();
          expect(node0.childNodes[1].$node).toBeUndefined();
          expect(node0.childNodes[2].$node).toBeUndefined();
        });
      });
    });

    describe("deleting all nodes", function() {
      it("updates model", function() {
        tree.deleteNodes([node0, node1, node2]);
        expect(tree.nodes.length).toBe(0);
        expect(Object.keys(tree.nodesMap).length).toBe(0);
      });

      it("updates html document", function() {
        tree.render(session.$entryPoint);

        tree.deleteNodes([node0, node1, node2]);
        expect(findAllNodes(tree).length).toBe(0);
      });
    });

  });

  describe("deleteAllChildNodes", function() {
    var model;
    var tree;
    var node0;
    var node1;
    var node2;
    var node1Child0;
    var node1Child1;
    var node1Child2;

    beforeEach(function() {
      model = createModelFixture(3, 1, true);
      tree = createTree(model);
      node0 = model.nodes[0];
      node1 = model.nodes[1];
      node2 = model.nodes[2];
      node1Child0 = node1.childNodes[0];
      node1Child1 = node1.childNodes[1];
      node1Child2 = node1.childNodes[1];
    });

    it("deletes all nodes from model", function() {
      expect(tree.nodes.length).toBe(3);
      expect(Object.keys(tree.nodesMap).length).toBe(12);

      tree.deleteAllChildNodes();
      expect(tree.nodes.length).toBe(0);
      expect(Object.keys(tree.nodesMap).length).toBe(0);
    });

    it("deletes all nodes from html document", function() {
      tree.render(session.$entryPoint);

      expect(findAllNodes(tree).length).toBe(12);

      tree.deleteAllChildNodes();
      expect(findAllNodes(tree).length).toBe(0);
    });

    it("deletes all nodes from model for a given parent", function() {
      expect(tree.nodes.length).toBe(3);
      expect(Object.keys(tree.nodesMap).length).toBe(12);

      tree.deleteAllChildNodes(node1);
      expect(node1.childNodes.length).toBe(0);
      expect(Object.keys(tree.nodesMap).length).toBe(9);
    });

    it("deletes all nodes from html document for a given parent", function() {
      tree.render(session.$entryPoint);

      expect(findAllNodes(tree).length).toBe(12);

      tree.deleteAllChildNodes(node1);
      expect(findAllNodes(tree).length).toBe(9);

      //Check that children are removed, parent must still exist
      expect(node1.$node).toBeDefined();
      expect(node1Child0.$node).toBeUndefined();
      expect(node1Child1.$node).toBeUndefined();
      expect(node1Child2.$node).toBeUndefined();
    });

  });

  describe("node click", function() {

    it("calls tree._onNodeMouseDown", function() {
      var model = createModelFixture(1);
      var tree = createTree(model);
      spyOn(tree, '_onNodeMouseDown');
      tree.render(session.$entryPoint);

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerMouseDown();

      expect(tree._onNodeMouseDown).toHaveBeenCalled();
    });

    it("sends selection and click events in one call in this order", function() {
      var model = createModelFixture(1);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerClick();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodesSelected', 'nodeClicked']);
    });

    it("sends selection, check and click events if tree is checkable and checkbox has been clicked", function() {
      var model = createModelFixture(1);
      var tree = createTree(model);
      tree.checkable = true;
      tree.render(session.$entryPoint);

      var $checkbox = tree.$container.find('.tree-node:first').children('.tree-node-checkbox')
        .children('div');
      $checkbox.triggerClick();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodesSelected', 'nodesChecked', 'nodeClicked']);
    });

    it("updates model (selection)", function() {
      var model = createModelFixture(1);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      expect(tree.selectedNodes.length).toBe(0);

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerClick();

      expect(tree.selectedNodes.length).toBe(1);
      expect(tree.selectedNodes[0].id).toBe(model.nodes[0].id);
    });

    it("does not send click if mouse down happens on another node than mouseup", function() {
      var model = createModelFixture(2);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var $node0 = tree.nodes[0].$node;
      var $node1 = tree.nodes[1].$node;
      $node0.triggerMouseDown();
      $node1.triggerMouseUp();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      // Must contain only selection event (of first node), no clicked
      expect(requestData).toContainEventsExactly([{
        nodeIds: [tree.nodes[0].id],
        target: tree.id,
        type: 'nodesSelected'
      }]);
    });

    it("does not send click if mouse down does not happen on a node", function() {
      var model = createModelFixture(1);
      var tree = createTree(model);
      var $div = session.$entryPoint.makeDiv().cssHeight(10).cssWidth(10);
      tree.render(session.$entryPoint);

      var $node0 = tree.nodes[0].$node;
      $node0.triggerMouseDown();
      $(window).triggerMouseUp({position: {left: 0, top:0}});

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      // Must contain only selection event (of first node), no clicked
      expect(requestData).toContainEventsExactly([{
        nodeIds: [tree.nodes[0].id],
        target: tree.id,
        type: 'nodesSelected'
      }]);

      jasmine.Ajax.uninstall();
      jasmine.Ajax.install();

      $(window).triggerMouseDown({position: {left: 0, top:0}});
      $node0.triggerMouseUp();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });
  });

  describe("check nodes", function() {

    it("checks a subnode -> mark upper nodes ", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;

      var node;
      //find node with more then one child level
      for (var i = 0; i < tree.nodes.length; i++) {
        if (tree.nodes[i].childNodes && tree.nodes[i].childNodes.length > 0 && tree.nodes[i].childNodes[0].childNodes && tree.nodes[i].childNodes[0].childNodes.length > 0) {
          node = tree.nodes[i].childNodes[0].childNodes[0];
          break;
        }
      }

      if (node) {
        tree.checkNode(node, true, true);
      }

      while (node.parentNode) {
        node = node.parentNode;
        expect(node.childrenChecked).toEqual(true);
      }

    });

    it("checks a node -> mark upper nodes -> uncheck node and test if node keeps marked because children are checked", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;
      var node, nodeToCheck;
      //find node with more then one child level
      for (var i = 0; i < tree.nodes.length; i++) {
        if (tree.nodes[i].childNodes && tree.nodes[i].childNodes.length > 0 && tree.nodes[i].childNodes[0].childNodes && tree.nodes[i].childNodes[0].childNodes.length > 0) {
          node = tree.nodes[i].childNodes[0].childNodes[0];
          nodeToCheck = tree.nodes[i].childNodes[0];
          break;
        }
      }

      if (node) {
        tree.checkNode(node, true, true);
      }
      tree.checkNode(nodeToCheck, true, true);
      var tmpNode = nodeToCheck;
      //upper nodes should be marked
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }
      expect(nodeToCheck.childNodes[0].checked).toEqual(true);

      //remove check state on second level node-> second level node should be marked because children of it are checked
      tree.checkNode(nodeToCheck, false, true);
      expect(nodeToCheck.checked).toEqual(false);
      expect(nodeToCheck.childrenChecked).toEqual(true);
      tmpNode = nodeToCheck;
      //upper nodes should be marked
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }
    });

    it("checks a subnode and its sibling->mark upper nodes -> uncheck one of the siblings", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;
      var nodeOne, nodeTwo;
      //find node with more then one child level
      for (var i = 0; i < tree.nodes.length; i++) {
        if (tree.nodes[i].childNodes && tree.nodes[i].childNodes.length > 0 && tree.nodes[i].childNodes[0].childNodes && tree.nodes[i].childNodes[0].childNodes.length > 1) {
          nodeOne = tree.nodes[i].childNodes[0].childNodes[0];
          nodeTwo = tree.nodes[i].childNodes[0].childNodes[1];
          break;
        }
      }
      if (nodeOne && nodeTwo) {
        tree.checkNode(nodeOne, true, true);
        tree.checkNode(nodeTwo, true, true);
      }
      //check if all upper nodes are marked
      var tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }

      //uncheck one of the two siblings
      tree.checkNode(nodeTwo, false, true);
      //marks on upper should exist
      tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(true);
      }

      //uncheck second siblings
      tree.checkNode(nodeOne, false, true);
      //marks on upper should be removed
      tmpNode = nodeOne;
      while (tmpNode.parentNode) {
        tmpNode = tmpNode.parentNode;
        expect(tmpNode.childrenChecked).toEqual(false);
      }
    });

    it("try to check a disabled node ", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.render(session.$entryPoint);
      tree.checkable = true;
      var node = tree.nodes[0];

      if (node) {
        node.enabled = false;
        tree.checkNode(node, true, true);
      }
      expect(node.checked).toEqual(false);

    });

    it("try to check a node in disabled tree ", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.enabled = false;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];

      if (node) {
        tree.checkNode(node, true, true);
      }
      expect(node.checked).toEqual(false);

    });

    it("try to check two nodes in singlecheck tree ", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.multiCheck = false;
      tree.checkable = true;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0],
        nodeTwo = tree.nodes[1];

      if (node && nodeTwo) {
        tree.checkNode(node, true, true);
        tree.checkNode(nodeTwo, true, true);
      }

      var checkedNodes = [];
      for (var j = 0; j < tree.nodes.length; j++) {
        if (tree.nodes[j].checked) {
          checkedNodes.push(tree.nodes[j]);
        }
      }
      expect(checkedNodes.length).toBe(1);
    });

    it("check a parent in autoCheckChildren tree ", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.multiCheck = true;
      tree.checkable = true;
      tree.autoCheckChildren = true;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];

      if (node) {
        tree.checkNode(node, true, false);
      }

      var nodesToCheck = node.childNodes.slice();
      for (var j = 0; j < nodesToCheck.length; j++) {
        var tmpNode = nodesToCheck[j];
        expect(tmpNode.checked).toEqual(true);
        tmpNode = nodesToCheck.concat(tmpNode.childNodes.splice());
      }
    });

    it("check a parent in autoCheckChildren = false tree ", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.multiCheck = true;
      tree.checkable = true;
      tree.autoCheckChildren = false;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];

      if (node) {
        tree.checkNode(node, true, true);
      }

      var nodesToCheck = node.childNodes.slice();
      for (var j = 0; j < nodesToCheck.length; j++) {
        var tmpNode = nodesToCheck[j];
        expect(tmpNode.checked).toEqual(false);
        tmpNode = nodesToCheck.concat(tmpNode.childNodes.splice());
      }
    });

    it("try to check nodes in uncheckable tree ", function() {
      var model = createModelFixture(4, 4);
      var tree = createTree(model);
      tree.multiCheck = false;
      tree.checkable = false;
      tree.render(session.$entryPoint);

      var node = tree.nodes[0];

      if (node) {
        tree.checkNode(node, true, true);
      }

      var checkedNodes = [];
      for (var j = 0; j < tree.nodes.length; j++) {
        if (tree.nodes[j].checked) {
          checkedNodes.push(tree.nodes[j]);
        }
      }
      expect(checkedNodes.length).toBe(0);
    });

  });

  describe("node double click", function() {

    beforeEach(function() {
      //Expansion happens with an animation (async).
      //Disabling it makes it possible to test the expansion state after the expansion
      $.fx.off = true;
    });

    afterEach(function() {
      $.fx.off = false;
    });

    it("expands/collapses the node", function() {
      var model = createModelFixture(1, 1, false);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var $node = tree.$container.find('.tree-node:first');
      expect($node).not.toHaveClass('expanded');

      $node.triggerDoubleClick();
      expect($node).toHaveClass('expanded');

      $node.triggerDoubleClick();
      expect($node).not.toHaveClass('expanded');
    });

    it("sends clicked, selection, action and expansion events", function() {
      var model = createModelFixture(1, 1, false);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerDoubleClick();

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['nodesSelected', 'nodeClicked', 'nodeAction', 'nodeExpanded']);
    });
  });

  describe("node control double click", function() {

    beforeEach(function() {
      //Expansion happens with an animation (async).
      //Disabling it makes it possible to test the expansion state after the expansion
      $.fx.off = true;
    });

    afterEach(function() {
      $.fx.off = false;
    });

    it("does the same as control single click (does NOT expand and immediately collapse again)", function() {
      var model = createModelFixture(1, 1, false);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var $nodeControl = tree.$container.find('.tree-node-control:first');
      var $node = $nodeControl.parent();
      expect($node).not.toHaveClass('expanded');

      $nodeControl.triggerDoubleClick();
      expect($node).toHaveClass('expanded');

      // Reset internal state because there is no "sleep" in JS
      tree._doubleClickSupport._lastTimestamp -= 5000; // simulate last click 5 seconds ago

      $nodeControl.triggerDoubleClick();
      expect($node).not.toHaveClass('expanded');
    });

    it("sends clicked, selection, action and expansion events", function() {
      var model = createModelFixture(1, 1, false);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var $node = tree.$container.find('.tree-node:first');
      $node.triggerDoubleClick();

      sendQueuedAjaxCalls();

      // clicked has to be after selected otherwise it is not possible to get the selected row in execNodeClick
      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['nodesSelected', 'nodeClicked', 'nodeAction', 'nodeExpanded']);
    });
  });

  describe("collapseAll", function() {

    it("collapses all nodes and updates model", function() {
      var i;
      var model = createModelFixture(3, 2, true);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var allNodes = [];
      tree._visitNodes(tree.nodes, function(node) {
        allNodes.push(node);
      });

      for (i = 0; i < allNodes.length; i++) {
        expect(allNodes[i].expanded).toBe(true);
      }

      tree.collapseAll();

      for (i = 0; i < allNodes.length; i++) {
        expect(allNodes[i].expanded).toBe(false);
      }

      //A nodeExpanded event must be sent for every node because all nodes were initially expanded
      sendQueuedAjaxCalls();
      expect(mostRecentJsonRequest().events.length).toBe(allNodes.length);
    });
  });

  describe("deselectAll", function() {

    it("clears the selection", function() {
      var model = createModelFixture(1, 1);
      var node0 = model.nodes[0];
      model.selectedNodes = [node0.id];

      var tree = createTree(model);
      tree.render(session.$entryPoint);
      expect(tree.$selectedNodes().length).toBe(1);

      tree.deselectAll();

      //Check model
      expect(tree.selectedNodes.length).toBe(0);

      //Check gui
      expect(tree.$selectedNodes().length).toBe(0);
    });
  });

  describe("selectNodes", function() {

    it("selects a node", function() {
      var model = createModelFixture(3, 3, false);
      var tree = createTree(model);
      var node0 = model.nodes[0];

      tree.render(session.$entryPoint);
      expect(tree.$selectedNodes().length).toBe(0);
      expect(node0.$node.isSelected()).toBe(false);

      tree.selectNodes([node0]);
      //Check model
      expect(tree.selectedNodes.length).toBe(1);
      expect(tree.selectedNodes[0].id).toBe(node0.id);

      //Check gui
      expect(tree.$selectedNodes().length).toBe(1);
      expect(node0.$node.isSelected()).toBe(true);
    });

    it("expands the parents if a hidden node should be selected whose parents are collapsed (revealing the selection)", function() {
      var model = createModelFixture(3, 3, false);
      var tree = createTree(model);
      var node0 = tree.nodes[0];
      var child0 = node0.childNodes[0];
      var grandchild0 = child0.childNodes[0];
      tree.render(session.$entryPoint);

      expect(node0.expanded).toBe(false);
      expect(child0.expanded).toBe(false);
      expect(child0.$node).toBeUndefined();

      tree.selectNodes([grandchild0]);
      expect(node0.expanded).toBe(true);
      expect(child0.expanded).toBe(true);
      expect(tree.$selectedNodes().length).toBe(1);
      expect(grandchild0.$node.isSelected()).toBe(true);

      sendQueuedAjaxCalls();

      var event0 = new scout.Event(tree.id, 'nodeExpanded', {
        nodeId: node0.id,
        expanded: true,
        expandedLazy: false
      });
      var event1 = new scout.Event(tree.id, 'nodeExpanded', {
        nodeId: child0.id,
        expanded: true,
        expandedLazy: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([event0, event1]);
    });

    it("also expands the node if bread crumb mode is enabled", function() {
      var model = createModelFixture(1, 1);
      var node0 = model.nodes[0];

      var tree = createTree(model);
      tree.breadcrumbEnabled = true;
      tree.render(session.$entryPoint);

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);
    });

    it("sets css class ancestor-of-selected on every ancestor of the selected element", function() {
      var model = createModelFixture(3, 3);
      var tree = createTree(model);
      var nodes = tree.nodes;
      var node1 = nodes[1];
      var child1 = node1.childNodes[1];
      var grandchild1 = child1.childNodes[1];

      tree.render(session.$entryPoint);

      var $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(0);

      tree.selectNodes(node1);
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(0);

      tree.selectNodes(child1);
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(1);
      expect($parents.eq(0)[0]).toBe(nodes[1].$node[0]);

      tree.selectNodes(grandchild1);
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(2);
      expect($parents.eq(0)[0]).toBe(nodes[1].$node[0]);
      expect($parents.eq(1)[0]).toBe(nodes[1].childNodes[1].$node[0]);

      tree.deselectAll();
      $parents = tree.$data.find('.tree-node.ancestor-of-selected');
      expect($parents.length).toBe(0);
    });

    it("sets css class child-of-selected on direct children of the selected element", function() {
      var model = createModelFixture(3, 3, true);
      var tree = createTree(model);
      var nodes = tree.nodes;
      var node1 = nodes[1];
      var child1 = node1.childNodes[1];
      var grandchild1 = child1.childNodes[1];

      tree.render(session.$entryPoint);

      var $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);

      // all nodes are expanded
      tree.selectNodes(node1);
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[1].childNodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].childNodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[1].childNodes[2].$node[0]);

      tree.deselectAll();
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);
    });
  });

  describe("expandNode", function() {

    it("sets css class child-of-selected on direct children if the expanded node is selected", function() {
      var model = createModelFixture(3, 3);
      var tree = createTree(model);
      var nodes = tree.nodes;
      var node1 = nodes[1];

      tree.render(session.$entryPoint);

      tree.selectNodes(node1);
      var $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(0);

      tree.expandNode(node1);
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(3);
      expect($children.eq(0)[0]).toBe(nodes[1].childNodes[0].$node[0]);
      expect($children.eq(1)[0]).toBe(nodes[1].childNodes[1].$node[0]);
      expect($children.eq(2)[0]).toBe(nodes[1].childNodes[2].$node[0]);

      tree.collapseNode(node1);
      $children = tree.$data.find('.tree-node.child-of-selected');
      expect($children.length).toBe(0);
    });

  });

  describe("collapseNode", function() {

    it("prevents collapsing in bread crumb mode if node is selected", function() {
      var model = createModelFixture(1, 1);
      var node0 = model.nodes[0];

      var tree = createTree(model);
      tree.breadcrumbEnabled = true;
      tree.render(session.$entryPoint);

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);

      tree.collapseNode(node0);

      // Still true
      expect(node0.expanded).toBe(true);
    });
  });

  describe("updateItemPath", function() {
    var model, tree, node1, child1, grandchild1, nodes;

    beforeEach(function() {
      model = createModelFixture(3, 3);
      tree = createTree(model);
      nodes = tree.nodes;
      node1 = nodes[1];
      child1 = node1.childNodes[1];
      grandchild1 = child1.childNodes[1];
    });

    it("Sets css class group on every element within the same group", function() {
      tree.render(session.$entryPoint);
      tree._isGroupingEnd = function(node) {
        return node.nodeType === 'groupingParent';
      };

      tree.selectNodes([]);
      var $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(0);

      tree.selectNodes(node1);
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);

      node1.nodeType = 'groupingParent';
      tree.selectNodes(child1);
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(1);
      expect($groupNodes.eq(0)[0]).toBe(child1.$node[0]);

      node1.nodeType = 'groupingParent';
      tree.selectNodes(grandchild1);
      $groupNodes = tree.$data.find('.tree-node.group');
      expect($groupNodes.length).toBe(4);
      expect($groupNodes.eq(0)[0]).toBe(child1.$node[0]);
      expect($groupNodes.eq(1)[0]).toBe(child1.childNodes[0].$node[0]);
      expect($groupNodes.eq(2)[0]).toBe(child1.childNodes[1].$node[0]);
      expect($groupNodes.eq(3)[0]).toBe(child1.childNodes[2].$node[0]);
    });
  });

  describe("tree filter", function() {

    it("filters nodes when filter() is called", function() {
      var model = createModelFixture(1, 1, true);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var filter = {
        accept: function(node) {
          return node === tree.nodes[0];
        }
      };
      tree.addFilter(filter);
      tree.filter();
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(false);

      tree.removeFilter(filter);
      tree.filter();
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(true);
    });

    it("filters nodes when tree gets rendered", function() {
      var model = createModelFixture(1, 1, true);
      var tree = createTree(model);
      var filter = {
        accept: function(node) {
          return node === tree.nodes[0];
        }
      };
      tree.addFilter(filter);
      tree.render(session.$entryPoint);

      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(false);

      tree.removeFilter(filter);
      tree.filter();
      expect(tree.nodes[0].filterAccepted).toBe(true);
      expect(tree.nodes[0].childNodes[0].filterAccepted).toBe(true);
    });

    it("makes sure only filtered nodes are displayed when node gets expanded", function() {
      var model = createModelFixture(2, 1);
      var tree = createTree(model);
      var filter = {
        accept: function(node) {
          return node === tree.nodes[0] || node === tree.nodes[0].childNodes[0];
        }
      };
      tree.addFilter(filter);
      tree.render(session.$entryPoint);

      expect(tree.nodes[0].$node.isVisible()).toBe(true);
      expect(tree.nodes[1].$node.isVisible()).toBe(false);
      expect(tree.nodes[0].childNodes[0].$node).toBeFalsy();
      expect(tree.nodes[0].childNodes[1].$node).toBeFalsy();

      tree.expandNode(tree.nodes[0]);
      expect(tree.nodes[0].$node.isVisible()).toBe(true);
      expect(tree.nodes[1].$node.isVisible()).toBe(false);
      expect(tree.nodes[0].childNodes[0].$node.isVisible()).toBe(true);
      expect(tree.nodes[0].childNodes[1].$node.isVisible()).toBe(false);
    });

    it("applies filter if a node gets changed", function() {
      var model = createModelFixture(2, 1);
      var tree = createTree(model);
      var filter = {
        accept: function(node) {
          return node.text === 'node 0';
        }
      };
      tree.addFilter(filter);
      tree.render(session.$entryPoint);

      expect(tree.nodes[0].$node.isVisible()).toBe(true);
      expect(tree.nodes[1].$node.isVisible()).toBe(false);

      var event = createNodeChangedEvent(model, tree.nodes[0].id);
      event.text = 'new Text';
      var message = {
        events: [event]
      };
      session._processSuccessResponse(message);

      expect(tree.nodes[0].text).toBe(event.text);
      // text has changed -> filter condition returns false -> must not be visible anymore
      expect(tree.nodes[0].$node.isVisible()).toBe(false);
      expect(tree.nodes[1].$node.isVisible()).toBe(false);
    });

  });

  describe("onModelAction", function() {

    describe("nodesInserted event", function() {
      var model;
      var tree;
      var node0;
      var node1;
      var node2;

      beforeEach(function() {
        model = createModelFixture(3, 1, true);
        tree = createTree(model);
        node0 = model.nodes[0];
        node1 = model.nodes[1];
        node2 = model.nodes[2];
      });

      it("calls insertNodes", function() {
        spyOn(tree, 'insertNodes');

        var newNode0Child3 = createModelNode('0_3', 'newNode0Child3', 3);
        var event = createNodesInsertedEvent(model, [newNode0Child3], node0.id);
        tree.onModelAction(event);
        expect(tree.insertNodes).toHaveBeenCalledWith([newNode0Child3], tree.nodes[0]);
      });

    });

    describe("nodesDeleted event", function() {
      var model;
      var tree;
      var node0;
      var node1;
      var node2;

      beforeEach(function() {
        // A large tree is used to properly test recursion
        model = createModelFixture(3, 2, true);
        tree = createTree(model);
        node0 = tree.nodes[0];
        node1 = tree.nodes[1];
        node2 = tree.nodes[2];
      });

      it("calls deleteNodes", function() {
        spyOn(tree, 'deleteNodes');

        var node2Child0 = node2.childNodes[0];
        var newNode0Child3 = createModelNode('0_3', 'newNode0Child3', 3);
        var event = createNodesDeletedEvent(model, [node2Child0.id], node2.id);
        tree.onModelAction(event);
        expect(tree.deleteNodes).toHaveBeenCalledWith([node2Child0], node2);
      });

    });

    describe("allChildNodesDeleted event", function() {
      var model;
      var tree;
      var node0;
      var node1;
      var node2;
      var node1Child0;
      var node1Child1;
      var node1Child2;

      beforeEach(function() {
        model = createModelFixture(3, 1, true);
        tree = createTree(model);
        node0 = model.nodes[0];
        node1 = model.nodes[1];
        node2 = model.nodes[2];
        node1Child0 = node1.childNodes[0];
        node1Child1 = node1.childNodes[1];
        node1Child2 = node1.childNodes[1];
      });

      it("calls deleteAllChildNodes", function() {
        spyOn(tree, 'deleteAllChildNodes');

        var event = createAllChildNodesDeletedEvent(model);
        tree.onModelAction(event);
        expect(tree.deleteAllChildNodes).toHaveBeenCalled();
      });

    });

    describe("nodesSelected event", function() {
      var model;
      var tree;
      var node0;
      var child0;
      var grandchild0;

      beforeEach(function() {
        model = createModelFixture(3, 3, false);
        tree = createTree(model);
        node0 = model.nodes[0];
        child0 = node0.childNodes[0];
        grandchild0 = child0.childNodes[0];
      });

      it("calls selectNodes", function() {
        spyOn(tree, 'selectNodes');

        var event = createNodesSelectedEvent(model, [node0.id]);
        tree.onModelAction(event);
        expect(tree.selectNodes).toHaveBeenCalledWith([node0], false);
      });

      it("does not send events if called when processing response", function() {
        tree.render(session.$entryPoint);

        var message = {
          events: [createNodesSelectedEvent(model, [node0.id])]
        };
        session._processSuccessResponse(message);

        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(0);
      });
    });

    describe("nodeChanged event", function() {
      var model, tree, nodes, node0, node1, child0, child1_1;

      beforeEach(function() {
        model = createModelFixture(3, 3, false);
        tree = createTree(model);
        nodes = tree.nodes;
        node0 = nodes[0];
        node1 = nodes[1];
        child0 = node0.childNodes[0];
        child1_1 = node1.childNodes[1];
      });

      it("updates the text of the model node", function() {
        var event = createNodeChangedEvent(model, node0.id);
        event.text = 'new Text';
        var message = {
          events: [event]
        };
        session._processSuccessResponse(message);

        expect(node0.text).toBe(event.text);
      });

      it("updates the text of the html node", function() {
        tree.render(session.$entryPoint);

        var event = createNodeChangedEvent(model, node0.id);
        event.text = 'new Text';
        var message = {
          events: [event]
        };
        session._processSuccessResponse(message);

        //Check gui
        var $node0 = node0.$node;
        expect($node0.text()).toBe(event.text);

        //Check whether tree-control is still there
        expect($node0.children('.tree-node-control').length).toBe(1);
      });

      it("updates custom cssClass of model and html node", function() {
        tree.selectedNodes = [node0];
        tree.render(session.$entryPoint);

        var event = createNodeChangedEvent(model, node0.id);
        event.cssClass = 'new-css-class';
        var message = {
          events: [event]
        };
        session._processSuccessResponse(message);

        // Check model
        expect(node0.cssClass).toBe(event.cssClass);

        // Check gui
        var $node0 = node0.$node;
        expect($node0).toHaveClass('new-css-class');
        // check if other classes are still there
        expect($node0).toHaveClass('tree-node');
        expect($node0).toHaveClass('selected');

        // Check if removal works (event does not contain cssClass again)
        event = createNodeChangedEvent(model, node0.id);
        message = {
          events: [event]
        };
        session._processSuccessResponse(message);

        // Check model
        expect(node0.cssClass).toBeFalsy();

        // Check gui
        $node0 = node0.$node;
        expect($node0).not.toHaveClass('new-css-class');
        // check if other classes are still there
        expect($node0).toHaveClass('tree-node');
        expect($node0).toHaveClass('selected');
      });

      it("preserves child-of-selected when root nodes get updated", function() {
        tree.selectedNodes = [];
        tree.render(session.$entryPoint);

        var $children = tree.$data.find('.tree-node.child-of-selected');
        expect($children.length).toBe(3);
        expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
        expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
        expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);

        var event = createNodeChangedEvent(model, node1.id);
        event.text = 'new text';
        var message = {
          events: [event]
        };
        session._processSuccessResponse(message);

        expect(node1.text).toBe(event.text);

        $children = tree.$data.find('.tree-node.child-of-selected');
        expect($children.length).toBe(3);
        expect($children.eq(0)[0]).toBe(nodes[0].$node[0]);
        expect($children.eq(1)[0]).toBe(nodes[1].$node[0]);
        expect($children.eq(2)[0]).toBe(nodes[2].$node[0]);
      });

      it("preserves child-of-selected when child nodes get updated", function() {
        tree.selectedNodes = [node1];
        node1.expanded = true;
        tree.render(session.$entryPoint);

        var $children = tree.$data.find('.tree-node.child-of-selected');
        expect($children.length).toBe(3);
        expect($children.eq(0)[0]).toBe(node1.childNodes[0].$node[0]);
        expect($children.eq(1)[0]).toBe(node1.childNodes[1].$node[0]);
        expect($children.eq(2)[0]).toBe(node1.childNodes[2].$node[0]);

        var event = createNodeChangedEvent(model, child1_1.id);
        event.text = 'new text';
        var message = {
          events: [event]
        };
        session._processSuccessResponse(message);

        expect(child1_1.text).toBe(event.text);

        $children = tree.$data.find('.tree-node.child-of-selected');
        expect($children.length).toBe(3);
        expect($children.eq(0)[0]).toBe(node1.childNodes[0].$node[0]);
        expect($children.eq(1)[0]).toBe(node1.childNodes[1].$node[0]);
        expect($children.eq(2)[0]).toBe(node1.childNodes[2].$node[0]);
      });

      it("preserves group css class when nodes get updated", function() {
        tree.selectedNodes = [node1];
        node1.expanded = false;
        tree.render(session.$entryPoint);

        tree._isGroupingEnd = function(node) {
          return node.nodeType === 'groupingParent';
        };

        var $groupNodes = tree.$data.find('.tree-node.group');
        expect($groupNodes.length).toBe(1);
        expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);

        var event = createNodeChangedEvent(model, node1.id);
        event.text = 'new text';
        var message = {
          events: [event]
        };
        session._processSuccessResponse(message);

        expect(node1.text).toBe(event.text);

        $groupNodes = tree.$data.find('.tree-node.group');
        expect($groupNodes.length).toBe(1);
        expect($groupNodes.eq(0)[0]).toBe(node1.$node[0]);
      });

    });

    describe("nodesUpdated event", function() {
      var model;
      var tree;
      var node0;
      var child0;

      beforeEach(function() {
        model = createModelFixture(3, 3, false);
        tree = createTree(model);
        node0 = model.nodes[0];
        child0 = node0.childNodes[0];
      });

      it("calls updateNodes", function() {
        spyOn(tree, 'updateNodes');

        var child0Update = {
          id: child0.id,
          enabled: false
        };
        var event = createNodesUpdatedEvent(model, [child0Update]);
        tree.onModelAction(event);
        expect(tree.updateNodes).toHaveBeenCalledWith([child0Update]);
      });
    });

    describe("multiple events", function() {

      var model;
      var tree;
      var node0;
      var node1;
      var node2;

      beforeEach(function() {
        model = createModelFixture(3, 1, true);
        tree = createTree(model);
        node0 = model.nodes[0];
        node1 = model.nodes[1];
        node2 = model.nodes[2];
      });

      it("handles delete, collapse, insert, expand events correctly", function() {
        tree.render(session.$entryPoint);

        //Delete child nodes from node0
        var message = {
          events: [createAllChildNodesDeletedEvent(model, node0.id)]
        };
        session._processSuccessResponse(message);
        expect(node0.childNodes.length).toBe(0);
        expect(findAllNodes(tree).length).toBe(9);

        //Collapse node0
        var $node0 = node0.$node;
        message = {
          events: [createNodeExpandedEvent(model, node0.id, false)]
        };
        session._processSuccessResponse(message);
        expect(node0.expanded).toBe(false);
        expect($node0).not.toHaveClass('expanded');

        //Insert new child node at node0
        var newNode0Child3 = createModelNode('0_3', 'newNode0Child3');
        message = {
          events: [createNodesInsertedEvent(model, [newNode0Child3], node0.id)]
        };
        session._processSuccessResponse(message);

        //Model should be updated, html nodes not added because node still is collapsed
        expect(node0.childNodes.length).toBe(1);
        expect(node0.childNodes[0].text).toBe(newNode0Child3.text);
        expect(Object.keys(tree.nodesMap).length).toBe(10);
        expect(findAllNodes(tree).length).toBe(9); //Still 9 nodes

        //Expand again
        message = {
          events: [createNodeExpandedEvent(model, node0.id, true)]
        };
        session._processSuccessResponse(message);

        expect(node0.expanded).toBe(true);
        expect($node0).toHaveClass('expanded');

        //Html nodes should now be added
        expect(findAllNodes(tree).length).toBe(10);
        expect(node0.childNodes[0].$node).toBeDefined();
      });
    });
  });

  describe("tree enabled/disabled", function() {

    var model;
    var tree;
    var node0;
    var node1;
    var node2;

    beforeEach(function() {
      model = createModelFixture(3, 1, true);
      model.checkable = true;
      model.nodes[2].enabled = false;

      tree = createTree(model);
      node0 = model.nodes[0];
      node1 = model.nodes[1];
      node2 = model.nodes[2];
    });

    it("disables checkboxes when tree is disabled", function() {
      tree.render(session.$entryPoint);

      expect(node0.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).not.toHaveClass('disabled');
      expect(node2.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');

      // Disable tree
      var message = {
        events: [createTreeEnabledEvent(model, false)]
      };
      session._processSuccessResponse(message);

      expect(node0.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');
      expect(node2.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');

      // Re-enable tree
      message = {
        events: [createTreeEnabledEvent(model, true)]
      };
      session._processSuccessResponse(message);

      expect(node0.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).not.toHaveClass('disabled');
      expect(node2.$node.children('.tree-node-checkbox').children('div').eq(0)[0]).toHaveClass('disabled');
    });
  });

});
