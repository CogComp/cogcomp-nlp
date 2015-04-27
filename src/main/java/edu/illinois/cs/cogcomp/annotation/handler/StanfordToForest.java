package edu.illinois.cs.cogcomp.annotation.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.nlp.curator.Pair;
import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.edison.data.curator.CuratorViewNames;
import edu.illinois.cs.cogcomp.thrift.base.Forest;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Node;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.base.Tree;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class StanfordToForest {
	
	/**
	 * A map between the spans of the tokenizer (that refer to the raw string)
	 * and the actual tokenized spans. 
	 */
	private static Map<Span, Span> tokenizedToRaw;
	private static List<Integer> tokenizedSentenceStarts;
	
	public static Forest convert(StanfordCoreNLP pipeline, Record record) throws TException {
		Forest parseForest = new Forest();
//        Forest dependencyForest = new Forest();
		parseForest.setSource("stanfordparser");
//        dependencyForest.setSource( "stanfordparser" );

		Labeling sentenceLabeling = record.getLabelViews().get(CuratorViewNames.sentences);
		Labeling tokenLabeling = record.getLabelViews().get(CuratorViewNames.tokens);
		convertRawToTokenized(tokenLabeling, sentenceLabeling);
		assert tokenizedSentenceStarts.size() == sentenceLabeling.getLabels().size();
		
		// The (tokenized) sentence offset in case we have more than one sentences in the record
    	int offset = 0;
            for (int sentInd = 0; sentInd < sentenceLabeling.getLabels().size(); ++sentInd) {
                Span rawSentSpan = sentenceLabeling.getLabels().get(sentInd);
                // We need to feed the tokenized sentence
                String rawText = record.getRawText();
                String sentenceText = "";
                List<Span> spans = tokenLabeling.getLabels();
                for (int i = 0; i < spans.size(); ++i) {
                    Span pos = spans.get(i);
                    if (pos.getStart() < rawSentSpan.getStart() || pos.getEnding() > rawSentSpan.getEnding())
                        continue;
                    sentenceText += rawText.substring(pos.getStart(), pos.getEnding()) + " ";
                }
                Annotation document = new Annotation(sentenceText.trim());
                pipeline.annotate(document);

                offset = tokenizedSentenceStarts.get(sentInd);
                // For each sentence (in the document) create a thrift.base.Tree
                List<CoreMap> sentences = document.get(SentencesAnnotation.class);
                // XXX Given the ssplit.eolonly option we set during the pipeline initialization
                // there should be only one sentence here
                for(CoreMap sentence: sentences) {
                    edu.stanford.nlp.trees.Tree stanfordTree = sentence.get(TreeAnnotation.class);
                    // Convert from Stanford Tree to thrift.base.Tree
                    for (edu.stanford.nlp.trees.Tree pt : stanfordTree.getChildrenAsList()) {
                        Tree tree = new Tree();
                        Node top = generateNode(pt, tree, offset);
                        tree.getNodes().add(top);
                        tree.setTop(tree.getNodes().size() - 1);
                        if (!parseForest.isSetTrees()) {
                            parseForest.setTrees(new ArrayList<Tree>());
                        }
                        parseForest.getTrees().add(tree);
                    }
                }
            }
        return parseForest;
//		return new Pair<Forest, Forest>( parseForest, dependencyForest);
	}

	/**
	 * Takes a Stanford Tree and Curator Tree and recursively populates the Curator
	 * Tree to match the Stanford Tree.
	 * Returns the top Node of the tree.
	 * @param parse Stanford Tree
	 * @param tree Curator Tree
	 * @param offset Offset of where we are in the rawText
	 * @return top Node of the Tree
	 * @throws TException
	 */
	private static Node generateNode(edu.stanford.nlp.trees.Tree parse, 
			Tree tree, int offset) throws TException {
		if (!tree.isSetNodes()) {
			tree.setNodes(new ArrayList<Node>());
		}
		List<Node> nodes = tree.getNodes();
		Node node = new Node();

		node.setLabel(parse.value());
		for (edu.stanford.nlp.trees.Tree pt : parse.getChildrenAsList()) {
			if (!node.isSetChildren()) {
				node.setChildren(new TreeMap<Integer, String>());
			}
			if (pt.isLeaf()) {
				continue;
			} else {
				Node child = generateNode(pt, tree, offset);
				nodes.add(child);
				//no arc label for children in parse trees
				node.getChildren().put(nodes.size() - 1, "");
			}
		}
		Span span = new Span();
		List<Word> words = parse.yieldWords();
		span.setStart(words.get(0).beginPosition() + offset);
		span.setEnding(words.get(words.size() - 1).endPosition() + offset);
		Span rawSpan = getRawSpan(span);
		node.setSpan(rawSpan);
		return node;
	}

	/**
	 * Helper method that creates a Map between the tokenized and the raw spans.
	 * To be used during the {@code generateNode} method in order to align the tokenized words.
	 * @param tokenLabeling The spans from the tokenizer that correspond to character indices in the raw text
	 * @return A map between the raw and tokenized spans.
	 */
	private static void convertRawToTokenized(Labeling tokenLabeling, Labeling sentenceLabeling) {
		tokenizedToRaw = new HashMap<Span, Span>();
		tokenizedSentenceStarts = new ArrayList<Integer>();
		tokenizedSentenceStarts.add(0);

		// First we need to calculate the sentence breakpoints to reset the offset
		List<Integer> sentenceBoundaries = new ArrayList<Integer>();
		for (Span sentSpan : sentenceLabeling.getLabels()) 
			sentenceBoundaries.add(sentSpan.getStart());
		
		List<Span> spans = tokenLabeling.getLabels();
		// The first span is always ok
		tokenizedToRaw.put(spans.get(0), spans.get(0));
		int offset = 0;
        for (int i = 1; i < spans.size(); ++i) {
        	Span rawSpan = spans.get(i);
        	Span prevRawSpan = spans.get(i-1);
        	if (rawSpan.getStart() == prevRawSpan.getEnding())
        		offset++;
        	Span tokenizedSpan = new Span(rawSpan.getStart() + offset, rawSpan.getEnding() + offset);
        	tokenizedToRaw.put(tokenizedSpan, rawSpan);
        	if (sentenceBoundaries.contains(rawSpan.getStart()))
        		tokenizedSentenceStarts.add(tokenizedSpan.getStart());
        }
	}
	
	private static Span getRawSpan(Span tokenizedSpan) {
		if (tokenizedToRaw.containsKey(tokenizedSpan))
			return tokenizedToRaw.get(tokenizedSpan);
		// This means that we are dealing with a multi-word span
		Span rawStartSpan = null, rawEndSpan = null;
		for (Entry<Span, Span> entry : tokenizedToRaw.entrySet()) {
			Span rawSpan = entry.getKey();
			if (rawSpan.getStart() == tokenizedSpan.getStart())
				rawStartSpan = entry.getValue();
			if (rawSpan.getEnding() == tokenizedSpan.getEnding())
				rawEndSpan = entry.getValue();
		}
		assert rawStartSpan != null;
		assert rawEndSpan != null;
		return new Span(rawStartSpan.getStart(), rawEndSpan.getEnding());
	}
}