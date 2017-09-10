package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.illinois.cs.cogcomp.datalessclassification.classifier.LabelTree;
import edu.illinois.cs.cogcomp.datalessclassification.classifier.AClassifierTree;
import edu.illinois.cs.cogcomp.datalessclassification.representation.AEmbedding;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVectorOperations;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class ConceptTree<T extends Serializable> extends AClassifierTree<ConceptTreeNode<T>> {

	private static Logger logger = Logger.getLogger(ConceptTree.class);
	
	private static final long serialVersionUID = 1L;
	
	private transient Map<T, Double> globalConceptWeights;
	
	private transient AEmbedding<T> embedding;
	
	protected transient int numConcepts;
	
	public ConceptTree (LabelTree labelTree) {
		super(labelTree);
	}
	
	public ConceptTree (LabelTree labelTree, AEmbedding<T> embedding, Map<T, Double> conceptWeights) {
		this(labelTree, embedding, conceptWeights, 500);
	}
	
	public ConceptTree (LabelTree labelTree, AEmbedding<T> embedding, Map<T, Double> conceptWeights, int embeddingSize) {
		super(labelTree);
		this.embedding = embedding;
		this.globalConceptWeights = conceptWeights;
		this.numConcepts = embeddingSize;
		
		initializeTree();
	}
	
	public static ConceptTree<Integer> generateDenseEmbeddedConceptTreefromLabelEmbeddingMap (LabelTree labelTree, 
			Map<String, SparseVector<Integer>> labelEmbeddings) {
		ConceptTree<Integer> conceptTree = new ConceptTree<>(labelTree);
		
		for (ConceptTreeNode<Integer> node : conceptTree.getNodes()) {
			String label = node.getLabel();
			node.setLabelDescription(labelTree.getLabelDescription(label));
			
			SparseVector<Integer> conceptVector = labelEmbeddings.get(label);
			node.setConceptVector(conceptVector);
		}
		
		return conceptTree;
	}
	
	public static ConceptTree<Integer> generateDenseEmbeddedConceptTreefromFile (LabelTree labelTree, String repFile) {
		logger.info("Reading Label Embeddings from " + repFile);
		File inputFile = new File(repFile);
		
		Map<String, SparseVector<Integer>> labelEmbeddings = new HashMap<>();
	    
		try {
			BufferedReader bf = new BufferedReader(new FileReader(inputFile));
			
			String line;
			
			while ((line = bf.readLine()) != null) {
				line = line.trim();
				
				if (line.length() ==  0)
					continue;
				
				String[] tokens = line.trim().split("\t", 2);
				String[] stringVec = tokens[1].split(" ");
				
				String label = tokens[0].trim();
				
				if (label.length() == 0)
					continue;
				
				Map<Integer, Double> scores = new HashMap<>();
				
				int i = 0;
				
				for (String dim : stringVec) {
					scores.put(i, Double.parseDouble(dim));
					i++;
				}
				
				SparseVector<Integer> vec = new SparseVector<>(scores);
				
		        labelEmbeddings.put(label, vec);
			}
			
			bf.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		ConceptTree<Integer> conceptTree = generateDenseEmbeddedConceptTreefromLabelEmbeddingMap(labelTree, labelEmbeddings);
		return conceptTree;
	}
	
	/**
	 * Copy Constructor
	 */
	public ConceptTree (ConceptTree<T> thatTree) {
		super(new LabelTree(thatTree.getLabelTree()));
		
		try {
			for (ConceptTreeNode<T> node : getNodes()) {
				if (isRoot(node))
					continue;
				
				String label = node.getLabel();
				ConceptTreeNode<T> thatNode = thatTree.getNodeFromLabel(label);
				
				String description = thatNode.getLabelDescription();
				node.setLabelDescription(description);
				
				SparseVector<T> vector = SparseVector.<T>deepCopy(thatNode.getConceptVector());
				node.setConceptVector(vector);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void initializeRoot (String root_label) {
		ConceptTreeNode<T> root = ConceptTreeNode.<T>makeBasicTypedNode(root_label);
		initializeRoot(root);
	}
	
	@Override
	public Set<ConceptTreeNode<T>> getChildren (String label) {
		Set<ConceptTreeNode<T>> set = getChildren((ConceptTreeNode<T>) ConceptTreeNode.<T>makeBasicTypedNode(label));
		
		if (set == null)
			return null;
		
		if (set.isEmpty())
			return Collections.emptySet();
		
		Set<ConceptTreeNode<T>> newSet = new HashSet<>(set.size());
		
		for (ConceptTreeNode<T> node : set) {
			newSet.add(node);
		}
		
		return newSet;
	}
	
	@Override
	public ConceptTreeNode<T> getParent (String label) {
		ConceptTreeNode<T> parent = getParent((ConceptTreeNode<T>) ConceptTreeNode.<T>makeBasicTypedNode(label));
		return parent;
	}
	
	@Override
	public boolean addEdge (String parent, String child) {
		ConceptTreeNode<T> parentNode = ConceptTreeNode.<T>makeBasicTypedNode(parent);
		ConceptTreeNode<T> childNode = ConceptTreeNode.<T>makeBasicTypedNode(child);
		
		return addEdge(parentNode, childNode);
	}
	
	public int getDepth (String label) {
		return getDepth((ConceptTreeNode<T>) ConceptTreeNode.<T>makeBasicTypedNode(label));
	}
	
	public List<ConceptTreeNode<T>> getAllParents (String label) {
		List<ConceptTreeNode<T>> parentNodes = getAllParents((ConceptTreeNode<T>) ConceptTreeNode.<T>makeBasicTypedNode(label));
		
		if (parentNodes == null)
			return null;
		
		List<ConceptTreeNode<T>> parents = new ArrayList<>(parentNodes.size());
		
		for (ConceptTreeNode<T> p : parentNodes) {
			parents.add(p);
		}
		
		return parentNodes;
	}
	
	public void initializeTree () {
		try {
			for (ConceptTreeNode<T> node : getNodes()) {
				String label = node.getLabel();
				String description = labelTree.getLabelDescription(label);
				
				node.setLabelDescription(description);
				
				SparseVector<T> concepts = embedding.getVector(node.getLabelDescription(), numConcepts);
				concepts.updateNorm(globalConceptWeights);
				
				node.setConceptVector(concepts);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static<T extends Serializable> ConceptTree<T> getAvgConceptTree (List<ConceptTree<T>> conceptTreeList) {
		ConceptTree<T> avgTree = new ConceptTree<>(conceptTreeList.get(0));
		
		for (ConceptTreeNode<T> node : avgTree.getNodes()) {
			if (avgTree.isRoot(node))
				continue;
			
			String currentLabel = node.getLabel();
			List<SparseVector<T>> vectors = new ArrayList<>();
			
			for (ConceptTree<T> tree : conceptTreeList) {
				vectors.add(tree.getNodeFromLabel(currentLabel).getConceptVector());
			}
			
			SparseVector<T> avgVector = SparseVectorOperations.averageMultipleVectors(vectors);
			node.setConceptVector(avgVector);
		}
		
		return avgTree;
	}
	
	public void dumpTreeAsString (String filePath) {
		try {
			FileWriter writer = new FileWriter(filePath);
			
			List<ConceptTreeNode<T>> nodeList = getBreadthOrderedNodeList();
			
			for (ConceptTreeNode<T> node : nodeList) {
				String parent = "";
				
				if (isRoot(node))
					parent = "NIL";
				else
					parent = getParent(node).getLabel();
				
				writer.write(parent + "\t" 
						+ node.getLabel() + "\t" 
						+ node.getLabelDescription() + "\t" 
						+ node.getConceptVector().toString() + "\n");
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static<K extends Serializable> ConceptTree<K> loadTree (String labelRepFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(labelRepFile));
		ConceptTree<K> tree = (ConceptTree<K>) in.readObject();
		in.close();
		
		return tree;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder("");
		
		List<ConceptTreeNode<T>> nodes = getBreadthOrderedNodeList();
		
		for (ConceptTreeNode<T> node : nodes) {
			if (isLeaf(node) == false) {
				for (ConceptTreeNode<T> child : getChildren(node)) {
					sb.append(node.getLabel() + "\t");
					sb.append(child + "\n");
				}
			}
		}
		
		return sb.toString();
	}
}