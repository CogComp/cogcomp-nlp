package edu.illinois.cs.cogcomp.annotation.handler;

import java.util.*;

import edu.illinois.cs.cogcomp.nlp.curator.Pair;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.ArrayCoreMap;
import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.edison.data.curator.CuratorViewNames;
import edu.illinois.cs.cogcomp.thrift.base.Forest;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Node;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.base.Tree;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewStanfordToForest {

    private static Logger logger = LoggerFactory.getLogger(NewStanfordToForest.class);

//FIXME: a hack during development
    private static boolean addDependencyTrees = true;
    /**
	 * A map between the spans of the tokenizer (that refer to the raw string)
	 * and the actual tokenized spans. 
	 */
//	private static Map<Span, Span> tokenizedToRaw;
//	private static List<Integer> tokenizedSentenceStarts;


    /**
     * given the input record, read off the text and generate a stanford constituency and dependency parse
     *
     * @param
     * @param record
     * @return a pair of Forests, the first being the constituency parse and the second the dependency parse
     * @throws TException
     */
	public static Pair< Forest, Forest > convert(POSTaggerAnnotator posAnnotator, ParserAnnotator parseAnnotator, Record record) throws TException {
		Forest parseForest = new Forest();
        Forest depForest = new Forest();
		parseForest.setSource("stanfordparser");
        depForest.setSource( "stanfordparser" );

		Labeling sentenceLabeling = record.getLabelViews().get(CuratorViewNames.sentences);
		Labeling tokenLabeling = record.getLabelViews().get(CuratorViewNames.tokens);
//		convertRawToTokenized(tokenLabeling, sentenceLabeling);
//		assert tokenizedSentenceStarts.size() == sentenceLabeling.getLabels().size();
//
		// The (tokenized) sentence offset in case we have more than one sentences in the record
        List< CoreMap > sentences = buildStanfordSentences( record );
        Annotation document = new Annotation( sentences );
        posAnnotator.annotate(document);
        parseAnnotator.annotate( document );
        sentences = document.get( SentencesAnnotation.class );

        int offset = 0; // word offset -- last token of last sentence, adjusts dependency position indexes

        for(CoreMap sentence: sentences) {
            //edu.stanford.nlp.trees.Tree
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

            if ( addDependencyTrees ) {
                // dependency stuff
                List<Tree> depTree = parseToDependencyTree(stanfordTree, offset, sentence);
                if (depTree == null) {
                    logger.error("Error creating dependency tree for: {}", sentence);
                } else {
                    if (!depForest.isSetTrees()) {
                        depForest.setTrees(new ArrayList<Tree>());
                    }
                    depForest.getTrees().addAll(depTree);
                }
            }
            offset += sentence.get(CoreAnnotations.TokensAnnotation.class ).size();

        }

		return new Pair<Forest, Forest>( parseForest, depForest);
	}

    private static List<CoreMap> buildStanfordSentences(Record record) {
        Labeling tokens = record.getLabelViews().get(CuratorViewNames.tokens);
        Labeling sentences = record.getLabelViews().get(CuratorViewNames.sentences);
        String rawText = record.getRawText();

        List<CoreMap> stanfordSentences = new LinkedList<CoreMap>();
        List<CoreLabel> stanfordTokens = new LinkedList<CoreLabel>();
        int tokIndex = 0;
        int sentIndex = 0;
        Span currentSentence = sentences.getLabels().get( 0 );
        String sentText = rawText.substring( currentSentence.getStart(), currentSentence.getEnding() );

        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();

        for (Span tok : tokens.getLabels())
        {
            if ( tok.getStart() > currentSentence.getEnding() )
            {
                CoreMap stanfordSentence = buildStanfordSentence( currentSentence, sentText, sentIndex++, stanfordTokens );
                stanfordSentences.add( stanfordSentence );
                stanfordTokens = new LinkedList< CoreLabel>();
                currentSentence = sentences.getLabels().get( sentIndex );
                sentText = rawText.substring( currentSentence.getStart(), currentSentence.getEnding() );
            }
            int tokStart = tok.getStart();
            int tokLength = tok.getEnding() - tokStart;

            String form = rawText.substring(tokStart, tok.getEnding());

            CoreLabel stanfordTok = tf.makeToken(form, tokStart, tokLength);//new CoreLabel();
//            stanfordTok.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, tok.getStart() );
//            stanfordTok.set( CoreAnnotations.CharacterOffsetEndAnnotation.class, tok.getEnding() );
//            stanfordTok.set( CoreAnnotations.TextAnnotation.class, form );
//            stanfordTok.set( CoreAnnotations.ValueAnnotation.class, form );
            stanfordTok.setIndex( tokIndex++ );
            stanfordTokens.add( stanfordTok );

        }
// should be one last sentence

        CoreMap stanfordSentence = buildStanfordSentence( currentSentence, sentText, sentIndex++, stanfordTokens );
        stanfordSentences.add(stanfordSentence);
//        stanfordTokens.clear();


        return stanfordSentences;
    }

    private static CoreMap buildStanfordSentence(Span sentence, String rawText, int sentIndex, List<CoreLabel> stanfordTokens) {
        CoreMap stanfordSentence = new ArrayCoreMap();
        CoreLabel firstTok = stanfordTokens.get( 0 );
        CoreLabel lastTok = stanfordTokens.get( stanfordTokens.size() -1 );

        stanfordSentence.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, sentence.getStart() );
        stanfordSentence.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, sentence.getEnding());
        stanfordSentence.set(CoreAnnotations.TokenBeginAnnotation.class, firstTok.index() );
        stanfordSentence.set(CoreAnnotations.TokenEndAnnotation.class, lastTok.index()+1 ); // at-the-end indexing?
        stanfordSentence.set( CoreAnnotations.TextAnnotation.class, rawText );
        stanfordSentence.set( CoreAnnotations.SentenceIndexAnnotation.class, sentIndex );
        stanfordSentence.set( CoreAnnotations.TokensAnnotation.class, stanfordTokens );
        return stanfordSentence;
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
		List<Node> nodes = tree.getNodes(); // tree is the curator Tree we are building
		Node node = new Node();

		node.setLabel(parse.value());
		for (edu.stanford.nlp.trees.Tree pt : parse.getChildrenAsList()) {
			if (!node.isSetChildren()) {
				node.setChildren(new TreeMap<Integer, String>());
			}
			if (pt.isLeaf()) {
				continue;
			} else { //generate child of parse, the current node in tree
				Node child = generateNode(pt, tree, offset);
				nodes.add(child);
				//no arc label for children in parse trees; one empty arc for each child
				node.getChildren().put(nodes.size() - 1, "");
			}
		}
		Span span = new Span();
		List<Word> words = parse.yieldWords();
		span.setStart(words.get(0).beginPosition() ); // stanford words account for off
		span.setEnding(words.get(words.size() - 1).endPosition() );
		node.setSpan(span);
		return node;
	}


    /**
     * Convert a Stanford parse tree to a dependency tree and then to a Curator Tree
     * @param parse
     * @param offset    the index of the last token of the previous sentence. Needed to adjust dependency indexes to node list positions.
     * @param input
     * @return  a tree corresponding to a single sentence. Tree nodes will have correct absolute char offsets wrt
     *            the original text; but dependency node offsets are relative to the *sentence* (position in the list
     *            of nodes just for this sentence, start index of zero).
     * @throws TException
     */
    private static synchronized List<Tree> parseToDependencyTree(
            edu.stanford.nlp.trees.Tree parse, int offset, CoreMap input)
            throws TException {
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

        List<Node> nodes = new ArrayList<Node>();
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
            if ( label.equals("root") )
                continue;

            Integer hcopy = td.gov().label().get(CoreAnnotations.CopyAnnotation.class);
            Integer dcopy = td.dep().label().get(CoreAnnotations.CopyAnnotation.class);




//			boolean hcopy = td.gov().label().get(CopyAnnotation.class) != null
//					&& td.gov().label().get(CopyAnnotation.class);
//			boolean dcopy = td.dep().label().get(CopyAnnotation.class) != null
//					&& td.dep().label().get(CopyAnnotation.class);

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
                CoreLabel stanfordTok = stanfordTokens.get( hpos );
                // assumes head span is the extent of the word representing the head
                Span headSpan = new Span( stanfordTok.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
                                            stanfordTok.get(CoreAnnotations.CharacterOffsetEndAnnotation.class ) );
                if (hcopy != null) {
                    headSpan.setAttributes(new HashMap<String, String>());
                    headSpan.getAttributes().put("copy", String.valueOf(hcopy));
                }
                headNode.setSpan(headSpan);
                nodes.add(headNode);
                // nodeMap: head index, head node and position in nodeList
                nodeMap.put(hpos, new Pair<Node, Integer>(headNode, nodes
                        .size() - 1));
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
                Span dependentSpan = new Span( stanfordTok.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class ),
                                                stanfordTok.get( CoreAnnotations.CharacterOffsetEndAnnotation.class ) ); //wordToSpan(sentence.get(dpos), offset);
                if (dcopy != null) {
                    dependentSpan.setAttributes(new HashMap<String, String>());
                    dependentSpan.getAttributes().put("copy", String.valueOf(dcopy));
                }
                depNode.setSpan(dependentSpan);
                nodes.add(depNode);
                nodeMap.put(dpos, new Pair<Node, Integer>(depNode,
                        nodes.size() - 1));
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
        List<Tree> trees = new ArrayList<Tree>();
        for (Integer head : headNodes) {
            try {
                Tree tree = extractTree(nodes, head);
                trees.add(tree);
            } catch (StackOverflowError e) {
                logger.error("getting stack overflow errors!!!!");
                logger.error("Input: {}", input);
                logger.error("Parse: {}", parse.toString());
                logger.error("Dependencies: {}", tdl);
                return null;
            }
        }
        return trees;
    }

    private static Tree extractTree(List<Node> allNodes, int headindex)
            throws TException {
        List<Node> nodes = new ArrayList<Node>();
        Node head = extractNode(allNodes, nodes, headindex);
        nodes.add(head);
        Tree tree = new Tree();
        tree.setNodes(nodes);
        tree.setTop(nodes.size() - 1);
        return tree;
    }

    private static Node extractNode(List<Node> allNodes, List<Node> nodes, int index) {
        Node current = allNodes.get(index);
        if (!current.isSetChildren()) {
            return current;
        }
        Map<Integer, String> children = new HashMap<Integer, String>();
        for (int childindex : current.getChildren().keySet()) {
            nodes.add(extractNode(allNodes, nodes, childindex));
            children.put(nodes.size() - 1, current.getChildren()
                    .get(childindex));
        }
        current.setChildren(children);
        return current;
    }

    /**
     * Converts a Stanford Word to a Curator Span
     * @param word
     * @param offset
     * @return
     * @throws TException
     */
    private static Span wordToSpan(Word word, int offset) throws TException {
        Span span = new Span();
        span.setStart(word.beginPosition() + offset);
        span.setEnding(word.endPosition() + offset);
        return span;
    }

}