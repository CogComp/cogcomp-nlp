/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.ta;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.annotation.Annotator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for all Dataless Annotators
 *
 * @author shashank
 */

public abstract class ADatalessAnnotator extends Annotator {
    private static Logger logger = LoggerFactory.getLogger(ADatalessAnnotator.class);

    protected int embedding_dim;
    protected int topK;

    protected AEmbedding<Integer> embedding;
    protected DatalessClassifierML<Integer> classifier;

    protected LabelTree labelTree;
    protected ConceptTree<Integer> conceptTree;

    protected Map<Integer, Double> conceptWeights;

    /**
     * Use this constructor with "isLazilyInitialized = True" to use your custom annotator
     * initializer
     */
    protected ADatalessAnnotator(String viewName, boolean isLazilyInitialized) {
        super(viewName, new String[] {ViewNames.TOKENS}, isLazilyInitialized);
    }

    protected ADatalessAnnotator(String viewName, ResourceManager config) {
        this(viewName, new String[] {ViewNames.TOKENS}, config);
    }

    protected ADatalessAnnotator(String viewName, ResourceManager config, boolean isLazilyInitialized) {
        this(viewName, new String[] {ViewNames.TOKENS}, isLazilyInitialized, config);
    }

    protected ADatalessAnnotator(String viewName, String[] requiredViews, ResourceManager config) {
        this(viewName, requiredViews, false, config);
    }

    protected ADatalessAnnotator(String viewName, String[] requiredViews,
            boolean isLazilyInitialized, ResourceManager config) {
        super(viewName, requiredViews, isLazilyInitialized, config);
    }

    protected abstract String getClassName();

    @Override
    public void initialize(ResourceManager rm) {
        String hierarchyFile = rm.getString(DatalessConfigurator.LabelHierarchy_Path);
        String labelNameFile = rm.getString(DatalessConfigurator.LabelName_Path);
        String labelDescFile = rm.getString(DatalessConfigurator.LabelDesc_Path);

        logger.info("Initializing LabelTree...");
        initializeLabelTree(hierarchyFile, labelNameFile, labelDescFile);
        logger.info("LabelTree Initialization Done.");

        logger.info("Initializing Embedding...");
        initializeEmbedding(rm);
        logger.info("Embedding Initialization Done.");

        logger.info("Initializing Classifier...");
        initializeClassifier(rm);
        logger.info("Classifier Initialization Done.");
    }

    /**
     * Initializes the LabelTree from the JSON representation of the Label Hierarchy
     * NOTE: This support is yet to come
     *
     * @throws NotImplementedException
     */
    protected void initializeLabelTree(JSONObject jsonHierarchy) throws NotImplementedException {
        // TODO: Start supporting JSON format for hierarchy input
        throw new NotImplementedException("JSON support coming soon..");
    }

    /**
     * Initializes the LabelTree from the mapping files
     */
    protected void initializeLabelTree(String hierarchyFile, String labelNameFile,
            String labelDescFile) {
        Set<String> topNodes = DatalessAnnotatorUtils.getTopNodes(hierarchyFile);
        Map<String, Set<String>> childMap = DatalessAnnotatorUtils.getParentChildMap(hierarchyFile);
        Map<String, String> labelNameMap = DatalessAnnotatorUtils.getLabelNameMap(labelNameFile);
        Map<String, String> labelDescMap =
                DatalessAnnotatorUtils.getLabelDescriptionMap(labelDescFile);

        initializeLabelTree(topNodes, childMap, labelNameMap, labelDescMap);
    }

    /**
     * Initializes the LabelTree structure, given the:
     * @param topNodes: Set containing the labelIDs of the top-level nodes in the tree
     * @param childMap: Map containing the parentID - childIds mapping
     * @param labelNameMap: Map containing the labelID - labelName mapping
     * @param labelDescMap: Map containing the labelID - labelDescription mapping
     */
    protected void initializeLabelTree(Set<String> topNodes, Map<String, Set<String>> childMap,
            Map<String, String> labelNameMap, Map<String, String> labelDescMap) {

        labelTree = new LabelTree();

        initializeTreeStructure(topNodes, childMap);
        initializeLabelNames(labelNameMap);
        initializeLabelDescriptions(labelDescMap);
    }

    /**
     * Initializes the LabelTree structure given the:
     * @param
     */
    private void initializeTreeStructure(Set<String> topNodes, Map<String, Set<String>> childMap) {
        labelTree.initializeTreeStructure(topNodes, childMap);
    }

    /**
     * Initializes the LabelNames
     */
    private void initializeLabelNames(Map<String, String> labelNameMap) {
        labelTree.initializeLabelNames(labelNameMap);
    }

    /**
     * Initializes the LabelDescriptions
     */
    private void initializeLabelDescriptions(Map<String, String> labelDesriptionMap) {
        labelTree.initializeLabelDescriptions(labelDesriptionMap);
    }

    /**
     * - initialize the embedding, embedding_dim and (optionally) conceptWeights objects here
     * - call this before calling initializeClassifier()
     */
    protected abstract void initializeEmbedding(ResourceManager config);

    /**
     * - Call this before trying to annotate the objects
     * - Call this only after calling initializeEmbedding
     */
    protected void initializeClassifier(ResourceManager config) {
        initializeConceptTree();
        topK = config.getInt(DatalessConfigurator.topK);
        classifier = new DatalessClassifierML<>(config, conceptTree);
    }

    /**
     * Initializes the ConceptTree
     */
    protected void initializeConceptTree() {
        conceptTree = new ConceptTree<>(labelTree, embedding, conceptWeights, embedding_dim);
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        SpanLabelView datalessView = new SpanLabelView(getViewName(), getClassName(), ta, 1d, true);

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

        ta.addView(getViewName(), datalessView);
    }
}
