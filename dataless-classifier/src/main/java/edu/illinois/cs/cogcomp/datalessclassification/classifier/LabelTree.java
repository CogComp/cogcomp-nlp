/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.datalessclassification.hierarchy.SimpleTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the user-desired label ontology/hierarchy, in which each node has a
 * - labelID
 * - labelName
 * - labelDescription
 *
 * @author shashank
 */

public class LabelTree extends SimpleTree<LabelTreeNode> {

    private static Logger logger = LoggerFactory.getLogger(LabelTree.class);
    private static final long serialVersionUID = 1L;

    private String root_label;

    public LabelTree() {
        this("root");
    }

    public LabelTree(String root) {
        super();
        this.root_label = root;
        initializeRoot(root_label);
    }

    /**
     * Copy Constructor
     */
    public LabelTree(LabelTree thatTree) {
        this(thatTree.root_label);

        Set<String> topNodes = thatTree.getChildren(thatTree.root_label);

        Map<String, Set<String>> childMap = new HashMap<>();
        Map<String, String> labelNameMap = new HashMap<>();
        Map<String, String> labelDescriptionMap = new HashMap<>();

        for (LabelTreeNode labelNode : thatTree.getNodes()) {
            String labelID = labelNode.getLabelID();

            if (!thatTree.isLeaf(labelNode)) {
                childMap.put(labelID, thatTree.getChildren(labelID));
            }

            labelNameMap.put(labelID, labelNode.getLabelName());
            labelDescriptionMap.put(labelID, labelNode.getLabelDescription());
        }

        initializeTree(topNodes, childMap, labelNameMap, labelDescriptionMap);
    }

    /**
     * Initializes the root node of the tree
     */
    private void initializeRoot(String root_label) {
        LabelTreeNode root = LabelTreeNode.makeBasicNode(root_label);
        initializeRoot(root);
    }

    /**
     * Initializes the Tree completely, given the:
     * @param topNodes: Set containing the labelIDs of the top-level nodes in the tree
     * @param childMap: Map containing the parentID - childIds mapping
     * @param labelNameMap: Map containing the labelID - labelName mapping
     * @param labelDescriptionMap: Map containing the labelID - labelDescription mapping
     */
    public void initializeTree(Set<String> topNodes, Map<String, Set<String>> childMap,
            Map<String, String> labelNameMap, Map<String, String> labelDescriptionMap) {

        initializeTreeStructure(topNodes, childMap);
        initializeLabelNames(labelNameMap);
        initializeLabelDescriptions(labelDescriptionMap);
    }

    /**
     * Initializes the structure of the Tree, given the:
     * @param topNodes: Set containing the labelIDs of the top-level nodes in the tree
     * @param childMap: Map containing the parentID - childIds mapping
     */
    public void initializeTreeStructure(Set<String> topNodes, Map<String, Set<String>> childMap) {
        List<String> exploreNodes = new ArrayList<>();

        for (String topNode : topNodes)
            addEdge(root_label, topNode);

        exploreNodes.addAll(topNodes);

        while (exploreNodes.size() != 0) {
            String node = exploreNodes.get(0);

            if (childMap.containsKey(node)) {
                Set<String> children = childMap.get(node);
                addEdges(node, children);
                exploreNodes.addAll(children);
            }

            exploreNodes.remove(0);
        }
    }

    /**
     * Clears the labelNames for all the nodes
     */
    public void clearLabelNames() {
        for (LabelTreeNode node : getNodes())
            node.setLabelName("");
    }

    /**
     * Sets the labelNames from the labelID -> labelName map
     */
    public void initializeLabelNames(Map<String, String> labelNameMap) {
        for (String labelId : labelNameMap.keySet())
            setLabelName(labelId, labelNameMap.get(labelId));
    }

    /**
     * Clears the labelDescriptions for all the nodes
     */
    public void clearLabelDescriptions() {
        for (LabelTreeNode node : getNodes())
            node.setLabelDescription("");
    }

    /**
     * Sets the labelDescriptions from the labelID -> labelDescription map
     */
    public void initializeLabelDescriptions(Map<String, String> labelDescriptionMap) {
        for (String labelId : labelDescriptionMap.keySet())
            setLabelDescription(labelId, labelDescriptionMap.get(labelId));
    }

    /**
     * Adds a new node to the Tree, given the:
     * @param parent: the labelId of the parent (the parent node should exist before adding the children)
     * @param labelId: the labelId of the node
     * @param labelName: the labelName of the node
     * @param labelDesc: the labelDescription of the node
     */
    protected boolean addNode(String parent, String labelId, String labelName, String labelDesc) {
        LabelTreeNode node = new LabelTreeNode(labelId, labelName, labelDesc);
        LabelTreeNode parentNode = LabelTreeNode.makeBasicNode(parent);

        return addEdge(parentNode, node);
    }

    /**
     * Returns the labelIDs of the children of a particular node (labelID)
     */
    public Set<String> getChildren(String labelId) {
        Set<LabelTreeNode> set = getChildren(LabelTreeNode.makeBasicNode(labelId));

        if (set == null)
            return null;

        if (set.isEmpty())
            return Collections.emptySet();

        Set<String> labelSet = new HashSet<>(set.size());

        for (LabelTreeNode node : set) {
            labelSet.add(node.getLabelID());
        }

        return labelSet;
    }

    /**
     * Returns the labelIDs of the leaf nodes in the tree
     */
    public Set<String> getLeafLabels() {
        Set<LabelTreeNode> set = getLeafSet();

        if (set == null)
            return null;

        if (set.isEmpty())
            return Collections.emptySet();

        Set<String> newSet = new HashSet<>(set.size());

        for (LabelTreeNode node : set) {
            newSet.add(node.getLabelID());
        }

        return newSet;
    }

    /**
     * Returns the labelID of a parent of a particular node (labelID)
     */
    public String getParent(String labelId) {
        LabelTreeNode parent = getParent(LabelTreeNode.makeBasicNode(labelId));

        if (parent == null)
            return null;

        return parent.getLabelID();
    }

    /**
     * Returns the labelName of a particular node (labelID) in the tree
     */
    public String getLabelName(String labelId) {
        LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));

        if (node == null)
            return null;

        return node.getLabelName();
    }

    /**
     * Sets the labelName of a particular node (labelID) in the tree
     */
    public boolean setLabelName(String labelId, String labelName) {
        LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));

        if (node == null)
            return false;

        node.setLabelName(labelName);
        return true;
    }

    /**
     * Returns the labelDescription of a particular node (labelID) in the tree
     */
    public String getLabelDescription(String labelId) {
        LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));

        if (node == null)
            return null;

        return node.getLabelDescription();
    }

    /**
     * Sets the labelDescription of a particular node (labelID) in the tree
     */
    public boolean setLabelDescription(String labelId, String labelDesc) {
        LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));

        if (node == null)
            return false;

        node.setLabelDescription(labelDesc);
        return true;
    }

    /**
     * Adds an edge between a parentNode and a childNode
     */
    protected boolean addEdge(String parent, String child) {
        LabelTreeNode parentNode = LabelTreeNode.makeBasicNode(parent);
        LabelTreeNode childNode = LabelTreeNode.makeBasicNode(child);

        return addEdge(parentNode, childNode);
    }

    /**
     * Adds edges between a parent node and child nodes
     */
    protected boolean addEdges(String parent, Set<String> children) {
        boolean success = true;

        for (String child : children) {
            success = addEdge(parent, child);

            if (!success)
                break;
        }

        return success;
    }

    /**
     * Returns the Depth of a particular node (labelID) in the tree
     */
    public int getDepth(String labelId) {
        LabelTreeNode node = LabelTreeNode.makeBasicNode(labelId);
        return getDepth(node);
    }

    /**
     * Returns all the ancestors of a particular node (labelID) in the tree
     */
    public List<String> getAllParents(String labelId) {
        List<LabelTreeNode> parentNodes = getAllParents(LabelTreeNode.makeBasicNode(labelId));

        if (parentNodes == null)
            return null;

        List<String> parents = new ArrayList<>(parentNodes.size());

        for (LabelTreeNode p : parentNodes) {
            parents.add(p.getLabelID());
        }

        return parents;
    }

    /**
     * Traverses the Tree in a Breadth-First order and returns the labelIDs
     */
    public List<String> getBreadthOrderedLabelList() {
        List<LabelTreeNode> nodes = getBreadthOrderedNodeList();

        if (nodes == null)
            return null;

        List<String> labelIds = new ArrayList<>(nodes.size());

        for (LabelTreeNode p : nodes) {
            labelIds.add(p.getLabelID());
        }

        return labelIds;
    }

    /**
     * Traverses the Tree in a (Pre-Order) Depth-First order and returns the labelIDs
     */
    public List<String> getDepthOrderedLabelList() {
        List<LabelTreeNode> nodes = getDepthOrderedNodeList();

        if (nodes == null)
            return null;

        List<String> labelIds = new ArrayList<>(nodes.size());

        for (LabelTreeNode p : nodes) {
            labelIds.add(p.getLabelID());
        }

        return labelIds;
    }

    /**
     * Returns whether the provided labelID corresponds to a leaf in the Tree or not
     */
    public boolean isLeaf(String labelId) {
        LabelTreeNode node = LabelTreeNode.makeBasicNode(labelId);
        return isLeaf(node);
    }

    /**
     * A utility function that appends the label descriptions of the child nodes
     *  to their parents' description
     *
     * Since nodes in a topic/label hierarchy usually follow IS-A property, this function can enrich
     * the descriptions of the parent nodes
     */
    public void aggregateChildrenDescription() {
        List<LabelTreeNode> nodeList = getBreadthOrderedNodeList();

        Collections.reverse(nodeList);

        for (LabelTreeNode node : nodeList) {
            String childDesc = getLabelDescription(node.getLabelID());

            if (!isRoot(node)) {
                LabelTreeNode parent = getParent(node);
                String parentDesc = getLabelDescription(parent.getLabelID());

                String newLabelDesc = parentDesc.trim() + " " + childDesc.trim();

                setLabelDescription(parent.getLabelID(), newLabelDesc);
            }
        }
    }

    /**
     * A Utility function that just appends the labelName to the labelDescription.
     */
    public void appendLabelNameToDesc() {
        for (LabelTreeNode node : getNodes()) {
            String labelId = node.getLabelID();

            String description = getLabelDescription(labelId) + " " +
                    getLabelName(labelId);

            node.setLabelDescription(description);
        }
    }

    /**
     * A Utility function that just copies the labelNames to labelDescriptions.
     *
     * In scenarios, where users don't provide descriptions for their labels, this function can
     * be used a last resort for Dataless Classification
     */
    public void copyLabelNameToDesc() {
        for (LabelTreeNode node : getNodes()) {
            String labelId = node.getLabelID();
            String labelName = getLabelName(labelId);
            node.setLabelDescription(labelName);
        }
    }

    /**
     * Returns the labelIDs of all nodes at the same level as the provided node (labelID)
     */
    public Set<String> getSameLevelLabels(String labelId) {
        Set<LabelTreeNode> nodes = getSameLevelNodes(labelId);

        if (nodes == null)
            return null;

        Set<String> output = new HashSet<>(nodes.size());

        for (LabelTreeNode p : nodes) {
            output.add(p.getLabelID());
        }

        return output;
    }

    /**
     * Returns all the nodes at the same level as the provided node (labelID)
     */
    public Set<LabelTreeNode> getSameLevelNodes(String labelId) {
        int depth = getDepth(labelId);

        if (depth == -1)
            return null;

        List<LabelTreeNode> nodes = getBreadthOrderedNodeList();

        Set<LabelTreeNode> output = new HashSet<>();

        for (LabelTreeNode node : nodes) {
            int thisDepth = getDepth(node);

            if (thisDepth > depth)
                break;
            else if (thisDepth == depth)
                output.add(node);
        }

        return output;
    }

    /**
     * A utility function which can be used to identify the top-level nodes in the tree,
     * if such an information is not explicitly provided by the end-user.
     *
     * This function uses the parent-children map to identify the top-level nodes.
     */
    public static Set<String> identifyTopNodes(Map<String, Set<String>> childMap) {
        Set<String> candidateTopNodes = new HashSet<>();
        Set<String> topNodes = new HashSet<>();

        Set<String> children = new HashSet<>();

        for (String parent : childMap.keySet()) {
            if (!children.contains(parent))
                candidateTopNodes.add(parent);

            children.addAll(childMap.get(parent));
        }

        for (String candidate : candidateTopNodes) {
            if (!children.contains(candidate))
                topNodes.add(candidate);
        }

        return topNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LabelTree))
            return false;

        LabelTree that = (LabelTree) o;

        if (!root_label.equals(that.root_label))
            return false;

        if (!super.equals(that))
            return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        List<LabelTreeNode> nodes = getBreadthOrderedNodeList();

        for (LabelTreeNode node : nodes) {
            if (!isLeaf(node)) {
                for (LabelTreeNode child : getChildren(node)) {
                    sb.append(node.getLabelID()).append("\t").append(child.getLabelID()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * This Utility function dumps a text representation of the tree to the disk.
     */
    public void dumpTreeLabelDesc(String outPath) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outPath)))) {
            StringBuilder sb = new StringBuilder("");

            List<LabelTreeNode> nodes = getBreadthOrderedNodeList();

            for (LabelTreeNode node : nodes) {
                if (isRoot(node))
                    continue;

                sb.append(node.getLabelID()).append("\t").append(node.getLabelDescription()).append("\n");
            }

            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while writing file");
            throw new RuntimeException("IO Error while writing file");
        }
    }
}
