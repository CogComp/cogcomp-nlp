/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */

//TODO: Transition this class to a setting wherein it extends our Tree API
public class LabelResultML {
	private LabelResultTreeNode rootLabel;
	private Map<Integer, List<LabelScorePair>> depthLabelMap;
	
	public LabelResultML () {
		rootLabel = new LabelResultTreeNode();
	}
	
	public LabelResultTreeNode getRootLabel () {
		return rootLabel;
	}

	public void setRootLabel (LabelResultTreeNode rootLabel) {
		this.rootLabel = rootLabel;
	}

	public Map<Integer, List<LabelScorePair>> getDepthLabelMap () {
		return depthLabelMap;
	}

	public void setDepthLabelMap (Map<Integer, List<LabelScorePair>> depthLabelMap) {
		this.depthLabelMap = depthLabelMap;
	}

	public Map<Integer, List<LabelScorePair>> processLabels() {
		processLabels(rootLabel, 1);
		
		for (Integer depth : depthLabelMap.keySet()) {
			Collections.sort(depthLabelMap.get(depth));
			Collections.reverse(depthLabelMap.get(depth));
		}
		
		return depthLabelMap;
	}
	
	//TODO: Make it non-recursive
	private void processLabels (LabelResultTreeNode node, double parentScore) {
		if (depthLabelMap == null) {
			depthLabelMap = new HashMap<Integer, List<LabelScorePair>>();
		}
		
		if (depthLabelMap.containsKey(node.getDepth()) == false) {
			depthLabelMap.put(node.getDepth(), new ArrayList<LabelScorePair>());
		}
		
		LabelScorePair labelScorePair = node.getLabelScorePair();
		labelScorePair.setScore(labelScorePair.getScore() * parentScore);
		depthLabelMap.get(node.getDepth()).add(labelScorePair);
		
		for (int i = 0; i < node.getChildren().size(); i++) {
			processLabels (node.getChildren().get(i),  1); //labelKVP.getScore());//
		}
	}
}

