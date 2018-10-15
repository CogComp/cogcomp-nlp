/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The Node class used by {@link LabelResultTree}
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class LabelResultTreeNode {
    private LabelScorePair labelScorePair;
    private boolean isLeaf;
    private int depth;

    private List<LabelResultTreeNode> children;

    public LabelResultTreeNode() {
        isLeaf = false;
        children = new ArrayList<>();
    }

    public LabelScorePair getLabelScorePair() {
        return labelScorePair;
    }

    public void setLabelScorePair(LabelScorePair labelScorePair) {
        this.labelScorePair = labelScorePair;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<LabelResultTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<LabelResultTreeNode> children) {
        this.children = children;
    }
}
