package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLPredicateInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SRLLagrangeInference extends NoOverlapInference {

	private final static Logger log = LoggerFactory
			.getLogger(SRLLagrangeInference.class);

	private int maxRounds;

	InferenceVariableLexManager variableManager;

	List<Variable> variables;

	public SRLLagrangeInference(SRLManager manager, TextAnnotation ta,
			List<Constituent> predicates, boolean standardIdClPipeline,
			int maxRounds) {

		super(manager, ta, predicates, standardIdClPipeline);

		this.maxRounds = maxRounds;

		variableManager = new InferenceVariableLexManager();
		variables = new ArrayList<SRLLagrangeInference.Variable>();

	}

	public PredicateArgumentView runInference() {

		if (numPredicates > 0) {
			intitializeVariables();

			// instantiate all the constraints to get and assign dual variables
			// for
			// each
			List<ILPConstraint> ilpConstraints = new ArrayList<ILPConstraint>();
			for (SRLConstraints c : manager.getConstraints()) {

				// no-overlap-inference takes care of the unique label, legal
				// arguments and no-overlap constraints
				if (c == SRLConstraints.noOverlappingArguments)
					continue;

				SRLILPConstraintGenerator generator = c.getGenerator(manager);

				ilpConstraints.addAll(generator.getILPConstraints(
						this.instance, variableManager));
			}

			double[] dual = new double[ilpConstraints.size()];

			boolean solved = false;
			// double updateRate = 0.01;

			double updateRate = averageScoreMargin;

			Pair<int[][], Double> assignment = null;
			for (int i = 0; i < maxRounds; i++) {
				assignment = super.getAssignment();

				boolean foundViolation = testFeasibility(assignment.getFirst(),
						ilpConstraints, dual, updateRate);

				if (!foundViolation) {
					solved = true;
					break;
				}

				if (debug) {

					System.out.println("----------");
					System.out.println("Round " + i + " output");
					System.out.println(getOutput(assignment.getFirst(),
							assignment.getSecond()));
					System.out.println("-----------");

				}

				sendIncomingMessages(ilpConstraints, dual);
			}

			if (!solved) {
				log.warn("Not solved inference after {} iterations, "
						+ "returning current best solution "
						+ "(might violate some constraints)", maxRounds);
			}
			assert assignment != null;
			return super.getOutput(assignment.getFirst(), assignment.getSecond());
		} else {
			return new PredicateArgumentView(viewName, manager.getSRLSystemIdentifier(), ta, 0d);
		}

	}

	private void sendIncomingMessages(List<ILPConstraint> ilpConstraints,
			double[] dual) {
		resetIncomingSenseScores();
		resetIncomingArgumentScore();

		// send incoming messages to the inference
		for (int constId = 0; constId < ilpConstraints.size(); constId++) {

			// get the constraint and the dual value
			ILPConstraint constraint = ilpConstraints.get(constId);
			double lambda = dual[constId];

			if (lambda == 0)
				continue;

			// less than and equality constraints decrease the value of the
			// objective coeficient. greater than constraints increase it.
			double sign = -1;
			if (constraint.sense == ILPConstraint.GREATER_THAN)
				sign = 1;

			// now, for each variable, that belongs to this constraint, add
			// to the update for that variable's score.

			for (int varId = 0; varId < constraint.vars.length; varId++) {
				int var = constraint.vars[varId];
				double coef = constraint.coeffs[varId];

				double update = lambda * sign * coef;

				Variable v = variables.get(var);

				if (v instanceof SenseVariable) {
					int predicateId = ((SenseVariable) v).predicateId;
					int label = ((SenseVariable) v).senseId;
					addToIncomingSenseScore(predicateId, label, update);
				} else {
					int predicateId = ((ArgumentVariable) v).predicateId;
					int label = ((ArgumentVariable) v).labelId;
					int candidate = ((ArgumentVariable) v).candidateId;
					addToIncomingArgumentScore(predicateId, candidate, label,
							update);
				}

			}
		}
	}

	protected boolean testFeasibility(int[][] assignment,
			List<ILPConstraint> constraints, double[] dual, double learningRate) {

		boolean foundViolation = false;

		double[] updates = new double[dual.length];

		for (int constId = 0; constId < constraints.size(); constId++) {
			ILPConstraint constraint = constraints.get(constId);

			double dualUpdate = testConstraintFeasibility(assignment,
					constraint);

			if (dualUpdate != 0) {
				foundViolation = true;

				updates[constId] -= learningRate * dualUpdate;

				if (debug) {
					System.out.println("Constraint violation: " + constraint);
					System.out.println("Dual update: " + dualUpdate);
					System.out.println("Current value of dual update: "
							+ updates[constId]);
				}
			}
		}

		if (foundViolation) {

			for (int constId = 0; constId < constraints.size(); constId++) {

				ILPConstraint constraint = constraints.get(constId);

				// update dual values
				dual[constId] += updates[constId];

				if (debug && dual[constId] != 0) {
					System.out.println("update for " + constraint + ": "
							+ dual[constId]);
				}

				if (constraint.sense != ILPConstraint.EQUAL) {
					if (dual[constId] < 0)
						dual[constId] = 0;
				}

			}
		}

		return foundViolation;
	}

	protected double testConstraintFeasibility(int[][] assignment,
			ILPConstraint constraint) {

		double lhs = 0;
		for (int i = 0; i < constraint.vars.length; i++) {
			int var = constraint.vars[i];
			double coeff = constraint.coeffs[i];

			boolean val;

			if (var >= variables.size()) {

				log.warn(
						"Possible error. Variable {} seems to be unknown! Ignoring for "
								+ "temporary sanity", var);
				continue;
			}
			Variable v = variables.get(var);

			if (v instanceof SenseVariable) {
				int predicateId = ((SenseVariable) v).predicateId;
				int label = ((SenseVariable) v).senseId;

				val = assignment[predicateId][0] == label;

			} else {
				int predicateId = ((ArgumentVariable) v).predicateId;
				int label = ((ArgumentVariable) v).labelId;
				int candidate = ((ArgumentVariable) v).candidateId;
				val = assignment[predicateId][candidate + 1] == label;
			}

			if (val) {
				lhs += coeff;
			}
		}

		double rhs = constraint.rhs;
		double update = 0;

		switch (constraint.sense) {
		case ILPConstraint.EQUAL:
			if (lhs != rhs) {
				update = rhs - lhs;
			}
			break;
		case ILPConstraint.LESS_THAN:
			if (lhs > rhs) {
				update = rhs - lhs;
			}
			break;
		case ILPConstraint.GREATER_THAN:
			if (lhs < rhs) {
				update = lhs - rhs;
			}
			break;
		}

		return update;
	}

	protected void intitializeVariables() {

		int var = 0;

		for (int predicateId = 0; predicateId < numPredicates; predicateId++) {

			SRLPredicateInstance x = this.instance.predicates.get(predicateId);

			List<SRLMulticlassInstance> candidates = x.getCandidateInstances();

			for (int candidateId = 0; candidateId < candidates.size(); candidateId++) {

				for (int labelId = 0; labelId < manager
						.getNumLabels(Models.Classifier); labelId++) {
					String label = manager.getArgument(labelId);

					assert label != null : labelId + " is a null object!";

					String variableIdentifier = SRLILPInference
							.getArgumentVariableIdentifier(viewName,
									predicateId, candidateId, label);

					variableManager.addVariable(variableIdentifier, var);

					Variable v = new ArgumentVariable(predicateId, candidateId,
							labelId);

					variables.add(v);

					// System.out.println(var + "-> " + v.toString());
					var++;

				}

			}

			for (int senseId = 0; senseId < manager.getNumLabels(Models.Sense); senseId++) {

				String label = manager.getSense(senseId);

				String variableIdentifier = SRLILPInference
						.getSenseVariableIdentifier(viewName, predicateId,
								label);

				variableManager.addVariable(variableIdentifier, var);

				SenseVariable v = new SenseVariable(predicateId, senseId);
				variables.add(v);

				// System.out.println(var + "-> " + v.toString());

				var++;

			}

		}

		assert variableManager.size() == variables.size() : "Unknown variables!";

		assert variableManager.size() > 0 : "No varaibles added for " + this.ta;
	}

	interface Variable {}

	class SenseVariable implements Variable {
		int predicateId;
		int senseId;

		public SenseVariable(int predicateId, int senseId) {
			this.predicateId = predicateId;
			this.senseId = senseId;
		}

		@Override
		public String toString() {
			return "predicate: " + predicateId + ", sense: " + senseId;
		}
	}

	class ArgumentVariable implements Variable {
		int predicateId;
		int candidateId;
		int labelId;

		public ArgumentVariable(int predicateId, int candidateId, int labelId) {
			this.predicateId = predicateId;
			this.candidateId = candidateId;
			this.labelId = labelId;
		}

		@Override
		public String toString() {
			return "predicate: " + predicateId + ", candidate: " + candidateId
					+ ", label: " + labelId;
		}
	}

}
