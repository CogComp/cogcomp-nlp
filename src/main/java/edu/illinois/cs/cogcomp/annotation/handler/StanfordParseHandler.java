package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

import java.util.LinkedList;
import java.util.List;

/**
 * A wrapper for Stanford dependency parser in an illinois-core-utilities Annotator, for use as a pipeline
 *    component.
 *
 * Created by James Clarke and Christos Christodoulopoulos.
 */

public class StanfordParseHandler extends PipelineAnnotator {

    private POSTaggerAnnotator posAnnotator;
    private ParserAnnotator parseAnnotator;
    private int maxParseSentenceLength;

    public StanfordParseHandler(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator) {
        this(posAnnotator, parseAnnotator, -1);
    }

    public StanfordParseHandler(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator, int maxSentenceLength) {
        super("Stanford Parser", "3.3.1", "stanfordparse", ViewNames.PARSE_STANFORD, new String[]{});
        this.posAnnotator = posAnnotator;
        this.parseAnnotator = parseAnnotator;
        this.maxParseSentenceLength = maxSentenceLength;
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException {
        // If the sentence is longer than STFRD_MAX_SENTENCE_LENGTH there is no point in trying to parse
        if (maxParseSentenceLength > 0 && textAnnotation.size() > maxParseSentenceLength) {
            throw new AnnotatorException("Unable to parse TextAnnotation " + textAnnotation.getId() +
                    " since it is larger than the maximum sentence length of the parser.");
        }
        TreeView treeView = new TreeView(ViewNames.PARSE_STANFORD, "StanfordParseHandler", textAnnotation, 1d);
        // The (tokenized) sentence offset in case we have more than one sentences in the record
        List<CoreMap> sentences = buildStanfordSentences(textAnnotation);
        Annotation document = new Annotation(sentences);
        posAnnotator.annotate(document);
        parseAnnotator.annotate(document);
        sentences = document.get(SentencesAnnotation.class);
        if (sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class).nodeString().equals("X")) {
            // This is most like because we ran out of time
            throw new AnnotatorException("Unable to parse TextAnnotation " + textAnnotation.getId() + ". " +
                    "This is most likely due to a timeout.");
        }

        for (int sentenceId = 0; sentenceId < sentences.size(); sentenceId++) {
            CoreMap sentence = sentences.get(sentenceId);

            edu.stanford.nlp.trees.Tree stanfordTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            Tree<String> tree = new Tree<>(stanfordTree.value());
            for (edu.stanford.nlp.trees.Tree pt : stanfordTree.getChildrenAsList()) {
                tree.addSubtree(generateNode(pt));
            }
            treeView.setParseTree(sentenceId, tree);
        }
        textAnnotation.addView( getViewName(), treeView );
    }


    protected static List<CoreMap> buildStanfordSentences(TextAnnotation ta) {
        View tokens = ta.getView(ViewNames.TOKENS);
        View sentences = ta.getView(ViewNames.SENTENCE);
        String rawText = ta.getText();

        List<CoreMap> stanfordSentences = new LinkedList<>();
        List<CoreLabel> stanfordTokens = new LinkedList<>();
        int tokIndex = 0;
        int sentIndex = 0;
        Constituent currentSentence = sentences.getConstituents().get(0);
        String sentText = rawText.substring(currentSentence.getStartCharOffset(), currentSentence.getEndCharOffset());

        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();

        for (Constituent tok : tokens.getConstituents()) {
            if (tok.getStartSpan() >= currentSentence.getEndSpan()) {
                CoreMap stanfordSentence = buildStanfordSentence(currentSentence, sentText, sentIndex++, stanfordTokens);
                stanfordSentences.add(stanfordSentence);
                stanfordTokens = new LinkedList<>();
                currentSentence = sentences.getConstituents().get(sentIndex);
                sentText = rawText.substring(currentSentence.getStartCharOffset(), currentSentence.getEndCharOffset());
            }
            int tokStart = tok.getStartCharOffset();
            int tokLength = tok.getEndCharOffset() - tokStart;

            String form = rawText.substring(tokStart, tok.getEndCharOffset());

            CoreLabel stanfordTok = tf.makeToken(form, tokStart, tokLength);
            stanfordTok.setIndex(tokIndex++);
            stanfordTokens.add(stanfordTok);

        }
        // should be one last sentence
        CoreMap stanfordSentence = buildStanfordSentence(currentSentence, sentText, sentIndex, stanfordTokens);
        stanfordSentences.add(stanfordSentence);
        return stanfordSentences;
    }

    private static CoreMap buildStanfordSentence(Constituent sentence, String rawText, int sentIndex,
                                                 List<CoreLabel> stanfordTokens) {
        CoreMap stanfordSentence = new ArrayCoreMap();
        CoreLabel firstTok = stanfordTokens.get(0);
        CoreLabel lastTok = stanfordTokens.get(stanfordTokens.size() - 1);

        stanfordSentence.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, sentence.getStartSpan());
        stanfordSentence.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, sentence.getEndSpan());
        stanfordSentence.set(CoreAnnotations.TokenBeginAnnotation.class, firstTok.index());
        stanfordSentence.set(CoreAnnotations.TokenEndAnnotation.class, lastTok.index() + 1); // at-the-end indexing?
        stanfordSentence.set(CoreAnnotations.TextAnnotation.class, rawText);
        stanfordSentence.set(CoreAnnotations.SentenceIndexAnnotation.class, sentIndex);
        stanfordSentence.set(CoreAnnotations.TokensAnnotation.class, stanfordTokens);
        return stanfordSentence;
    }

    /**
     * Takes a Stanford Tree and Curator Tree and recursively populates the Curator
     * Tree to match the Stanford Tree.
     * Returns the top Node of the tree.
     *
     * @param parse  Stanford Tree
     * @return top Node of the Tree
     */
    private static Tree<String> generateNode(edu.stanford.nlp.trees.Tree parse) {
        Tree<String> node = new Tree<>(parse.value());

        for (edu.stanford.nlp.trees.Tree pt : parse.getChildrenAsList()) {
            if (pt.isLeaf()) {
                node.addLeaf(pt.nodeString());
            } else {
                //generate child of parse, the current node in tree
                node.addSubtree(generateNode(pt));
            }
        }
        return node;
    }
}