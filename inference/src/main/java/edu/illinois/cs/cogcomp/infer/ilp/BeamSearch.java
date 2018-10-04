/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vivek Srikumar
 * 
 */
public class BeamSearch implements ILPSolver {

    private static final double EPSILON = 0.00000001;

    private static boolean DEBUG = false;

    private static Logger log = LoggerFactory.getLogger(BeamSearch.class);

    private final int beamSize;

    private List<int[]> variables;
    private List<double[]> scores;

    private List<ILPConstraint> constraints;

    private int numVars;

    private boolean isSolved;
    private boolean[] assignment;
    private double objectiveValue;
    private boolean maximize;

    private final static Comparator<PartialAssignment> maxComparator =
            new Comparator<PartialAssignment>() {

                public int compare(PartialAssignment o1, PartialAssignment o2) {
                    if (o1.score > o2.score)
                        return -1;
                    else if (o1.score == o2.score)
                        return 0;
                    else
                        return 1;
                }

            };

    private final static Comparator<PartialAssignment> minComparator =
            new Comparator<PartialAssignment>() {

                public int compare(PartialAssignment o1, PartialAssignment o2) {
                    if (o1.score < o2.score)
                        return -1;
                    else if (o1.score == o2.score)
                        return 0;
                    else
                        return 1;

                }

            };

    public BeamSearch(int beamSize) {
        this.beamSize = beamSize;
        this.maximize = false;
        reset();
    }

    public int addBooleanVariable(double arg0) {
        int id = numVars;
        numVars++;

        this.variables.add(new int[] {id});
        this.scores.add(new double[] {arg0});

        this.isSolved = false;

        return id;
    }

    public int addRealVariable(double arg0) {
        throw new RuntimeException("Not implemented");
    }

    public int addIntegerVariable(double arg0) {
        throw new RuntimeException("Not implemented");
    }


    public int[] addDiscreteVariable(double[] arg0) {

        int[] vars = new int[arg0.length];
        double[] scores = arg0.clone();

        for (int i = 0; i < arg0.length; i++)
            vars[i] = numVars++;

        this.variables.add(vars);
        this.scores.add(scores);

        this.isSolved = false;

        return vars;
    }

    private void addConstraint(int[] ids, double[] coefs, double rhs, char type) {
        constraints.add(new ILPConstraint(ids, coefs, rhs, type));
        isSolved = false;
    }

    public void addEqualityConstraint(int[] arg0, double[] arg1, double arg2) {
        addConstraint(arg0, arg1, arg2, ILPConstraint.EQUAL);
    }

    public void addGreaterThanConstraint(int[] arg0, double[] arg1, double arg2) {
        addConstraint(arg0, arg1, arg2, ILPConstraint.GREATER_THAN);
    }

    public void addLessThanConstraint(int[] arg0, double[] arg1, double arg2) {
        addConstraint(arg0, arg1, arg2, ILPConstraint.LESS_THAN);
    }

    public boolean getBooleanValue(int arg0) {
        if (!isSolved)
            return false;
        else
            return assignment[arg0];
    }

    @Override
    public double getRealValue(int arg0) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getIntegerValue(int arg0) {
        throw new RuntimeException("Not implemented");
    }


    public boolean isSolved() {
        return isSolved;
    }

    public double objectiveValue() {
        return this.objectiveValue;
    }

    @Override
    public double objectiveCoeff(int index) {
        return scores.get(index)[0];
    }

    public void reset() {
        this.constraints = new ArrayList<ILPConstraint>();
        this.scores = new ArrayList<double[]>();
        this.variables = new ArrayList<int[]>();
        this.numVars = 0;
        this.isSolved = false;
        this.objectiveValue = 0;
        this.assignment = null;
    }

    public void setMaximize(boolean arg0) {
        this.maximize = arg0;

    }

    public boolean solve() throws Exception {

        if (DEBUG)
            System.out.println("Starting inference");
        long start = System.currentTimeMillis();

        // We fix an ordering of the variables as the order in which they were
        // added. This may or may not be good, without further information, this
        // will do.

        PartialAssignment initState =
                new PartialAssignment(new boolean[this.numVars], new BitSet(), 0.0, -1, -1);

        if (DEBUG)
            System.out.println("Initial state: " + initState);

        List<PartialAssignment> beam = new ArrayList<PartialAssignment>();
        List<PartialAssignment> nextQ = expandState(initState);

        beam.addAll(nextQ);

        while (true) {

            if (beam.size() == 0) {
                isSolved = false;
                long end = System.currentTimeMillis();

                log.debug("Beam size = 0.");
                log.debug("Beam search took {} ms", (end - start));

                return false;
            }

            nextQ.clear();

            for (PartialAssignment a : beam) {
                nextQ.addAll(expandState(a));
            }

            if (nextQ.size() == 0)
                break;

            beam.clear();
            beam.addAll(nextQ);

            beam = sortAndResize(beam);

        }

        if (beam.size() == 0) {
            isSolved = false;
            long end = System.currentTimeMillis();

            log.debug("Beam size = 0. Exiting search after assigning " + "all variables");
            log.debug("Beam search took {} ms", (end - start));
            return false;
        }

        isSolved = true;

        PartialAssignment top = beam.get(0);
        this.assignment = top.assignment;
        this.objectiveValue = top.score;

        long end = System.currentTimeMillis();

        log.debug("Beam search took {} ms", (end - start));

        if (top.maxVariableCollectionAssigned + 1 != this.variables.size()) {
            log.warn("Beam search halted. Choosing unconstrained solution.");
            runUnconstrainedSearch();
        }

        return true;

    }

    private void runUnconstrainedSearch() {

        assignment = new boolean[this.numVars];
        this.objectiveValue = 0;

        for (int variableCollectionId = 0; variableCollectionId < this.variables.size(); variableCollectionId++) {

            int[] vars = this.variables.get(variableCollectionId);
            double[] scores = this.scores.get(variableCollectionId);

            double bestScore = Double.NEGATIVE_INFINITY;
            int bestVariable = -1;

            for (int id = 0; id < vars.length; id++) {
                int variableId = vars[id];
                double score = scores[id];

                if (score > bestScore) {
                    bestScore = score;
                    bestVariable = variableId;
                }
            }

            assert bestVariable >= 0;
            for (int id = 0; id < vars.length; id++) {
                int variableId = vars[id];
                if (variableId == bestVariable)
                    assignment[variableId] = true;
                else
                    assignment[variableId] = false;

            }
            this.objectiveValue += bestScore;
        }

        isSolved = true;
    }

    private List<PartialAssignment> expandState(PartialAssignment state) {

        if (DEBUG) {
            System.out.println();
            System.out.println("Expanding state: " + state);
        }

        List<PartialAssignment> output = new ArrayList<PartialAssignment>();

        int variableCollection = state.maxVariableCollectionAssigned + 1;

        if (DEBUG) {
            System.out.println("Trying to expand state by adding variable collection: "
                    + variableCollection);
        }

        if (variableCollection >= this.variables.size()) {
            if (DEBUG)
                System.out.println("All variables seen!");
            return output;
        }

        int[] vars = this.variables.get(variableCollection);
        double[] scores = this.scores.get(variableCollection);

        if (vars.length == 1) {

            int variableId = vars[0];
            int maxVariable = Math.max(variableId, state.maxVariableAssigned);

            boolean[] assignTrue = state.assignment.clone();
            assignTrue[variableId] = true;
            double scoreTrue = state.score + scores[0];

            PartialAssignment aTrue =
                    new PartialAssignment(assignTrue, state.constraintsSatisfied, scoreTrue,
                            maxVariable, variableCollection);

            if (DEBUG)
                System.out.println("Checking state: " + aTrue);
            boolean constraintsSatisfied = checkConstraints(aTrue);

            if (DEBUG)
                System.out.println("Constraints satisfied: " + constraintsSatisfied);

            if (constraintsSatisfied)
                output.add(aTrue);

            boolean[] assignFalse = state.assignment.clone();
            assignFalse[variableId] = false;
            double scoreFalse = state.score;

            PartialAssignment aFalse =
                    new PartialAssignment(assignFalse, state.constraintsSatisfied, scoreFalse,
                            maxVariable, variableCollection);

            if (DEBUG)
                System.out.println("Checking state: " + aFalse);

            constraintsSatisfied = checkConstraints(aFalse);

            if (DEBUG)
                System.out.println("Constraints satisfied: " + constraintsSatisfied);

            if (constraintsSatisfied)
                output.add(aFalse);

        } else {

            int maxVariable = Math.max(vars[vars.length - 1], state.maxVariableAssigned);

            if (DEBUG)
                System.out.println("Max variable id = " + maxVariable);

            for (int i = 0; i < vars.length; i++) {

                int variableId = vars[i];
                double score = scores[i] + state.score;

                boolean[] assign = state.assignment.clone();
                assign[variableId] = true;

                PartialAssignment newState =
                        new PartialAssignment(assign, state.constraintsSatisfied, score,
                                maxVariable, variableCollection);

                if (DEBUG)
                    System.out.println("Checking state: " + newState);

                boolean constraintsSatisfied = checkConstraints(newState);

                if (DEBUG)
                    System.out.println("Constraints satisfied: " + constraintsSatisfied);

                if (constraintsSatisfied)
                    output.add(newState);

            }
        }

        return output;
    }

    private boolean checkConstraints(PartialAssignment a) {
        int numConstraints = this.constraints.size();
        for (int constraintId = 0; constraintId < numConstraints; constraintId++) {
            ILPConstraint constraint = this.constraints.get(constraintId);

            if (a.maxVariableAssigned < constraint.maximumVariableId)
                continue;

            if (a.constraintsSatisfied.get(constraintId))
                continue;

            double lhs = 0;
            for (int i = 0; i < constraint.vars.length; i++) {
                if (a.assignment[constraint.vars[i]])
                    lhs += constraint.coeffs[i];
            }

            boolean satisfied = true;
            if (constraint.sense == ILPConstraint.EQUAL)
                if (Math.abs(lhs - constraint.rhs) > EPSILON)
                    satisfied = false;
            if (constraint.sense == ILPConstraint.LESS_THAN)
                if (lhs > constraint.rhs)
                    satisfied = false;

            if (constraint.sense == ILPConstraint.GREATER_THAN)
                if (lhs < constraint.rhs)
                    satisfied = false;

            if (!satisfied) {
                if (DEBUG)
                    System.out.println(constraint + "\t" + lhs + constraint.sense + constraint.rhs);
                return false;
            }

            a.constraintsSatisfied.set(constraintId);
        }

        return true;
    }

    public void write(StringBuffer arg0) {}

    private List<PartialAssignment> sortAndResize(List<PartialAssignment> pq) {

        sortBeam(pq);

        if (beamSize < 0)
            return pq;

        if (pq.size() <= beamSize)
            return pq;

        List<PartialAssignment> pq1 = new ArrayList<PartialAssignment>();
        for (int i = 0; i < beamSize; i++) {
            pq1.add(pq.get(i));
        }
        return pq1;
    }

    private void sortBeam(List<PartialAssignment> pq) {
        if (this.maximize)
            Collections.sort(pq, maxComparator);
        else
            Collections.sort(pq, minComparator);
    }

    private static class PartialAssignment {

        final boolean[] assignment;
        final BitSet constraintsSatisfied;
        final double score;
        final int maxVariableAssigned;

        final int maxVariableCollectionAssigned;

        /**
         * @param assignment
         * @param constraintsSatisfied
         * @param score
         * @param maxVariableAssigned
         */
        public PartialAssignment(boolean[] assignment, BitSet constraintsSatisfied, double score,
                int maxVariableAssigned, int maxVariableCollectionAssigned) {
            this.maxVariableCollectionAssigned = maxVariableCollectionAssigned;
            this.assignment = assignment;
            this.constraintsSatisfied = (BitSet) constraintsSatisfied.clone();
            this.score = score;

            this.maxVariableAssigned = maxVariableAssigned;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[ ");

            for (int i = 0; i < maxVariableAssigned + 1; i++) {
                sb.append(assignment[i] ? i + " " : "");
            }
            // for (int i = maxVariableAssigned + 1; i < assignment.length; i++)
            // {
            // sb.append("* ");
            // }
            sb.append("],  Score=" + score);

            return sb.toString();
        }
    }
}
