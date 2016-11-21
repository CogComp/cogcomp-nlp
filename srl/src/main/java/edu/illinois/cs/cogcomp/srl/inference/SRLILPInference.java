/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.infer.ilp.*;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory.SolverType;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.learn.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final public class SRLILPInference extends AbstractILPInference<SRLSentenceStructure> {

    final static boolean DEBUG = false;

    private final static Logger log = LoggerFactory.getLogger(SRLILPInference.class);

    public final SRLSentenceInstance instance;

    protected TextAnnotation ta;

    protected final SRLManager manager;

    private String viewName;

    private final int numPredicates;

    public boolean debugMode = false;

    private ILPOutput outputGenerator;

    public SRLILPInference(ILPSolverFactory solverFactory, SRLManager manager,
            List<Constituent> predicates) throws Exception {
        super(solverFactory, DEBUG);
        this.manager = manager;

        this.outputGenerator = new ILPOutput(manager);

        List<SRLPredicateInstance> instances = new ArrayList<>();
        for (Constituent predicate : predicates) {
            Constituent predicateClone = predicate.cloneForNewView(predicate.getViewName());
            SRLPredicateInstance x;

            assert predicateClone.hasAttribute(PredicateArgumentView.LemmaIdentifier);

            x = new SRLPredicateInstance(predicateClone, manager, manager.getArgumentIdentifier());
            x.cacheAllFeatureVectors(false);

            instances.add(x);
        }

        ta = predicates.get(0).getTextAnnotation();

        viewName = manager.getPredictedViewName();

        instance = new SRLSentenceInstance(instances);
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
    protected SRLSentenceStructure getOutput(ILPSolver xmp,
            InferenceVariableLexManager variableManager) throws Exception {
        return outputGenerator.getOutput(xmp, variableManager, this.instance);
    }

    protected void addConstraints(ILPSolver xmp, List<ILPConstraint> constraints,
            String debugMessage) {
        log.debug(debugMessage);

        for (ILPConstraint c : constraints) {
            addConstraint(xmp, c);
        }
    }

    @Override
    protected void addConstraints(ILPSolver xmp, InferenceVariableLexManager variableManager) {
        Set<SRLConstraints> constraints = manager.getConstraints();
        if (constraints.size() == 0)
            log.error("No constraints found. This can't be right.");

        for (SRLConstraints cc : constraints) {

            if (debugMode) {
                System.out.println("Adding constriant: " + cc);
            }

            SRLILPConstraintGenerator c = cc.getGenerator(manager);

            if (c.isDelayedConstraint() && xmp instanceof JLISCuttingPlaneILPSolverGurobi) {
                ((JLISCuttingPlaneILPSolverGurobi) xmp).addCuttingPlaneConstraintGenerator(c);
            } else {
                List<ILPConstraint> cs = c.getILPConstraints(instance, variableManager);
                addConstraints(xmp, cs, c.name);
            }
        }
    }

    @Override
    protected void addVariables(ILPSolver xmp, InferenceVariableLexManager variableManager) {
        assert xmp != null;

        for (int predicateId = 0; predicateId < numPredicates; predicateId++) {
            SRLPredicateInstance x = instance.predicates.get(predicateId);

            SRLMulticlassInstance senseX = x.getSenseInstance();
            List<SRLMulticlassInstance> candidates = x.getCandidateInstances();

            String lemma = senseX.getPredicateLemma();
            assert lemma != null;

            log.debug("Adding variables for " + lemma);

            Set<String> legalArgs = manager.getLegalArguments(lemma);

            Set<Integer> set;
            for (int candidateId = 0; candidateId < candidates.size(); candidateId++) {

                SRLMulticlassInstance cX = candidates.get(candidateId);
                double[] scores = manager.getScores(cX, Models.Classifier, true);

                log.debug("\tCandidate = " + cX.toString());

                assert scores.length == manager.getAllArguments().size();

                double idScore;
                double[] sc = manager.getScores(cX, Models.Identifier, true);
                idScore = sc[1] - sc[0];


                set = new HashSet<>();
                for (int labelId = 0; labelId < scores.length; labelId++) {
                    String label = manager.getArgument(labelId);

                    assert label != null : labelId + " is a null object!";

                    if (!legalArgs.contains(label))
                        continue;

                    double score = scores[labelId];
                    if (label.equals(SRLManager.NULL_LABEL))
                        score -= idScore;

                    String variableIdentifier =
                            getArgumentVariableIdentifier(viewName, predicateId, candidateId, label);

                    int var = xmp.addBooleanVariable(score);
                    variableManager.addVariable(variableIdentifier, var);

                    set.add(var);

                    log.debug("Arg variable: " + score + " " + variableIdentifier + " " + var + " "
                            + label);
                }

                log.debug("Adding unique arg label constraint for {}", candidateId);
                addUniqueLabelConstraint(xmp, set);

            }

            double[] senseScores = manager.getScores(senseX, Models.Sense, true);

            set = new HashSet<>();

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

    public static String getArgumentVariableIdentifier(String type, int predicateId,
            int candidateId, String label) {
        return type + ":" + predicateId + ":" + candidateId + ":" + label;
    }

    public static String getSenseVariableIdentifier(String type, int predicateId, String label) {
        return type + ":sense:" + predicateId + ":" + label;
    }

    public PredicateArgumentView getOutputView() throws Exception {
        return runInference().getView(manager, ta);
    }
}
