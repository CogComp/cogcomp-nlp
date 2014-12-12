package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.core.datastructures.Option;
import edu.illinois.cs.cogcomp.edison.data.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassLabel;

import java.util.List;
import java.util.Set;

public class LearnedPredicateDetector extends AbstractPredicateDetector {

	private AbstractPredicateDetector heuristic;
	private final String heuristicPredicateView;
	private WeightVector w;

	public LearnedPredicateDetector(SRLManager manager) throws Exception {
		super(manager);
		heuristic = manager.getHeuristicPredicateDetector();
		heuristicPredicateView = "HeuristicPredicateView:" + manager.getSRLType().name();
		w = manager.getModelInfo(Models.Predicate).getWeights();
	}

	@Override
	public Option<String> getLemma(TextAnnotation ta, int tokenId)
			throws Exception {
		if (!ta.hasView(heuristicPredicateView)) {
			addHeuristicPredicateView(ta);
		}

		View view = ta.getView(heuristicPredicateView);

		List<Constituent> constituentsCoveringToken = view
				.getConstituentsCoveringToken(tokenId);
		if (constituentsCoveringToken.size() == 0)
			return Option.empty();

		Constituent c = constituentsCoveringToken.get(0);

		String lemma = c.getAttribute(CoNLLColumnFormatReader.LemmaIdentifier);

		SRLManager manager = getManager();

		boolean isPredicate;
		if (manager.getSRLType() == SRLType.Verb && lemma.equals("be")) {
			isPredicate = true;
		}
		else {
			SRLMulticlassInstance x = new SRLMulticlassInstance(c, c, manager);

			Set<Feature> features = manager.getModelInfo(Models.Predicate).fex.getFeatures(c);
			x.cacheFeatureVector(Models.Predicate, features);

			SRLMulticlassLabel y0 = new SRLMulticlassLabel(x, 0, Models.Predicate, manager);
			SRLMulticlassLabel y1 = new SRLMulticlassLabel(x, 1, Models.Predicate, manager);

			double score = w.dotProduct(y1.getFeatureVector()) - w.dotProduct(y0.getFeatureVector());

			if (debug) {
				System.out.println("Score = " + score);
			}
			isPredicate = score >= 0;
		}

		if (isPredicate) {
			return new Option<String>(lemma);
		} else
			return Option.empty();

	}

	private void addHeuristicPredicateView(TextAnnotation ta) throws Exception {
		View view = new View(heuristicPredicateView, "", ta, 1.0);
		List<Constituent> predicates = heuristic.getPredicates(ta);
		for (Constituent c : predicates)
			view.addConstituent(c);
		ta.addView(heuristicPredicateView, view);
	}
}
