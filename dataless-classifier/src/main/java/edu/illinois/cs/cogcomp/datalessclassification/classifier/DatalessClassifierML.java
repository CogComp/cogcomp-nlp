/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.DatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.util.HashSort;
import edu.illinois.cs.cogcomp.datalessclassification.util.LabelScorePair;
import edu.illinois.cs.cogcomp.datalessclassification.util.LabelResultTree;
import edu.illinois.cs.cogcomp.datalessclassification.util.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVectorOperations;

/**
 * The class which implements various Inference algorithms for the Multi-Label Hierarchical Dataless Classification.
 * - Supports both Bottom-Up and Top-Down inference
 * - Provides support for controlling the minimum and maximum number of labels selected at each level
 * - Provides support for controlling the number of labels selected at a level based on the cumulative similarity score
 * - Provides functions to retrieve just a flat-list of selected labels, or the full depth-level classification information
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */
public class DatalessClassifierML<T extends Serializable> implements IConceptClassificationTree<T> {
    private ConceptTree<T> conceptTree;

    private boolean bottomUp;

    private double classifierThreshold;
    private int classifierLeastK;
    private int classifierMaxK;

    public DatalessClassifierML(ResourceManager config, ConceptTree<T> conceptTree) {
        this.conceptTree = conceptTree;

        this.bottomUp = config.getBoolean(DatalessConfigurator.BottomUp_Inference.key);
        this.classifierThreshold = config.getDouble(DatalessConfigurator.classifierThreshold.key);
        this.classifierLeastK = config.getInt(DatalessConfigurator.classifierLeastK.key);
        this.classifierMaxK = config.getInt(DatalessConfigurator.classifierMaxK.key);
    }

    @Override
    public Map<Integer, List<LabelScorePair>> getFullDepthPredictions(SparseVector<T> docVector) {
        return getFullDepthPredictions(docVector, new HashMap<>());
    }

    /**
     * Overrides getFullDepthPredictions to provide additional functionality for providing different weights to different dimensions
     *  of the underlying embedding
     */
    public Map<Integer, List<LabelScorePair>> getFullDepthPredictions(SparseVector<T> docVector,
                                                                      Map<T, Double> conceptWeights) {
        if (bottomUp)
            return getFullDepthPredictionsBottomUp(docVector, conceptWeights);
        else
            return getFullDepthPredictionsTopDown(docVector, conceptWeights);
    }

    /**
     * Selects some leaf nodes (using either TopK or a score based Threshold), and then selects their path to the root as the
     * output label set, while reusing the scores of the leaf labels.
     *
     * Returns a Map, where key is the depth, and value is a list of selected labelIDs at that depth with their absolute similarity scores
     */
    private Map<Integer, List<LabelScorePair>> getFullDepthPredictionsBottomUp(SparseVector<T> docVector, Map<T, Double> conceptWeights) {
        double classifierMLThreshold = classifierThreshold;
        int leastK = classifierLeastK;
        int maxK = classifierMaxK;

        Set<ConceptTreeNode<T>> leafSet = conceptTree.getLeafSet();

        Map<String, Double> orgSimilarities = new HashMap<>();
        Map<String, Double> normalizedSimilarities = new HashMap<>();


        /**
         * We calculate normalized similarities so as to be able to threshold at a particular
         * (absolute) value while selecting the labels
         */
        double maxSimilarity = 0 - Double.MAX_VALUE;
        double minSimilarity = Double.MAX_VALUE;

        for (ConceptTreeNode<T> leafNode : leafSet) {
            double similarity =
                    SparseVectorOperations.cosine(leafNode.getConceptVector(), docVector,
                            conceptWeights);
            orgSimilarities.put(leafNode.getLabelID(), similarity);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }

            if (similarity < minSimilarity) {
                minSimilarity = similarity;
            }
        }

        if (minSimilarity < 0) {
            for (String labelID : orgSimilarities.keySet()) {
                orgSimilarities.put(labelID, orgSimilarities.get(labelID) - minSimilarity);
                maxSimilarity = maxSimilarity - minSimilarity;
                minSimilarity = 0;
            }
        }

        double sumSimilarity = 0;

        for (String leafLabel : orgSimilarities.keySet()) {
            double value =
                    (orgSimilarities.get(leafLabel) - minSimilarity)
                            / (maxSimilarity - minSimilarity + Double.MIN_VALUE);

            if (orgSimilarities.size() == 1) {
                value = 1;
            }

            normalizedSimilarities.put(leafLabel, value);
            sumSimilarity += value;
        }

        for (String leafLabel : normalizedSimilarities.keySet()) {
            normalizedSimilarities.put(leafLabel, normalizedSimilarities.get(leafLabel)
                    / (sumSimilarity + Double.MIN_VALUE));
        }

        Map<Integer, List<LabelScorePair>> depthLabelMap = new HashMap<>();

        TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(normalizedSimilarities);

        double ratio = 0;
        int labelCount = 0;

        /**
         * Basically the portion of the code below selects certain leaf nodes (either by similarity
         * threshold or by topK), and selects their path to the root in the tree -- with their
         * scores being used as the scores of their leaf nodes.
         */

        for (String leafLabelID : sortedSimilarities.keySet()) {
            ratio += normalizedSimilarities.get(leafLabelID);

            if ((ratio < classifierMLThreshold && labelCount < maxK) || labelCount < leastK) {
                String labelID = leafLabelID;
                double leafSimilarity = orgSimilarities.get(leafLabelID);

                while (labelID != null) {
                    int depth = conceptTree.getDepth(labelID);

                    if (!depthLabelMap.containsKey(depth)) {
                        depthLabelMap.put(depth, new ArrayList<>());
                    }

                    LabelScorePair labelPair = new LabelScorePair(labelID, leafSimilarity);
                    depthLabelMap.get(depth).add(labelPair);

                    labelID = conceptTree.getLabelTree().getParent(labelID);
                }
            }

            labelCount++;
        }

        return depthLabelMap;
    }

    /**
     * Gets the DepthPredictions (using either bottomUp or topDown) and then just returns
     *  a flat-bag of selected labelIDs (independent of their depth in the tree)
     *
     * Use this function if you just want a flat-list of selected labelIDs from the tree, where only topK labels
     *  have been selected at each level
     */
    @Override
    public Set<String> getFlatPredictions(SparseVector<T> docVector, int topK) {
        Map<Integer, Set<String>> testDepthLabelMap = getPrunedDepthPredictions(docVector, topK);

        Set<String> predictedLabels = new HashSet<>();

        for (Set<String> labels : testDepthLabelMap.values()) {
            predictedLabels.addAll(labels);
        }

        return predictedLabels;
    }

    /**
     * Gets the FullPredictions (using either bottomUp or topDown) and then selects at most topK labels at each level
     *
     * Return a Map, where key is the Depth, and the value is the Set of selected labelIDs at the depth
     *
     * Use this function when you want the depth information associated with the selected labelIDs as well, and want to
     *   limit the number of labels selected at each depth
     */
    @Override
    public Map<Integer, Set<String>> getPrunedDepthPredictions(SparseVector<T> docVector, int topK) {
        Map<Integer, Set<String>> testDepthLabelMap = new HashMap<>();

        Map<Integer, List<LabelScorePair>> labelResultsInDepth = getFullDepthPredictions(docVector);

        for (int depth : labelResultsInDepth.keySet()) {
            /**
                TODO: This block assumes that Depth = 0 will always be the Root Node of the Tree
                TODO: However, instead of the actual root node provided by the end-user, the underlying tree implementation might use a placeholder
                TODO:        for the root node, and thus this check might lead to some logical errors later.

                TODO: Thus, this is very risky and needs to go once the labelTree and ConceptTree classes have been refactored
             */
            if (depth == 0)
                continue;

            List<LabelScorePair> classifiedLabelList = labelResultsInDepth.get(depth);

            if (classifiedLabelList == null) {
                classifiedLabelList = new ArrayList<>();
            }

            Set<String> classifiedLabelSet = new HashSet<>();

            for (int i = 0; i < Math.min(topK, classifiedLabelList.size()); i++) {
                //This check is currently required since labelIDs are represented as a String, and thus they might clash
                if (!classifiedLabelSet.contains(classifiedLabelList.get(i).getLabelID()))
                    classifiedLabelSet.add(classifiedLabelList.get(i).getLabelID());
            }

            testDepthLabelMap.put(depth, classifiedLabelSet);
        }

        return testDepthLabelMap;
    }

    /**
     * Selects at most K children per node, while traversing Top-Down in the tree.
     */
    private Map<Integer, List<LabelScorePair>> getFullDepthPredictionsTopDown(SparseVector<T> documentConceptVector, Map<T, Double> conceptWeights) {
        LabelResultTree labelResult = new LabelResultTree();
        LabelScorePair labelPair = new LabelScorePair(conceptTree.getRootLabel(), 1);

        LabelResultTreeNode resultTreeRootNode = labelResult.getRootNode();

        resultTreeRootNode.setLabelScorePair(labelPair);
        resultTreeRootNode.setDepth(0);

        retrieveLabelTopDown(documentConceptVector, conceptTree.getRoot(), resultTreeRootNode, conceptWeights);

        Map<Integer, List<LabelScorePair>> labelResultsInDepth = labelResult.getFullDepthPredictions();
        return labelResultsInDepth;
    }

    /**
     * Recursive function
     *
     * Overall, given the Root of a ConceptTree, this function selects at most K children for each Node,
     *  creates a corresponding LabelResultTree, and returns the root of that Tree.
     */
    private void retrieveLabelTopDown(SparseVector<T> docConceptVector,
            ConceptTreeNode<T> conceptTreeRootNode, LabelResultTreeNode resultTreeRootNode,
            Map<T, Double> conceptWeights) {

        int maxK = classifierMaxK;

        Map<String, Double> orgSimilarities = new HashMap<>();
        Map<String, Double> similarities = new HashMap<>();
        Map<String, ConceptTreeNode<T>> labelIdNodeMap = new HashMap<>();

        double maxSimilarity = 0 - Double.MAX_VALUE;
        double minSimilarity = Double.MAX_VALUE;

        for (ConceptTreeNode<T> childNode : conceptTree.getChildren(conceptTreeRootNode)) {
            double similarity =
                    SparseVectorOperations.cosine(docConceptVector, childNode.getConceptVector(),
                            conceptWeights);

            orgSimilarities.put(childNode.getLabelID(), similarity);
            labelIdNodeMap.put(childNode.getLabelID(), childNode);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }

            if (similarity < minSimilarity) {
                minSimilarity = similarity;
            }
        }

        if (minSimilarity < 0) {
            for (String labelID : orgSimilarities.keySet()) {
                orgSimilarities.put(labelID, orgSimilarities.get(labelID) - minSimilarity);
                maxSimilarity = maxSimilarity - minSimilarity;
                minSimilarity = 0;
            }
        }

        double sumSimilarity = 0;

        for (String labelID : orgSimilarities.keySet()) {
            double value =
                    (orgSimilarities.get(labelID) - minSimilarity)
                            / (maxSimilarity - minSimilarity + Double.MIN_VALUE);

            if (orgSimilarities.size() == 1) {
                value = 1;
            }

            similarities.put(labelID, value);
            sumSimilarity += value;
        }

        for (String labelID : similarities.keySet()) {
            similarities.put(labelID, similarities.get(labelID) / (sumSimilarity + Double.MIN_VALUE));
        }

        if (conceptTree.getChildCount(conceptTreeRootNode) == 0) {
            resultTreeRootNode.setIsLeaf(true);
        } else {
            resultTreeRootNode.setIsLeaf(false);
        }

        TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);

        int labelCount = 0;

        List<LabelResultTreeNode> labelResultChildNodes = resultTreeRootNode.getChildren();

        if (sumSimilarity > 0) {
            for (String labelID : sortedSimilarities.keySet()) {
                if (labelCount < maxK && similarities.get(labelID) > 0) {
                    LabelScorePair labelPair =
                            new LabelScorePair(labelID, orgSimilarities.get(labelID));

                    LabelResultTreeNode labelResultChildNode = new LabelResultTreeNode();
                    labelResultChildNode.setLabelScorePair(labelPair);
                    labelResultChildNode.setDepth(resultTreeRootNode.getDepth() + 1);

                    labelResultChildNodes.add(labelResultChildNode);

                    retrieveLabelTopDown(docConceptVector, labelIdNodeMap.get(labelID), labelResultChildNode,
                            conceptWeights);
                }

                labelCount++;

                if (labelCount >= maxK) {
                    break;
                }
            }
        }
    }
}
