/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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

import org.apache.log4j.Logger;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.DatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.util.HashSort;
import edu.illinois.cs.cogcomp.datalessclassification.util.LabelScorePair;
import edu.illinois.cs.cogcomp.datalessclassification.util.LabelResultML;
import edu.illinois.cs.cogcomp.datalessclassification.util.LabelResultTreeNode;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVectorOperations;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */
public class DatalessClassifierML<T extends Serializable> implements IConceptClassificationTree<T> {

	static Logger logger = Logger.getLogger(DatalessClassifierML.class);
	
	private ConceptTree<T> conceptTree;
	
	private boolean bottomUp;
	
	double classifierThreshold;
	int classifierLeastK;
	int classifierMaxK;
	
	public DatalessClassifierML (ConceptTree<T> conceptTree) {
		this(new DatalessConfigurator().getDefaultConfig(), conceptTree);
	}
	
	public DatalessClassifierML (ResourceManager config, ConceptTree<T> conceptTree) {
		this.conceptTree = conceptTree;
		
		this.bottomUp = config.getBoolean(DatalessConfigurator.BottomUp_Inference.key);
		this.classifierThreshold = config.getDouble(DatalessConfigurator.classifierThreshold.key);
		this.classifierLeastK = config.getInt(DatalessConfigurator.classifierLeastK.key);
		this.classifierMaxK = config.getInt(DatalessConfigurator.classifierMaxK.key);
	}
	
	@Override
	public Map<Integer, List<LabelScorePair>> getFullPredictions (SparseVector<T> docVector) {
		return getFullPredictions(docVector, new HashMap<T, Double>());
	}
	
	@Override
	public Set<String> getFlatPredictions (SparseVector<T> docVector, int topK) {
		Map<Integer, Set<String>> testDepthLabelMap = getDepthPredictions(docVector, topK);
		
		Set<String> predictedLabels = new HashSet<>();
		
		for (Set<String> labels : testDepthLabelMap.values()) {
			predictedLabels.addAll(labels);
		}
		
		return predictedLabels;
	}
	
	@Override
	public Map<Integer, Set<String>> getDepthPredictions (SparseVector<T> docVector, int topK) {
		Map<Integer, Set<String>> testDepthLabelMap = new HashMap<>();
		
		Map<Integer, List<LabelScorePair>> labelResultsInDepth = getFullPredictions(docVector);
		
		for (int depth : labelResultsInDepth.keySet()) {
			//TOOD: This is very risky and needs to go once the labelTree and ConceptTree classes have been refactored
			if (depth == 0)
				continue;
			
			List<LabelScorePair> classifiedLabelList = labelResultsInDepth.get(depth);
			
			if (classifiedLabelList == null) {
				classifiedLabelList = new ArrayList<LabelScorePair>();
			}
			
			Set<String> classifiedLabelSet = new HashSet<String>();
			
			//TODO: classifiedLabelList can have duplicated labels with different scores -- is that desirable?
			for (int i = 0; i < Math.min(topK, classifiedLabelList.size()); i++) {
				classifiedLabelSet.add(classifiedLabelList.get(i).getLabel());
			}
			
			testDepthLabelMap.put(depth, classifiedLabelSet);
		}
		
		return testDepthLabelMap;
	}
	
	
	public Map<Integer, List<LabelScorePair>> getFullPredictions (SparseVector<T> docVector, Map<T, Double> conceptWeights) {
		if (bottomUp)
			return getFullPredictionsBottomUp(docVector, conceptWeights);
		else
			return getFullPredictionsTopDown(docVector, conceptWeights);
	}

	/**
	 * Selects some leaf nodes (TopK/AboveThreshold), and then selects their path to the root as the output label set
	 */
	private Map<Integer, List<LabelScorePair>> getFullPredictionsBottomUp (SparseVector<T> docVector, Map<T, Double> conceptWeights) {
		double classifierMLThreshold = classifierThreshold;
		int leastK = classifierLeastK;
		int maxK = classifierMaxK;
		
		Set<ConceptTreeNode<T>> leafSet = conceptTree.getLeafSet();
		
		Map<String, Double> orgSimilarities = new HashMap<String, Double>();
		Map<String, Double> normalizedSimilarities = new HashMap<String, Double>();
		
		
		/**
		 * We calculate normalized similarities so as to be able to threshold at a particular (absolute) value while selecting the labels
		 */
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		
		for (ConceptTreeNode<T> leaf : leafSet) {
			double similarity = SparseVectorOperations.cosine(leaf.getConceptVector(), docVector, conceptWeights);
			orgSimilarities.put(leaf.getLabel(), similarity);
			
			if (similarity > maxSimilarity) {
				 maxSimilarity = similarity;
			}
			
			if (similarity < minSimilarity) {
				minSimilarity = similarity;
			}	
		}
		
		if (minSimilarity < 0) {
			for (String simKey : orgSimilarities.keySet()) {
				orgSimilarities.put(simKey, orgSimilarities.get(simKey) - minSimilarity);
				maxSimilarity = maxSimilarity - minSimilarity;
				minSimilarity = 0;
			}
		}
		
		double sumSimilarity = 0;
		
		for (String leafLabel : orgSimilarities.keySet()) {
			double value = (orgSimilarities.get(leafLabel) - minSimilarity) / (maxSimilarity - minSimilarity + Double.MIN_VALUE);
			
			if (orgSimilarities.size() == 1) {
				value = 1;
			}
			
			normalizedSimilarities.put(leafLabel, value);
			sumSimilarity += value;
		}
		
		for (String leafLabel : normalizedSimilarities.keySet()) {
			normalizedSimilarities.put(leafLabel, normalizedSimilarities.get(leafLabel) / (sumSimilarity + Double.MIN_VALUE));
		}
		
		Map<Integer, List<LabelScorePair>> depthLabelMap = new HashMap<Integer, List<LabelScorePair>>();

		TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(normalizedSimilarities);
		
		double ratio = 0;
		int labelCount = 0;
		
		/**
		 * Basically the portion of the code below selects certain leaf nodes (either by similarity threshold or by topK),
		 * and selects their path to the root in the tree -- with their scores being used as the scores of their leaf nodes.
		 * 
		 * TODO: There can be duplicate nodes at a particular level/depth with different similarity scores -- how are they handled later?
		 * Also check different scenarios with ratio and classifierMLThreshold comparison 
		 * -- for instance, the very first ratio comparison exceeding the threshold
		 */
		
		for (String leafLabel : sortedSimilarities.keySet()) {
			ratio += normalizedSimilarities.get(leafLabel);
			
			if ((ratio < classifierMLThreshold && labelCount < maxK) || labelCount < leastK) {
				String label = leafLabel;
				double leafSimilarity = orgSimilarities.get(leafLabel);
				
				while (label != null) {
					int depth = conceptTree.getDepth(label);
					
					if (depthLabelMap.containsKey(depth) == false) {
						depthLabelMap.put(depth, new ArrayList<LabelScorePair>());
					}
					
					LabelScorePair labelPair = new LabelScorePair(label, leafSimilarity);
					depthLabelMap.get(depth).add(labelPair);
					
					label = conceptTree.getLabelTree().getParent(label);
				}
			}
			
			labelCount++;
		}
		
		return depthLabelMap;
	}
	
	/**
	 * Selects at most K children per node, while traversing Top-Down in the tree.
	 */
	private Map<Integer, List<LabelScorePair>> getFullPredictionsTopDown (SparseVector<T> documentConceptVector, Map<T, Double> conceptWeights) {
		LabelResultML labelResult = new LabelResultML();
		LabelScorePair labelPair = new LabelScorePair(conceptTree.getRootLabel(), 1);
		labelResult.getRootLabel().setLabelScorePair(labelPair);
		labelResult.getRootLabel().setDepth(0);
		
		labelResult.setRootLabel(retrieveLabelTopDown(documentConceptVector, conceptTree.getRoot(), labelResult.getRootLabel(), conceptWeights));
		
		Map<Integer, List<LabelScorePair>> labelResultsInDepth = labelResult.processLabels();
		return labelResultsInDepth;
	}
	
	/**
	 * TODO: rootLabel is modified inside the function here -- seems like a non-standard way of doing what this function does.
	 * Convert this from a recursive function to a loop
	 */
	public LabelResultTreeNode retrieveLabelTopDown (SparseVector<T> docConceptVector, ConceptTreeNode<T> rootNode, 
			LabelResultTreeNode rootLabel, Map<T, Double> conceptWeights) {
		
		int maxK = classifierMaxK;
		
		Map<String, Double> orgSimilarities = new HashMap<String, Double>();
		Map<String, Double> similarities = new HashMap<String, Double>();
		Map<String, ConceptTreeNode<T>> childrenMap = new HashMap<>();
		
		double maxSimilarity = 0 - Double.MAX_VALUE;
		double minSimilarity = Double.MAX_VALUE;
		
		for (ConceptTreeNode<T> child : conceptTree.getChildren(rootNode)) {
			double similarity = SparseVectorOperations.cosine(docConceptVector, child.getConceptVector(), conceptWeights); 
			orgSimilarities.put(child.getLabel(), similarity);
			childrenMap.put(child.getLabel(), child);
			
			if (child.getLabel().contains("Sports")) {
				logger.info("\n");
			}
			
			if (similarity > maxSimilarity) {
				 maxSimilarity = similarity;
			}
			
			if (similarity < minSimilarity) {
				minSimilarity = similarity;
			}
		}
		
		if (minSimilarity < 0) {
			for (String simiKey : orgSimilarities.keySet()) {
				orgSimilarities.put(simiKey, orgSimilarities.get(simiKey) - minSimilarity);
				/*
				 * TODO: Shouldn't we update maxSimilarity and minSimilarity here as well
				 * Or, why not do this regardless of the value of minSimilarity
				 */
			}
		}
		
		double sumSimilarity = 0;
		
		for (String simKey : orgSimilarities.keySet()) {
			double value = (orgSimilarities.get(simKey) - minSimilarity) / (maxSimilarity - minSimilarity + Double.MIN_VALUE);
			
			if (orgSimilarities.size() == 1) {
				value = 1;
			}
			
			similarities.put(simKey, value);
			sumSimilarity += value;
		}
		
		for (String simKey : similarities.keySet()) {
			similarities.put(simKey, similarities.get(simKey) / (sumSimilarity + Double.MIN_VALUE));
		}
		
		if (conceptTree.getChildCount(rootNode) == 0) {
			rootLabel.setIsLeaf(true);
		} 
		else {
			rootLabel.setIsLeaf(false);
		}
		
		TreeMap<String, Double> sortedSimilarities = HashSort.sortByValues(similarities);
		
//		double ratio = 0;
		int labelCount = 0;
		
		if (sumSimilarity > 0) {
			for (String simKey : sortedSimilarities.keySet()) {
//				ratio += similarities.get(simiKey);
//				if ((ratio < classifierMLThreshold && labelCount < maxK) || labelCount < leastK) {

				if (labelCount < maxK && similarities.get(simKey) > 0) {
					LabelScorePair labelPair = new LabelScorePair(simKey, orgSimilarities.get(simKey));
					LabelResultTreeNode labelNode = new LabelResultTreeNode();
					labelNode.setLabelScorePair(labelPair);
					labelNode.setDepth(rootLabel.getDepth() + 1);
					
					rootLabel.getChildren().add(labelNode);
					
					retrieveLabelTopDown(docConceptVector, childrenMap.get(simKey), labelNode, conceptWeights);
				}
				
				labelCount++;
				
				if (labelCount >= maxK) {
					break;
				}
			}
		}
		
		return rootLabel;
	}
}