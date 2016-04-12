package edu.illinois.cs.cogcomp.srl.learn;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.utilities.Parallel;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.srl.SRLProperties;
import edu.illinois.cs.cogcomp.srl.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SRLPredicateInstance implements IInstance {
	private static int FEATURE_EXTRACTION_N_THREADS;

	private final static Logger log = LoggerFactory.getLogger(SRLPredicateInstance.class);

	private final List<SRLMulticlassInstance> candidates;
	private final SRLMulticlassInstance senseInstance;
	private final SRLManager manager;

	public SRLPredicateInstance(List<SRLMulticlassInstance> candidates,
			SRLMulticlassInstance sense, SRLManager manager) {
		this.candidates = candidates;
		this.senseInstance = sense;
		this.manager = manager;
	}

	public SRLPredicateInstance(Constituent predicate, SRLManager manager) {
		this.manager = manager;

        //XXX Generate a clone of the predicate to avoid changing the gold TA
        Constituent predicateClone = predicate.cloneForNewView(predicate.getViewName());

		senseInstance = new SRLMulticlassInstance(predicateClone, predicateClone, manager);

		List<SRLMulticlassInstance> list = new ArrayList<>();

		ArgumentCandidateGenerator candidateGenerator = manager.getArgumentCandidateGenerator();

		List<Constituent> cands = candidateGenerator.generateCandidates(predicate);

		for (Constituent c : cands) {
			list.add(new SRLMulticlassInstance(c, predicateClone, manager));
		}
		this.candidates = Collections.unmodifiableList(list);
	}

	public SRLPredicateInstance(Constituent predicate, SRLManager manager, ArgumentIdentifier identifier) {
		this.manager = manager;

        //XXX Generate a clone of the predicate to avoid changing the gold TA
        Constituent predicateClone = predicate.cloneForNewView(predicate.getViewName());

		senseInstance = new SRLMulticlassInstance(predicateClone, predicateClone, manager);

		ArgumentCandidateGenerator candidateGenerator = manager.getArgumentCandidateGenerator();

		List<SRLMulticlassInstance> allCandidates = new ArrayList<>();

		for (Constituent c : candidateGenerator.generateCandidates(predicate)) {
			allCandidates.add(new SRLMulticlassInstance(c, predicateClone, manager));
		}

		cacheIdentifierFeatures(allCandidates);

		List<SRLMulticlassInstance> list = new ArrayList<>();

		for (SRLMulticlassInstance c : allCandidates) {
			if (identifier.getIdentifierScaledDecision(c))
				list.add(c);
		}

		this.candidates = Collections.unmodifiableList(list);
	}

	public List<SRLMulticlassInstance> getCandidateInstances() {
		return candidates;
	}

	public SRLMulticlassInstance getSenseInstance() {
		return senseInstance;
	}

	public double size() {
		return this.candidates.size() + 1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (SRLMulticlassInstance c : candidates) {
			sb.append("Candidate: ").append(c.toString()).append("\n");
		}

		sb.append("Sense: ").append(senseInstance.toString()).append("\n");

		return sb.toString();
	}

	private synchronized void cacheIdentifierFeatures(List<SRLMulticlassInstance> xs) {
		for (SRLMulticlassInstance x : xs) {
			ModelInfo modelInfo = manager.getModelInfo(Models.Identifier);

			try {
				Set<Feature> feats = modelInfo.fex.getFeatures(x.getConstituent());

				x.cacheFeatureVector(Models.Identifier, feats);
			} catch (Exception e) {
				log.error("Unable to extract features for {}", x, e);

				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * This method caches features for all argument candidates for given
	 * predicate. This is used only during testing to speed up prediction.
	 *
	 * @param cacheIdentifier
	 *            Should the identifier also be cached?
	 */
	public void cacheAllFeatureVectors(boolean cacheIdentifier) {
		List<Pair<SRLMulticlassInstance, Models>> list = new ArrayList<>();
		list.add(new Pair<>(senseInstance, Models.Sense));
		for (SRLMulticlassInstance x : this.getCandidateInstances()) {
			list.add(new Pair<>(x, Models.Classifier));

			if (cacheIdentifier)
				list.add(new Pair<>(x, Models.Identifier));
		}

		Parallel.Method<Pair<SRLMulticlassInstance, Models>> function =
				new Parallel.Method<Pair<SRLMulticlassInstance, Models>>() {

			@Override
			public void run(Pair<SRLMulticlassInstance, Models> argument) {
				SRLMulticlassInstance x = argument.getFirst();
				Models m = argument.getSecond();

				ModelInfo modelInfo = manager.getModelInfo(m);

				try {
					Set<Feature> feats = modelInfo.fex.getFeatures(x.getConstituent());
					x.cacheFeatureVector(m, feats);
				} catch (Exception e) {
					log.error("Unable to extract features for {}", x, e);

					throw new RuntimeException(e);
				}
			}
		};
		SRLProperties props = SRLProperties.getInstance();
		FEATURE_EXTRACTION_N_THREADS=Math.min(props.getNumFeatExtThreads(), Runtime.getRuntime().availableProcessors());

		log.debug("Using {} threads for feature ext.", FEATURE_EXTRACTION_N_THREADS);
		try {
			int timeout = 10;
			Parallel.forLoop(FEATURE_EXTRACTION_N_THREADS, list, function, timeout, TimeUnit.MINUTES);
		} catch (Exception e) {
			log.error("Waited for ten minutes for feature extraction. Giving up!", e);
		}

	}
}
