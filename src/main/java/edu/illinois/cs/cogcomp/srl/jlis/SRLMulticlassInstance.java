package edu.illinois.cs.cogcomp.srl.jlis;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;
import edu.illinois.cs.cogcomp.srl.core.ModelInfo;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SRLMulticlassInstance implements IInstance {

	private final Constituent c;

	private final Map<Models, IFeatureVector> features;
	private String predicateLemma;

	private final Constituent predicate;

	private SRLManager manager;

	public SRLMulticlassInstance(Constituent c, Constituent predicate, SRLManager manager) {
		this.c = c;
		this.predicate = predicate;
		this.manager = manager;
		predicateLemma = predicate.getAttribute(CoNLLColumnFormatReader.LemmaIdentifier);

		features = new ConcurrentHashMap<>();
	}

	public SRLMulticlassInstance(Models model, String lemma, String features) {
		c = null;
		this.predicate = null;
		this.predicateLemma = lemma;

		this.features = new ConcurrentHashMap<>();

		this.cacheFeatureVector(model, getFeatureVector(features));
	}

	public double size() {
		return 1;
	}

	public String getPredicateLemma() {
		return predicateLemma;
	}

	@Override
	public String toString() {
		return "SRLMulticlassInstance [cand=" + c + ", predicate=" + predicateLemma + "]";
	}

	public void cacheFeatureVector(Models m, IFeatureVector f) {
		assert !features.containsKey(m);
		features.put(m, f);
	}

	public IFeatureVector getCachedFeatureVector(Models m) {
		assert features.containsKey(m);
		return features.get(m);
	}

	private IFeatureVector getFeatureVector(String features) {
		String[] parts = features.split(" ");
		int[] idx = new int[parts.length];
		float[] vals = new float[parts.length];

		for (int i = 0; i < parts.length; i++) {
			String[] f = parts[i].split(":");

			idx[i] = Integer.parseInt(f[0]);
			vals[i] = Float.parseFloat(f[1]);
		}

		return new SparseFeatureVector(idx, vals);
	}

	public Constituent getConstituent() {
		return c;
	}

	public IntPair getSpan() {
		return c.getSpan();
	}

	public Constituent getPredicate() {
		return predicate;
	}

	public void cacheFeatureVector(Models model, Set<Feature> features) {
		Map<String, Float> featureMap = new HashMap<>();
		for (Feature f : features) {
			featureMap.put(f.getName(), f.getValue());
		}

		ModelInfo modelInfo = manager.getModelInfo(model);
		Pair<int[], float[]> feats = modelInfo.getLexicon().getFeatureVector(featureMap);
		this.cacheFeatureVector(model, new SparseFeatureVector(feats.getFirst(), feats.getSecond()));
	}
}
