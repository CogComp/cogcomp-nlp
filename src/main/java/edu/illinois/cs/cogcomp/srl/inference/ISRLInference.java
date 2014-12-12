package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;

public interface ISRLInference {

	public PredicateArgumentView getOutputView() throws Exception;
}
