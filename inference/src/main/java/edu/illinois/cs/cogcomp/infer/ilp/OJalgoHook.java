/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

import java.util.Arrays;

/**
 * A hook for the oj! Algorithms (ojAlgo), an Open Source Java code to do mathematics, linear
 * algebra and optimisation. For further information, see <a
 * href="http://ojalgo.org/">http://ojalgo.org/</a>
 */
public class OJalgoHook implements ILPSolver {

    // initially there are no variables in the model.
    private int numvars = 0;

    // initial number of constraints
    private int numConstraints = 0;

    private ExpressionsBasedModel model = new ExpressionsBasedModel();

    // Internal flag for keeping optimization state
    private boolean minimize = true;

    // internal variable for result of optimization
    private Optimisation.Result result;

    private boolean log = false;

    /**
     * Set bounds of variable in the specified position.
     *
     * @param colId position of the variable
     * @param lower domain lower bound
     * @param upper domain upper bound
     */
    public void setBounds(int colId, double lower, double upper) {
        if (upper == Double.POSITIVE_INFINITY)
            model.getVariable(colId).upper(null);
        else
            model.getVariable(colId).upper(upper);

        if (lower == Double.NEGATIVE_INFINITY)
            model.getVariable(colId).lower(null);
        else
            model.getVariable(colId).lower(lower);
    }

    /**
     * Set lower bound to unbounded (infinite)
     *
     * @param colId position of the variable
     */
    public void setUnboundUpperBound(int colId) {
        model.getVariable(colId).upper(null);
    }

    public void setUpperBound(int colId, double u) {
        model.getVariable(colId).upper(u);
    }

    /**
     * Set upper bound to unbounded (infinite)
     *
     * @param colId position of the variable
     */
    public void setUnboundLowerBound(int colId) {
        model.getVariable(colId).lower(null);
    }

    public void setLowerBound(int colId, double l) {
        model.getVariable(colId).lower(l);
    }

    /**
     * Set the column/variable as an integer variable
     *
     * @param colId position of the variable
     */
    public void setInteger(int colId) {
        model.getVariable(colId).integer(true);
    }

    /**
     * Set the column / variable as an binary integer variable
     *
     * @param colId position of the variable
     */
    public void setBinary(int colId) {
        model.getVariable(colId).binary();
    }

    /**
     * Set the column/variable as a float variable
     *
     * @param colId position of the variable
     */
    public void setFloat(int colId) {
        model.getVariable(colId).integer(false);
    }

    public void setMaximize(boolean d) {
        if (log)
            System.out.println("OJalgoHook: setMaximize(" + d + ")");
        if (d) {
            model.setMaximisation();
            minimize = false;
        } else {
            model.setMinimisation();
            minimize = true;
        }
    }

    public int addBooleanVariable(double c) {
        if (log)
            System.out.println("OJalgoHook: addBooleanVariable(c=" + c + ")");

        numvars++;
        Variable var = Variable.makeBinary(Integer.toString(numvars)).weight(c);
        model.addVariable(var);
        return numvars - 1;
    }

    public int addRealVariable(double c) {
        if (log)
            System.out.println("OJalgoHook: addRealVariable(c=" + c + ")");

        numvars++;
        Variable var = Variable.makeBinary(Integer.toString(numvars)).weight(c);
        model.addVariable(var);
        return numvars - 1;
    }

    public int addIntegerVariable(double c) {
        if (log)
            System.out.println("OJalgoHook: addIntegerVariable(c=" + c + ")");

        numvars++;
        Variable var = Variable.makeBinary(Integer.toString(numvars)).weight(c);
        model.addVariable(var);
        return numvars - 1;
    }

    /**
     * Adds a general, multi-valued discrete variable, which is implemented as a set of Boolean
     * variables, one per value of the discrete variable, with exactly one of those variables set
     * <code>true</code> at any given time.
     * */
    public int[] addDiscreteVariable(double[] c) {
        if (log) {
            System.out.print("OJalgoHook: addDiscreteVariable(");
            for (double w : c)
                System.out.print(w + ", ");
            System.out.println(")");
        }

        int[] varIndices = new int[c.length];
        int ind = 0;
        while (ind < c.length) {
            varIndices[ind] = addBooleanVariable(c[ind]);
            ind++;
        }

        if (log) {
            System.out.print("output indices = ");
            for (int idx : varIndices)
                System.out.print(idx + ", ");
            System.out.println();
        }

        // make sure only one of them is true
        double[] ones = new double[varIndices.length];
        Arrays.fill(ones, 1);
        if (log) {
            System.out.println("ones size = " + ones.length);
            System.out.println("varindices size = " + varIndices.length);
        }
        addEqualityConstraint(varIndices, ones, 1);

        return varIndices;
    }

    public void addEqualityConstraint(int[] i, double[] a, double b) {
        if (log) {
            System.out.print("OJalgoHook: addEqualityConstraint(");
            for (int idx = 0; idx < i.length; idx++)
                System.out.print("(i=" + i[idx] + ", a=" + a[idx] + ") ");
            System.out.println("b= " + b + ")");
        }

        numConstraints++;
        Expression constraint =
                model.addExpression("EqualityConstraint: " + Integer.toString(numConstraints));
        constraint.level(b);
        for (int ind = 0; ind < i.length; ind++) {
            constraint.set(i[ind], a[ind]);
        }
    }

    public void addGreaterThanConstraint(int[] i, double[] a, double b) {
        if (log) {
            System.out.print("OJalgoHook: addGreaterThanConstraint(");
            for (int idx = 0; idx < i.length; idx++)
                System.out.print("(i=" + i[idx] + ", a=" + a[idx] + ") ");
            System.out.println("b= " + b + ")");
        }

        numConstraints++;
        Expression constraint =
                model.addExpression("GreaterThanConstraint: " + Integer.toString(numConstraints));
        constraint.lower(b);
        for (int ind = 0; ind < i.length; ind++) {
            constraint.set(i[ind], a[ind]);
        }
    }

    public void addLessThanConstraint(int[] i, double[] a, double b) {
        if (log) {
            System.out.print("OJalgoHook: addLessThanConstraint(");
            for (int idx = 0; idx < i.length; idx++)
                System.out.print("(i=" + i[idx] + ", a=" + a[idx] + ") ");
            System.out.println("b= " + b + ")");
        }

        numConstraints++;
        Expression constraint =
                model.addExpression("LessThanConstraint: " + Integer.toString(numConstraints));
        constraint.upper(b);
        for (int ind = 0; ind < i.length; ind++) {
            constraint.set(i[ind], a[ind]);
        }
    }

    // Note: oJalgo does not support pre-solving!
    public boolean solve() throws Exception {
        if (log)
            System.out.println("OJalgoHook: solve() ");

        if (minimize)
            result = model.minimise();
        else
            result = model.maximise();

        if (log) {
            if (result.getState() == Optimisation.State.OPTIMAL)
                System.out.println("Good news!: the optimization solution is optimal! ");
            if (result.getState() == Optimisation.State.DISTINCT)
                System.out.println("Good news!: the optimization solution is unique! ");
            if (result.getState() == Optimisation.State.INFEASIBLE)
                System.out.println("Warning: the optimization is infeasible! ");
            if (result.getState() == Optimisation.State.UNBOUNDED)
                System.out.println("Warning: the optimization is unbounded! ");
            if (result.getState() == Optimisation.State.APPROXIMATE)
                System.out.println("Warning: the optimization is approximate! ");
        }
        return result.getState().isSuccess();
    }

    public boolean isSolved() {
        if (log)
            System.out.println("OJalgoHook: isSolved() ");
        return result != null && result.getState().isSuccess();
    }

    public boolean getBooleanValue(int index) {
        if (log)
            System.out.println("OJalgoHook: getBooleanValue(" + index + ") ");
        if (result.get(index).intValue() != 1 && result.get(index).intValue() != 0)
            System.out.println("Warning! The value of the binary variable is not 0/1! ");
        return (result.get(index).intValue() == 1);
    }

    public double getRealValue(int index) {
        if (log)
            System.out.println("OJalgoHook: getRealValue(" + index + ") ");
        if (result.get(index).intValue() != 1 && result.get(index).intValue() != 0)
            System.out.println("Warning! The value of the binary variable is not 0/1! ");
        return result.get(index).doubleValue();
    }

    public int getIntegerValue(int index) {
        if (log)
            System.out.println("OJalgoHook: getIntegerValue(" + index + ") ");
        if (result.get(index).intValue() != 1 && result.get(index).intValue() != 0)
            System.out.println("Warning! The value of the binary variable is not 0/1! ");
        return result.get(index).intValue();
    }

    public double objectiveValue() {
        if (log)
            System.out.println("OJalgoHook: objectiveValue()");
        return result.getValue();
    }

    @Override
    public double objectiveCoeff(int index) {
        return model.getVariable(index).getContributionWeight().doubleValue();
    }

    public void reset() {
        if (log)
            System.out.println("OJalgoHook: reset()");
        // no implementation
    }

    public void write(StringBuffer buffer) {
        if (log)
            System.out.println("OJalgoHook: write()");
        // no implementation
    }

    /**
     * Set a time limit for solver optimization. After the limit is reached the solver stops
     * running.
     *
     * @param limit the time limit
     */
    public void setTimeout(int limit) {
        assert (0 <= limit);
        model.options.time_abort = limit;
    }

    public void printModelInfo() {
        System.out.println(model.toString());
        System.out.println("objective: " + result.getValue());
    }
}
