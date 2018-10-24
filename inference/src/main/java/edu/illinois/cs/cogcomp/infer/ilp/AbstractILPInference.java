/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import edu.illinois.cs.cogcomp.infer.Inference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vivek Srikumar
 */
public abstract class AbstractILPInference<T> implements Inference<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractILPInference.class);

    public boolean debug;
    protected ILPSolver xmp;

    protected final ILPSolverFactory solverFactory;

    public AbstractILPInference(ILPSolverFactory solverFactory, boolean debug) {
        this.solverFactory = solverFactory;
        this.debug = debug;
    }

    public T runInference() throws Exception {
        xmp = solverFactory.getSolver();

        reset();

        InferenceVariableLexManager variableManager = new InferenceVariableLexManager();

        initializeSolver(xmp, variableManager);

        addVariables(xmp, variableManager);

        addConstraints(xmp, variableManager);

        xmp.setMaximize(true);

        if (debug) {
            printDebugInfo(xmp, variableManager);
        }

        if (variableManager.size() == 0) {
            log.error("No variables added!");
        }

        boolean solved = xmp.solve();

        if (debug) {
            if (xmp instanceof GurobiHook)
                ((GurobiHook) xmp).printSolution();
        }

        if (!solved) {
            boolean timedOut = false;
            if (xmp instanceof GurobiHook)
                timedOut = ((GurobiHook) xmp).isTimedOut();
            else if (xmp instanceof JLISCuttingPlaneILPSolverGurobi)
                timedOut = ((JLISCuttingPlaneILPSolverGurobi) xmp).isTimedOut();

            if (!timedOut) {
                if (debug)
                    printDebugInfo(xmp, variableManager);
                if (xmp instanceof JLISCuttingPlaneILPSolverGurobi)
                    ((JLISCuttingPlaneILPSolverGurobi) xmp).printModelStatus();

                throw new Exception("Unsat");
            } else {
                throw new Exception("Timed out!");
            }
        }

        return getOutput(xmp, variableManager);
    }

    protected void initializeSolver(ILPSolver xmp, InferenceVariableLexManager variableManager) {

    }

    protected void reset() {
        // xmp.reset();
    }


    protected abstract T getOutput(ILPSolver xmp, InferenceVariableLexManager variableManager)
            throws Exception;

    protected void printDebugInfo(ILPSolver xmp, InferenceVariableLexManager variableManager) {
        StringBuffer sb = new StringBuffer();
        xmp.write(sb);
        System.out.println(sb);
    }

    protected abstract void addConstraints(ILPSolver xmp,
            InferenceVariableLexManager variableManager);

    protected abstract void addVariables(ILPSolver xmp, InferenceVariableLexManager variableManager);

    protected void addConstraint(ILPSolver xmp, ILPConstraint c) {
        if (c.sense == ILPConstraint.EQUAL)
            addEqualityConstraint(xmp, c.vars, c.coeffs, c.rhs);
        else if (c.sense == ILPConstraint.GREATER_THAN)
            addGreaterThanConstraint(xmp, c.vars, c.coeffs, c.rhs);
        else if (c.sense == ILPConstraint.LESS_THAN)
            addLessThanConstraint(xmp, c.vars, c.coeffs, c.rhs);
    }

    protected void addEqualityConstraint(ILPSolver xmp, int[] vars, double[] coefs, double rhs) {
        if (debug) {
            for (int i = 0; i < vars.length; i++) {
                System.out.print(coefs[i] + " x_" + vars[i] + " + ");
            }
            System.out.println(" = " + rhs);
        }
        xmp.addEqualityConstraint(vars, coefs, rhs);
    }

    protected void addGreaterThanConstraint(ILPSolver xmp, int[] vars, double[] coefs, double rhs) {
        if (debug) {
            for (int i = 0; i < vars.length; i++) {
                System.out.print(coefs[i] + " x_" + vars[i] + " + ");
            }
            System.out.println(" >= " + rhs);
        }

        xmp.addGreaterThanConstraint(vars, coefs, rhs);
    }

    protected void addLessThanConstraint(ILPSolver xmp, int[] vars, double[] coefs, double rhs) {
        if (debug) {
            for (int i = 0; i < vars.length; i++) {
                System.out.print(coefs[i] + " x_" + vars[i] + " + ");
            }
            System.out.println(" <= " + rhs);
        }

        xmp.addLessThanConstraint(vars, coefs, rhs);
    }
}
