/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used by the Dataless Classifier for selecting labels while traversing Top-Down
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class LabelResultTree {
    private LabelResultTreeNode rootNode;

    public LabelResultTree() {
        rootNode = new LabelResultTreeNode();
    }

    public LabelResultTreeNode getRootNode() {
        return rootNode;
    }

    /**
     * Returns a Map, where key is the depth, and value is a list of the metadata associated with the nodes at that depth
     */
    public Map<Integer, List<LabelScorePair>> getFullDepthPredictions() {
        Map<Integer, List<LabelScorePair>> depthLabelMap = new HashMap<>();

        populateDepthPredictions(rootNode, 1, depthLabelMap);

        for (Integer depth : depthLabelMap.keySet()) {
            Collections.sort(depthLabelMap.get(depth));
            Collections.reverse(depthLabelMap.get(depth));
        }

        return depthLabelMap;
    }

    /**
     * Recursively populates the DepthLabelMap
     */
    private void populateDepthPredictions(LabelResultTreeNode root, double parentScore, Map<Integer, List<LabelScorePair>> depthLabelMap) {
        if (!depthLabelMap.containsKey(root.getDepth())) {
            depthLabelMap.put(root.getDepth(), new ArrayList<>());
        }

        LabelScorePair labelScorePair = root.getLabelScorePair();
        labelScorePair.setScore(labelScorePair.getScore() * parentScore);
        depthLabelMap.get(root.getDepth()).add(labelScorePair);

        for (LabelResultTreeNode childNode: root.getChildren()) {
            populateDepthPredictions(childNode, 1, depthLabelMap);
        }
    }
}
