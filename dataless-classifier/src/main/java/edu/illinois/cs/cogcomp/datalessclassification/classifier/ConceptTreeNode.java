/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.Serializable;

import edu.illinois.cs.cogcomp.datalessclassification.hierarchy.TreeNode;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 *
 * The Node Class used by {@link ConceptTree} internally.
 * Wraps labelDescription and vector representation of the node within it.
 *
 * ConceptTreeNode is to {@link ConceptTree}, as {@link LabelTreeNode} is to {@link LabelTree}
 *
 * Each ConceptTreeNode contains the vector representation for a node (labelID) in the LabelTree.
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class ConceptTreeNode<T extends Serializable> extends TreeNode {

    private static final long serialVersionUID = 1L;

    private String labelDescription;
    private SparseVector<T> conceptVector;

    /**
     * A convenience factory function to create a basic ConceptTreeNode
     */
    public static <T extends Serializable> ConceptTreeNode<T> makeBasicTypedNode(String labelID) {
        ConceptTreeNode<T> node = new ConceptTreeNode<>("", labelID, null);
        return node;
    }

    /**
     * A convenience factory function to create a ConceptTreeNode
     */
    public static <T extends Serializable> ConceptTreeNode<T> makeNode(String labelDesc,
            String labelID, SparseVector<T> conceptVector) {
        ConceptTreeNode<T> node = new ConceptTreeNode<>(labelDesc, labelID, conceptVector);
        return node;
    }

    public ConceptTreeNode(String labelID) {
        this("", labelID);
    }

    public ConceptTreeNode(String labelDesc, String labelID) {
        this(labelDesc, labelID, null);
    }

    public ConceptTreeNode(String labelDesc, String labelID, SparseVector<T> conceptVector) {
        super(labelID);
        setLabelDescription(labelDesc);
        setConceptVector(conceptVector);
    }

    /**
     * Returns the labelDescription of the node
     */
    public String getLabelDescription() {
        return this.labelDescription;
    }

    /**
     * Sets the labelDescription of the node
     */
    public void setLabelDescription(String labelDesc) {
        this.labelDescription = labelDesc;
    }

    /**
     * Returns the vector representation of the node
     */
    public SparseVector<T> getConceptVector() {
        return this.conceptVector;
    }

    /**
     * Sets the vector representation of the node
     */
    public void setConceptVector(SparseVector<T> vector) {
        if (vector == null)
            vector = new SparseVector<>();

        this.conceptVector = vector;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConceptTreeNode<?>))
            return false;

        ConceptTreeNode<T> other = (ConceptTreeNode<T>) o;

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
        sb.append(conceptVector);

        return sb.toString();
    }
}
