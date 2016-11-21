/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.infer.ilp.ILPOutputGenerator;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolver;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.learn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ILPOutput implements ILPOutputGenerator {
    private final static Logger log = LoggerFactory.getLogger(ILPOutput.class);
    private SRLManager manager;

    public ILPOutput(SRLManager manager) {
        this.manager = manager;

    }

    public SRLSentenceStructure getOutput(ILPSolver xmp,
            InferenceVariableLexManager variableManager, IInstance ins) {

        SRLSentenceInstance instance = (SRLSentenceInstance) ins;

        int numPredicates = instance.numPredicates();
        List<SRLPredicateStructure> output = new ArrayList<>();

        for (int predicateId = 0; predicateId < numPredicates; predicateId++) {

            SRLPredicateInstance x = instance.predicates.get(predicateId);

            SRLMulticlassInstance senseInstance = x.getSenseInstance();

            String lemma = senseInstance.getPredicateLemma();

            int senseLabel =
                    getSenseLabelFromPrediction(xmp, variableManager, manager, predicateId, lemma);

            int[] argLabels =
                    getArgumentLabels(xmp, variableManager, manager, predicateId, x, lemma);

            output.add(new SRLPredicateStructure(x, argLabels, senseLabel, manager));
        }

        return new SRLSentenceStructure(instance, output);
    }

    private int[] getArgumentLabels(ILPSolver xmp, InferenceVariableLexManager variableManager,
            SRLManager manager, int predicateId, SRLPredicateInstance x, String lemma) {
        Set<String> legalArgsSet = manager.getLegalArguments(lemma);

        List<SRLMulticlassInstance> candidateInstances = x.getCandidateInstances();

        int numCandidates = candidateInstances.size();

        int[] argLabels = new int[numCandidates];

        log.debug("Getting output for " + x);

        for (int candidateId = 0; candidateId < numCandidates; candidateId++) {

            log.debug("Considering {}", x.getCandidateInstances().get(candidateId));
            String label = null;
            for (String l : legalArgsSet) {
                String variableIdentifier =
                        SRLILPInference.getArgumentVariableIdentifier(
                                manager.getPredictedViewName(), predicateId, candidateId, l);
                int var = variableManager.getVariable(variableIdentifier);

                if (var < 0)
                    continue;

                boolean value = xmp.getBooleanValue(var);

                log.debug("  {}: {}", l, value);

                if (value) {

                    label = l;

                    log.debug("Variable: " + var + ", label = " + label + ", label-id = "
                            + manager.getArgumentId(label));
                    break;
                }
            }

            assert label != null;
            argLabels[candidateId] = manager.getArgumentId(label);

            log.debug("Prediction for {}: {}", x.getCandidateInstances().get(candidateId), label);

        }
        return argLabels;
    }

    private int getSenseLabelFromPrediction(ILPSolver xmp,
            InferenceVariableLexManager variableManager, SRLManager manager, int predicateId,
            String lemma) {
        String sense = null;
        Set<String> validSenseLabels = manager.getLegalSenses(lemma);
        for (String label : validSenseLabels) {
            String varName =
                    SRLILPInference.getSenseVariableIdentifier(manager.getPredictedViewName(),
                            predicateId, label);

            int var = variableManager.getVariable(varName);

            if (var < 0)
                continue;

            if (xmp.getBooleanValue(var)) {
                sense = label;
                break;
            }
        }

        assert sense != null;

        int senseLabel = manager.getSenseId(sense);
        return senseLabel;
    }

}
