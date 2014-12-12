package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.jlis.*;

import java.util.*;

public class NoOverlapConstraint extends SRLILPConstraintGenerator {

	public static String name = "noOverlappingArguments";

	public NoOverlapConstraint(SRLManager manager) {
		super(manager, name, false);
	}

	@Override
	public List<ILPConstraint> getILPConstraints(IInstance x,
			InferenceVariableLexManager variables) {
		return getViolatedILPConstraints(x, null, variables);
	}

	private List<ILPConstraint> getNoOverlapConstraint(SRLManager manager,
			InferenceVariableLexManager variables, int predicateId,
			SRLPredicateInstance x, SRLPredicateStructure y) {

		String type = manager.getPredictedViewName();

		final Map<Integer, Set<Integer>> wordToCandidateId = new HashMap<Integer, Set<Integer>>();

		List<SRLMulticlassInstance> candidates = x.getCandidateInstances();

		for (int candidateId = 0; candidateId < candidates.size(); candidateId++) {

			IntPair span = candidates.get(candidateId).getSpan();

			for (int wordId = span.getFirst(); wordId < span.getSecond(); wordId++) {
				if (!wordToCandidateId.containsKey(wordId))
					wordToCandidateId.put(wordId, new HashSet<Integer>());
				wordToCandidateId.get(wordId).add(candidateId);
			}

		}

		Set<Set<Integer>> overlaps = new HashSet<Set<Integer>>();
		for (int wordId : wordToCandidateId.keySet()) {
			overlaps.add(wordToCandidateId.get(wordId));
		}

		// int count = 0;

		int nullId = manager.getArgumentId(SRLManager.NULL_LABEL);

		List<ILPConstraint> list = new ArrayList<ILPConstraint>();

		for (Set<Integer> cands : overlaps) {
			if (cands.size() == 1)
				continue;

			int[] vars = new int[cands.size()];
			double[] coefs = new double[cands.size()];

			int i = 0;

			int numNonNull = 0;

			for (int candId : cands) {
				coefs[i] = 1;
				vars[i] = getArgumentVariable(variables, type, predicateId,
						candId, SRLManager.NULL_LABEL);
				i++;

				if (y != null) {
					if (y.getArgLabel(candId) != nullId)
						numNonNull++;
				}
			}

			if (y != null && numNonNull <= 1)
				continue;

			Pair<int[], double[]> cleanedVar = cleanupVariables(vars, coefs);
			vars = cleanedVar.getFirst();
			coefs = cleanedVar.getSecond();

			if (vars.length > 0) {
				// addGreaterThanConstraint(xmp, vars, coefs, vars.length - 1);
				list.add(new ILPConstraint(vars, coefs, vars.length - 1,
						ILPConstraint.GREATER_THAN));
				// count++;
			}

		}

		return list;

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

			list.addAll(getNoOverlapConstraint(manager, variables, predicateId,
					xp, yp));
		}

		return list;
	}

}
