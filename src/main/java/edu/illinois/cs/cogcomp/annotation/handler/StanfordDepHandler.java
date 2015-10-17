package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A wrapper for Stanford dependency parser in an illinois-core-utilities Annotator, for use as a pipeline
 *    component.
 *
 * Created by James Clarke and Christos Christodoulopoulos.
 */
public class StanfordDepHandler extends PipelineAnnotator{
    private POSTaggerAnnotator posAnnotator;
    private ParserAnnotator parseAnnotator;
    private Logger logger = LoggerFactory.getLogger( StanfordDepHandler.class );

    public StanfordDepHandler(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator) {
        super("Stanford Dependency Parser", "3.3.1", "stanforddep");
        this.posAnnotator = posAnnotator;
        this.parseAnnotator = parseAnnotator;
    }

    @Override
    public String getViewName() {
        return ViewNames.DEPENDENCY_STANFORD;
    }

    @Override
    public View getView(TextAnnotation textAnnotation) throws AnnotatorException {
        TreeView treeView = new TreeView(ViewNames.DEPENDENCY_STANFORD, "StanfordDepHandler", textAnnotation, 1d);
        // The (tokenized) sentence offset in case we have more than one sentences in the record
        List<CoreMap> sentences = StanfordParseHandler.buildStanfordSentences(textAnnotation);
        Annotation document = new Annotation(sentences);
        posAnnotator.annotate(document);
        parseAnnotator.annotate(document);
        sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (int sentenceId = 0; sentenceId < sentences.size(); sentenceId++) {
            CoreMap sentence = sentences.get(sentenceId);
            SemanticGraph depGraph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            IndexedWord root;

            try {
                root = depGraph.getFirstRoot();
            }
            catch ( RuntimeException e )
            {
                String msg = "ERROR in getting root of dep graph for sentence.  Sentence is:\n" +
                        sentence.toString() + "'\nDependency graph is:\n" + depGraph.toCompactString() +
                        "\nText is:\n" + textAnnotation.getText();
                logger.error( msg );
                System.err.println( msg );
                e.printStackTrace();
                throw e;
            }
            int tokenStart = getNodePosition(textAnnotation, root, sentenceId);
            Pair<String, Integer> nodePair = new Pair<>(root.originalText(), tokenStart);
            Tree<Pair<String, Integer>> tree = new Tree<>(nodePair);
            populateChildren(depGraph, root, tree, textAnnotation, sentenceId);
            treeView.setDependencyTree(sentenceId, tree);
        }
        textAnnotation.addView( getViewName(), treeView );

        return treeView;
    }

    @Override
    public String[] getRequiredViews() {
        return new String[]{ViewNames.SENTENCE, ViewNames.TOKENS};
    }


    private void populateChildren(SemanticGraph depGraph, IndexedWord root, Tree<Pair<String, Integer>> tree, TextAnnotation ta, int sentId) {
        if (depGraph.getChildren(root).size() == 0)
            return;
        for (IndexedWord child : depGraph.getChildren(root)) {
            int childPosition = getNodePosition(ta, child, sentId);
            Pair<String, Integer> nodePair = new Pair<>(child.originalText(), childPosition);
            Tree<Pair<String, Integer>> childTree = new Tree<>(nodePair);
            tree.addSubtree(childTree, new Pair<>(depGraph.getEdge(root, child).toString(), childPosition));
            populateChildren(depGraph, child, childTree, ta, sentId);
        }
    }

    /**
     * Gets the token index of a Stanford dependency node relative to the current sentence
     * @param ta The TextAnnotation containing the sentences
     * @param node The Stanford Dependency node
     * @param sentId The sentence number
     * @return The token index relative to sentence
     */
    private int getNodePosition(TextAnnotation ta, IndexedWord node, int sentId) {
        int sentenceStart = ta.getView(ViewNames.SENTENCE).getConstituents().get(sentId).getStartSpan();
        int nodeCharacterOffset = node.beginPosition();
        int tokenStartSpan = ta.getTokenIdFromCharacterOffset(nodeCharacterOffset);
        return tokenStartSpan - sentenceStart;
    }
}
