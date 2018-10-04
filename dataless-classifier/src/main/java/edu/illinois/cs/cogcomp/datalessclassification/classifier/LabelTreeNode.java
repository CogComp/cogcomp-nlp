/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import edu.illinois.cs.cogcomp.datalessclassification.hierarchy.TreeNode;

/**
 * The Node Class used by {@link LabelTree} internally.
 * Wraps labelID, labelName and labelDescription within it.
 *
 * @author shashank
 */

public class LabelTreeNode extends TreeNode {

    private static final long serialVersionUID = 1L;
    private String labelName;
    private String labelDescription;

    /**
     * A convenience factory function to create a basic LabelTreeNode
     */
    public static LabelTreeNode makeBasicNode(String labelID) {
        LabelTreeNode node = new LabelTreeNode(labelID, "", "");
        return node;
    }

    /**
     * Copy Constructor
     */
    public LabelTreeNode(LabelTreeNode thatNode) {
        this(thatNode.getLabelID(), thatNode.getLabelName(), thatNode.getLabelDescription());
    }

    /**
     * Initializes the Node with the provided labelID, labelName and labelDescription
     */
    LabelTreeNode(String labelID, String labelName, String labelDesc) {
        super(labelID);
        setLabelName(labelName);
        setLabelDescription(labelDesc);
    }

    /**
     * Gets the LabelDescription for the node
     */
    String getLabelDescription() {
        return this.labelDescription;
    }

    /**
     * Sets the LabelDescription for the node
     */
    void setLabelDescription(String description) {
        this.labelDescription = description;
    }

    /**
     * Gets the LabelName for the node
     */
    String getLabelName() {
        return this.labelName;
    }

    /**
     * Sets the LabelName for the node
     */
    void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LabelTreeNode))
            return false;

        LabelTreeNode other = (LabelTreeNode) o;

        return this.labelID.equals(other.getLabelID());
    }

    @Override
    public int hashCode() {
        return labelID.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append(labelID).append("\t");
        sb.append(labelName).append("\t");
        sb.append(labelDescription);

        return sb.toString();
    }
}
