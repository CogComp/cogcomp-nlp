package edu.illinois.cs.cogcomp.srl.experiment;

import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

class PreExtractRecord {
	String lemma;
	int label;
	IFeatureVector features;

	public PreExtractRecord(String lemma, int label, IFeatureVector features) {
		this.lemma = lemma;
		this.label = label;
		this.features = features;
	}

}