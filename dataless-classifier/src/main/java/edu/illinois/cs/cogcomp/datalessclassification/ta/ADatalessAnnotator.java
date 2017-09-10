package edu.illinois.cs.cogcomp.datalessclassification.ta;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.json.simple.JSONObject;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.classifier.ConceptTree;
import edu.illinois.cs.cogcomp.datalessclassification.classifier.DatalessClassifierML;
import edu.illinois.cs.cogcomp.datalessclassification.classifier.LabelTree;
import edu.illinois.cs.cogcomp.datalessclassification.config.DatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.representation.AEmbedding;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * Abstract class for all Dataless Annotators
 * 
 * @author shashank
 */

public abstract class  ADatalessAnnotator {
	protected static final String Class_Name = ADatalessAnnotator.class.getCanonicalName();
	public static final String Annotator_Name = "Abstract-Dataless";
	
	protected int embedding_dim;
	protected AEmbedding<Integer> embedding;
	protected DatalessClassifierML<Integer> classifier;
	
	protected LabelTree labelTree;
	protected ConceptTree<Integer> conceptTree;
	
	protected Map<Integer, Double> conceptWeights;
	
	public ADatalessAnnotator () {
		this(
				new DatalessConfigurator().getDefaultConfig()
			);
	}
	
	public ADatalessAnnotator (ResourceManager config) {
		this(
				config.getString(DatalessConfigurator.LabelHierarchy_Path.key),
				config.getString(DatalessConfigurator.LabelName_Path.key),
				config.getString(DatalessConfigurator.LabelDesc_Path.key)
			);
	}
	
	public ADatalessAnnotator (JSONObject treeObj) throws NotImplementedException {
		initializeLabelTree(treeObj);
	}
	
	public ADatalessAnnotator (String hierarchyPath, String labelNameFile, String labelDescFile) {
		initializeLabelTree(hierarchyPath, labelNameFile, labelDescFile);
	}
	
	public ADatalessAnnotator (Set<String> topNodes, Map<String, Set<String>> childMap, 
			Map<String, String> labelNameMap, Map<String, String> labelDescMap) {
		
		initializeLabelTree(topNodes, childMap, labelNameMap, labelDescMap);
	}
	
	
	/**
	 * initialize the embedding, embedding_dim and (optionally) conceptWeights objects here
	 * call this before calling initializeClassifier()
	 */
	protected abstract void initializeEmbedding (ResourceManager config);
	
	
	/**
	 * Call this before trying to annotate the objects
	 * Call this only after calling initializeEmbedding
	 */
	protected void initializeClassifier (ResourceManager config) {
		initializeConceptTree();
		classifier = new DatalessClassifierML<>(config, conceptTree);
	}
	
	protected void initializeConceptTree () {
		conceptTree = new ConceptTree<Integer>(labelTree, embedding, conceptWeights, embedding_dim);
	}
	
	protected void initializeLabelTree (JSONObject tree) throws NotImplementedException {
		//TODO: Populate the following data structures from the JSON object here
		
//		Set<String> topNodes = null;
//		Map<String, Set<String>> childMap = null;
//		Map<String, String> labelNameMap = null;
//		Map<String, String> labelDescMap = null;
//		
//		initializeLabelTree(topNodes, childMap, labelNameMap, labelDescMap);
		
		throw new NotImplementedException("JSON support coming soon..");
	}
	
	protected void initializeLabelTree (String hierarchyPath, String labelNameFile, String labelDescFile) {
		Set<String> topNodes = DatalessAnnotatorUtils.getTopNodes(hierarchyPath);
		Map<String, Set<String>> childMap = DatalessAnnotatorUtils.getParentChildMap(hierarchyPath);
		Map<String, String> labelNameMap = DatalessAnnotatorUtils.getLabelNameMap(labelNameFile);
		Map<String, String> labelDescMap = DatalessAnnotatorUtils.getLabelDescriptionMap(labelDescFile);
		
		initializeLabelTree(topNodes, childMap, labelNameMap, labelDescMap);
	}
	
	protected void initializeLabelTree (Set<String> topNodes, Map<String, Set<String>> childMap, 
			Map<String, String> labelNameMap, Map<String, String> labelDescMap) {
		
		labelTree = new LabelTree();
		
		initializeTreeStructure(topNodes, childMap);
		initializeLabelNames(labelNameMap);
		initializeLabelDescriptions(labelDescMap);
	}
	
	protected void initializeTreeStructure (Set<String> topNodes, Map<String, Set<String>> childMap) {
		labelTree.initializeTreeStructure(topNodes, childMap);
	}
	
	protected void initializeLabelNames (Map<String, String> labelNameMap) {
		labelTree.initializeLabelNames(labelNameMap);
	}
	
	protected void initializeLabelDescriptions (Map<String, String> labelDesriptionMap) {
		labelTree.initializeLabelDescriptions(labelDesriptionMap);
	}
	
	
	/**
	 * @return The name of the Dataless Annotator
	 */
	public String getName () {
		return Annotator_Name;
	}
	
	/**
	 * Adds the Dataless view to the input TextAnnotation 
	 * 
	 * @param ta The TextAnnotation to annotate with the dataless view
	 * @throws AnnotatorException
	 */
	public void labelText (TextAnnotation ta) throws AnnotatorException {
		labelText(ta, 1);
	}
	
	/**
	 * Adds the Dataless view to the input TextAnnotation 
	 * 
	 * @param ta The TextAnnotation to annotate with the dataless view
	 * @param topK Maximum number of labels to select at every depth
	 * @throws AnnotatorException
	 */
	public void labelText (TextAnnotation ta, int topK) throws AnnotatorException {
		SpanLabelView datalessView = new SpanLabelView(getName(), Class_Name, ta, 1d, true);
		
		List<Constituent> tokens = ta.getView(ViewNames.TOKENS).getConstituents();
		
		int numTokens = tokens.size();
		
		int textStart = tokens.get(0).getSpan().getFirst();
		int textEnd = tokens.get(numTokens - 1).getSpan().getSecond();
		
		StringBuilder sb = new StringBuilder();
		
		for (String s : ta.getTokensInSpan(textStart, textEnd)) {
			sb.append(s);
			sb.append(" ");
		}
		
		SparseVector<Integer> docVector = embedding.getVector(sb.toString().trim());
		
		Set<String> labelIDs = classifier.getFlatPredictions(docVector, topK);
		
		for (String labelID : labelIDs) {
			datalessView.addSpanLabel(textStart, textEnd, labelID, 1d);
		}
		
		ta.addView(getName(), datalessView);
	}
}
