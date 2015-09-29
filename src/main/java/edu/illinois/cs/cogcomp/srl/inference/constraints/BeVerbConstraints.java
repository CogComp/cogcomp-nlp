package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.jlis.*;

import java.util.*;

public class BeVerbConstraints extends SRLILPConstraintGenerator {

	public static final String name = "beVerbConstraints";

	private final static Set<String> validArgs = new HashSet<String>(
			Arrays.asList("A1", "AA", "AM", "AM-ADV", "AM-CAU", "AM-DIR",
					"AM-DIS", "AM-EXT", "AM-LOC", "AM-MNR", "AM-MOD", "AM-NEG",
					"AM-PNC", "AM-PRD", "AM-REC", "AM-TMP", "C-A0", "C-A1",
					"C-A2", "C-A3", "C-AM-ADV", "C-AM-CAU", "C-AM-DIS",
					"C-AM-EXT", "C-AM-LOC", "C-AM-MNR", "R-A0", "R-A1", "R-A2",
					"R-A3", "R-AA", "R-AM-ADV", "R-AM-LOC", "R-AM-MNR",
					"R-AM-PNC", "R-AM-TMP", "V", "C-V", SRLManager.NULL_LABEL,
					"C-A4", "C-A5", "C-AM-DIR", "C-AM-NEG", "C-AM-PNC",
					"C-AM-TMP", "R-A4", "R-AA", "R-AM-CAU", "R-AM-DIR",
					"R-AM-EXT"));

	public BeVerbConstraints(SRLManager manager) {
		super(manager, name, false);
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

			SRLMulticlassInstance senseInstance = xp.getSenseInstance();

			String lemma = senseInstance.getPredicateLemma();

			if (!lemma.equals("be"))
				return list;

			Constituent predicate = senseInstance.getConstituent();

			TextAnnotation ta = predicate.getTextAnnotation();

			int predicateToken = senseInstance.getConstituent().getStartSpan();

			Set<String> allowedArgs = new HashSet<String>(validArgs);

			boolean done = false;
			if (predicateToken + 1 < ta.getSentence(predicate.getSentenceId())
					.getEndSpan()) {
				String nextToken = WordHelpers.getLemma(ta, predicateToken + 1);

				if (nextToken.toLowerCase().equals("like")) {
					allowedArgs.add("A0");

				}
				done = true;
			}

			if (!done)
				allowedArgs.add("A2");

			SRLPredicateStructure yp = (y == null ? null : y.ys
					.get(predicateId));

			for (int candidateId = 0; candidateId < xp.getCandidateInstances()
					.size(); candidateId++) {

				if (y != null) {
					int argLabel = yp.getArgLabel(candidateId);
					if (!allowedArgs.contains(manager.getArgument(argLabel))) {
						continue;
					}
				}

				int[] vars = new int[allowedArgs.size()];
				double[] coef = new double[allowedArgs.size()];

				int id = 0;
				for (String label : allowedArgs) {

					vars[id] = getArgumentVariable(variables,
							manager.getPredictedViewName(), predicateId,
							candidateId, label);
					coef[id] = 1.0;
					id++;
				}

				Pair<int[], double[]> pair = cleanupVariables(vars, coef);

				vars = pair.getFirst();
				coef = pair.getSecond();

				if (vars.length > 0)
					list.add(new ILPConstraint(vars, coef, 1.0,
							ILPConstraint.EQUAL));
			}
		}
		return list;
	}
}
