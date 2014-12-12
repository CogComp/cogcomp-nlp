package edu.illinois.cs.cogcomp.srl.experiment;

import edu.illinois.cs.cogcomp.sl.util.FeatureVector;

class PreExtractRecord {
	String lemma;
	int label;
	FeatureVector features;

	public PreExtractRecord(String lemma, int label, FeatureVector features) {
		this.lemma = lemma;
		this.label = label;
		this.features = features;
	}

}