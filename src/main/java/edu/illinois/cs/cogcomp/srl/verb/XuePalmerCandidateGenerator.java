package edu.illinois.cs.cogcomp.srl.verb;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.features.helpers.ParseHelper;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.edison.utilities.ParseUtils;
import edu.illinois.cs.cogcomp.srl.core.ArgumentCandidateGenerator;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XuePalmerCandidateGenerator extends ArgumentCandidateGenerator {

	public XuePalmerCandidateGenerator(SRLManager manager) {
		super(manager);
	}

	private final static Logger log = LoggerFactory.getLogger(XuePalmerCandidateGenerator.class);

	@Override
	public String getCandidateViewName() {
		return "XuePalmerHeuristicView";
	}

	@Override
	public List<Constituent> generateCandidates(Constituent predicate) {
		Constituent predicateClone = predicate.cloneForNewView(getCandidateViewName());

		TextAnnotation ta = predicateClone.getTextAnnotation();
		int sentenceId = ta.getSentenceId(predicateClone);
		Tree<String> tree = ParseHelper.getParseTree(manager.defaultParser, ta, sentenceId);

		Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);

		int sentenceStart = ta.getSentence(sentenceId).getStartSpan();
		int predicatePosition = predicateClone.getStartSpan() - sentenceStart;

		Set<Constituent> out = new HashSet<Constituent>();

		List<Tree<Pair<String, IntPair>>> yield = spanLabeledTree.getYield();

		if (predicatePosition >= yield.size()) {
			System.out.println(ta);

			System.out.println("Predicate: " + predicatePosition + "\t" + predicateClone);
			System.out.println(tree);
			System.out.println(spanLabeledTree);

			System.out.println("Tree view");
			System.out.println(ta.getView(manager.defaultParser));

			throw new RuntimeException();
		}

		Tree<Pair<String, IntPair>> predicateTree = yield.get(predicatePosition);

		Tree<Pair<String, IntPair>> currentNode = predicateTree.getParent();

		boolean done = false;
		while (!done) {
			if (currentNode.isRoot())
				done = true;
			else {
				List<Constituent> candidates = new ArrayList<Constituent>();

				for (Tree<Pair<String, IntPair>> sibling : currentNode.getParent().getChildren()) {
					Pair<String, IntPair> siblingNode = sibling.getLabel();

					// do not take the predicate as the argument
					IntPair siblingSpan = siblingNode.getSecond();
					if (siblingSpan.equals(predicateClone.getSpan()))
						continue;

					// do not take any constituent including the predicate as an argument
					if ((predicatePosition >= siblingSpan.getFirst())
							&& (predicateClone.getEndSpan() <= siblingSpan.getSecond()))
						continue;

					String siblingLabel = siblingNode.getFirst();

					int start = siblingSpan.getFirst() + sentenceStart;
					int end = siblingSpan.getSecond() + sentenceStart;

					candidates.add(getNewConstituent(ta, predicateClone, start, end));

					if (siblingLabel.startsWith("PP")) {
						for (Tree<Pair<String, IntPair>> child : sibling.getChildren()) {
							int candidateStart = child.getLabel().getSecond().getFirst() + sentenceStart;
							int candidateEnd = child.getLabel().getSecond().getSecond() + sentenceStart;

							candidates.add(getNewConstituent(ta, predicateClone, candidateStart, candidateEnd));
						}
					}
				}
				out.addAll(candidates);

				currentNode = currentNode.getParent();
			}
		}

		// Punctuations maketh an argument not!
		List<Constituent> output = new ArrayList<Constituent>();
		for (Constituent c : out) {
			if (!ParseTreeProperties.isPunctuationToken(c.getSurfaceString()))
				output.add(c);
		}

		if (log.isDebugEnabled()) {
			Exception ex = new Exception();
			String callerClass = ex.getStackTrace()[1].getClassName();
			String callerMethod = ex.getStackTrace()[1].getMethodName();
			int lineNumber = ex.getStackTrace()[1].getLineNumber();
			String caller = callerClass + "." + callerMethod + ":" + lineNumber;

			log.debug("Candidates for {} from heuristic: {}. Call from {}",
					new String[] { predicateClone.toString(), output.toString(), caller });
		}

		return output;
	}
}
