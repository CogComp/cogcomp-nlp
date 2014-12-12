package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraintGenerator;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

import java.util.ArrayList;
import java.util.List;

abstract public class SRLILPConstraintGenerator extends ILPConstraintGenerator {

	protected SRLManager manager;

	public SRLILPConstraintGenerator(SRLManager manager, String name,
			boolean delayedConstraint) {
		super(name, delayedConstraint);
		this.manager = manager;
	}

	protected Pair<int[], double[]> cleanupVariables(int[] vars, double[] coefs) {
		List<Double> c = new ArrayList<Double>();
		List<Integer> v = new ArrayList<Integer>();

		for (int i = 0; i < vars.length; i++) {
			if (vars[i] >= 0) {
				v.add(vars[i]);
				c.add(coefs[i]);

			}
		}

		int[] v1 = new int[v.size()];
		double[] d = new double[c.size()];

		for (int i = 0; i < c.size(); i++) {
			v1[i] = v.get(i);
			d[i] = c.get(i);
		}

		return new Pair<int[], double[]>(v1, d);

	}

	protected int getArgumentVariable(InferenceVariableLexManager variables,
			String type, int predicateId, int candidateId, String arg) {

		String identifier = SRLILPInference.getArgumentVariableIdentifier(type,
				predicateId, candidateId, arg);

		return variables.getVariable(identifier);
	}
}
