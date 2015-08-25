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
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

import java.util.LinkedList;
import java.util.List;

public class StanfordParseHandler {

    /**
     * given the input record, read off the text and generate a stanford constituency and dependency parse
     */
    public static TextAnnotation annotate(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator, TextAnnotation record) throws AnnotatorException {
        TreeView treeView = new TreeView(ViewNames.PARSE_STANFORD, "StanfordConverter", record, 1d);
        // The (tokenized) sentence offset in case we have more than one sentences in the record
        List<CoreMap> sentences = buildStanfordSentences(record);
        Annotation document = new Annotation(sentences);
        posAnnotator.annotate(document);
        parseAnnotator.annotate(document);
        sentences = document.get(SentencesAnnotation.class);

        for (int sentenceId = 0; sentenceId < sentences.size(); sentenceId++) {
            CoreMap sentence = sentences.get(sentenceId);
            //edu.stanford.nlp.trees.Tree
            edu.stanford.nlp.trees.Tree stanfordTree = sentence.get(TreeAnnotation.class);
            // Convert from Stanford Tree to thrift.base.Tree
            Tree<String> tree = new Tree<String>();
            for (edu.stanford.nlp.trees.Tree pt : stanfordTree.getChildrenAsList()) {
                tree.addSubtree(generateNode(pt, tree));
            }
            treeView.setParseTree(sentenceId, tree);
        }
        return null;
    }

    protected static List<CoreMap> buildStanfordSentences(TextAnnotation ta) {
        View tokens = ta.getView(ViewNames.TOKENS);
        View sentences = ta.getView(ViewNames.SENTENCE);
        String rawText = ta.getText();

        List<CoreMap> stanfordSentences = new LinkedList<CoreMap>();
        List<CoreLabel> stanfordTokens = new LinkedList<CoreLabel>();
        int tokIndex = 0;
        int sentIndex = 0;
        Constituent currentSentence = sentences.getConstituents().get(0);
        String sentText = rawText.substring(currentSentence.getStartCharOffset(), currentSentence.getEndCharOffset());

        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();

        for (Constituent tok : tokens.getConstituents()) {
            if (tok.getStartSpan() >= currentSentence.getEndSpan()) {
                CoreMap stanfordSentence = buildStanfordSentence(currentSentence, sentText, sentIndex++, stanfordTokens);
                stanfordSentences.add(stanfordSentence);
                stanfordTokens = new LinkedList<CoreLabel>();
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
     * @param tree   Curator Tree
     * @return top Node of the Tree
     */
    private static Tree<String> generateNode(edu.stanford.nlp.trees.Tree parse, Tree<String> tree) {
        List<Tree<String>> nodes = tree.getChildren(); // tree is the curator Tree we are building
        Tree<String> node = new Tree<String>(parse.value());

        for (edu.stanford.nlp.trees.Tree pt : parse.getChildrenAsList()) {
            if (pt.isLeaf()) {
                node.addLeaf(pt.nodeString());
            } else { //generate child of parse, the current node in tree
                Tree<String> child = generateNode(pt, tree);
                nodes.add(child);
            }
        }
        return node;
    }
}