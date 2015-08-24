package edu.illinois.cs.cogcomp.transitional;

/**
 * Created by mssammon on 8/23/15.
 */

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilderInterface;
import edu.illinois.cs.cogcomp.common.CuratorViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;
import edu.illinois.cs.cogcomp.thrift.base.*;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import edu.illinois.cs.cogcomp.thrift.base.*;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A utility class to map from Record data structures to TextAnnotation data structures
 * -- to be retired when Record dependencies are retired.
 */

public class CuratorDataStructureInterface {

    private static final Logger log = LoggerFactory
            .getLogger(CuratorDataStructureInterface.class);
//    private final TextAnnotationBuilderInterface textAnnotationBuilder;

//    public CuratorDataStructureInterface( TextAnnotationBuilderInterface taBuilder )
//    {
//        this.textAnnotationBuilder = taBuilder;
//    }

    public static TextAnnotation getTextAnnotationViewsFromRecord(String corpusId, String textId, Record record) {

        Labeling tokensLabeling = record.labelViews.get(ViewNames.TOKENS);
        Labeling sentenceLabeling = record.labelViews.get(ViewNames.SENTENCE);

        TextAnnotation ta = createTextAnnotation(corpusId, textId, record.getRawText(),
                tokensLabeling, sentenceLabeling);

        if (record.isSetParseViews()) {

            Map<String, Forest> parseViews = record.getParseViews();

            if (parseViews.containsKey(CuratorViewNames.stanfordDep)) {
                Forest dep = parseViews.get(CuratorViewNames.stanfordDep);
                TreeView depTree = alignForestToDependencyView(
                        ViewNames.DEPENDENCY_STANFORD, ta, dep);
                ta.addView(ViewNames.DEPENDENCY_STANFORD, depTree);
            }

            if (parseViews.containsKey(CuratorViewNames.dependencies)) {
                Forest dep = parseViews.get(CuratorViewNames.dependencies);
                TreeView depTree = alignForestToDependencyView(
                        ViewNames.DEPENDENCY, ta, dep);
                ta.addView(ViewNames.DEPENDENCY, depTree);
            }

            if (parseViews.containsKey(CuratorViewNames.charniak)) {
                Forest parseForest = parseViews.get(CuratorViewNames.charniak);
                TreeView parse = CuratorDataStructureInterface
                        .alignForestToParseTreeView(ViewNames.PARSE_CHARNIAK,
                                ta, parseForest);

                ta.addView(ViewNames.PARSE_CHARNIAK, parse);
            }

            if (parseViews.containsKey(CuratorViewNames.stanfordParse)) {
                Forest parseForest = parseViews
                        .get(CuratorViewNames.stanfordParse);
                TreeView parse = CuratorDataStructureInterface
                        .alignForestToParseTreeView(ViewNames.PARSE_STANFORD,
                                ta, parseForest);

                ta.addView(ViewNames.PARSE_STANFORD, parse);
            }

            if (parseViews.containsKey(ViewNames.PARSE_BERKELEY)) {
                Forest parseForest = parseViews.get(ViewNames.PARSE_BERKELEY);
                TreeView parse = CuratorDataStructureInterface
                        .alignForestToParseTreeView(ViewNames.PARSE_BERKELEY,
                                ta, parseForest);

                ta.addView(ViewNames.PARSE_BERKELEY, parse);
            }

            if (parseViews.containsKey(CuratorViewNames.srl)) {
                Forest srl = parseViews.get(CuratorViewNames.srl);
                PredicateArgumentView pav = alignForestToPredicateArgumentView(
                        ViewNames.SRL_VERB, ta, srl);
                ta.addView(ViewNames.SRL_VERB, pav);
            }

            if (parseViews.containsKey(CuratorViewNames.nom)) {
                Forest srl = parseViews.get(CuratorViewNames.nom);
                PredicateArgumentView pav = alignForestToPredicateArgumentView(
                        ViewNames.SRL_NOM, ta, srl);
                ta.addView(ViewNames.SRL_NOM, pav);
            }

        }

        if (record.isSetLabelViews()) {

            Map<String, Labeling> labelViews = record.getLabelViews();
            if (labelViews.containsKey(CuratorViewNames.chunk)) {
                Labeling chunkLabeling = labelViews.get(CuratorViewNames.chunk);
                SpanLabelView chunks = CuratorDataStructureInterface
                        .alignLabelingToSpanLabelView(ViewNames.SHALLOW_PARSE,
                                ta, chunkLabeling, false);
                ta.addView(ViewNames.SHALLOW_PARSE, chunks);
            }

            if (labelViews.containsKey(CuratorViewNames.ner)) {
                Labeling chunkLabeling = labelViews.get(CuratorViewNames.ner);
                SpanLabelView chunks = CuratorDataStructureInterface
                        .alignLabelingToSpanLabelView(ViewNames.NER, ta,
                                chunkLabeling, false);
                ta.addView(ViewNames.NER, chunks);
            }

            if (labelViews.containsKey(CuratorViewNames.quantities)) {
                Labeling chunkLabeling = labelViews
                        .get(CuratorViewNames.quantities);
                SpanLabelView chunks = CuratorDataStructureInterface
                        .alignLabelingToSpanLabelView(ViewNames.QUANTITIES, ta,
                                chunkLabeling, false);
                ta.addView(ViewNames.QUANTITIES, chunks);
            }

            if (labelViews.containsKey(CuratorViewNames.wikifier)) {
                Labeling chunkLabeling = labelViews
                        .get(CuratorViewNames.wikifier);
                SpanLabelView chunks = CuratorDataStructureInterface
                        .alignLabelingToSpanLabelView(ViewNames.WIKIFIER, ta,
                                chunkLabeling, true);
                ta.addView(ViewNames.WIKIFIER, chunks);
            }

            if (labelViews.containsKey(CuratorViewNames.pos)) {
                Labeling posLabeling = labelViews.get(CuratorViewNames.pos);
                TokenLabelView tlv = alignLabelingToTokenLabelView(
                        ViewNames.POS, ta, posLabeling);
                ta.addView(ViewNames.POS, tlv);
            }

            if (labelViews.containsKey(CuratorViewNames.lemma)) {
                Labeling lemmaLabeling = labelViews.get(CuratorViewNames.lemma);
                TokenLabelView tlv = alignLabelingToTokenLabelView(ViewNames.LEMMA, ta, lemmaLabeling);
                ta.addView(ViewNames.LEMMA, tlv);
            }
        }

        if (record.isSetClusterViews()) {
            Map<String, Clustering> clusterViews = record.getClusterViews();

            if (clusterViews.containsKey(CuratorViewNames.coref)) {
                Clustering clustering = clusterViews
                        .get(CuratorViewNames.coref);
                CoreferenceView coref = alignClusteringToCoreferenceView(
                        ViewNames.COREF, ta, clustering);
                ta.addView(ViewNames.COREF, coref);
            }
        }

        return ta;
    }

    public static TextAnnotation createTextAnnotation(String corpusId, String textId, String rawText, Labeling tokensLabeling, Labeling sentenceLabeling) {
        final List<Span> sentenceLabels = sentenceLabeling.getLabels();

        final int[] sentenceEndPositions = new int[sentenceLabels.size()];

        for (int i = 0; i < sentenceEndPositions.length; i++)
            sentenceEndPositions[i] = -1;

        final List<Span> labels = tokensLabeling.getLabels();
        final IntPair[] tokenCharOffsets = new IntPair[ labels.size() ];

        final String[] tokensArray = new String[labels.size()];

        int tokenId = 0;
        int sentenceId = 0;
        int nextSentenceEnd = sentenceLabels.get(sentenceId).getEnding();

        boolean startedSentence = true;
        boolean endedSentence = false;

        for (final Span token : labels) {

            final int rawTokenStart = token.getStart();
            final int rawTokenEnd = token.getEnding();

            tokensArray[tokenId] = rawText
                    .substring(rawTokenStart, rawTokenEnd);

            if (rawTokenEnd == nextSentenceEnd) {
                // we found a sentence. Let's mark it's end point in terms of
                // number of tokens.
                sentenceEndPositions[sentenceId] = tokenId + 1;

                endedSentence = true;

                sentenceId++;
                if (sentenceId < sentenceLabels.size())
                    nextSentenceEnd = sentenceLabels.get(sentenceId)
                            .getEnding();
                else {
                    // we have no more sentences. This means that there should
                    // be no more tokens. As a sanity check, let's assert this.
                    if (tokenId != labels.size() - 1) {
                        log.error("Found tokens that don't belong to any"
                                + " sentence for input: " + rawText);
                    }
                }

            }

            tokenId++;
            startedSentence = true;

        }

        // if we have started a sentence and seen some tokens, but not closed
        // it, then there is something wrong. However, to make things easy,
        // let's close the sentence with the expected sentence boundary.
        if (startedSentence && !endedSentence) {
            sentenceEndPositions[sentenceId] = tokenId;
        }

        return new TextAnnotation(corpusId, textId, rawText, tokenCharOffsets, tokensArray,
                sentenceEndPositions);
    }



    public static TreeView alignForestToParseTreeView(String viewName,
                                                      TextAnnotation ta, Forest parseForest) {

        final List<edu.illinois.cs.cogcomp.thrift.base.Tree> trees = parseForest
                .getTrees();
        final String parseSource = parseForest.getSource();

        final TreeView parseView = new TreeView(viewName, parseSource, ta, 0d);

        int sentenceId = 0;
        for (final edu.illinois.cs.cogcomp.thrift.base.Tree tree : trees) {
            Tree<String> parseTree;

            if (tree.isSetScore()) {

                final Pair<Tree<String>, Tree<Double>> treeInfo = getParseTreeScores(
                        parseForest.getRawText(), tree);

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

    protected static Pair<Tree<String>, Tree<Double>> getParseTreeScores(
            String sentenceRawString,
            edu.illinois.cs.cogcomp.thrift.base.Tree tree) {

        int top = tree.getTop();
        final List<Node> nodes = tree.getNodes();

        String topLabel = nodes.get(top).getLabel();
        double score = tree.getScore();

        Tree<String> parse = new Tree<String>(topLabel);

        Tree<Double> scores = new Tree<Double>(score);

        Map<Integer, Tree<String>> nodeMap = new HashMap<Integer, Tree<String>>();
        nodeMap.put(top, parse);

        Map<Integer, Tree<Double>> scoreNodeMap = new HashMap<Integer, Tree<Double>>();
        scoreNodeMap.put(top, scores);

        Queue<Integer> nodeIndices = new LinkedList<Integer>();
        nodeIndices.add(top);

        while (!nodeIndices.isEmpty()) {

            // get the top
            int current = nodeIndices.poll();
            Tree<String> parent = nodeMap.get(current);
            Tree<Double> scoreParent = scoreNodeMap.get(current);

            // get its children, and sort them
            Node currentNode = nodes.get(current);

            if (currentNode.isSetChildren()
                    && currentNode.getChildren().size() > 0) {
                List<Integer> childrenSetList = new ArrayList<Integer>();

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
                    Tree<String> childNode = new Tree<String>(nodes
                            .get(childId).getLabel());

                    Tree<Double> childScoreNode = new Tree<Double>(nodes.get(
                            childId).getScore());

                    parent.addSubtree(childNode);

                    scoreParent.addSubtree(childScoreNode);

                    nodeMap.put(childId, childNode);
                    scoreNodeMap.put(childId, childScoreNode);
                    nodeIndices.add(childId);
                }
            } else {
                // this is a leaf.

                Span span = currentNode.span;
                String leafLabel = sentenceRawString.substring(span.getStart(),
                        span.getEnding()).trim();

                leafLabel = ParseUtils.convertBracketsToPTBFormat(leafLabel);

                parent.addLeaf(leafLabel);

                scoreParent.addLeaf(0d);
            }

        }

        return new Pair<Tree<String>, Tree<Double>>(parse, scores);

    }

    protected static Tree<String> getParseTree(String sentenceRawString,
                                               edu.illinois.cs.cogcomp.thrift.base.Tree tree) {

        assert sentenceRawString != null;

        int top = tree.getTop();
        final List<Node> nodes = tree.getNodes();

        String topLabel = nodes.get(top).getLabel();
        Tree<String> parse = new Tree<String>(topLabel);

        Map<Integer, Tree<String>> nodeMap = new HashMap<Integer, Tree<String>>();
        nodeMap.put(top, parse);

        Queue<Integer> nodeIndices = new LinkedList<Integer>();
        nodeIndices.add(top);

        while (!nodeIndices.isEmpty()) {

            // get the top
            int current = nodeIndices.poll();
            Tree<String> parent = nodeMap.get(current);

            // get its children, and sort them
            Node currentNode = nodes.get(current);
            if (currentNode.isSetChildren()
                    && currentNode.getChildren().size() > 0) {
                List<Integer> childrenSet = new ArrayList<Integer>();

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
                    Tree<String> childNode = new Tree<String>(nodes
                            .get(childId).getLabel());

                    parent.addSubtree(childNode);

                    nodeMap.put(childId, childNode);
                    nodeIndices.add(childId);
                }

            } else {
                // this is a leaf.
                Span span = nodes.get(current).span;
                String leafLabel = sentenceRawString.substring(span.getStart(),
                        span.getEnding()).trim();

                leafLabel = ParseUtils.convertBracketsToPTBFormat(leafLabel);

                parent.addLeaf(leafLabel);
            }

        }

        return parse;

    }

    public static PredicateArgumentView alignForestToPredicateArgumentView(
            String viewName, TextAnnotation ta, Forest forest) {

        if (forest == null)
            return new PredicateArgumentView(viewName, "", ta, 1.0);

        String source = "";
        if (forest.isSetSource())
            source = forest.getSource();

        PredicateArgumentView pav = new PredicateArgumentView(viewName, source,
                ta, 1.0);

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

            Constituent predicate = getNewConstituentForSpan(label, viewName,
                    ta, rootSpan);
            if (rootSpan.isSetAttributes()) {
                Map<String, String> attr = rootSpan.getAttributes();

                if (attr.containsKey("sense")) {
                    predicate.addAttribute(
                            CoNLLColumnFormatReader.SenseIdentifer,
                            attr.get("sense"));
                }

                if (attr.containsKey("predicate")) {
                    predicate.addAttribute(
                            CoNLLColumnFormatReader.LemmaIdentifier,
                            attr.get("predicate"));
                }
            }

            List<Constituent> arguments = new ArrayList<Constituent>();

            String[] relations = new String[tree.getNodes().size() - 1];
            double[] scores = new double[relations.length];

            int index = 0;
            for (int childNodeId : rootNode.getChildren().keySet()) {

                Node childNode = tree.getNodes().get(childNodeId);

                String childLabel = rootNode.getChildren().get(childNodeId);

                Constituent spanConstituent = getNewConstituentForSpan(
                        childNode.getLabel(), viewName, ta, childNode.getSpan());

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
     * Aligns a {@link Clustering} to a {@link TextAnnotation} to produce a
     * {@link CoreferenceView}. Note: This function assumes that the longest
     * mention (in terms of the number of characters) is the canonical mention.
     *
     * @param viewName
     * @param ta
     * @param clustering
     * @return A coreference view
     */
    public static CoreferenceView alignClusteringToCoreferenceView(
            String viewName, TextAnnotation ta, Clustering clustering) {

        String generator = clustering.getSource();
        double score = clustering.getScore();

        CoreferenceView view = new CoreferenceView(viewName, generator, ta,
                score);

        for (Labeling labeling : clustering.getClusters()) {

            List<Constituent> coreferentMentions = new ArrayList<Constituent>();
            Constituent canonicalMention = null;

            double[] scores = new double[labeling.getLabels().size()];
            int spanId = 0;

            for (Span span : labeling.getLabels()) {

                // first make a constituent, for the span, copy all attributes
                Constituent constituent = getNewConstituentForSpan(
                        span.getLabel(), viewName, ta, span);

                coreferentMentions.add(constituent);
                if (canonicalMention == null
                        || canonicalMention.getSurfaceString().length() < constituent
                        .getSurfaceString().length())
                    canonicalMention = constituent;

                scores[spanId] = span.getScore();
                spanId++;

            }

            view.addCorefEdges(canonicalMention, coreferentMentions, scores);

        }

        return view;
    }

    /**
     * Aligns a {@link Labeling} to a {@link SpanLabelView}.
     *
     * @param viewName
     * @param ta
     * @param spanLabeling
     * @return A SpanLabelView
     */
    public static SpanLabelView alignLabelingToSpanLabelView(String viewName,
                                                             TextAnnotation ta, Labeling spanLabeling,
                                                             boolean allowOverlappingSpans) {
        List<Span> labels = spanLabeling.getLabels();
        double score = spanLabeling.getScore();
        String generator = spanLabeling.getSource();

        SpanLabelView view = new SpanLabelView(viewName, generator, ta, score,
                allowOverlappingSpans);

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
                endTokenId = ta
                        .getTokenIdFromCharacterOffset(span.getEnding() - 1);

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
                existsSpan = view.getConstituentsCoveringSpan(tokenId,
                        endTokenId + 1).size() > 0;

            if (!existsSpan || allowOverlappingSpans) {

                Constituent newConstituent = view.addSpanLabel(tokenId,
                        endTokenId + 1, span.getLabel(), span.getScore());

                if (span.isSetAttributes() && span.getAttributes().size() > 0) {

                    copyAttributesToConstituent(span, newConstituent);

                }
            }
        }
        return view;
    }

    /**
     * Aligns a {@link Labeling} to a {@link TokenLabelView}.
     *
     * @param viewName
     * @param ta
     * @param labeling
     * @return A TokenLabelView
     */
    public static TokenLabelView alignLabelingToTokenLabelView(String viewName,
                                                               TextAnnotation ta, Labeling labeling) {
        List<Span> labels = labeling.getLabels();
        double score = labeling.getScore();
        String generator = labeling.getSource();

        TokenLabelView view = new TokenLabelView(viewName, generator, ta, score);

        for (Span span : labels) {

            int tokenId = ta.getTokenIdFromCharacterOffset(span.getStart());

            int endTokenId = ta.getTokenIdFromCharacterOffset(span.getEnding());

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

    public static  TreeView alignForestToDependencyView(String viewName,
                                                       TextAnnotation ta, Forest dep) {
        TreeView view = new TreeView(viewName, dep.getSource(), ta, 0.0d);

        for (edu.illinois.cs.cogcomp.thrift.base.Tree tree : dep.getTrees()) {

            int topId = tree.getTop();
            List<Node> nodes = tree.getNodes();
            int topTokenStart = nodes.get(topId).getSpan().getStart();
            int topTokenId = ta.getTokenIdFromCharacterOffset(topTokenStart);
            int sentenceId = ta.getSentenceId(topTokenId);

            Tree<Pair<String, Integer>> dependencyTree = makeDependencyTree(ta,
                    tree);

            double score = tree.getScore();

            view.setDependencyTree(sentenceId, dependencyTree, score);
        }
        return view;
    }

    /**
     * Converts a curator tree into a dependency tree that can be added to a
     * TreeView.
     *
     * @param tree
     * @return A dependency tree for the curator Tree
     */
    protected static Tree<Pair<String, Integer>> makeDependencyTree(
            TextAnnotation ta, edu.illinois.cs.cogcomp.thrift.base.Tree tree) {

        Map<Node, Tree<Pair<String, Integer>>> nodeToTreeMap = new HashMap<Node, Tree<Pair<String, Integer>>>();

        Queue<Node> nodeQueue = new LinkedList<Node>();

        int topId = tree.getTop();
        List<Node> nodes = tree.getNodes();

        Node topNode = nodes.get(topId);
        int topTokenId = ta.getTokenIdFromCharacterOffset(topNode.getSpan()
                .getStart());

        int sentenceStart = ta.getSentence(ta.getSentenceId(topTokenId))
                .getStartSpan();

        Tree<Pair<String, Integer>> topTree = new Tree<Pair<String, Integer>>(
                new Pair<String, Integer>(ta.getToken(topTokenId), topTokenId
                        - sentenceStart));

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

                int childTokenId = ta.getTokenIdFromCharacterOffset(childNode
                        .getSpan().getStart());

                String childToken = ta.getToken(childTokenId);

                Tree<Pair<String, Integer>> childTree = new Tree<Pair<String, Integer>>(
                        new Pair<String, Integer>(childToken, childTokenId
                                - sentenceStart));

                treeNode.addSubtree(childTree, new Pair<String, Integer>(
                        edgeLabel, 1));

                nodeToTreeMap.put(childNode, childTree);
                nodeQueue.add(childNode);

            }
        }

        return nodeToTreeMap.get(topNode);

    }

    protected static Constituent getNewConstituentForSpan(String label,
                                                          String viewName, TextAnnotation ta, Span span) {

        int start = ta.getTokenIdFromCharacterOffset(span.getStart());
        int end = ta.getTokenIdFromCharacterOffset(span.getEnding() - 1) + 1;

        Constituent constituent = new Constituent(label, viewName, ta, start,
                end);

        if (span.isSetAttributes()) {
            copyAttributesToConstituent(span, constituent);
        }
        return constituent;
    }

    protected static void copyAttributesToConstituent(Span span,
                                                      Constituent newConstituent) {

        for (String attributeKey : span.getAttributes().keySet()) {
            newConstituent.addAttribute(attributeKey,
                    span.getAttributes().get(attributeKey));
        }
    }

    public Forest convertPredicateArgumentViewToForest(
            PredicateArgumentView pav) {
        Forest forest = new Forest();

        forest.setSource(pav.getViewGenerator());
        forest.setTrees(new ArrayList<edu.illinois.cs.cogcomp.thrift.base.Tree>());

        for (Constituent predicate : pav.getPredicates()) {

            edu.illinois.cs.cogcomp.thrift.base.Tree tree = new edu.illinois.cs.cogcomp.thrift.base.Tree();

            tree.setNodes(new ArrayList<Node>());

            Span span = new Span();
            span.setStart(predicate.getStartCharOffset());
            span.setEnding(predicate.getEndCharOffset());
            span.setAttributes(new HashMap<String, String>());
            span.getAttributes()
                    .put("predicate",
                            predicate
                                    .getAttribute(CoNLLColumnFormatReader.LemmaIdentifier));
            span.getAttributes()
                    .put("sense",
                            predicate
                                    .getAttribute(CoNLLColumnFormatReader.SenseIdentifer));

            Node pred = new Node();
            pred.setSpan(span);
            pred.setLabel("Predicate");
            pred.setChildren(new HashMap<Integer, String>());

            tree.getNodes().add(pred);
            tree.setTop(tree.getNodes().size() - 1);

            for (Relation argument : pav.getArguments(predicate)) {
                Span argSpan = new Span();
                Constituent argConstituent = argument.getTarget();
                argSpan.setStart(argConstituent.getStartCharOffset());
                argSpan.setEnding(argConstituent.getEndCharOffset());

                argSpan.setAttributes(new HashMap<String, String>());
                for (String attrib : argConstituent.getAttributeKeys()) {
                    argSpan.getAttributes().put(attrib,
                            argConstituent.getAttribute(attrib));
                }

                Node arg = new Node();
                arg.setSpan(argSpan);
                arg.setLabel("Argument");

                tree.getNodes().add(arg);
                pred.getChildren().put(tree.getNodes().size() - 1,
                        argument.getRelationName());
            }

            forest.getTrees().add(tree);
        }

        return forest;
    }
}
