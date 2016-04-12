package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.learn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CArgConstraints extends SRLILPConstraintGenerator {
	private final static Logger log = LoggerFactory.getLogger(CArgConstraints.class);

	public static final String name = "CArgumentConstraint";

	public CArgConstraints(SRLManager manager) {
		super(manager, name, true);
	}

	@Override
	public List<ILPConstraint> getILPConstraints(IInstance x,
			InferenceVariableLexManager variables) {
		log.debug("Getting ILP constraints for {}", name);
		return getViolatedILPConstraints(x, null, variables);
	}

	@Override
	public List<ILPConstraint> getViolatedILPConstraints(IInstance ins,
			IStructure s, InferenceVariableLexManager variables) {

		SRLSentenceInstance x = (SRLSentenceInstance) ins;
		SRLSentenceStructure y = (SRLSentenceStructure) s;

		List<ILPConstraint> list = new ArrayList<>();
		log.debug("Adding C-Arg constriants");

		int numPredicates = x.numPredicates();
		for (int predicateId = 0; predicateId < numPredicates; predicateId++) {
			SRLPredicateInstance xp = x.predicates.get(predicateId);

			log.debug("Predicate: " + x);

			SRLPredicateStructure yp = y == null ? null : y.ys.get(predicateId);
			list.addAll(getPredicateCArgConstraints(manager, variables,
					predicateId, xp, yp));
		}

		return list;
	}

	private List<ILPConstraint> getPredicateCArgConstraints(SRLManager manager,
			InferenceVariableLexManager variables, int predicateId,
			SRLPredicateInstance x, SRLPredicateStructure y) {

		List<Integer> candidateIds = getSortedCandidates(x);

		List<ILPConstraint> list = new ArrayList<>();
		for (String arg : manager.getCoreArguments()) {
			list.addAll(getCArgConstraint(manager, predicateId, variables,
					candidateIds, arg, x, y));
		}

		for (String arg : manager.getModifierArguments()) {

			list.addAll(getCArgConstraint(manager, predicateId, variables,
					candidateIds, arg, x, y));
		}

		if (manager.getSRLType() == SRLType.Nom)
			list.addAll(getCArgConstraint(manager, predicateId, variables,
					candidateIds, "SUP", x, y));

		return list;
	}

	private List<ILPConstraint> getCArgConstraint(SRLManager manager,
			int predicateId, InferenceVariableLexManager variables,
			List<Integer> candidateIds, String arg, SRLPredicateInstance x,
			SRLPredicateStructure y) {
		List<ILPConstraint> list = new ArrayList<>();

		String cArg = "C-" + arg;
		int numCandidates = x.getCandidateInstances().size();
		String type = manager.getPredictedViewName();

		for (int position = 0; position < numCandidates - 1; position++) {

			int[] vars = new int[position + 2];
			double[] coefs = new double[position + 2];

			int cCandidateId = candidateIds.get(position + 1);
			vars[position + 1] = getArgumentVariable(variables, type,
					predicateId, cCandidateId, cArg);

			if (vars[position + 1] < 0)
				continue;

			coefs[position + 1] = -1;

			if (y != null) {
				if (y.getArgLabel(cCandidateId) != manager.getArgumentId(cArg))
					continue;
			}

			boolean foundArg = false;
			for (int i = 0; i < position + 1; i++) {
				int candidateId = candidateIds.get(i);

				vars[i] = getArgumentVariable(variables, type, predicateId,
						candidateId, arg);
				coefs[i] = 1;

				if (y != null) {
					if (y.getArgLabel(candidateId) == manager
							.getArgumentId(arg))
						foundArg = true;
				}
			}

			if (y != null && foundArg)
				continue;

			Pair<int[], double[]> cleanedVar = cleanupVariables(vars, coefs);
			vars = cleanedVar.getFirst();
			coefs = cleanedVar.getSecond();

			if (vars.length > 0) {
				ILPConstraint constraint = new ILPConstraint(vars, coefs, 0,
						ILPConstraint.GREATER_THAN);

				log.debug(arg + ": " + constraint);

				list.add(constraint);

			}
		}
		return list;
	}

	private List<Integer> getSortedCandidates(SRLPredicateInstance x) {
		List<Integer> candidateIds = new ArrayList<>();

		final List<SRLMulticlassInstance> candidates = x
				.getCandidateInstances();
		int numCandidates = candidates.size();

		for (int candidateId = 0; candidateId < numCandidates; candidateId++) {
			candidateIds.add(candidateId);
		}

		Collections.sort(candidateIds, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {

				IntPair span1 = candidates.get(o1).getConstituent().getSpan();
				IntPair span2 = candidates.get(o2).getConstituent().getSpan();

				int start1 = span1.getFirst();
				int start2 = span2.getFirst();
				if (start1 < start2)
					return -1;
				else if (start1 == start2)
					return 0;
				else
					return 1;
			}
		});
		return candidateIds;
	}

}
