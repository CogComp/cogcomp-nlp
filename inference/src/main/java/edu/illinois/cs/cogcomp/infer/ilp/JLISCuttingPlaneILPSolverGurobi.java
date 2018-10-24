/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import gurobi.GRB;
import gurobi.GRBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class JLISCuttingPlaneILPSolverGurobi implements ILPSolver {

    private final static Logger log = LoggerFactory
            .getLogger(JLISCuttingPlaneILPSolverGurobi.class);

    private static final int MAX_ITER = 100;
    private final ILPSolver baseSolver;
    boolean isSolved = false;
    boolean hasUpdate = false;
    boolean timedOut = false;

    int numVariables = 0;

    private IInstance x;

    private InferenceVariableLexManager variables;

    private final List<ILPConstraintGenerator> cuttingPlaneConstraints;

    private ILPOutputGenerator outputGenerator;

    public JLISCuttingPlaneILPSolverGurobi(ILPSolver baseSolver) {
        this.baseSolver = baseSolver;
        cuttingPlaneConstraints = new ArrayList<ILPConstraintGenerator>();
        hasUpdate = false;
        isSolved = false;

    }

    public void setOutputGenerator(ILPOutputGenerator outputGenerator) {
        this.outputGenerator = outputGenerator;
    }

    public void setInput(IInstance x) {
        this.x = x;
    }

    public void setVariableManager(InferenceVariableLexManager variables) {
        this.variables = variables;
    }

    @Override
    public int addBooleanVariable(double arg0) {
        isSolved = false;
        hasUpdate = true;

        numVariables++;

        return baseSolver.addBooleanVariable(arg0);
    }

    @Override
    public int addRealVariable(double arg0) {
        isSolved = false;
        hasUpdate = true;

        numVariables++;

        return baseSolver.addRealVariable(arg0);
    }


    @Override
    public int addIntegerVariable(double arg0) {
        isSolved = false;
        hasUpdate = true;

        numVariables++;

        return baseSolver.addIntegerVariable(arg0);
    }


    @Override
    public int[] addDiscreteVariable(double[] arg0) {
        isSolved = false;
        hasUpdate = true;

        numVariables += arg0.length;

        return baseSolver.addDiscreteVariable(arg0);
    }

    public void addEqualityConstraint(int[] arg0, double[] arg1, double arg2) {
        this.baseSolver.addEqualityConstraint(arg0, arg1, arg2);
        isSolved = false;
    }

    public void addGreaterThanConstraint(int[] arg0, double[] arg1, double arg2) {
        this.baseSolver.addGreaterThanConstraint(arg0, arg1, arg2);
        isSolved = false;
    }

    public void addLessThanConstraint(int[] arg0, double[] arg1, double arg2) {
        this.baseSolver.addLessThanConstraint(arg0, arg1, arg2);
        isSolved = false;
    }

    public void addCuttingPlaneConstraintGenerator(ILPConstraintGenerator c) {
        this.cuttingPlaneConstraints.add(c);
    }

    public boolean getBooleanValue(int arg0) {
        return baseSolver.getBooleanValue(arg0);
    }

    public double getRealValue(int arg0) {
        return baseSolver.getRealValue(arg0);
    }


    public int getIntegerValue(int arg0) {
        return baseSolver.getIntegerValue(arg0);
    }


    public boolean isSolved() {
        return isSolved;
    }

    public double objectiveValue() {
        return baseSolver.objectiveValue();
    }

    @Override
    public double objectiveCoeff(int index) {
        return 0;
    }

    public void reset() {

        baseSolver.reset();
        this.cuttingPlaneConstraints.clear();
        this.isSolved = false;
        this.hasUpdate = false;
        this.timedOut = false;
    }

    public void setMaximize(boolean arg0) {
        baseSolver.setMaximize(arg0);
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    @Override
    public boolean solve() throws Exception {
        assert numVariables > 0 : "No variables added!";

        long start = System.currentTimeMillis();
        int iteration = 0;
        int numConstraints = 0;

        boolean solved = baseSolver.solve();

        isSolved = false;

        if (baseSolver instanceof GurobiHook) {
            GurobiHook s = (GurobiHook) baseSolver;
            timedOut = s.isTimedOut();
            // s.noPresolveTimelimit = 0;
        }

        if (!solved) {
            isSolved = false;

        } else {

            List<List<ILPConstraint>> violated = new ArrayList<List<ILPConstraint>>();

            populateViolatedConstraints(violated);

            // XXX This is the wrong way to check if there are any violated constraints
            // boolean constraintsSatisfied = violated.size() == 0;
            boolean constraintsSatisfied = areConstraintsSatisfied(violated);

            while (!isSolved && !constraintsSatisfied && iteration < MAX_ITER) {
                for (List<ILPConstraint> cList : violated) {
                    for (ILPConstraint c : cList) {
                        addConstraint(c);
                        numConstraints++;
                    }
                }

                log.debug("Added {} constraints", numConstraints);

                solved = baseSolver.solve();

                populateViolatedConstraints(violated);

                // XXX This is the wrong way to check if there are any violated constraints
                // constraintsSatisfied = violated.size() == 0;
                constraintsSatisfied = areConstraintsSatisfied(violated);

                if (!solved) {
                    isSolved = false;
                    if (baseSolver instanceof GurobiHook) {
                        GurobiHook s = (GurobiHook) baseSolver;
                        timedOut = s.isTimedOut();

                        System.out.println("Status from gurobi model: ");
                        s.printModelStatus();

                        if (timedOut) {
                            log.error("Timed out!");
                        }

                        if (s.unsat()) {
                            log.error("Unsat!");
                            throw new RuntimeException("Unsat");
                        }
                    }

                    break;
                }

                iteration++;
            }

            if (!constraintsSatisfied)
                isSolved = false;
            else
                isSolved = true;
        }
        long end = System.currentTimeMillis();

        log.debug("Took {} ms and {} iterations. Added " + numConstraints + " constraints",
                (end - start), iteration);

        return isSolved;
    }

    private boolean areConstraintsSatisfied(List<List<ILPConstraint>> constraints) {
        for (List<ILPConstraint> list : constraints) {
            if (!list.isEmpty())
                return false;
        }
        return true;
    }

    private void populateViolatedConstraints(List<List<ILPConstraint>> violated) {
        violated.clear();

        IStructure y = outputGenerator.getOutput(baseSolver, variables, x);

        for (ILPConstraintGenerator c : this.cuttingPlaneConstraints) {

            // see if the constraint generator has anything to say about the input
            // upon which it operates. If not, then use the input to the inference,
            // namely x
            IInstance input = c.getConstraintInput();
            if (input == null)
                input = x;

            IStructure output = c.getConstraintOutput(y);

            List<ILPConstraint> cs = c.getViolatedILPConstraints(input, output, variables);
            violated.add(cs);
        }
    }

    public void printModelStatus() throws GRBException {
        if (baseSolver instanceof GurobiHook) {
            ((GurobiHook) baseSolver).printModelStatus();
        }
    }

    private void addConstraint(ILPConstraint c) {
        if (c.sense == GRB.EQUAL)
            baseSolver.addEqualityConstraint(c.vars, c.coeffs, c.rhs);
        else if (c.sense == GRB.GREATER_EQUAL)
            baseSolver.addGreaterThanConstraint(c.vars, c.coeffs, c.rhs);
        else if (c.sense == GRB.LESS_EQUAL)
            baseSolver.addLessThanConstraint(c.vars, c.coeffs, c.rhs);
        else
            assert false : c.sense;
    }

    public void write(StringBuffer arg0) {
        baseSolver.write(arg0);
    }
}
