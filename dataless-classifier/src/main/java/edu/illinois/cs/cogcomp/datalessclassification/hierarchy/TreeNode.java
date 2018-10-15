/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.hierarchy;

import java.io.Serializable;

/**
 * The most basic Node class; contains just the ID of the label
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class TreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String labelID;

    /**
     * A convenience factory function to create a basic TreeNode
     */
    public static TreeNode makeBasicNode(String labelID) {
        TreeNode node = new TreeNode(labelID);
        return node;
    }

    /**
     * Copy Constructor
     */
    public TreeNode(TreeNode thatNode) {
        this(thatNode.getLabelID());
    }

    /**
     * Initializes the TreeNode with the provided labelID
     */
    public TreeNode(String labelID) {
        this.labelID = labelID;
    }

    /**
     * Gets the LabelID for the node
     */
    public String getLabelID() {
        return this.labelID;
    }

    /**
     * Sets the LabelID for the node
     */
    public void setLabelID(String labelID) {
        this.labelID = labelID;
    }

    @Override
    public String toString() {
        return labelID;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TreeNode))
            return false;

        TreeNode other = (TreeNode) o;
        return this.labelID.equals(other.labelID);
    }

    @Override
    public int hashCode() {
        return labelID.hashCode();
    }
}
