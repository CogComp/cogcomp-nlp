/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.inference;

import edu.illinois.cs.cogcomp.infer.ilp.ILPOutputGenerator;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolver;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.jlis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ILPOutput implements ILPOutputGenerator {
    private SenseManager manager;

    public ILPOutput(SenseManager manager) {
        this.manager = manager;
    }

    public SentenceStructure getOutput(ILPSolver xmp, InferenceVariableLexManager variableManager,
            IInstance ins) {
        SentenceInstance instance = (SentenceInstance) ins;

        int numPredicates = instance.numPredicates();
        List<SenseStructure> output = new ArrayList<>();

        for (int predicateId = 0; predicateId < numPredicates; predicateId++) {
            SenseInstance senseInstance = instance.predicates.get(predicateId);
            String lemma = senseInstance.getPredicateLemma();

            int senseLabel =
                    getSenseLabelFromPrediction(xmp, variableManager, manager, predicateId, lemma);
            output.add(new SenseStructure(senseInstance, senseLabel, manager));
        }

        return new SentenceStructure(instance, output);
    }

    private int getSenseLabelFromPrediction(ILPSolver xmp,
            InferenceVariableLexManager variableManager, SenseManager manager, int predicateId,
            String lemma) {
        String sense = null;
        Set<String> validSenseLabels = manager.getLegalSenses(lemma);
        for (String label : validSenseLabels) {
            String varName =
                    ILPInference.getSenseVariableIdentifier(SenseManager.getPredictedViewName(),
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
        return manager.getSenseId(sense);
    }
}
