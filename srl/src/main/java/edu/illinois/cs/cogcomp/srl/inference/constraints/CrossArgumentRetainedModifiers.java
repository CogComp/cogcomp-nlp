/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.data.LegalArguments;
import edu.illinois.cs.cogcomp.srl.inference.SRLConstraints;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;

import edu.illinois.cs.cogcomp.srl.learn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * Once a modifier, always a modifier. A modifier for one predicate can only be a modifier for
 * another. The label need not be the same, though.
 * <p>
 * For verbs, the first argument cannot be an AM-PNC and the second argument can either have the
 * same label OR the label of the second argument could be X with a sibling C-X OR the label of the
 * second argument could be C-X.
 * 
 * @author Vivek Srikumar
 * 
 */
public class CrossArgumentRetainedModifiers extends SRLILPConstraintGenerator {

    private final static Logger log = LoggerFactory.getLogger(CrossArgumentRetainedModifiers.class);

    private static Map<String, Map<String, String>> verbViolations, nomViolations;

    public CrossArgumentRetainedModifiers(SRLManager manager) {
        super(manager, SRLConstraints.crossArgumentRetainedModifiers.name(), true);

        if (manager.getSRLType() == SRLType.Verb) {
            if (verbViolations == null) {
                synchronized (log) {
                    if (verbViolations == null) {
                        try {
                            verbViolations = loadValidViolations(manager.getSRLType());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } else {
            if (nomViolations == null) {
                synchronized (log) {
                    if (nomViolations == null) {
                        try {
                            nomViolations = loadValidViolations(manager.getSRLType());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
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

        int numPredicates = x.numPredicates();

        // WARNING: Hairy code ahead. Read the comments carefully.

        List<ILPConstraint> constraints = new ArrayList<>();

        for (int predicateId = 0; predicateId < numPredicates; predicateId++) {

            List<SRLMulticlassInstance> candidateInstances =
                    x.predicates.get(predicateId).getCandidateInstances();
            int numCandidates = candidateInstances.size();

            SRLPredicateStructure predicateStructure = null;
            if (y != null) {
                predicateStructure = y.ys.get(predicateId);
            }

            for (int candidateId = 0; candidateId < numCandidates; candidateId++) {

                // for all modifier arguments,
                for (String modifier : manager.getModifierArguments()) {
                    if (predicateStructure != null) {
                        // if this is not the modifier being verified, don't
                        // add any constraint
                        if (predicateStructure.getArgLabel(candidateId) != manager
                                .getArgumentId(modifier))
                            continue;
                    }

                    addModifierConstraint(predicateId, candidateId, modifier, candidateInstances
                            .get(candidateId).getConstituent(), x, y, constraints, variables);

                } // foreach modifier
            }
        }
        return constraints;
    }

    private void addModifierConstraint(int predicateId, int candidateId, String modifier,
            Constituent candidate, SRLSentenceInstance x, SRLSentenceStructure y,
            List<ILPConstraint> constraints, InferenceVariableLexManager variables) {
        int modVar =
                this.getArgumentVariable(variables, manager.getPredictedViewName(), predicateId,
                        candidateId, modifier);

        // If argument is a modifier, then a candidate for another preidcate
        // that has the same span can ONLY have one of a few labels. Let's
        // enumerate them.

        for (int otherPredicateId = 0; otherPredicateId < x.numPredicates(); otherPredicateId++) {

            if (otherPredicateId == predicateId)
                continue;

            SRLPredicateInstance otherPredicateInstance = x.predicates.get(otherPredicateId);
            List<SRLMulticlassInstance> candidateInstances =
                    otherPredicateInstance.getCandidateInstances();

            String otherLemma = otherPredicateInstance.getSenseInstance().getPredicateLemma();

            int numCandidates = candidateInstances.size();

            SRLPredicateStructure otherPredicateStructure = null;
            if (y != null) {
                otherPredicateStructure = y.ys.get(otherPredicateId);
            }

            for (int otherCandidateId = 0; otherCandidateId < numCandidates; otherCandidateId++) {
                // This constraint only applies for candidates that have teh
                // same span.
                Constituent otherConstituent =
                        candidateInstances.get(otherCandidateId).getConstituent();
                if (!candidate.getSpan().equals(otherConstituent.getSpan()))
                    continue;

                int label = -1;
                if (y != null) {
                    assert otherPredicateStructure != null;
                    label = otherPredicateStructure.getArgLabel(otherCandidateId);
                }

                Set<Integer> vars = new HashSet<>();

                // go over all the options. At each point, check if the
                // constraint is satisfied. If so, don't add the constraint.

                // 1. It can be the modifier
                if (checkAddConstraint(variables, otherPredicateId, otherCandidateId, label, vars,
                        modifier))
                    continue;

                // 2. It can be null;
                if (checkAddConstraint(variables, otherPredicateId, otherCandidateId, label, vars,
                        SRLManager.NULL_LABEL))
                    continue;

                // 3. If the "otherlemma" is one of the allowed violations, it
                // can be the label specified in the data. The allowed
                // violations are generated by the function
                // generateLegalArgumentsFile below.
                if (checkAddValidViolationConstraint(modifier, variables, otherPredicateId,
                        otherCandidateId, otherLemma, label, vars))
                    continue;

                // 4. If the other label is one of the C-args
                boolean ignore = false;
                for (String arg : manager.getAllArguments()) {
                    if (checkAddConstraint(variables, otherPredicateId, otherCandidateId, label,
                            vars, "C-" + arg)) {
                        ignore = true;
                        break;
                    }
                }
                if (ignore)
                    continue;

                if (manager.getSRLType() == SRLType.Nom) {
                    // 5. For nom, the only "other modifier label" allowed: If
                    // this label is AM-MNR, other can be AM-ADV. and vice
                    // versa. Because this is not a very clear distinction in
                    // the training set.
                    if (modifier.equals("AM-MNR")) {
                        if (checkAddConstraint(variables, otherPredicateId, otherCandidateId,
                                label, vars, "AM-ADV"))
                            continue;
                    }

                    if (modifier.equals("AM-ADV")) {
                        if (checkAddConstraint(variables, otherPredicateId, otherCandidateId,
                                label, vars, "AM-MNR"))
                            continue;
                    }
                }

                /*
                 * XXX: Not doing this for now.
                 * 
                 * // 6. The most complicated one. The other argument can be any // argument label,
                 * provided some other candidate for that // predicate is a C-arg for that label.
                 * Checking for this // involves looping over all argument labels, and other //
                 * candidates and finally adding the constraint.
                 * 
                 * checkCArgConstraints(variables, modVar, vars, otherPredicateId, otherCandidateId,
                 * otherPredicateStructure, label, constraints);
                 */

                int[] v = new int[vars.size() + 1];
                double[] c = new double[vars.size() + 1];
                v[0] = modVar;
                c[0] = -1;

                int count = 1;
                for (int i : vars) {
                    v[count] = i;
                    c[count] = 1;
                    count++;
                }

                Pair<int[], double[]> clean = cleanupVariables(v, c);

                if (clean.getFirst().length > 0)
                    constraints.add(new ILPConstraint(clean.getFirst(), clean.getSecond(), 0,
                            ILPConstraint.GREATER_THAN));

            }

        }// for all other predicates
    }

    private boolean checkAddValidViolationConstraint(String modifier,
            InferenceVariableLexManager variables, int otherPredicateId, int otherCandidateId,
            String otherLemma, int label, Set<Integer> vars) {

        Map<String, Map<String, String>> vv;
        if (manager.getSRLType() == SRLType.Verb)
            vv = verbViolations;
        else
            vv = nomViolations;

        if (!vv.containsKey(modifier))
            return false;

        if (!vv.get(modifier).containsKey(otherLemma))
            return false;

        String arg = vv.get(modifier).get(otherLemma);

        return checkAddConstraint(variables, otherPredicateId, otherCandidateId, label, vars, arg);

    }

    /**
     * Check if the other candidate has label 'arg'. If so, then there is no violation. So return
     * true. Otherwise, add the variable to the accumulator set.
     */
    private boolean checkAddConstraint(InferenceVariableLexManager variables, int otherPredicateId,
            int otherCandidateId, int label, Set<Integer> vars, String arg) {
        boolean valid = false;
        if (label >= 0) {
            if (label == manager.getArgumentId(arg))
                valid = true;
        } else {
            int variable =
                    this.getArgumentVariable(variables, manager.getPredictedViewName(),
                            otherPredicateId, otherCandidateId, arg);
            if (variable >= 0)
                vars.add(variable);
        }
        return valid;
    }

    private static Map<String, Map<String, String>> loadValidViolations(SRLType srlType)
            throws Exception {
        Map<String, Map<String, String>> map = new HashMap<>();

        String file = srlType + ".cross-predicate.modifiers";

        List<URL> list = IOUtils.lsResources(LegalArguments.class, file);
        if (list.size() == 0) {
            log.error("Cannot find file " + file + " in the classpath!");
            throw new Exception("Cannot find file " + file + " in the classpath!");
        } else {

            URL url = list.get(0);
            Scanner scanner = new Scanner(url.openStream());

            log.info("Loading allowed cross-argument" + " modifier arguments from {}", file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.length() == 0)
                    continue;

                String[] parts = line.split("\t");
                String mod = parts[0];

                map.put(mod, new HashMap<String, String>());
                for (int i = 1; i < parts.length; i++) {
                    String[] pp = parts[i].split(",");

                    String lemma = pp[0];
                    String arg = pp[1];

                    map.get(mod).put(lemma, arg);
                }
            }
        }

        return map;
    }
}
