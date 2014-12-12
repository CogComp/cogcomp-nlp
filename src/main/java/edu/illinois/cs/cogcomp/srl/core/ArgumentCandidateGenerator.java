package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

import java.util.List;

/**
 * The base class for argument candidate generators.
 * 
 * @author Vivek Srikumar
 * 
 */
public abstract class ArgumentCandidateGenerator {

	protected final SRLManager manager;

	public ArgumentCandidateGenerator(SRLManager manager) {
		this.manager = manager;
	}

	public abstract String getCandidateViewName();

	public abstract List<Constituent> generateCandidates(Constituent predicate);

	protected Constituent getNewConstituent(TextAnnotation ta,
			Constituent predicateClone, int start, int end) {

		Constituent newConstituent = new Constituent("", 1.0,
				getCandidateViewName(), ta, start, end);

		new Relation("ChildOf", predicateClone, newConstituent, 1.0);
		return newConstituent;
	}

}
