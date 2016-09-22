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
import edu.illinois.cs.cogcomp.srl.inference.SRLILPInference;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateStructure;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceStructure;

import java.util.*;


public class PredicateSenseConstraints extends SRLILPConstraintGenerator {

    public static final String name = "predicateSense";

    public PredicateSenseConstraints(SRLManager manager) {
        super(manager, name, true);
    }

    @Override
    public List<ILPConstraint> getILPConstraints(IInstance x, InferenceVariableLexManager variables) {
        return getViolatedILPConstraints(x, null, variables);
    }

    @Override
    public List<ILPConstraint> getViolatedILPConstraints(IInstance ins, IStructure s,
            InferenceVariableLexManager variables) {

        SRLSentenceInstance x = (SRLSentenceInstance) ins;
        SRLSentenceStructure y = (SRLSentenceStructure) s;

        List<ILPConstraint> list = new ArrayList<>();
        for (int predicateId = 0; predicateId < x.numPredicates(); predicateId++) {

            SRLPredicateInstance xp = x.predicates.get(predicateId);
            SRLPredicateStructure yp = y == null ? null : y.ys.get(predicateId);

            list.addAll(getPredicateSenseConstraints(manager, predicateId, xp, yp, variables));
        }

        return list;
    }

    private List<ILPConstraint> getPredicateSenseConstraints(SRLManager manager, int predicateId,
            SRLPredicateInstance x, SRLPredicateStructure y, InferenceVariableLexManager variables) {
        List<ILPConstraint> list = new ArrayList<>();

        int numCandidates = x.getCandidateInstances().size();
        String type = manager.getPredictedViewName();

        String lemma = x.getSenseInstance().getPredicateLemma();

        Map<String, Set<String>> validLabelsForSense = manager.getLegalLabelsForSense(lemma);

        Set<String> allLabels = manager.getAllArguments();

        Set<String> validSenses = manager.getLegalSenses(lemma);

        for (int candidateId = 0; candidateId < numCandidates; candidateId++) {
            for (String sense : validSenses) {
                if (y != null) {
                    if (y.getSense() != manager.getSenseId(sense))
                        continue;
                }

                String senseVarName =
                        SRLILPInference.getSenseVariableIdentifier(type, predicateId, sense);
                int senseVar = variables.getVariable(senseVarName);

                Set<String> validLabels = validLabelsForSense.get(sense);

                Set<String> labels = new HashSet<>();

                for (String label : allLabels) {
                    if (label.startsWith("AM-") || label.startsWith("R-AM-")
                            || label.startsWith("C-AM-"))
                        labels.add(label);
                    else {
                        String ll = label.replaceAll("R-", "").replaceAll("C-", "");
                        if (validLabels.contains(ll))
                            labels.add(label);
                    }
                }

                labels.add("C-V");
                labels.add(SRLManager.NULL_LABEL);
                if (manager.getSRLType() == SRLType.Nom) {
                    labels.add("SUP");
                    labels.add("C-SUP");
                    labels.add("A1");
                }

                if (y != null) {
                    int argLabel = y.getArgLabel(candidateId);
                    if (labels.contains(manager.getArgument(argLabel)))
                        continue;
                }

                int[] vars = new int[labels.size() + 2];
                double[] coefs = new double[vars.length];
                vars[0] = senseVar;
                coefs[0] = -1;

                vars[1] =
                        getArgumentVariable(variables, type, predicateId, candidateId,
                                SRLManager.NULL_LABEL);
                coefs[1] = 1.0;

                int i = 2;
                for (String validLabel : labels) {
                    int var =
                            getArgumentVariable(variables, type, predicateId, candidateId,
                                    validLabel);
                    vars[i] = var;
                    coefs[i] = 1.0;
                    i++;
                }

                Pair<int[], double[]> cleanedVar = cleanupVariables(vars, coefs);
                vars = cleanedVar.getFirst();
                coefs = cleanedVar.getSecond();

                if (vars.length > 0) {
                    // addGreaterThanConstraint(xmp, vars, coefs, 0);
                    list.add(new ILPConstraint(vars, coefs, 0, ILPConstraint.GREATER_THAN));
                }

            }// foreach valid sense

        }// foreach candidate

        return list;
    }
}
