/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.learn;

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

	public double size() {
		return size;
	}

	public int numPredicates() {
		return predicates.size();
	}
}
