package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.data.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.srl.jlis.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SRLExampleGenerator {
	private SRLManager manager;

	public SRLExampleGenerator(SRLManager manager) {
		this.manager = manager;
	}

	public Pair<SRLSentenceInstance, SRLSentenceStructure> getExamples(TextAnnotation ta) throws Exception {
		List<SRLPredicateInstance> predicates = new ArrayList<SRLPredicateInstance>();
		List<SRLPredicateStructure> structures = new ArrayList<SRLPredicateStructure>();

		if (ta.hasView(manager.getGoldViewName()))
			getTreebankExamples(ta, predicates, structures);
		else
			getExamples(ta, predicates);

		SRLSentenceInstance sx = new SRLSentenceInstance(predicates);
		SRLSentenceStructure sy = new SRLSentenceStructure(sx, structures);

		return new Pair<SRLSentenceInstance, SRLSentenceStructure>(sx, sy);
	}

	/**
	 * Generates SRL examples using the predicate detector to identify predicates
	 */
	private void getExamples(TextAnnotation ta, List<SRLPredicateInstance> predicates) throws Exception {
		AbstractPredicateDetector predicateDetector = manager.getLearnedPredicateDetector();

		for (Constituent predicate : predicateDetector.getPredicates(ta)) {
			if (!predicate.hasAttribute(CoNLLColumnFormatReader.LemmaIdentifier)) {
				System.out.println(ta);
				System.out.println(predicate + " has no lemma!");
				assert false;
			}

			SRLPredicateInstance x = new SRLPredicateInstance(predicate, manager);
			predicates.add(x);
		}
	}

	private void getTreebankExamples(TextAnnotation ta,
									 List<SRLPredicateInstance> predicates,
									 List<SRLPredicateStructure> structures) {
		PredicateArgumentView pav = (PredicateArgumentView) ta.getView(manager.getGoldViewName());

		for (Constituent predicate : pav.getPredicates()) {
			if (!predicate.hasAttribute(CoNLLColumnFormatReader.LemmaIdentifier)) {
				System.out.println(ta);
				System.out.println(pav);
				System.out.println(predicate + " has no lemma!");
				assert false;
			}

			SRLPredicateInstance x = new SRLPredicateInstance(predicate, manager);
			Map<IntPair, String> args = getGoldArgumentSpanLabels(predicate);
			int[] argLabels = new int[x.getCandidateInstances().size()];

			int id = 0;
			for (SRLMulticlassInstance c : x.getCandidateInstances()) {
				if (args.containsKey(c.getSpan())) {
					String label = args.get(c.getSpan());

					// A hack to deal with invalid Propbank/Nombank data.
					if (label.startsWith("AM") && !manager.getAllArguments().contains(label)) {
						System.out.println(ta);
						System.out.println(pav);

						if (label.equals("AM-TM"))
							label = "AM-TMP";
						else if (manager.getSRLType() == SRLType.Nom) {
							// the two errors in NomBank are both AM-TMP
							label = "AM-TMP";
						} else {
							System.out.println("Replacing " + label + " with AM-LOC");
							label = "AM-LOC";
						}
					}

					argLabels[id] = manager.getArgumentId(label);
				}
				else
					argLabels[id] = manager.getArgumentId(SRLManager.NULL_LABEL);
				id++;
			}

			int sense = manager.getSenseId(predicate.getAttribute(CoNLLColumnFormatReader.SenseIdentifer));
			SRLPredicateStructure y = new SRLPredicateStructure(x, argLabels, sense, manager);

			predicates.add(x);
			structures.add(y);
		}
	}

	private Map<IntPair, String> getGoldArgumentSpanLabels(Constituent predicate) {
		List<Relation> args = predicate.getOutgoingRelations();

		Map<IntPair, String> argSpans = new HashMap<IntPair, String>();
		for (Relation r : args) {
			Constituent arg = r.getTarget();
			argSpans.put(new IntPair(arg.getStartSpan(), arg.getEndSpan()), r.getRelationName());
		}
		return argSpans;
	}

}
