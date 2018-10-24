/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import edu.illinois.cs.cogcomp.core.datastructures.vectors.DVector;
import edu.illinois.cs.cogcomp.core.datastructures.vectors.OVector;
import gurobi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This is an interface to the <a href="http://www.gurobi.com">Gurobi Optimizer</a>. Make sure the
 * jar file is on your <code>CLASSPATH</code> and that the Gurobi libraries are installed
 * appropriately on your system before attempting to compile and use this class.
 *
 * @author Nick Rizzolo
 **/
public class GurobiHook implements ILPSolver {

    private int timelimit = 200;

    // when solving without presolve how long should we take before
    // timeout
    private int noPresolveTimelimit = -1;

    private static final Logger logger = LoggerFactory.getLogger(GurobiHook.class);

    /** Prints an error message and exits the JVM. */
    protected static void handleException(GRBException e) {
        System.out.println("Gurobi error " + e.getErrorCode() + ": " + e.getMessage());
        e.printStackTrace();
        System.exit(-1);
    }

    /** The model to be optimized will be associated with this environment. */
    protected GRBEnv environment;
    /** The model to be optimized. */
    protected GRBModel model;
    /** The model's decision variables. */
    protected OVector variables;
    /** Contains all of the Gurobi SOS objects created for the model. */
    protected OVector SOSes;
    /** whether the optimization has been timed out */
    private boolean isTimedOut;
    /** whether the optimization has been unsatisfactory */
    private boolean unsat;
    /**
     * Whether or not the <code>GRBModel.update()</code> method needs to be called before adding
     * more constraints.
     **/
    protected boolean needsUpdate;
    /** Whether or not the model has been solved. */
    protected boolean isSolved;
    /**
     * Verbosity level. {@link ILPSolver#VERBOSITY_NONE} produces no incidental output. If set to
     * {@link ILPSolver#VERBOSITY_LOW}, only variable and constraint counts are reported on
     * <code>STDOUT</code>. If set to {@link ILPSolver#VERBOSITY_HIGH}, a textual representation of
     * the entire optimization problem is also generated on <code>STDOUT</code>.
     **/
    protected int verbosity;
    /**
     * The coefficients of the variables in the objective function. This is redundant memory, and
     * it's only being stored in the event that someone wants to call {@link #write(StringBuffer)}.
     * Once we get Gurobi 4.0, we can discard of it.
     **/
    protected DVector objectiveCoefficients;

    public void setTimeoutLimit(int limit) {
        timelimit = limit;
    }

    /** Create a new Gurobi hook with the default environment parameters. */
    public GurobiHook() {
        this(ILPSolver.VERBOSITY_NONE);
    }

    /**
     * Create a new Gurobi hook with the default environment parameters.
     *
     * @param v Setting for the {@link #verbosity} level.
     **/
    public GurobiHook(int v) {
        try {
            environment = new GRBEnv();
            environment.set(GRB.IntParam.OutputFlag, 0); // no output

            // how many threads can we use?
            // environment.set(GRB.IntParam.Threads,
            // Math.min(8, Runtime.getRuntime().availableProcessors()));

            // dump big stuff to filespace
            // environment.set(GRB.DoubleParam.NodefileStart, 0.5);

            // environment.set(GRB.DoubleParam.MIPGap, 1e-10);

            // environment.set(GRB.IntParam.Presolve, 0);

        } catch (GRBException e) {
            handleException(e);
        }
        verbosity = v;
        reset();
    }

    /**
     * Create a new Gurobi hook with the specified environment.
     *
     * @param env An environment containing user-specified parameters.
     **/
    public GurobiHook(GRBEnv env) {
        this(env, ILPSolver.VERBOSITY_NONE);
    }

    /**
     * Create a new Gurobi hook with the specified environment.
     *
     * @param env An environment containing user-specified parameters.
     * @param v Setting for the {@link #verbosity} level.
     **/
    public GurobiHook(GRBEnv env, int v) {
        environment = env;
        verbosity = v;
        reset();
    }


    public boolean isTimedOut() {
        return isTimedOut;
    }

    public boolean unsat() {
        return unsat;
    }

    /**
     * This method clears the all constraints and variables out of the ILP solver's problem
     * representation, bringing the <code>ILPSolver</code> instance back to the state it was in when
     * first constructed.
     **/
    public void reset() {
        try {
            model = new GRBModel(environment);
        } catch (GRBException e) {
            handleException(e);
        }
        variables = new OVector();
        SOSes = new OVector();
        objectiveCoefficients = new DVector();
        needsUpdate = isSolved = false;
    }

    /**
     * Sets the direction of the objective function.
     *
     * @param d <code>true</code> if the objective function is to be maximized.
     **/
    public void setMaximize(boolean d) {
        try {
            model.set(GRB.IntAttr.ModelSense, d ? -1 : 1);
        } catch (GRBException e) {
            handleException(e);
        }
    }

    /**
     * Adds a new Boolean variable (an integer variable constrained to take either the value 0 or
     * the value 1) with the specified coefficient in the objective function to the problem.
     *
     * @param c The objective function coefficient for the new Boolean variable.
     * @return The index of the created variable.
     **/
    public int addBooleanVariable(double c) {
        int id = variables.size();
        try {
            variables.add(model.addVar(0, 1, c, GRB.BINARY, "x_" + id));
        } catch (GRBException e) {
            handleException(e);
        }
        // TODO: delete the line below once we get Gurobi 4.0
        objectiveCoefficients.add(c);
        needsUpdate = true;
        return id;
    }

    public int addIntegerVariable(double c) {
        int id = variables.size();
        try {
            variables.add(model.addVar(-GRB.INFINITY, GRB.INFINITY, c, GRB.INTEGER, "x_" + id));
        } catch (GRBException e) {
            handleException(e);
        }
        // TODO: delete the line below once we get Gurobi 4.0
        objectiveCoefficients.add(c);
        needsUpdate = true;
        return id;
    }

    public int addRealVariable(double c) {
        int id = variables.size();
        try {
            variables.add(model.addVar(-GRB.INFINITY, GRB.INFINITY, c, GRB.CONTINUOUS, "x_" + id));
        } catch (GRBException e) {
            handleException(e);
        }
        // TODO: delete the line below once we get Gurobi 4.0
        objectiveCoefficients.add(c);
        needsUpdate = true;
        return id;
    }


    /**
     * Adds a general, multi-valued discrete variable, which is implemented as a set of Boolean
     * variables, one per value of the discrete variable, with exactly one of those variables set
     * <code>true</code> at any given time.
     *
     * @param c The objective function coefficients for the new Boolean variables.
     * @return The indexes of the newly created variables.
     **/
    public int[] addDiscreteVariable(double[] c) {
        int[] result = new int[c.length];
        for (int i = 0; i < c.length; ++i)
            result[i] = addBooleanVariable(c[i]);

        double[] w = new double[c.length];
        Arrays.fill(w, 1);
        addGreaterThanConstraint(result, w, 1);
        try {
            SOSes.add(model.addSOS(idsToVariables(result), w, GRB.SOS_TYPE1));
        } catch (GRBException e) {
            handleException(e);
        }
        return result;
    }

    /**
     * Adds a new constraint to the problem with the specified type. This method is called by all
     * the other <code>add*Constraint()</code> methods.
     *
     * @param i The indexes of the variables with non-zero coefficients.
     * @param a The coefficients of the variables with the given indexes.
     * @param b The new constraint will enforce (in)equality with this constant.
     * @param t The type of linear inequality constraint to add.
     **/
    protected void addConstraint(int[] i, double[] a, double b, char t) {
        if (needsUpdate) {
            try {
                model.update();
            } catch (GRBException e) {
                handleException(e);
            }
            needsUpdate = false;
        }

        try {
            int constraints = model.get(GRB.IntAttr.NumConstrs);
            model.addConstr(makeLinearExpression(i, a), t, b, "c_" + constraints);
        } catch (GRBException e) {
            handleException(e);
        }
    }


    /**
     * Adds a new fixed constraint to the problem. The two array arguments must be the same length,
     * as their elements correspond to each other. Variables whose coefficients are zero need not be
     * mentioned. Variables that are mentioned must have previously been added via
     * {@link #addBooleanVariable(double)} or {@link #addDiscreteVariable(double[])}. The resulting
     * constraint has the form: <blockquote> <code>x<sub>i</sub> * a = b</code> </blockquote> where
     * <code>x<sub>i</sub></code> represents the inference variables whose indexes are contained in
     * the array <code>i</code> and <code>*</code> represents dot product.
     *
     * @param i The indexes of the variables with non-zero coefficients.
     * @param a The coefficients of the variables with the given indexes.
     * @param b The new constraint will enforce equality with this constant.
     **/
    public void addEqualityConstraint(int[] i, double[] a, double b) {
        addConstraint(i, a, b, GRB.EQUAL);
    }


    /**
     * Adds a new lower bounded constraint to the problem. The two array arguments must be the same
     * length, as their elements correspond to each other. Variables whose coefficients are zero
     * need not be mentioned. Variables that are mentioned must have previously been added via
     * {@link #addBooleanVariable(double)} or {@link #addDiscreteVariable(double[])}. The resulting
     * constraint has the form: <blockquote> <code>x<sub>i</sub> * a &gt;= b</code> </blockquote>
     * where <code>x<sub>i</sub></code> represents the inference variables whose indexes are
     * contained in the array <code>i</code> and <code>*</code> represents dot product.
     *
     * @param i The indexes of the variables with non-zero coefficients.
     * @param a The coefficients of the variables with the given indexes.
     * @param b The lower bound for the new constraint.
     **/
    public void addGreaterThanConstraint(int[] i, double[] a, double b) {
        addConstraint(i, a, b, GRB.GREATER_EQUAL);
    }


    /**
     * Adds a new upper bounded constraint to the problem. The two array arguments must be the same
     * length, as their elements correspond to each other. Variables whose coefficients are zero
     * need not be mentioned. Variables that are mentioned must have previously been added via
     * {@link #addBooleanVariable(double)} or {@link #addDiscreteVariable(double[])}. The resulting
     * constraint has the form: <blockquote> <code>x<sub>i</sub> * a &lt;= b</code> </blockquote>
     * where <code>x<sub>i</sub></code> represents the inference variables whose indexes are
     * contained in the array <code>i</code> and <code>*</code> represents dot product.
     *
     * @param i The indexes of the variables with non-zero coefficients.
     * @param a The coefficients of the variables with the given indexes.
     * @param b The upper bound for the new constraint.
     **/
    public void addLessThanConstraint(int[] i, double[] a, double b) {
        addConstraint(i, a, b, GRB.LESS_EQUAL);
    }


    public void printSolution() throws GRBException {
        GRBVar[] varsAll = model.getVars();
        for (int i = 0; i < varsAll.length; i++) {
            GRBVar var = varsAll[i];
            System.out.println("x_" + i + "\t" + var.get(GRB.StringAttr.VarName) + "\t"
                    + var.get(GRB.DoubleAttr.Obj) + "\t" + var.get(GRB.DoubleAttr.X));
        }
    }

    /**
     * Solves the ILP problem, saving the solution internally.
     *
     * @return <code>true</code> iff a solution was found successfully.
     **/
    public boolean solve() throws Exception {
        if (verbosity > ILPSolver.VERBOSITY_NONE) {
            System.out.println("  variables: " + model.get(GRB.IntAttr.NumVars));
            System.out.println("  constraints: " + model.get(GRB.IntAttr.NumConstrs));
        }

        assert model.getVars().length > 0 : "Model has no variables!";

        // here we first attempt to presolve (some models can be solved quicker
        // directly than the time it takes to presolve them)

        isTimedOut = false;
        boolean more = false; // should we perform more solving?
        long start = System.currentTimeMillis();
        // XXX: if you always see your solve time > noPresolveTimelimit then you
        // probably want to set noPresolveTimelimit to 0 as you are always
        // getting into the second solving loop.
        if (noPresolveTimelimit > 0) {
            environment.set(GRB.IntParam.Presolve, 0);
            environment.set(GRB.DoubleParam.TimeLimit, noPresolveTimelimit);
            model.optimize();
            more = model.get(GRB.IntAttr.Status) == GRB.TIME_LIMIT;
        }

        // we may have timed out because noPresolveTimelimit is usually small
        // we can solve big hard models quicker with presolve
        if (more) {
            logger.debug("Trying presolve version of problem");
            environment.set(GRB.IntParam.Presolve, -1); // auto presolve settings
            environment.set(GRB.DoubleParam.TimeLimit, timelimit);
            model.optimize();
        }

        long end = System.currentTimeMillis();
        logger.debug("Gurobi took {} ms and reported {} s", (end - start),
                model.get(GRB.DoubleAttr.Runtime));

        if (verbosity == ILPSolver.VERBOSITY_HIGH) {
            StringBuffer buffer = new StringBuffer();
            write(buffer);
            System.out.print(buffer);
        }

        model.optimize();
        int status = model.get(GRB.IntAttr.Status);
        isSolved = status == GRB.OPTIMAL || status == GRB.SUBOPTIMAL;

        unsat = false;
        if (!isSolved) {
            int statusNotSolved = model.get(GRB.IntAttr.Status);
            logger.info("Gurobi returned with status code: {}", statusNotSolved);

            if (status == GRB.TIME_LIMIT) {
                isTimedOut = true;
            }

            if (status == GRB.INFEASIBLE) {
                logger.info("Infeasable constraint set!");
                model.computeIIS();
                model.write("gurobi.ilp");
                model.write("gurobi.lp");
                logger.info("ILP information written to gurobi.ilp. Full LP written to gurobi.lp");
                unsat = true;
            }
        }

        return isSolved;
    }

    public void printModelStatus() throws GRBException {
        int status = model.get(GRB.IntAttr.Status);
        System.out.println("Model status: " + status);

        if (status == GRB.INFEASIBLE) {
            System.out.println("INFEASIBLE");
        }
    }

    /**
     * Tests whether the problem represented by this <code>ILPSolver</code> instance has been solved
     * already.
     **/
    public boolean isSolved() {
        return isSolved;
    }

    /**
     * When the problem has been solved, use this method to retrieve the value of any Boolean
     * inference variable. The result of this method is undefined when the problem has not yet been
     * solved.
     *
     * @param index The index of the variable whose value is requested.
     * @return The value of the variable.
     **/
    public boolean getBooleanValue(int index) {
        if (!isSolved)
            return false;
        try {
            double x = ((GRBVar) variables.get(index)).get(GRB.DoubleAttr.X);
            return x > 0.5;
        } catch (GRBException e) {
            handleException(e);
        }
        return false;
    }

    @Override
    public int getIntegerValue(int index) {
        if (!isSolved)
            return 0;
        try {
            double x = ((GRBVar) variables.get(index)).get(GRB.DoubleAttr.X);
            return (int) Math.round(x);
        } catch (GRBException e) {
            handleException(e);
        }
        return 0;
    }

    @Override
    public double getRealValue(int index) {
        if (!isSolved)
            return 0.0;
        try {
            return ((GRBVar) variables.get(index)).get(GRB.DoubleAttr.X);
        } catch (GRBException e) {
            handleException(e);
        }
        return 0.0;
    }

    public int getVariableSize() {
        return variables.size();
    }

    public OVector getVariables() {
        return variables;
    }

    /**
     * When the problem has been solved, use this method to retrieve the value of the objective
     * function at the solution. The result of this method is undefined when the problem has not yet
     * been solved. If the problem had no feasible solutions, negative (positive, respectively)
     * infinity will be returned if maximizing (minimizing).
     *
     * @return The value of the objective function at the solution.
     **/
    public double objectiveValue() {
        try {
            if (isSolved)
                return model.get(GRB.DoubleAttr.ObjVal);
            int status = model.get(GRB.IntAttr.Status);
            if (status == GRB.INFEASIBLE || status == GRB.INF_OR_UNBD || status == GRB.UNBOUNDED)
                return model.get(GRB.IntAttr.ModelSense) == -1 ? Double.NEGATIVE_INFINITY
                        : Double.POSITIVE_INFINITY;
        } catch (GRBException e) {
            handleException(e);
        }
        return 0;
    }

    @Override
    public double objectiveCoeff(int index) {
        return 0;
    }


    /**
     * Given an array of variable indexes, this method returns the corresponding Gurobi variable
     * objects in an array.
     *
     * @param ids The array of variable indexes.
     * @return The corresponding Gurobi variable objects.
     **/
    protected GRBVar[] idsToVariables(int[] ids) {
        GRBVar[] result = new GRBVar[ids.length];
        for (int i = 0; i < ids.length; i++)
            result[i] = (GRBVar) variables.get(ids[i]);
        return result;
    }


    /**
     * Creates a Gurobi linear expression object representing the dot product of the variables with
     * the specified indexes and the specified coefficients.
     *
     * @param ids The indexes of the variables.
     * @param c The corresponding coefficients.
     * @return A Gurobi linear expression representing the dot product.
     **/
    protected GRBLinExpr makeLinearExpression(int[] ids, double[] c) {
        try {
            GRBLinExpr expr = new GRBLinExpr();
            expr.addTerms(c, idsToVariables(ids));
            return expr;
        } catch (GRBException e) {
            handleException(e);
        }
        return null;
    }


    /**
     * Creates a textual representation of the ILP problem in an algebraic notation.
     *
     * @param buffer The created textual representation will be appended here.
     **/
    public void write(StringBuffer buffer) {
        try {
            model.update();
            if (model.get(GRB.IntAttr.ModelSense) == -1)
                buffer.append("max");
            else
                buffer.append("min");

            // Using this bit of code until we get Gurobi 4.0 or higher.
            for (int i = 0; i < objectiveCoefficients.size(); ++i) {
                double c = objectiveCoefficients.get(i);
                buffer.append(" ");
                if (c >= 0)
                    buffer.append("+");
                buffer.append(c);
                buffer.append(" x_");
                buffer.append(i);
            }

            /*
             * This code should work once we have Gurobi 4.0 or higher. Then we don't have to
             * redundantly store objectiveCoefficients. GRBLinExpr objective = (GRBLinExpr)
             * model.getObjective(); int objectiveSize = objective.size(); for (int i = 0; i <
             * objectiveSize; ++i) { double c = objective.getCoeff(i); buffer.append(" "); if (c >=
             * 0) buffer.append("+"); buffer.append(c); buffer.append(" ");
             * buffer.append(objective.getVar(i).get(GRB.StringAttr.VarName)); }
             */

            buffer.append("\n");

            int SOSesSize = SOSes.size();
            for (int i = 0; i < SOSesSize; ++i) {
                GRBSOS sos = (GRBSOS) SOSes.get(i);
                int[] type = new int[1];
                int size = model.getSOS(sos, null, null, type);
                GRBVar[] sosVariables = new GRBVar[size];
                model.getSOS(sos, sosVariables, new double[size], type);

                buffer.append(" atmost 1 of (x in {");
                for (int j = 0; j < size; ++j) {
                    buffer.append(sosVariables[j].get(GRB.StringAttr.VarName));
                    if (j + 1 < size)
                        buffer.append(", ");
                }
                buffer.append("}) (x > 0)\n");
            }

            GRBConstr[] constraints = model.getConstrs();
            for (int i = 0; i < constraints.length; ++i) {
                GRBLinExpr row = model.getRow(constraints[i]);
                int rowSize = row.size();
                buffer.append(" ");

                for (int j = 0; j < rowSize; ++j) {
                    double c = row.getCoeff(j);
                    buffer.append(" ");
                    if (c >= 0)
                        buffer.append("+");
                    buffer.append(c);
                    buffer.append(" ");
                    buffer.append(row.getVar(j).get(GRB.StringAttr.VarName));
                }

                char type = constraints[i].get(GRB.CharAttr.Sense);
                if (type == GRB.LESS_EQUAL)
                    buffer.append(" <= ");
                else if (type == GRB.GREATER_EQUAL)
                    buffer.append(" >= ");
                else
                    buffer.append(" = ");

                buffer.append(constraints[i].get(GRB.DoubleAttr.RHS));
                buffer.append("\n");
            }
        } catch (GRBException e) {
            handleException(e);
        }
    }
}
