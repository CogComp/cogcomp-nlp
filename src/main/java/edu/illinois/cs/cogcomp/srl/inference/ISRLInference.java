package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;


public interface ISRLInference {

	PredicateArgumentView getOutputView() throws Exception;
}
