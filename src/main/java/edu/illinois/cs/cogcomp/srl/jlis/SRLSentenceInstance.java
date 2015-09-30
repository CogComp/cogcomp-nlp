package edu.illinois.cs.cogcomp.srl.jlis;

import edu.illinois.cs.cogcomp.sl.core.IInstance;

import java.util.List;

public class SRLSentenceInstance implements IInstance {

	public final List<SRLPredicateInstance> predicates;
	private int size;

	public SRLSentenceInstance(List<SRLPredicateInstance> instances) {
		this.predicates = instances;
		size = 0;
		for (SRLPredicateInstance x : instances) {
			size += x.size();
		}
	}

//	@Override
	public double size() {
		return size;
	}

	
	public int numPredicates() {
		return predicates.size();
	}
}
