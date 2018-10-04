/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A Basic Tree implementation that satisfies project specific needs by wrapping around
 * {@link UnorderedTree}
 *
 * @author shashank
 */

public class SimpleTree<N extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected UnorderedTree<N, Integer> _tree;

    protected int addedEdgeCnt = 0;

    public SimpleTree() {
        _tree = new UnorderedTree<>();
    }

    /**
     * @param node the node whose number of children is to be returned
     * @return the number of children that the {@code node} has
     */
    public int getChildCount(N node) {
        return _tree.getChildCount(node);
    }

    /**
     * Returns a set of node's children. If the node has no children then an empty set will be
     * returned.
     */
    public Set<N> getChildren(N node) {
        return _tree.getChildren(node);
    }

    /**
     * @return the depth of the node in this tree, or -1 if the node is not present in this tree
     */
    public int getDepth(N node) {
        return _tree.getDepth(node);
    }

    /**
     * Returns the height of the tree, or -1 if the tree is empty.
     */
    public int getHeight() {
        return _tree.getHeight();
    }

    public N getParent(N node) {
        return _tree.getParent(node);
    }

    public N getRoot() {
        return _tree.getRoot();
    }

    public N getNode(N node) {
        if (!containsNode(node))
            return null;

        if (isRoot(node))
            return _tree.getRoot();

        int edge = _tree.getParentEdge(node);

        return _tree.getEndpoints(edge).getSecond();
    }

    /**
     * Adds the specified node ({@code child}) as a child of the parent node ({@code parent}).
     *
     * @param parent node (must exist prior to addition)
     * @param child node
     * @return {@code true} if the graph has been modified
     */
    public boolean addEdge(N parent, N child) {
        boolean success = _tree.addEdge(addedEdgeCnt + 1, parent, child);

        if (success)
            addedEdgeCnt++;

        return success;
    }

    /**
     * Initializes the tree with the given node. Can only be invoked once i.e. once the root is set,
     * invoking this will throw an exception.
     *
     * @param node to be used as the root
     * @return
     */
    public boolean initializeRoot(N node) {
        return _tree.addVertex(node);
    }

    /**
     * Returns <code>true</code> if <code>node</code> is a leaf of this tree, i.e., if it has no
     * children.
     *
     * @param node the node to be queried
     */
    public boolean isLeaf(N node) {
        return _tree.isLeaf(node);
    }

    /**
     * Returns true iff <code>v1</code> is the parent of <code>v2</code>. Note that if
     * <code>v2</code> is the root and <code>v1</code> is <code>null</code>, this method still
     * returns <code>true</code>.
     */
    public boolean isParent(N v1, N v2) {
        return _tree.isPredecessor(v1, v2);
    }

    /**
     * Returns <code>true</code> if the given <code>node</code> is the root of this tree
     *
     * @param node the node to be queried
     */
    public boolean isRoot(N node) {
        return _tree.isRoot(node);
    }

    /**
     * Returns true iff <code>v1</code> is the child of <code>v2</code>. Note that if
     * <code>v2</code> is a leaf node and <code>v1</code> is <code>null</code>, this method returns
     * <code>true</code>.
     */
    public boolean isChild(N v1, N v2) {
        return _tree.isSuccessor(v1, v2);
    }

    public boolean containsNode(N node) {
        return _tree.containsVertex(node);
    }

    public boolean containsEdge(N v1, N v2) {
        Integer edgeIndex = _tree.findEdge(v1, v2);

        if (edgeIndex == null)
            return false;
        else
            return true;
    }

    public int getNodeCount() {
        return _tree.getVertexCount();
    }

    public Set<N> getNodes() {
        return _tree.getVertices();
    }

    public List<N> getBreadthOrderedNodeList() {
        List<N> output = new ArrayList<>(_tree.getVertexCount());
        List<N> exploreNodes = new ArrayList<>();

        exploreNodes.add(_tree.getRoot());

        while (exploreNodes.size() != 0) {
            N node = exploreNodes.get(0);
            exploreNodes.remove(0);
            output.add(node);

            if (!isLeaf(node)) {
                Set<N> children = getChildren(node);
                exploreNodes.addAll(children);
            }
        }

        return output;
    }

    public List<N> getDepthOrderedNodeList() {
        List<N> output = new ArrayList<>(_tree.getVertexCount());
        List<N> exploreNodes = new ArrayList<>();

        exploreNodes.add(_tree.getRoot());

        while (exploreNodes.size() != 0) {
            N node = exploreNodes.get(0);
            exploreNodes.remove(0);
            output.add(node);

            if (!isLeaf(node)) {
                Set<N> children = getChildren(node);

                for (N child : children) {
                    exploreNodes.add(0, child);
                }
            }
        }

        return output;
    }

    public Set<N> getSiblingsInclusive(N node) {
        if (!_tree.containsVertex(node))
            return null;

        if (isRoot(node))
            return Collections.singleton(node);

        Set<N> siblings = new HashSet<>();
        siblings.addAll(getSiblingsExclusive(node));
        siblings.add(node);

        return new ImmutableSet.Builder<N>().addAll(siblings).build();
    }

    public Set<N> getSiblingsExclusive(N node) {
        if (!_tree.containsVertex(node))
            return null;

        if (isRoot(node))
            return null;

        N parent = getParent(node);

        Set<N> siblings = new HashSet<>();

        siblings.addAll(getChildren(parent));

        siblings.remove(node);

        return new ImmutableSet.Builder<N>().addAll(siblings).build();
    }

    public List<N> getAllParents(N node) {
        if (!_tree.containsVertex(node))
            return null;

        if (isRoot(node))
            return null;

        List<N> parents = new ArrayList<>();

        N child = node;
        N parent;

        while ((parent = getParent(child)) != null) {
            parents.add(parent);
            child = parent;
        }

        return new ImmutableList.Builder<N>().addAll(parents).build();
    }

    public Set<N> getLeafSet() {
        Set<N> leafSet = new HashSet<>();

        for (N node : getNodes()) {
            if (isLeaf(node))
                leafSet.add(node);
        }

        return new ImmutableSet.Builder<N>().addAll(leafSet).build();
    }

    public Set<N> getNodesAtSameLevel(N node) {
        if (!containsNode(node))
            return null;

        int depth = getDepth(node);

        return getAllNodesAtDepth(depth);
    }

    private Set<N> getAllNodesAtDepth(int depth) {
        if (getHeight() < depth)
            return null;

        if (getHeight() == depth)
            return getLeafSet();

        Set<N> nodeSet = new HashSet<>();

        for (N node : getNodes()) {
            if (getDepth(node) == depth)
                nodeSet.add(node);
        }

        return new ImmutableSet.Builder<N>().addAll(nodeSet).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleTree<?>))
            return false;

        SimpleTree<N> that = (SimpleTree<N>) o;

        if (this.addedEdgeCnt != that.addedEdgeCnt)
            return false;

        if (!this._tree.equals(that._tree))
            return false;

        return true;
    }
}
