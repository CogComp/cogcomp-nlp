package edu.illinois.cs.cogcomp.srl.jlis;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

public class SRLMulticlassLabel implements IStructure {

	private SRLMulticlassInstance x;
	private int label;
	private Models type;
	private SRLManager manager;

	public SRLMulticlassLabel(SRLMulticlassInstance x, int label, Models type, SRLManager manager) {
		this.x = x;
		this.label = label;
		this.type = type;
		this.manager = manager;
	}

//	@Override
//	public FeatureVector getFeatureVector() {
//		FeatureVector feats = x.getCachedFeatureVector(type);
//		return feats.copyWithShift(label * manager.getModelInfo(type).getLexicon().size());
//	}

	public int getLabel() {
		return label;
	}

}
