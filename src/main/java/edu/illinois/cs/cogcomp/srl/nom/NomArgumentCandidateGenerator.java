package edu.illinois.cs.cogcomp.srl.nom;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeTraversal;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;
import edu.illinois.cs.cogcomp.srl.core.ArgumentCandidateGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NomArgumentCandidateGenerator extends ArgumentCandidateGenerator {

	private final static Logger log = LoggerFactory
			.getLogger(NomArgumentCandidateGenerator.class);

	public NomArgumentCandidateGenerator(NomSRLManager manager) {
		super(manager);
	}

	@Override
	public String getCandidateViewName() {
		return "NomArgumentCandidateView";
	}

	@Override
	public List<Constituent> generateCandidates(Constituent predicate) {
		TextAnnotation ta = predicate.getTextAnnotation();

		int predicateSentenceId = ta.getSentenceId(predicate);

		int predicateSentenceStart = ta.getSentence(predicateSentenceId).getStartSpan();

		// get the parse tree
		Tree<String> tree = ParseUtils.getParseTree(manager.defaultParser, ta, predicateSentenceId);
		Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);

		Constituent predicateClone = predicate.cloneForNewView(this.getCandidateViewName());

		int predicatePosition = predicate.getStartSpan() - predicateSentenceStart;

		Set<Constituent> out = new HashSet<>();

		// add all non terminals in the tree
		for (Tree<Pair<String, IntPair>> c : TreeTraversal.depthFirstTraversal(spanLabeledTree)) {
			if (!c.isRoot() && !c.isLeaf() && !c.getChild(0).isLeaf()) {
				int start = c.getLabel().getSecond().getFirst() + predicateSentenceStart;
				int end = c.getLabel().getSecond().getSecond() + predicateSentenceStart;

				out.add(getNewConstituent(ta, predicateClone, start, end));
			}
		}

		Tree<Pair<String, IntPair>> predicateNode = spanLabeledTree.getYield().get(predicatePosition);
		// add all siblings of the predicate
		for (Tree<Pair<String, IntPair>> sibling : predicateNode.getParent().getParent().getChildren()) {
			Pair<String, IntPair> siblingNode = sibling.getLabel();
			IntPair siblingSpan = siblingNode.getSecond();
			int siblingSpanFirst = siblingSpan.getFirst();
			int siblingSpanSecond = siblingSpan.getSecond();

			// unlike in VerbSRL (XuePalmerCandidateGenerator) we do allow the predicate
			// to also be an argument, so there is no need to exclude it here

			int start = siblingSpanFirst + predicateSentenceStart;
			int end = siblingSpanSecond + predicateSentenceStart;

			out.add(getNewConstituent(ta, predicateClone, start, end));
		}

		// verb nodes that dominate the predicate
		Tree<Pair<String, IntPair>> node = predicateNode.getParent();

		while (!node.isRoot() && !ParseTreeProperties.isNonTerminalVerb(node.getLabel().getFirst()))
			node = node.getParent();

		for (Tree<Pair<String, IntPair>> verbCandidate : node.getYield()) {
			if (POSUtils.isPOSVerb(verbCandidate.getParent().getLabel().getFirst())) {
				int start = verbCandidate.getLabel().getSecond().getFirst() + predicateSentenceStart;
				int end = start + 1;

				out.add(getNewConstituent(ta, predicateClone, start, end));
			}
		}

		// pronouns in NPs within the same clause that dominate this predicate
		node = predicateNode.getParent();
		while (!node.isRoot()) {
			String label = node.getLabel().getFirst();
			if (label.startsWith("S"))
				break;

			if (ParseTreeProperties.isNonTerminalNoun(label)) {
				for (Tree<Pair<String, IntPair>> nominalCandidate : node.getYield()) {
					if (POSUtils.isPOSPossessivePronoun(nominalCandidate.getParent().getLabel().getFirst())) {
						int start = nominalCandidate.getLabel().getSecond().getFirst() + predicateSentenceStart;
						int end = start + 1;

						out.add(getNewConstituent(ta, predicateClone, start, end));
					}
				}
			}
			node = node.getParent();

		}

		// if predicate is dominated by a PP, then the head of that PP
		node = predicateNode.getParent();
		boolean ppParentFound = false;
		while (!node.isRoot()) {
			String label = node.getLabel().getFirst();
			if (ParseTreeProperties.isNonTerminalPP(label)) {
				ppParentFound = true;
				break;
			} else if (label.startsWith("S"))
				break;

			node = node.getParent();
		}

		if (ppParentFound) {
			int start = node.getLabel().getSecond().getFirst() + predicateSentenceStart;
			int end = start + 1;
			out.add(getNewConstituent(ta, predicateClone, start, end));
		}

		log.debug("Number of candidates for {} from heuristic: {}", predicate.toString(), out.toString());
		return new ArrayList<>(out);
	}

}
