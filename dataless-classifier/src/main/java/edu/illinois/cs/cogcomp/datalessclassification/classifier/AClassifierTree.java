/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.datalessclassification.hierarchy.SimpleTree;
import edu.illinois.cs.cogcomp.datalessclassification.hierarchy.TreeNode;

/**
 * An Abstract Tree class that has the same link structure as a {@link LabelTree}.
 *
 * Ideally, different classes will extend this class with different types of Nodes, wherein their corresponding Node
 * will contain the additional payload (data) required by their corresponding classifier.
 *
 * For instance, a simple nearest-neighbor based Dataless Classifier requires a Tree wherein each node contains the label
 * representation, which is achieved by extending this class ({@link ConceptTree}), and the corresponding Node class {@link ConceptTreeNode}).
 *
 * @author shashank
 */

public abstract class AClassifierTree<T extends TreeNode> extends SimpleTree<T> {

    private static final long serialVersionUID = 1L;

    protected String root_label;
    protected LabelTree labelTree;

    public AClassifierTree(LabelTree labelTree) {
        super();
        setLabelTree(labelTree);
        initializeTreeStructure();
    }

    protected boolean isLabelTreeInitialized() {
        return (!(labelTree == null));
    }

    @SuppressWarnings("unchecked")
    protected void initializeRoot(String root_label) {
        T root = (T) T.makeBasicNode(root_label);
        initializeRoot(root);
    }

    public String getRootLabel() {
        return root_label;
    }

    protected void setLabelTree(LabelTree labelTree) {
        if (isLabelTreeInitialized())
            return;

        this.root_label = labelTree.getRoot().getLabelID();
        initializeRoot(root_label);
        this.labelTree = labelTree;
    }

    public LabelTree getLabelTree() {
        return labelTree;
    }

    @SuppressWarnings("unchecked")
    public Set<T> getChildren(String label) {
        Set<T> set = getChildren((T) T.makeBasicNode(label));

        if (set == null)
            return null;

        if (set.isEmpty())
            return Collections.emptySet();

        Set<T> newSet = new HashSet<T>(set.size());

        newSet.addAll(set);

        return newSet;
    }

    @SuppressWarnings("unchecked")
    public T getParent(String label) {
        T parent = getParent((T) T.makeBasicNode(label));
        return parent;
    }

    @SuppressWarnings("unchecked")
    public boolean addEdge(String parent, String child) {
        T parentNode = (T) T.makeBasicNode(parent);
        T childNode = (T) T.makeBasicNode(child);

        return addEdge(parentNode, childNode);
    }

    public boolean addEdges(String parent, Set<String> children) {
        boolean success = true;

        for (String child : children) {
            success = addEdge(parent, child);

            if (!success)
                break;
        }

        return success;
    }

    public Set<String> getLeafLabels() {
        Set<String> set = labelTree.getLeafLabels();
        return set;
    }

    @SuppressWarnings("unchecked")
    public boolean isLeaf(String label) {
        T node = (T) T.makeBasicNode(label);
        return isLeaf(node);
    }

    @SuppressWarnings("unchecked")
    public int getDepth(String label) {
        return getDepth((T) T.makeBasicNode(label));
    }

    @SuppressWarnings("unchecked")
    public List<T> getAllParents(String label) {
        List<T> parentNodes = getAllParents((T) T.makeBasicNode(label));

        if (parentNodes == null)
            return null;

        List<T> parents = new ArrayList<>(parentNodes.size());

        parents.addAll(parentNodes);

        return parents;
    }

    public List<String> getAllParentLabels(String label) {
        List<T> parentNodes = getAllParents(label);

        if (parentNodes == null)
            return null;

        List<String> parents = new ArrayList<>(parentNodes.size());

        for (T p : parentNodes) {
            parents.add(p.getLabelID());
        }

        return parents;
    }

    @SuppressWarnings("unchecked")
    public T getNodeFromLabel(String label) {
        T node = getNode((T) T.makeBasicNode(label));

        return node;
    }

    public Set<T> getSameLevelNodes(String label) {
        int depth = getDepth(label);

        if (depth == -1)
            return null;

        List<T> nodes = getBreadthOrderedNodeList();

        Set<T> output = new HashSet<>();

        for (T node : nodes) {
            int thisDepth = getDepth(node);

            if (thisDepth > depth)
                break;
            else if (thisDepth == depth)
                output.add(node);
        }

        return output;
    }

    public Set<String> getSameLevelLabels(String label) {
        Set<T> nodes = getSameLevelNodes(label);

        if (nodes == null)
            return null;

        Set<String> output = new HashSet<>(nodes.size());

        for (T p : nodes) {
            output.add(p.getLabelID());
        }

        return output;
    }

    public void initializeTreeStructure() {
        List<String> nodes = labelTree.getBreadthOrderedLabelList();

        for (String node : nodes) {
            if (!labelTree.isLeaf(node)) {
                Set<String> children = labelTree.getChildren(node);
                addEdges(node, children);
            }
        }
    }
}
