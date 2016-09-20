/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateStructure;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RArgConstraints extends SRLILPConstraintGenerator {

	private final static Logger log = LoggerFactory
			.getLogger(RArgConstraints.class);

	public final static String name = "RArgumentConstraint";

	public RArgConstraints(SRLManager manager) {
		super(manager, name, true);
	}

	@Override
	public List<ILPConstraint> getILPConstraints(IInstance x,
			InferenceVariableLexManager variables) {

		log.debug("Adding R-Arg constraints");
		return getViolatedILPConstraints(x, null, variables);
	}

	@Override
	public List<ILPConstraint> getViolatedILPConstraints(IInstance ins,
			IStructure s, InferenceVariableLexManager variables) {

		SRLSentenceInstance x = (SRLSentenceInstance) ins;
		SRLSentenceStructure y = (SRLSentenceStructure) s;

		List<ILPConstraint> list = new ArrayList<>();

		for (int predicateId = 0; predicateId < x.numPredicates(); predicateId++) {

			SRLPredicateInstance xp = x.predicates.get(predicateId);
			SRLPredicateStructure yp = y == null ? null : y.ys.get(predicateId);

			log.debug("Predicate: " + x);
			list.addAll(addPredicateRArgConstraints(manager, variables,
					predicateId, xp, yp));
		}

		return list;
	}

	private List<ILPConstraint> addPredicateRArgConstraints(SRLManager manager,
			InferenceVariableLexManager variables, int predicateId,
			SRLPredicateInstance x, SRLPredicateStructure y) {

		Set<String> coreArgs = manager.getCoreArguments();

		List<ILPConstraint> list = new ArrayList<>();
		for (String arg : coreArgs) {
			list.addAll(addRArgConstraint(manager, predicateId, variables, x,
					y, arg));
		}

		return list;
	}

	private List<ILPConstraint> addRArgConstraint(SRLManager manager,
			int predicateId, InferenceVariableLexManager variables,
			SRLPredicateInstance x, SRLPredicateStructure y, String arg) {

		String type = manager.getPredictedViewName();
		int numCandidates = x.getCandidateInstances().size();

		String rArg = "R-" + arg;

		log.debug("R-Arg constraint for {} for predicate {}", rArg, x);

		List<ILPConstraint> list = new ArrayList<>();
		for (int otherCandidateId = 0; otherCandidateId < numCandidates; otherCandidateId++) {

			if (y != null) {
				if (y.getArgLabel(otherCandidateId) != manager
						.getArgumentId(rArg))
					continue;
			}

			int[] vars = new int[numCandidates];
			double[] coefs = new double[numCandidates];

			int rArgVariable = getArgumentVariable(variables, type,
					predicateId, otherCandidateId, rArg);
			if (rArgVariable < 0)
				continue;

			vars[0] = rArgVariable;
			coefs[0] = -1;

			int id = 1;

			boolean foundArg = false;

			for (int candidateId = 0; candidateId < numCandidates; candidateId++) {
				if (candidateId == otherCandidateId)
					continue;

				int variable = getArgumentVariable(variables, type,
						predicateId, candidateId, arg);

				vars[id] = variable;
				coefs[id] = 1;
				id++;

				if (y != null)
					if (y.getArgLabel(candidateId) == manager
							.getArgumentId(arg))
						foundArg = true;
			}

			if (y != null && foundArg)
				continue;

			Pair<int[], double[]> pair = cleanupVariables(vars, coefs);

			vars = pair.getFirst();
			coefs = pair.getSecond();

			ILPConstraint ilpConstraint = new ILPConstraint(vars, coefs, 0,
					ILPConstraint.GREATER_THAN);

			log.debug("Constraint: " + ilpConstraint);
			list.add(ilpConstraint);

		}

		return list;
	}

}
