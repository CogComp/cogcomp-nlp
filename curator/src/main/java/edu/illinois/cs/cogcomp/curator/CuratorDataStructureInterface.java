/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;
import edu.illinois.cs.cogcomp.thrift.base.*;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CuratorDataStructureInterface {

    private final static Logger log = LoggerFactory.getLogger(CuratorDataStructureInterface.class);

    public static TextAnnotation getTextAnnotationFromRecord(String corpusId, String textId,
            Record record, Labeling tokensLabeling, Labeling sentenceLabeling) {

        final String rawText = record.getRawText();
        final List<Span> sentenceLabels = sentenceLabeling.getLabels();

        final int[] sentenceEndPositions = new int[sentenceLabels.size()];

        Arrays.fill(sentenceEndPositions, -1);

        final List<Span> labels = tokensLabeling.getLabels();
        List<IntPair> charOffsetsList = new ArrayList<>();

        final String[] tokensArray = new String[labels.size()];

        int tokenId = 0;
        int sentenceId = 0;
        int nextSentenceEnd = sentenceLabels.get(sentenceId).getEnding();

        boolean endedSentence = false;

        for (final Span token : labels) {
            // The raw token spans are corrected for sentence indexing, no translation needed
            final int rawTokenStart = token.getStart();
            final int rawTokenEnd = token.getEnding();

            tokensArray[tokenId] = rawText.substring(rawTokenStart, rawTokenEnd);
            charOffsetsList.add(new IntPair(rawTokenStart, rawTokenEnd));

            if (rawTokenEnd == nextSentenceEnd) {
                // we found a sentence. Let's mark it's end point in terms of
                // number of tokens.
                sentenceEndPositions[sentenceId] = tokenId + 1;

                endedSentence = true;

                sentenceId++;
                if (sentenceId < sentenceLabels.size())
                    nextSentenceEnd = sentenceLabels.get(sentenceId).getEnding();
                else {
                    // we have no more sentences. This means that there should
                    // be no more tokens. As a sanity check, let's assert this.
                    if (tokenId != labels.size() - 1)
                        log.error("Found tokens that don't belong to any sentence for input: "
                                + rawText);
                }

            }
            tokenId++;
        }

        // if we have started a sentence and seen some tokens, but not closed
        // it, then there is something wrong. However, to make things easy,
        // let's close the sentence with the expected sentence boundary.
        if (!endedSentence) {
            sentenceEndPositions[sentenceId] = tokenId;
        }

        IntPair[] characterOffsets = new IntPair[charOffsetsList.size()];
        for (int i = 0; i < charOffsetsList.size(); i++)
            characterOffsets[i] = charOffsetsList.get(i);

        return new TextAnnotation(corpusId, textId, rawText, characterOffsets, tokensArray,
                sentenceEndPositions);
    }

    /**
     * note that the trees in the Forest argument need not be actual parse trees (they can e.g. be output by
     *    SRL components) -- so we cannot enforce a constraint requiring that the number of trees be
     *    the same as the number of sentences.
     * @param viewName
     * @param ta
     * @param parseForest
     * @return
     */
    public static TreeView alignForestToParseTreeView(String viewName, TextAnnotation ta,
            Forest parseForest) {

//        if ((parseForest.trees.size() != ta.getView(ViewNames.SENTENCE).getNumberOfConstituents())) {
//            String msg = "ERROR: Number of trees does not agree with number of sentences (" +
//                    parseForest.trees.size() + " vs. " + ta.getView(ViewNames.SENTENCE).getNumberOfConstituents() + ").";
//            log.warn(msg);
//        }

        final List<edu.illinois.cs.cogcomp.thrift.base.Tree> trees = parseForest.getTrees();
        final String parseSource = parseForest.getSource();

        final TreeView parseView = new TreeView(viewName, parseSource, ta, 0d);

        int sentenceId = 0;
        for (final edu.illinois.cs.cogcomp.thrift.base.Tree tree : trees) {
            Tree<String> parseTree;

            if (tree.isSetScore()) {

                final Pair<Tree<String>, Tree<Double>> treeInfo =
                        getParseTreeScores(parseForest.getRawText(), tree);

                parseTree = treeInfo.getFirst();
                final Tree<Double> score = treeInfo.getSecond();
                parseView.setScoredParseTree(sentenceId, parseTree, score);

            } else {
                parseTree = getParseTree(ta.getText(), tree);
                parseView.setParseTree(sentenceId, parseTree);
            }
            sentenceId++;
        }
        return parseView;
    }

    protected static Pair<Tree<String>, Tree<Double>> getParseTreeScores(String sentenceRawString,
            edu.illinois.cs.cogcomp.thrift.base.Tree tree) {

        int top = tree.getTop();
        final List<Node> nodes = tree.getNodes();

        String topLabel = nodes.get(top).getLabel();
        double score = tree.getScore();

        Tree<String> parse = new Tree<>(topLabel);

        Tree<Double> scores = new Tree<>(score);

        Map<Integer, Tree<String>> nodeMap = new HashMap<>();
        nodeMap.put(top, parse);

        Map<Integer, Tree<Double>> scoreNodeMap = new HashMap<>();
        scoreNodeMap.put(top, scores);

        Queue<Integer> nodeIndices = new LinkedList<>();
        nodeIndices.add(top);

        while (!nodeIndices.isEmpty()) {

            // get the top
            int current = nodeIndices.poll();
            Tree<String> parent = nodeMap.get(current);
            Tree<Double> scoreParent = scoreNodeMap.get(current);

            // get its children, and sort them
            Node currentNode = nodes.get(current);

            if (currentNode.isSetChildren() && currentNode.getChildren().size() > 0) {
                List<Integer> childrenSetList = new ArrayList<>();

                childrenSetList.addAll(currentNode.children.keySet());
                Collections.sort(childrenSetList, new Comparator<Integer>() {
                    public int compare(Integer arg0, Integer arg1) {
                        int span0 = nodes.get(arg0).span.getStart();
                        int span1 = nodes.get(arg1).span.getStart();

                        if (span0 < span1)
                            return -1;
                        else if (span0 > span1)
                            return 1;
                        else
                            return 0;
                    }
                });

                // add each child to the correct place in the parse tree
                for (int childId : childrenSetList) {
                    Tree<String> childNode = new Tree<>(nodes.get(childId).getLabel());

                    Tree<Double> childScoreNode = new Tree<>(nodes.get(childId).getScore());

                    parent.addSubtree(childNode);

                    scoreParent.addSubtree(childScoreNode);

                    nodeMap.put(childId, childNode);
                    scoreNodeMap.put(childId, childScoreNode);
                    nodeIndices.add(childId);
                }
            } else {
                // this is a leaf.
                Span span = currentNode.span;
                String leafLabel =
                        sentenceRawString.substring(span.getStart(), span.getEnding()).trim();

                leafLabel = ParseUtils.convertBracketsToPTBFormat(leafLabel);

                parent.addLeaf(leafLabel);

                scoreParent.addLeaf(0d);
            }
        }
        return new Pair<>(parse, scores);
    }

    protected static Tree<String> getParseTree(String sentenceRawString,
            edu.illinois.cs.cogcomp.thrift.base.Tree tree) {

        assert sentenceRawString != null;

        int top = tree.getTop();
        final List<Node> nodes = tree.getNodes();

        String topLabel = nodes.get(top).getLabel();
        Tree<String> parse = new Tree<>(topLabel);

        Map<Integer, Tree<String>> nodeMap = new HashMap<>();
        nodeMap.put(top, parse);

        Queue<Integer> nodeIndices = new LinkedList<>();
        nodeIndices.add(top);

        while (!nodeIndices.isEmpty()) {

            // get the top
            int current = nodeIndices.poll();
            Tree<String> parent = nodeMap.get(current);

            // get its children, and sort them
            Node currentNode = nodes.get(current);
            if (currentNode.isSetChildren() && currentNode.getChildren().size() > 0) {
                List<Integer> childrenSet = new ArrayList<>();

                childrenSet.addAll(currentNode.children.keySet());
                Collections.sort(childrenSet, new Comparator<Integer>() {
                    public int compare(Integer arg0, Integer arg1) {
                        int span0 = nodes.get(arg0).span.getStart();
                        int span1 = nodes.get(arg1).span.getStart();

                        if (span0 < span1)
                            return -1;
                        else if (span0 > span1)
                            return 1;
                        else
                            return 0;
                    }
                });

                // add each child to the correct place in the parse tree
                for (int childId : childrenSet) {
                    Tree<String> childNode = new Tree<>(nodes.get(childId).getLabel());

                    parent.addSubtree(childNode);

                    nodeMap.put(childId, childNode);
                    nodeIndices.add(childId);
                }

            } else {
                // this is a leaf.
                Span span = nodes.get(current).span;
                String leafLabel =
                        sentenceRawString.substring(span.getStart(), span.getEnding()).trim();

                leafLabel = ParseUtils.convertBracketsToPTBFormat(leafLabel);

                parent.addLeaf(leafLabel);
            }
        }
        return parse;
    }

    public static PredicateArgumentView alignForestToPredicateArgumentView(String viewName,
            TextAnnotation ta, Forest forest) {
        if (forest == null)
            return new PredicateArgumentView(viewName, "", ta, 1.0);

//        if ((forest.trees.size() != ta.getView(ViewNames.SENTENCE).getNumberOfConstituents())) {
//
//            String msg = "ERROR: Number of trees does not agree with number of sentences (" +
//                    forest.trees.size() + " vs. " + ta.getView(ViewNames.SENTENCE).getNumberOfConstituents() + ").";
//            log.warn(msg);
////         IllegalStateException();
//        }

        String source = "";
        if (forest.isSetSource())
            source = forest.getSource();

        PredicateArgumentView pav = new PredicateArgumentView(viewName, source, ta, 1.0);

        if (!forest.isSetTrees())
            return pav;

        for (edu.illinois.cs.cogcomp.thrift.base.Tree tree : forest.getTrees()) {
            if (!tree.isSetTop())
                continue;

            int root = tree.getTop();
            Node rootNode = tree.getNodes().get(root);

            Span rootSpan = rootNode.getSpan();

            String label;

            if (rootNode.isSetLabel())
                label = rootNode.getLabel();
            else if (rootSpan.isSetLabel())
                label = rootSpan.getLabel();
            else
                label = "";

            Constituent predicate = getNewConstituentForSpan(label, viewName, ta, rootSpan);
            if (rootSpan.isSetAttributes()) {
                Map<String, String> attr = rootSpan.getAttributes();

                if (attr.containsKey("sense")) {
                    predicate.addAttribute(PredicateArgumentView.SenseIdentifer, attr.get("sense"));
                }

                if (attr.containsKey("predicate")) {
                    predicate.addAttribute(PredicateArgumentView.LemmaIdentifier,
                            attr.get("predicate"));
                }
            }

            List<Constituent> arguments = new ArrayList<>();

            String[] relations = new String[tree.getNodes().size() - 1];
            double[] scores = new double[relations.length];

            int index = 0;
            for (int childNodeId : rootNode.getChildren().keySet()) {

                Node childNode = tree.getNodes().get(childNodeId);

                String childLabel = rootNode.getChildren().get(childNodeId);

                Constituent spanConstituent =
                        getNewConstituentForSpan(childNode.getLabel(), viewName, ta,
                                childNode.getSpan());

                arguments.add(spanConstituent);
                relations[index] = childLabel;
                scores[index] = childNode.getScore();

                index++;
            }

            pav.addPredicateArguments(predicate, arguments, relations, scores);
        }
        return pav;
    }

    /**
     * Aligns a {@link edu.illinois.cs.cogcomp.thrift.base.Clustering} to a
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation} to produce
     * a {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView}. Note:
     * This function assumes that the longest mention (in terms of the number of characters) is the
     * canonical mention.
     *
     * @return A coreference view
     */
    public static CoreferenceView alignClusteringToCoreferenceView(String viewName,
            TextAnnotation ta, Clustering clustering) {

        String generator = clustering.getSource();
        double score = clustering.getScore();

        CoreferenceView view = new CoreferenceView(viewName, generator, ta, score);

        for (Labeling labeling : clustering.getClusters()) {

            List<Constituent> coreferentMentions = new ArrayList<>();
            Constituent canonicalMention = null;

            double[] scores = new double[labeling.getLabels().size()];
            int spanId = 0;

            for (Span span : labeling.getLabels()) {

                // first make a constituent, for the span, copy all attributes
                Constituent constituent =
                        getNewConstituentForSpan(span.getLabel(), viewName, ta, span);

                coreferentMentions.add(constituent);
                if (canonicalMention == null
                        || canonicalMention.getSurfaceForm().length() < constituent
                                .getSurfaceForm().length())
                    canonicalMention = constituent;

                scores[spanId] = span.getScore();
                spanId++;

            }

            view.addCorefEdges(canonicalMention, coreferentMentions, scores);
        }
        return view;
    }

    /**
     * Aligns a {@link edu.illinois.cs.cogcomp.thrift.base.Labeling} to a
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView}.
     *
     * @return A SpanLabelView
     */
    public static SpanLabelView alignLabelingToSpanLabelView(String viewName, TextAnnotation ta,
            Labeling spanLabeling, boolean allowOverlappingSpans) {
        List<Span> labels = spanLabeling.getLabels();
        double score = spanLabeling.getScore();
        String generator = spanLabeling.getSource();

        SpanLabelView view =
                new SpanLabelView(viewName, generator, ta, score, allowOverlappingSpans);

        for (Span span : labels) {
            int tokenId = ta.getTokenIdFromCharacterOffset(span.getStart());

            // The following check is to verify that the end token is not zero.
            // This happens very rarely with the Illinois NER (version 2.1). For
            // example, for the one-word sentence "Conrail", it picks both start
            // and end of the ORG as the character zero. (Why, I don't know!)

            int endTokenId;
            if (span.getEnding() == 0)
                endTokenId = 0;
            else
                endTokenId = ta.getTokenIdFromCharacterOffset(span.getEnding() - 1);

            // Hack to deal with the fact that sometimes the alignment might not
            // be perfect. For example, consider the sentence
            //
            // The Sidley-Ashurst venture will also be staffed by another Sidley
            // partner specializing in corporate law , a partner from Ashurst
            // concentrating on acquisitions and a Japanese attorney .
            //
            // If this is given to the NER, then it will annotatate Sidley as
            // ORG and Ashurst as MISC. However, this will be a single token in
            // the TextAnnotation. One way to deal with this is to add an
            // attribute each time we see a duplicate label.
            // However, this is a maintenance nightmare. So for now, the first
            // label will be taken and everything else ignored.

            boolean existsSpan = false;
            if (!allowOverlappingSpans)
                existsSpan = view.getConstituentsCoveringSpan(tokenId, endTokenId + 1).size() > 0;

            if (allowOverlappingSpans || !existsSpan) {
                Constituent newConstituent =
                        view.addSpanLabel(tokenId, endTokenId + 1, span.getLabel(), span.getScore());

                if (span.isSetAttributes() && span.getAttributes().size() > 0) {
                    copyAttributesToConstituent(span, newConstituent);
                }
            }
        }
        return view;
    }

    /**
     * Aligns a {@link edu.illinois.cs.cogcomp.thrift.base.Labeling} to a
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView}.
     *
     * <b>NOTE:</b> must correct for one-past-the-end labeling when calling
     * {@link TextAnnotation#getTokenIdFromCharacterOffset(int)}.
     * 
     * @return A TokenLabelView
     */
    public static TokenLabelView alignLabelingToTokenLabelView(String viewName, TextAnnotation ta,
            Labeling labeling) {
        List<Span> labels = labeling.getLabels();
        double score = labeling.getScore();
        String generator = labeling.getSource();

        TokenLabelView view = new TokenLabelView(viewName, generator, ta, score);

        for (Span span : labels) {

            int tokenId = ta.getTokenIdFromCharacterOffset(span.getStart());

            int endTokenId = ta.getTokenIdFromCharacterOffset(span.getEnding() - 1);

            if (tokenId == endTokenId)
                endTokenId++;

            for (int i = tokenId; i < endTokenId; i++) {

                view.addTokenLabel(i, span.getLabel(), span.getScore());

                if (span.isSetAttributes() && span.getAttributes().size() > 0) {
                    Constituent c = view.getConstituentAtToken(i);
                    copyAttributesToConstituent(span, c);
                }
            }

        }
        return view;
    }

    public static TreeView alignForestToDependencyView(String viewName, TextAnnotation ta,
            Forest dep) {
        TreeView view = new TreeView(viewName, dep.getSource(), ta, 0.0d);

        for (edu.illinois.cs.cogcomp.thrift.base.Tree tree : dep.getTrees()) {
            int topId = tree.getTop();
            List<Node> nodes = tree.getNodes();
            int topTokenStart = nodes.get(topId).getSpan().getStart();
            int topTokenId = ta.getTokenIdFromCharacterOffset(topTokenStart);
            int sentenceId = ta.getSentenceId(topTokenId);

            Tree<Pair<String, Integer>> dependencyTree = makeDependencyTree(ta, tree);

            double score = tree.getScore();

            view.setDependencyTree(sentenceId, dependencyTree, score);
        }
        return view;
    }

    /**
     * Converts a curator tree into a dependency tree that can be added to a TreeView.
     *
     * @return A dependency tree for the curator Tree
     */
    protected static Tree<Pair<String, Integer>> makeDependencyTree(TextAnnotation ta,
            edu.illinois.cs.cogcomp.thrift.base.Tree tree) {

        Map<Node, Tree<Pair<String, Integer>>> nodeToTreeMap = new HashMap<>();

        Queue<Node> nodeQueue = new LinkedList<>();

        int topId = tree.getTop();
        List<Node> nodes = tree.getNodes();

        Node topNode = nodes.get(topId);
        int topTokenId = ta.getTokenIdFromCharacterOffset(topNode.getSpan().getStart());

        int sentenceStart = ta.getSentence(ta.getSentenceId(topTokenId)).getStartSpan();

        Tree<Pair<String, Integer>> topTree =
                new Tree<>(new Pair<>(ta.getToken(topTokenId), topTokenId - sentenceStart));

        nodeToTreeMap.put(topNode, topTree);

        nodeQueue.add(topNode);

        while (!nodeQueue.isEmpty()) {
            Node node = nodeQueue.poll();

            if (node == null)
                break;

            if (!node.isSetChildren())
                continue;

            if (node.getChildren().size() == 0)
                continue;

            Tree<Pair<String, Integer>> treeNode = nodeToTreeMap.get(node);

            for (int childId : node.getChildren().keySet()) {
                String edgeLabel = node.getChildren().get(childId);

                Node childNode = nodes.get(childId);

                int childTokenId = ta.getTokenIdFromCharacterOffset(childNode.getSpan().getStart());

                String childToken = ta.getToken(childTokenId);

                Tree<Pair<String, Integer>> childTree =
                        new Tree<>(new Pair<>(childToken, childTokenId - sentenceStart));

                treeNode.addSubtree(childTree, new Pair<>(edgeLabel, 1));

                nodeToTreeMap.put(childNode, childTree);
                nodeQueue.add(childNode);
            }
        }
        return nodeToTreeMap.get(topNode);
    }

    protected static Constituent getNewConstituentForSpan(String label, String viewName,
            TextAnnotation ta, Span span) {
        int start = ta.getTokenIdFromCharacterOffset(span.getStart());
        int end = ta.getTokenIdFromCharacterOffset(span.getEnding() - 1) + 1;

        Constituent constituent = new Constituent(label, viewName, ta, start, end);

        if (span.isSetAttributes()) {
            copyAttributesToConstituent(span, constituent);
        }
        return constituent;
    }

    protected static void copyAttributesToConstituent(Span span, Constituent newConstituent) {
        for (String attributeKey : span.getAttributes().keySet()) {
            newConstituent.addAttribute(attributeKey, span.getAttributes().get(attributeKey));
        }
    }
}
