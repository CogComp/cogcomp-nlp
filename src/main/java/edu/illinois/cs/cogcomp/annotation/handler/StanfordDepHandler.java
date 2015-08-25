package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StanfordDepHandler {
    private static Logger logger = LoggerFactory.getLogger(StanfordDepHandler.class);

    /**
     * given the input record, read off the text and generate a stanford constituency and dependency parse
     */
    public static TextAnnotation annotate(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator, TextAnnotation record) throws AnnotatorException {
        TreeView treeView = new TreeView(ViewNames.DEPENDENCY_STANFORD, "StanfordConverter", record, 1d);
        // The (tokenized) sentence offset in case we have more than one sentences in the record
        List<CoreMap> sentences = StanfordParseHandler.buildStanfordSentences(record);
        Annotation document = new Annotation(sentences);
        posAnnotator.annotate(document);
        parseAnnotator.annotate(document);
        sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        int offset = 0; // word offset -- last token of last sentence, adjusts dependency position indexes

        for (int sentenceId = 0; sentenceId < sentences.size(); sentenceId++) {
            CoreMap sentence = sentences.get(sentenceId);
            //edu.stanford.nlp.trees.Tree
            edu.stanford.nlp.trees.Tree stanfordTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            // dependency stuff
            Tree<String> tree = new Tree<String>();
            List<Tree<String>> depTree = parseToDependencyTree(stanfordTree, offset, sentence);
            for (Tree<String> subTree : depTree)
                tree.addSubtree(subTree);
            treeView.setParseTree(sentenceId, tree);

            offset += sentence.get(CoreAnnotations.TokensAnnotation.class).size();

        }
        return null;
    }

    /**
     * Convert a Stanford parse tree to a dependency tree and then to a Curator Tree
     *
     * @param parse
     * @param offset the index of the last token of the previous sentence. Needed to adjust dependency indexes to node list positions.
     * @param input
     * @return a tree corresponding to a single sentence. Tree nodes will have correct absolute char offsets wrt
     * the original text; but dependency node offsets are relative to the *sentence* (position in the list
     * of nodes just for this sentence, start index of zero).
     * @throws AnnotatorException
     */
    private static synchronized List<Tree<String>> parseToDependencyTree(
            edu.stanford.nlp.trees.Tree parse, int offset, CoreMap input)
            throws AnnotatorException {
        //       List<LabeledWord> sentence = parse.labeledYield();
        List<CoreLabel> stanfordTokens = input.get(CoreAnnotations.TokensAnnotation.class);
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsedTree();

        // position in sentence, Node and position in nodes
        Map<Integer, Pair<Node, Integer>> mainNodeMap = new HashMap<Integer, Pair<Node, Integer>>();
        // will store any copy nodes
        Map<Integer, Pair<Node, Integer>> copyNodeMap = new HashMap<Integer, Pair<Node, Integer>>();

        List<Tree<String>> nodes = new ArrayList<Tree<String>>();
        Set<Integer> nodesWithHeads = new HashSet<Integer>();


        //we will bind this nodeMap to the correct one as we build
        Map<Integer, Pair<Node, Integer>> nodeMap;
        Set<TypedDependency> seen = new HashSet<TypedDependency>();
        Set<TreeGraphNode> hasHeads = new HashSet<TreeGraphNode>();

        //TODO: this part needs documenting since it is quite involved
        //basically we have to convert from Stanford's td which are pairs
        //of words into a Tree structure.
        for (TypedDependency td : tdl) {
            logger.debug("{} duplicate? {}", td, seen.contains(td));
            logger.debug("has heads: {}", hasHeads.contains(td.dep()));
            //work around for duplicate dependencies
            if (seen.contains(td)) {
                logger.warn("Duplicate dependencies found for sentence:");
                logger.warn("{}", input);
                continue;
            }
            seen.add(td);

            //work around for words with multiple heads (we only take the first head we encounter)
            if (hasHeads.contains(td.dep())) {
                logger.warn("Non-tree dependency structure found for sentence:");
                logger.warn("{}", input);
                continue;
            }
            // dependent node has a head.
            hasHeads.add(td.dep());
            //TODO: understand how indexes are getting set.  Maybe token indexes should NOT be set.
            //  -- I didn't see them set in stanford tokenizer.
            // are these meant to correspond to nodes?

            // dependency indexes refer to *absolute* WORD indexes. correct using sentence offset.
            int hpos = td.gov().index() - offset;
            int dpos = td.dep().index() - offset;

            String label = td.reln().getShortName();
            if (label.equals("root"))
                continue;

            Integer hcopy = td.gov().label().get(CoreAnnotations.CopyAnnotation.class);
            Integer dcopy = td.dep().label().get(CoreAnnotations.CopyAnnotation.class);

            if (hpos == dpos) {
                logger.debug("hcopy: {}", hcopy);
                logger.debug("dcopy: {}", dcopy);
            }

            int depNodePos;
            Node headNode;
            Node depNode;
            if (hcopy != null) {
                nodeMap = copyNodeMap;
            } else {
                nodeMap = mainNodeMap;
            }
            if (nodeMap.containsKey(hpos)) {
                headNode = nodeMap.get(hpos).first;

            } else {
                headNode = new Node();
                headNode.setLabel("dependency node");
                CoreLabel stanfordTok = stanfordTokens.get(hpos);
                // assumes head span is the extent of the word representing the head
                Span headSpan = new Span(stanfordTok.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
                        stanfordTok.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
                if (hcopy != null) {
                    headSpan.setAttributes(new HashMap<String, String>());
                    headSpan.getAttributes().put("copy", String.valueOf(hcopy));
                }
                headNode.setSpan(headSpan);
                nodes.add(headNode);
                // nodeMap: head index, head node and position in nodeList
                nodeMap.put(hpos, new Pair<Node, Integer>(headNode, nodes.size() - 1));
            }

            if (dcopy != null) {
                nodeMap = copyNodeMap;
            } else {
                nodeMap = mainNodeMap;
            }
            if (nodeMap.containsKey(dpos)) {
                Pair<Node, Integer> pair = nodeMap.get(dpos);
                depNode = pair.first;
                depNodePos = pair.second;
            } else {
                depNode = new Node();
                depNode.setLabel("dependency node");
                CoreLabel stanfordTok = stanfordTokens.get(dpos);
                Span dependentSpan = new Span(stanfordTok.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
                        stanfordTok.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)); //wordToSpan(sentence.get(dpos), offset);
                if (dcopy != null) {
                    dependentSpan.setAttributes(new HashMap<String, String>());
                    dependentSpan.getAttributes().put("copy", String.valueOf(dcopy));
                }
                depNode.setSpan(dependentSpan);
                nodes.add(depNode);
                nodeMap.put(dpos, new Pair<Node, Integer>(depNode, nodes.size() - 1));
                depNodePos = nodes.size() - 1;
            }

            if (!headNode.isSetChildren()) {
                headNode.setChildren(new HashMap<Integer, String>());
            }
            headNode.getChildren().put(depNodePos, td.reln().getShortName());
            nodesWithHeads.add(depNodePos);
        }// end for td

        Set<Integer> headNodes = new HashSet<Integer>();

        for (int i = 0; i < nodes.size(); i++) {
            if (nodesWithHeads.contains(i)) {
                continue;
            }
            headNodes.add(i);
        }
        List<Tree<String>> trees = new ArrayList<Tree<String>>();
        for (Integer head : headNodes) {
            trees.add(extractTree(nodes, head));
        }
        return trees;
    }

    private static Tree<String> extractTree(List<Tree<String>> allNodes, int headindex) throws AnnotatorException {
        List<Tree<String>> nodes = new ArrayList<Tree<String>>();
        Tree<String> head = extractNode(allNodes, nodes, headindex);
        nodes.add(head);
        Tree<String> tree = new Tree<String>();
        tree.setNodes(nodes);
        tree.setTop(nodes.size() - 1);
        return tree;
    }

    private static Tree<String> extractNode(List<Tree<String>> allNodes, List<Tree<String>> nodes, int index) {
        Tree<String> current = allNodes.get(index);
        if (!current.isSetChildren()) {
            return current;
        }
        Map<Integer, String> children = new HashMap<Integer, String>();
        for (int childindex : current.getChildren().keySet()) {
            nodes.add(extractNode(allNodes, nodes, childindex));
            children.put(nodes.size() - 1, current.getChildren().get(childindex));
        }
        current.setChildren(children);
        return current;
    }
}
