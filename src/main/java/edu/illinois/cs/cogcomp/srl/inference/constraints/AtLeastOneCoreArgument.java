package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.inference.SRLConstraints;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.jlis.SRLPredicateInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLPredicateStructure;
import edu.illinois.cs.cogcomp.srl.jlis.SRLSentenceInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLSentenceStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Every verb should have at leaast one argument that is one of A0, A1 or A2.
 * Holds only for verb.
 * 
 * @author Vivek Srikumar
 * 
 */
public class AtLeastOneCoreArgument extends SRLILPConstraintGenerator {

	public AtLeastOneCoreArgument(SRLManager manager) {
		super(manager, SRLConstraints.atLeastOneCoreArgument.name(), true);
	}

	@Override
	public List<ILPConstraint> getILPConstraints(IInstance x,
			InferenceVariableLexManager variables) {
		return getViolatedILPConstraints(x, null, variables);
	}

	@Override
	public List<ILPConstraint> getViolatedILPConstraints(IInstance ins,
			IStructure s, InferenceVariableLexManager variables) {
		SRLSentenceInstance x = (SRLSentenceInstance) ins;
		SRLSentenceStructure y = (SRLSentenceStructure) s;

		List<ILPConstraint> list = new ArrayList<ILPConstraint>();

		for (int predicateId = 0; predicateId < x.numPredicates(); predicateId++) {
			SRLPredicateInstance xp = x.predicates.get(predicateId);

			SRLPredicateStructure yp = y == null ? null : y.ys.get(predicateId);

			list.addAll(addPredicateConstraints(manager, variables,
					manager.getCoreArguments(), predicateId, xp, yp));
		}

		return list;
	}

	private List<ILPConstraint> addPredicateConstraints(SRLManager manager,
			InferenceVariableLexManager variables, Set<String> coreArgs,
			int predicateId, SRLPredicateInstance x, SRLPredicateStructure y) {

		String type = manager.getPredictedViewName();
		int numCandidates = x.getCandidateInstances().size();

		List<ILPConstraint> list = new ArrayList<ILPConstraint>();

		int[] vars = new int[numCandidates * coreArgs.size()];
		double[] coefs = new double[vars.length];

		boolean found = false;

		int id = 0;

		for (String coreArgument : coreArgs) {

			int argId = manager.getArgumentId(coreArgument);

			for (int candidateId = 0; candidateId < numCandidates; candidateId++) {

				vars[id] = getArgumentVariable(variables, type, predicateId,
						candidateId, coreArgument);

				coefs[id] = 1;

				id++;

				if (y != null) {
					if (y.getArgLabel(candidateId) == argId) {
						found = true;
						break;
					}
				}
			}

			if (found)
				break;
		}

		if (!found) {
			Pair<int[], double[]> cleanedVar = cleanupVariables(vars, coefs);
			vars = cleanedVar.getFirst();
			coefs = cleanedVar.getSecond();

			if (vars.length > 0)
				list.add(new ILPConstraint(vars, coefs, 1.0,
						ILPConstraint.GREATER_THAN));
		}

		return list;
	}

}
