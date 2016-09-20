package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.inference.SRLConstraints;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.learn.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateStructure;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceStructure;

import java.util.ArrayList;
import java.util.List;

/**
 * This comes from section 7.1 of
 * "Combination Strategies for Semantic Role Labeling": Arguments fror two
 * different verbs can not overlap, but they can embed.
 * <p>
 * This is almost true in the verb SRL training set, but not so for nom.
 * 
 * @author svivek
 * 
 */
public class CrossArgumentExclusiveOverlap extends SRLILPConstraintGenerator {

	private int nullId;

	public CrossArgumentExclusiveOverlap(SRLManager manager) {
		super(manager, SRLConstraints.noCrossArgumentExclusiveOverlap.name(),
				false);
		nullId = manager.getArgumentId(SRLManager.NULL_LABEL);

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

		int numPredicates = x.numPredicates();

		List<ILPConstraint> constraints = new ArrayList<>();

		for (int predicateId = 0; predicateId < numPredicates; predicateId++) {

			List<SRLMulticlassInstance> candidateInstances = x.predicates.get(
					predicateId).getCandidateInstances();
			int numCandidates = candidateInstances.size();

			SRLPredicateStructure predicateStructure = null;
			if (y != null) {
				predicateStructure = y.ys.get(predicateId);
			}

			for (int candidateId = 0; candidateId < numCandidates; candidateId++) {

				if (y != null) {
					if (predicateStructure.getArgLabel(candidateId) == nullId)
						continue;
				}

				addCandidateConstraints(predicateId, candidateId,
						candidateInstances.get(candidateId).getConstituent(),
						x, y, constraints, variables);

			}

		}

		return constraints;
	}

	private void addCandidateConstraints(int predicateId, int candidateId,
			Constituent candidate, SRLSentenceInstance x,
			SRLSentenceStructure y, List<ILPConstraint> constraints,
			InferenceVariableLexManager variables) {

		int nullVar = this.getArgumentVariable(variables,
				manager.getPredictedViewName(), predicateId, candidateId,
				SRLManager.NULL_LABEL);

		for (int otherPredicateId = predicateId + 1; otherPredicateId < x
				.numPredicates(); otherPredicateId++) {

			List<SRLMulticlassInstance> candidateInstances = x.predicates.get(
					otherPredicateId).getCandidateInstances();
			int numCandidates = candidateInstances.size();

			SRLPredicateStructure otherPredicateStructure = null;
			if (y != null) {
				otherPredicateStructure = y.ys.get(predicateId);
			}

			for (int otherCandidateId = 0; otherCandidateId < numCandidates; otherCandidateId++) {

				Constituent otherConstituent = candidateInstances.get(
						otherCandidateId).getConstituent();

				int otherLabel = -1;
				if (y != null) {
					otherLabel = otherPredicateStructure
							.getArgLabel(candidateId);

					if (otherLabel == nullId)
						continue;
				}

				if (!Queries.exclusivelyOverlaps(candidate).transform(
						otherConstituent))
					continue;

				int otherNullVar = this.getArgumentVariable(variables,
						manager.getPredictedViewName(), otherPredicateId,
						otherCandidateId, SRLManager.NULL_LABEL);

				// at least one of these two variables should be null.

				constraints.add(new ILPConstraint(new int[] { nullVar,
						otherNullVar }, new double[] { 1, 1 }, 1,
						ILPConstraint.GREATER_THAN));
			}

		}
	}
}
