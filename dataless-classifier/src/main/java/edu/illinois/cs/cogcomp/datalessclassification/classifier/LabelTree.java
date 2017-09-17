/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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

/**
 * @author shashank
 */

public class LabelTree extends SimpleTree<LabelTreeNode> {
	
	private static final long serialVersionUID = 1L;
	
	private String root_label;
	
	public LabelTree () {
		this("root");
	}
	
	public LabelTree (String root) {
		super();
		this.root_label = root;
		initializeRoot(root_label);
	}
	
	/**
	 * Copy Constructor
	 */
	public LabelTree (LabelTree thatTree) {
		this(thatTree.root_label);
		
		Set<String> topNodes = thatTree.getChildren(thatTree.root_label);
		
		Map<String, Set<String>> childMap = new HashMap<>();
		Map<String, String> labelNameMap = new HashMap<>();
		Map<String, String> labelDescriptionMap = new HashMap<>();
		
		for (LabelTreeNode labelNode : thatTree.getNodes()) {
			String label = labelNode.getLabel();
			
			if (thatTree.isLeaf(labelNode) == false) {
				childMap.put(label, thatTree.getChildren(label));
			}
			
			labelNameMap.put(label, labelNode.getLabelName());
			labelDescriptionMap.put(label, labelNode.getLabelDescription());
		}
		
		initializeTree(topNodes, childMap, labelNameMap, labelDescriptionMap);
	}
	
	private void initializeRoot (String root_label) {
		LabelTreeNode root = LabelTreeNode.makeBasicNode(root_label);
		initializeRoot(root);
	}
	
	public void initializeTree (Set<String> topNodes, Map<String, Set<String>> childMap,
			Map<String, String> labelNameMap, Map<String, String> labelDescriptionMap) {
		
		initializeTreeStructure(topNodes, childMap);
		initializeLabelNames(labelNameMap);
		initializeLabelDescriptions(labelDescriptionMap);
	}
	
	public void initializeTreeStructure (Set<String> topNodes, Map<String, Set<String>> childMap) {
		List<String> exploreNodes = new ArrayList<String>();
		
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
	
	public void clearLabelNames () {
		for (LabelTreeNode node : getNodes())
			node.setLabelName("");
	}
	
	public void initializeLabelNames (Map<String, String> labelNameMap) {
		for (String labelId : labelNameMap.keySet())
			setLabelName(labelId, labelNameMap.get(labelId));
	}
	
	public void clearLabelDescriptions () {
		for (LabelTreeNode node : getNodes())
			node.setLabelDescription("");
	}
	
	public void initializeLabelDescriptions (Map<String, String> labelDescriptionMap) {
		for (String labelId : labelDescriptionMap.keySet())
			setLabelDescription(labelId, labelDescriptionMap.get(labelId));
	}
	
	protected boolean addNode (String parent, String labelId, String labelName, String labelDesc) {
		LabelTreeNode node = new LabelTreeNode(labelId, labelName, labelDesc);
		LabelTreeNode parentNode = LabelTreeNode.makeBasicNode(labelId);
		
		return addEdge(parentNode, node);
	}
	
	public Set<String> getChildren (String labelId) {
		Set<LabelTreeNode> set = getChildren(LabelTreeNode.makeBasicNode(labelId));
		
		if (set == null)
			return null;
		
		if (set.isEmpty())
			return Collections.emptySet();
		
		Set<String> labelSet = new HashSet<String>(set.size());
		
		for (LabelTreeNode node : set) {
			labelSet.add(node.getLabel());
		}
		
		return labelSet;
	}
	
	public Set<String> getLeafLabels () {
		Set<LabelTreeNode> set = getLeafSet();
		
		if (set == null)
			return null;
		
		if (set.isEmpty())
			return Collections.emptySet();
		
		Set<String> newSet = new HashSet<String> (set.size());
		
		for (LabelTreeNode node : set) {
			newSet.add(node.getLabel());
		}
		
		return newSet;
	}
	
	public String getParent (String labelId) {
		LabelTreeNode parent = getParent(LabelTreeNode.makeBasicNode(labelId));
		
		if (parent == null)
			return null;
		
		return parent.getLabel();
	}
	
	public String getLabelName (String labelId) {
		LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));
		
		if (node == null)
			return null;
		
		return node.getLabelName();
	}
	
	public boolean setLabelName (String labelId, String labelName) {
		LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));
		
		if (node == null)
			return false;
		
		node.setLabelName(labelName);
		return true;
	}
	
	public String getLabelDescription (String labelId) {
		LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));
		
		if (node == null)
			return null;
		
		return node.getLabelDescription();
	}
	
	public boolean setLabelDescription (String labelId, String labelDesc) {
		LabelTreeNode node = getNode(LabelTreeNode.makeBasicNode(labelId));
		
		if (node == null)
			return false;
		
		node.setLabelDescription(labelDesc);
		return true;
	}
	
	public boolean addEdge (String parent, String child) {
		LabelTreeNode parentNode = LabelTreeNode.makeBasicNode(parent);
		LabelTreeNode childNode = LabelTreeNode.makeBasicNode(child);
		
		return addEdge(parentNode, childNode);
	}
	
	public boolean addEdges (String parent, Set<String> children) {
		boolean success = true;
		
		for (String child : children) {
			success = addEdge(parent, child);
			
			if (success == false)
				break;
		}
		
		return success;
	}
	
	public int getDepth (String labelId) {
		LabelTreeNode node = LabelTreeNode.makeBasicNode(labelId);
		return getDepth(node);
	}
	
	public List<String> getAllParents (String labelId) {
		List<LabelTreeNode> parentNodes = getAllParents(LabelTreeNode.makeBasicNode(labelId));
		
		if (parentNodes == null)
			return null;
		
		List<String> parents = new ArrayList<>(parentNodes.size());
		
		for (LabelTreeNode p : parentNodes) {
			parents.add(p.getLabel());
		}
		
		return parents;
	}

	public List<String> getBreadthOrderedLabelList () {
		List<LabelTreeNode> nodes = getBreadthOrderedNodeList();
		
		if (nodes == null)
			return null;
		
		List<String> labelIds = new ArrayList<>(nodes.size());
		
		for (LabelTreeNode p : nodes) {
			labelIds.add(p.getLabel());
		}
		
		return labelIds;
	}
	
	public List<String> getDepthOrderedLabelList () {
		List<LabelTreeNode> nodes = getDepthOrderedNodeList();
		
		if (nodes == null)
			return null;
		
		List<String> labelIds = new ArrayList<>(nodes.size());
		
		for (LabelTreeNode p : nodes) {
			labelIds.add(p.getLabel());
		}
		
		return labelIds;
	}
	
	public boolean isLeaf (String labelId) {
		LabelTreeNode node = LabelTreeNode.makeBasicNode(labelId);
		return isLeaf(node);
	}
	
	public void aggregateChildrenDescription () {
		List<LabelTreeNode> nodeList = getBreadthOrderedNodeList();
		
		Collections.reverse(nodeList);
		
		for (LabelTreeNode node : nodeList) {
			String childDesc = getLabelDescription(node.getLabel());
			
			if (isRoot(node) == false) {
				LabelTreeNode parent = getParent(node);
				String parentDesc = getLabelDescription(parent.getLabel()); 
				
				String newLabelDesc = parentDesc.trim() + " " + childDesc.trim();
				
				setLabelDescription(parent.getLabel(), newLabelDesc);
			}
		}
	}
	
	public void appendLabelNameToDesc () {
		for (LabelTreeNode node : getNodes()) {
			String labelId = node.getLabel();
			
			StringBuilder description = new StringBuilder(getLabelDescription(labelId));
			description.append(" ");
			description.append(getLabelName(labelId));
			
			node.setLabelDescription(description.toString());
		}
	}
	
	public void copyLabelNameToDesc () {
		for (LabelTreeNode node : getNodes()) {
			String labelId = node.getLabel();
			String labelName = getLabelName(labelId);
			node.setLabelDescription(labelName);
		}
	}
	
	public Set<String> getSameLevelLabels (String labelId) {
		Set<LabelTreeNode> nodes = getSameLevelNodes(labelId);
		
		if (nodes == null)
			return null;
		
		Set<String> output = new HashSet<>(nodes.size());
		
		for (LabelTreeNode p : nodes) {
			output.add(p.getLabel());
		}
		
		return output;
	}
	
	Set<LabelTreeNode> getSameLevelNodes (String labelId) {
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
	
	public static Set<String> identifyTopNodes (Map<String, Set<String>> childMap) {
		Set<String> candidateTopNodes = new HashSet<>();
		Set<String> topNodes = new HashSet<>();
		
		Set<String> children = new HashSet<>();
		
		for (String parent : childMap.keySet()) {
			if (children.contains(parent) == false)
				candidateTopNodes.add(parent);
			
			children.addAll(childMap.get(parent));
		}
		
		for (String candidate : candidateTopNodes) {
			if (children.contains(candidate) == false)
				topNodes.add(candidate);
		}
		
		return topNodes;
	}
	
	@Override
	public boolean equals (Object o) {
		if ((o instanceof LabelTree) == false)
			return false;
		
		LabelTree that = (LabelTree) o;
		
		if (root_label.equals(that.root_label) == false)
			return false;
		
		if (super.equals(that) == false)
			return false;
		
		return true;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder("");
		
		List<LabelTreeNode> nodes = getBreadthOrderedNodeList();
		
		for (LabelTreeNode node : nodes) {
			if (isLeaf(node) == false) {
				for (LabelTreeNode child : getChildren(node)) {
					sb.append(node.getLabel() + "\t" + child.getLabel() + "\n");
				}
			}
		}
		
		return sb.toString();
	}
	
	public void dumpTreeLabelDesc (String outPath) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outPath)));
		StringBuilder sb = new StringBuilder("");
		
		List<LabelTreeNode> nodes = getBreadthOrderedNodeList();
		
		for (LabelTreeNode node : nodes) {
			if (isRoot(node) == true)
				continue;
			
			sb.append(node.getLabel() + "\t" + node.getLabelDescription() + "\n");
		}
		
		bw.write(sb.toString());
		bw.close();
	}
	
	public String getLabelDescriptions () {
		StringBuilder sb = new StringBuilder("");
		
		List<LabelTreeNode> nodes = getBreadthOrderedNodeList();
		
		for (LabelTreeNode node : nodes) {
			sb.append(node.getLabel() + "\t" + node.getLabelDescription() + "\n");
		}
		
		return sb.toString();
	}
}