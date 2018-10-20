/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.inference;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.infer.ilp.*;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory.SolverType;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SentenceInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SentenceStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ILPInference extends AbstractILPInference<SentenceStructure> {

    private final static Logger log = LoggerFactory.getLogger(ILPInference.class);

    public final SentenceInstance instance;
    protected TextAnnotation ta;
    protected final SenseManager manager;
    private String viewName;
    private final int numPredicates;
    private ILPOutput outputGenerator;

    public ILPInference(ILPSolverFactory solverFactory, SenseManager manager,
            List<Constituent> predicates) throws Exception {
        super(solverFactory, false);
        this.manager = manager;

        this.outputGenerator = new ILPOutput(manager);

        List<SenseInstance> instances = new ArrayList<>();
        for (Constituent predicate : predicates) {
            Constituent predicateClone = predicate.cloneForNewView(predicate.getViewName());
            SenseInstance x;

            assert predicateClone.hasAttribute(PredicateArgumentView.LemmaIdentifier);

            x = new SenseInstance(predicateClone, manager);

            x.cacheAllFeatureVectors();

            instances.add(x);
        }

        ta = predicates.get(0).getTextAnnotation();

        viewName = SenseManager.getPredictedViewName();

        instance = new SentenceInstance(instances);
        numPredicates = instance.numPredicates();
    }

    @Override
    protected void initializeSolver(ILPSolver xmp, InferenceVariableLexManager variableManager) {
        if (this.solverFactory.type == SolverType.JLISCuttingPlaneGurobi) {
            JLISCuttingPlaneILPSolverGurobi s = (JLISCuttingPlaneILPSolverGurobi) xmp;
            s.setInput(instance);
            s.setVariableManager(variableManager);
            s.setOutputGenerator(outputGenerator);
        }
    }

    @Override
    protected SentenceStructure getOutput(ILPSolver xmp, InferenceVariableLexManager variableManager)
            throws Exception {
        return outputGenerator.getOutput(xmp, variableManager, this.instance);
    }

    @Override
    protected void addConstraints(ILPSolver xmp, InferenceVariableLexManager variableManager) {
        // XXX Add constraints for sense classifier?
    }

    @Override
    protected void addVariables(ILPSolver xmp, InferenceVariableLexManager variableManager) {
        assert xmp != null;

        for (int predicateId = 0; predicateId < numPredicates; predicateId++) {
            SenseInstance senseX = instance.predicates.get(predicateId);

            String lemma = senseX.getPredicateLemma();
            assert lemma != null;

            log.debug("Adding variables for " + lemma);

            double[] senseScores = manager.getScores(senseX, true);

            Set<Integer> set = new HashSet<>();

            for (int senseId = 0; senseId < senseScores.length; senseId++) {
                if (!manager.isValidSense(lemma, senseId))
                    continue;

                String label = manager.getSense(senseId);
                double score = senseScores[senseId];
                String variableIdentifier =
                        getSenseVariableIdentifier(viewName, predicateId, label);
                int var = xmp.addBooleanVariable(score);
                variableManager.addVariable(variableIdentifier, var);
                set.add(var);
                log.debug("Sense variable: " + score + " " + variableIdentifier + " " + var + " "
                        + label);
            }

            log.debug("Adding unique sense label constraint");
            addUniqueLabelConstraint(xmp, set);
        }

        assert variableManager.size() > 0 : "No varaibles added for " + this.ta;
    }

    private void addUniqueLabelConstraint(ILPSolver xmp, Set<Integer> set) {
        int[] vars = new int[set.size()];
        double[] coeffs = new double[set.size()];
        int i = 0;
        for (int v : set) {
            vars[i] = v;
            coeffs[i++] = 1.0;
        }

        addEqualityConstraint(xmp, vars, coeffs, 1.0);
    }

    public static String getSenseVariableIdentifier(String type, int predicateId, String label) {
        return type + ":sense:" + predicateId + ":" + label;
    }

    public TokenLabelView getOutputView() throws Exception {
        return runInference().getView(manager, ta);
    }
}
