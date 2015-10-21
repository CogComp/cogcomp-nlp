package edu.illinois.cs.cogcomp.srl.jlis;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

public class SRLPredicateStructure implements IStructure {

	public final SRLPredicateInstance x;
	private final int[] argLabels;
	private final int sense;
	private final SRLManager manager;

	public SRLPredicateStructure(SRLPredicateInstance x, int[] argLabels, int sense, SRLManager manager) {
		this.x = x;
		this.argLabels = argLabels;
		this.sense = sense;
		this.manager = manager;

	}

//	@Override
//	public FeatureVector getFeatureVector() {
//		throw new RuntimeException("Not yet implemented!");
//	}

	public int getArgLabel(int candidateId) {
		return argLabels[candidateId];
	}

	public int getSense() {
		return sense;
	}

	public SRLMulticlassLabel getClassifierMulticlassLabel(int candidateId) {
		return new SRLMulticlassLabel(argLabels[candidateId], Models.Classifier, manager);
	}

	public SRLMulticlassLabel getIdentifierMulticlassLabel(int candidateId) {
		boolean isNull = manager.isNullLabel(argLabels[candidateId]);

		int c = isNull ? 0 : 1;

		return new SRLMulticlassLabel(c, Models.Identifier, manager);
	}

	public SRLMulticlassLabel getSenseMulticlassLabel() {
		return new SRLMulticlassLabel(sense, Models.Sense, manager);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (int candidateId = 0; candidateId < this.argLabels.length; candidateId++) {
			sb.append("Candidate ").append(candidateId).append(": ").
					append(manager.getArgument(argLabels[candidateId])).append("\n");
		}

		sb.append("Sense: ").append(manager.getSense(sense)).append("\n");

		return sb.toString();
	}

}
