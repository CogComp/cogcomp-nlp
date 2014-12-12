package edu.illinois.cs.cogcomp.srl.verb;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.srl.core.ArgumentCandidateGenerator;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

import java.util.List;

public class ShallowParseCandidateGenerator extends ArgumentCandidateGenerator {

	public ShallowParseCandidateGenerator(SRLManager manager) {
		super(manager);
	}

	@Override
	public String getCandidateViewName() {
		return "ShallowParseCandidateView";
	}

	@Override
	public List<Constituent> generateCandidates(Constituent predicate) {
		// TODO Auto-generated method stub
		return null;
	}

}
