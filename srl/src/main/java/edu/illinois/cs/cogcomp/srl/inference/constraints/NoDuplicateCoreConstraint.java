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
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateStructure;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceStructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This says that some arguments can't be repeated for a predicate. Originally,
 * it was just used for core arguments, but it turns out that it applies to
 * others too.
 * 
 * @author Vivek Srikumar
 * 
 */
public class NoDuplicateCoreConstraint extends SRLILPConstraintGenerator {

	public final static String name = "noDuplicateCore";
	private final Set<String> argSet;

	public NoDuplicateCoreConstraint(SRLManager manager) {
		super(manager, name, true);

		argSet = new HashSet<>(manager.getCoreArguments());

		// XXX: This might be a bit dubious.
		boolean nom = manager.getSRLType() == SRLType.Nom;
		if (nom)
			argSet.add("SUP");

		// XXX: These come from statistics on the training set. The following
		// hold more than 99% of the time in the training set.

		if (nom) {
			// 100 %
			argSet.add("AM-ADV");
			argSet.add("AM-DIR");
			argSet.add("AM-DIS");
			argSet.add("AM-EXT");

			// following are not always true, but very often
			argSet.add("AM-LOC");
			argSet.add("AM-MNR");

		} else {
			argSet.add("AM-REC");
		}

		argSet.add("AM-NEG");
		argSet.add("AM-PNC");
		argSet.add("AM-PRD");

	}

	@Override
	public List<ILPConstraint> getILPConstraints(IInstance x,
			InferenceVariableLexManager variables) {
		return getViolatedILPConstraints(x, null, variables);
	}

	private List<ILPConstraint> addPredicateConstraints(SRLManager manager,
			InferenceVariableLexManager variables, Set<String> coreArgs,
			int predicateId, SRLPredicateInstance x, SRLPredicateStructure y) {

		String type = manager.getPredictedViewName();
		int numCandidates = x.getCandidateInstances().size();

		List<ILPConstraint> list = new ArrayList<>();

		for (String coreArgument : coreArgs) {
			int count = 0;

			int argId = manager.getArgumentId(coreArgument);

			int[] vars = new int[numCandidates];
			double[] coefs = new double[numCandidates];
			for (int candidateId = 0; candidateId < numCandidates; candidateId++) {
				vars[candidateId] = getArgumentVariable(variables, type, predicateId, candidateId, coreArgument);

				coefs[candidateId] = 1;

				if (y != null) {
					if (y.getArgLabel(candidateId) == argId)
						count++;
				}
			}

			// XXX count == 0 doesn't seem right
			if (y != null && count < 2)
				continue;

			Pair<int[], double[]> cleanedVar = cleanupVariables(vars, coefs);
			vars = cleanedVar.getFirst();
			coefs = cleanedVar.getSecond();

			if (vars.length > 0)
				list.add(new ILPConstraint(vars, coefs, 1.0, ILPConstraint.LESS_THAN));

		}

		return list;
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

			list.addAll(addPredicateConstraints(manager, variables, argSet, predicateId, xp, yp));
		}

		return list;
	}
}
