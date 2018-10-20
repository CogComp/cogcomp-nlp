/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

/**
 * Classes that implement this interface contain implementations of algorithms that solve Integer
 * Linear Programming problems.
 *
 * @author Nick Rizzolo
 **/
public interface ILPSolver {
    /** A possible setting for {@link #verbosity}. */
    public static final int VERBOSITY_NONE = 0;
    /** A possible setting for {@link #verbosity}. */
    public static final int VERBOSITY_LOW = 1;
    /** A possible setting for {@link #verbosity}. */
    public static final int VERBOSITY_HIGH = 2;

    /**
     * Sets the direction of the objective function.
     *
     * @param d <code>true</code> if the objective function is to be maximized.
     **/
    public void setMaximize(boolean d);


    /**
     * Adds a new Boolean variable (an integer variable constrained to take either the value 0 or
     * the value 1) with the specified coefficient in the objective function to the problem.
     *
     * @param c The objective function coefficient for the new Boolean variable.
     * @return The indexes of the created variable.
     **/
    public int addBooleanVariable(double c);

    /**
     * Adds a new Real variable with the specified coefficient in the objective function to the
     * problem.
     *
     * @param c The objective function coefficient for the new Real variable.
     * @return The indexes of the created variable.
     **/
    int addRealVariable(double c);

    /**
     * Adds a new Integer variable with the specified coefficient in the objective function to the
     * problem.
     *
     * @param c The objective function coefficient for the new Integer variable.
     * @return The indexes of the created variable.
     **/
    int addIntegerVariable(double c);

    /**
     * Adds a general, multi-valued discrete variable, which is implemented as a set of Boolean
     * variables, one per value of the discrete variable, with exactly one of those variables set
     * <code>true</code> at any given time.
     *
     * @param c The objective function coefficients for the new Boolean variables.
     * @return The indexes of the newly created variables.
     **/
    public int[] addDiscreteVariable(double[] c);

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
    public void addEqualityConstraint(int[] i, double[] a, double b);


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
    public void addGreaterThanConstraint(int[] i, double[] a, double b);


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
    public void addLessThanConstraint(int[] i, double[] a, double b);


    /**
     * Solves the ILP problem, saving the solution internally. This method may throw an exception if
     * something doesn't go right.
     **/
    public boolean solve() throws Exception;


    /**
     * Tests whether the problem represented by this <code>ILPSolver</code> instance has been solved
     * already.
     **/
    public boolean isSolved();


    /**
     * When the problem has been solved, use this method to retrieve the value of any Boolean
     * inference variable. The result of this method is undefined when the problem has not yet been
     * solved.
     *
     * @param index The index of the variable whose value is requested.
     * @return The value of the variable.
     **/
    public boolean getBooleanValue(int index);

    /**
     * When the problem has been solved, use this method to retrieve the value of any Integer
     * inference variable. The result of this method is undefined when the problem has not yet been
     * solved.
     *
     * @param index The index of the variable whose value is requested.
     * @return The value of the variable.
     **/
    int getIntegerValue(int index);

    /**
     * When the problem has been solved, use this method to retrieve the value of any Real inference
     * variable. The result of this method is undefined when the problem has not yet been solved.
     *
     * @param index The index of the variable whose value is requested.
     * @return The value of the variable.
     **/
    double getRealValue(int index);

    /**
     * When the problem has been solved, use this method to retrieve the value of the objective
     * function at the solution. The result of this method is undefined when the problem has not yet
     * been solved. If the problem had no feasible solutions, negative (positive, respectively)
     * infinity will be returned if maximizing (minimizing).
     *
     * @return The value of the objective function at the solution.
     **/
    public double objectiveValue();


    /**
     * The coefficient of the variable in the objective function.
     */
    public double objectiveCoeff(int index);

    /**
     * This method clears the all constraints and variables out of the ILP solver's problem
     * representation, bringing the <code>ILPSolver</code> instance back to the state it was in when
     * first constructed.
     **/
    public void reset();


    /**
     * Creates a textual representation of the ILP problem in an algebraic notation.
     *
     * @param buffer The created textual representation will be appended here.
     **/
    public void write(StringBuffer buffer);
}
