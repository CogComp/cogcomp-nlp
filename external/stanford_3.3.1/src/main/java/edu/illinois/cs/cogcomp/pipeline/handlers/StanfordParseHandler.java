/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers;


import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.HandlerUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * A wrapper for Stanford dependency parser in an illinois-core-utilities Annotator, for use as a
 * pipeline component.
 *
 * Created by James Clarke and Christos Christodoulopoulos.
 */

public class StanfordParseHandler extends Annotator {

    private final static Logger logger = LoggerFactory.getLogger(StanfordParseHandler.class);
    private final boolean throwExceptionOnSentenceLengthCheck;
    private POSTaggerAnnotator posAnnotator;
    private ParserAnnotator parseAnnotator;
    private int maxParseSentenceLength;

    public StanfordParseHandler(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator) {
        this(posAnnotator, parseAnnotator, -1, false);
    }

    public StanfordParseHandler(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator,
            int maxSentenceLength, boolean throwExceptionOnSentenceLengthCheck) {
        super(ViewNames.PARSE_STANFORD, new String[] {}, false);
        this.posAnnotator = posAnnotator;
        this.parseAnnotator = parseAnnotator;
        this.maxParseSentenceLength = maxSentenceLength;
        this.throwExceptionOnSentenceLengthCheck = throwExceptionOnSentenceLengthCheck;

    }

    static void checkLength(TextAnnotation textAnnotation,
            boolean throwExceptionOnSentenceLengthCheck, int maxParseSentenceLength)
            throws AnnotatorException {
        if (throwExceptionOnSentenceLengthCheck) {
            Constituent c =
                    HandlerUtils.checkTextAnnotationRespectsSentenceLengthLimit(textAnnotation,
                            maxParseSentenceLength);

            if (null != c) {
                String msg =
                        HandlerUtils.getSentenceLengthError(textAnnotation.getId(),
                                c.getSurfaceForm(), maxParseSentenceLength);
                logger.error(msg);
                throw new AnnotatorException(msg);
            }
        }
    }

    static List<CoreMap> buildStanfordSentences(TextAnnotation ta) {
        View tokens = ta.getView(ViewNames.TOKENS);
        View sentences = ta.getView(ViewNames.SENTENCE);
        String rawText = ta.getText();

        List<CoreMap> stanfordSentences = new LinkedList<>();
        List<CoreLabel> stanfordTokens = new LinkedList<>();
        int tokIndex = 0;
        int sentIndex = 0;
        Constituent currentSentence = sentences.getConstituents().get(0);
        String sentText =
                rawText.substring(currentSentence.getStartCharOffset(),
                        currentSentence.getEndCharOffset());

        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();

        for (Constituent tok : tokens.getConstituents()) {
            if (tok.getStartSpan() >= currentSentence.getEndSpan()) {
                CoreMap stanfordSentence =
                        buildStanfordSentence(currentSentence, sentText, sentIndex++,
                                stanfordTokens);
                stanfordSentences.add(stanfordSentence);
                stanfordTokens = new LinkedList<>();
                currentSentence = sentences.getConstituents().get(sentIndex);
                sentText =
                        rawText.substring(currentSentence.getStartCharOffset(),
                                currentSentence.getEndCharOffset());
            }
            int tokStart = tok.getStartCharOffset();
            int tokLength = tok.getEndCharOffset() - tokStart;

            String form = rawText.substring(tokStart, tok.getEndCharOffset());

            CoreLabel stanfordTok = tf.makeToken(form, tokStart, tokLength);
            stanfordTok.setIndex(tokIndex++);
            stanfordTokens.add(stanfordTok);

        }
        // should be one last sentence
        CoreMap stanfordSentence =
                buildStanfordSentence(currentSentence, sentText, sentIndex, stanfordTokens);
        stanfordSentences.add(stanfordSentence);
        return stanfordSentences;
    }

    private static CoreMap buildStanfordSentence(Constituent sentence, String rawText,
            int sentIndex, List<CoreLabel> stanfordTokens) {
        CoreMap stanfordSentence = new ArrayCoreMap();
        CoreLabel firstTok = stanfordTokens.get(0);
        CoreLabel lastTok = stanfordTokens.get(stanfordTokens.size() - 1);

        stanfordSentence.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class,
                sentence.getStartSpan());
        stanfordSentence.set(CoreAnnotations.CharacterOffsetEndAnnotation.class,
                sentence.getEndSpan());
        stanfordSentence.set(CoreAnnotations.TokenBeginAnnotation.class, firstTok.index());
        stanfordSentence.set(CoreAnnotations.TokenEndAnnotation.class, lastTok.index() + 1); // at-the-end
                                                                                             // indexing?
        stanfordSentence.set(CoreAnnotations.TextAnnotation.class, rawText);
        stanfordSentence.set(CoreAnnotations.SentenceIndexAnnotation.class, sentIndex);
        stanfordSentence.set(CoreAnnotations.TokensAnnotation.class, stanfordTokens);
        return stanfordSentence;
    }

    /**
     * Takes a Stanford Tree and Curator Tree and recursively populates the Curator Tree to match
     * the Stanford Tree. Returns the top Node of the tree.
     *
     * @param parse Stanford Tree
     * @return top Node of the Tree
     */
    private static Tree<String> generateNode(edu.stanford.nlp.trees.Tree parse) {
        Tree<String> node = new Tree<>(parse.value());

        for (edu.stanford.nlp.trees.Tree pt : parse.getChildrenAsList()) {
            if (pt.isLeaf()) {
                node.addLeaf(pt.nodeString());
            } else {
                // generate child of parse, the current node in tree
                node.addSubtree(generateNode(pt));
            }
        }
        return node;
    }

    /**
     * noop
     * 
     * @param resourceManager
     */
    @Override
    public void initialize(ResourceManager resourceManager) {
        ;
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException {
        // If the sentence is longer than STFRD_MAX_SENTENCE_LENGTH there is no point in trying to
        // parse
        checkLength(textAnnotation, throwExceptionOnSentenceLengthCheck, maxParseSentenceLength);

        TreeView treeView =
                new TreeView(ViewNames.PARSE_STANFORD, "StanfordParseHandler", textAnnotation, 1d);
        // The (tokenized) sentence offset in case we have more than one sentences in the record
        List<CoreMap> sentences = buildStanfordSentences(textAnnotation);
        Annotation document = new Annotation(sentences);
        posAnnotator.annotate(document);
        parseAnnotator.annotate(document);
        sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class).nodeString().equals("X")) {
            // This is most like because we ran out of time
            throw new AnnotatorException("Unable to parse TextAnnotation " + textAnnotation.getId()
                    + ". " + "This is most likely due to a timeout.");
        }

        for (int sentenceId = 0; sentenceId < sentences.size(); sentenceId++) {
            CoreMap sentence = sentences.get(sentenceId);

            if (maxParseSentenceLength > 0 && sentence.size() > maxParseSentenceLength) {

                logger.warn("Unable to parse TextAnnotation " + textAnnotation.getId()
                        + " since it is larger than the maximum sentence length of the parser ("
                        + maxParseSentenceLength + ").");

            } else {

                edu.stanford.nlp.trees.Tree stanfordTree =
                        sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                Tree<String> tree = new Tree<>(stanfordTree.value());
                for (edu.stanford.nlp.trees.Tree pt : stanfordTree.getChildrenAsList()) {
                    tree.addSubtree(generateNode(pt));
                }
                treeView.setParseTree(sentenceId, tree);
            }
        }
        textAnnotation.addView(getViewName(), treeView);
    }
}
