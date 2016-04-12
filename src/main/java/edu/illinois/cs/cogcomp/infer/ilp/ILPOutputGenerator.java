package edu.illinois.cs.cogcomp.infer.ilp;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public interface ILPOutputGenerator {
	IStructure getOutput(ILPSolver xmp, InferenceVariableLexManager variableManager, IInstance ins);
}
