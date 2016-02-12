package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.nlp.common.HandlerUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

import java.util.List;
import java.util.logging.Handler;

/**
 * A wrapper for Stanford dependency parser in an illinois-core-utilities Annotator, for use as a pipeline
 *    component.
 *
 * Created by James Clarke and Christos Christodoulopoulos.
 */
public class StanfordDepHandler extends PipelineAnnotator {
    private POSTaggerAnnotator posAnnotator;
    private ParserAnnotator parseAnnotator;
    private int maxParseSentenceLength;
    private Logger logger = LoggerFactory.getLogger( StanfordDepHandler.class );
    private boolean throwExceptionOnSentenceLengthCheck;


    public StanfordDepHandler(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator) {
        this(posAnnotator, parseAnnotator, -1, true);
    }

    public StanfordDepHandler(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator, int maxSentenceLength,
                              boolean throwExceptionOnSentenceLengthCheck ) {
        super("Stanford Dependency Parser", "3.3.1", "stanforddep", ViewNames.DEPENDENCY_STANFORD, new String[]{});
        this.posAnnotator = posAnnotator;
        this.parseAnnotator = parseAnnotator;
        this.maxParseSentenceLength = maxSentenceLength;
        this.throwExceptionOnSentenceLengthCheck = throwExceptionOnSentenceLengthCheck;
    }


    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException {
        // If the sentence is longer than STFRD_MAX_SENTENCE_LENGTH there is no point in trying to parse

        if ( throwExceptionOnSentenceLengthCheck )
        {
            Constituent c = HandlerUtils.checkTextAnnotationRespectsSentenceLengthLimit(textAnnotation, maxParseSentenceLength);

            if ( null != c )
            {
                String msg = HandlerUtils.getSentenceLengthError( textAnnotation.getId(), c.getSurfaceForm(), maxParseSentenceLength );
                logger.error( msg );
                throw new AnnotatorException( msg );
            }
        }

        TreeView treeView = new TreeView(ViewNames.DEPENDENCY_STANFORD, "StanfordDepHandler", textAnnotation, 1d);
        // The (tokenized) sentence offset in case we have more than one sentences in the record
        List<CoreMap> sentences = StanfordParseHandler.buildStanfordSentences(textAnnotation);
        Annotation document = new Annotation(sentences);
        posAnnotator.annotate(document);
        parseAnnotator.annotate(document);
        sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class).nodeString().equals("X")) {
            // This is most like because we ran out of time
            throw new AnnotatorException("Unable to parse TextAnnotation " + textAnnotation.getId() + ". " +
                    "This is most likely due to a timeout.");
        }

        for (int sentenceId = 0; sentenceId < sentences.size(); sentenceId++) {
            CoreMap sentence = sentences.get(sentenceId);
            if ( maxParseSentenceLength > 0 && sentence.size() > maxParseSentenceLength )
            {
                logger.warn(HandlerUtils.getSentenceLengthError( textAnnotation.getId(), sentence.toString(), maxParseSentenceLength ) );
            }
            else {
                SemanticGraph depGraph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                IndexedWord root;

                try {
                    root = depGraph.getFirstRoot();
                } catch (RuntimeException e) {
                    String msg = "ERROR in getting root of dep graph for sentence.  Sentence is:\n" +
                            sentence.toString() + "'\nDependency graph is:\n" + depGraph.toCompactString() +
                            "\nText is:\n" + textAnnotation.getText();
                    logger.error(msg);
                    System.err.println(msg);
                    e.printStackTrace();
                    throw e;
                }
                int tokenStart = getNodePosition(textAnnotation, root, sentenceId);
                Pair<String, Integer> nodePair = new Pair<>(root.originalText(), tokenStart);
                Tree<Pair<String, Integer>> tree = new Tree<>(nodePair);
                populateChildren(depGraph, root, tree, textAnnotation, sentenceId);
                treeView.setDependencyTree(sentenceId, tree);
            }
        }
        textAnnotation.addView( getViewName(), treeView );
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
