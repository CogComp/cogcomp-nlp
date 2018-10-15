/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import edu.illinois.cs.cogcomp.core.math.MathUtilities;
import gurobi.GRB;

/**
 * @author Vivek Srikumar
 */
public class ILPConstraint {

    public final static char EQUAL = '=';
    public final static char LESS_THAN = '<';
    public final static char GREATER_THAN = '>';

    private static int counter = 0;

    public final int[] vars;
    public final double[] coeffs;
    public final double rhs;
    public final char sense;

    public final String name;

    final int maximumVariableId;

    public ILPConstraint(int[] vars, double[] coefs, double rhs, char sense) {
        this.name = "c" + (counter++);

        this.vars = vars;
        this.coeffs = coefs;
        this.rhs = rhs;
        this.sense = sense;
        maximumVariableId = MathUtilities.max(vars).getSecond();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < vars.length; i++) {
            if (coeffs[i] > 0)
                sb.append(" + " + coeffs[i] + " x_" + vars[i]);
            else if (coeffs[i] < 0)
                sb.append(" " + coeffs[i] + " x_" + vars[i]);
        }

        sb.append(" " + sense + " " + rhs);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + java.util.Arrays.hashCode(coeffs);
        result = prime * result + java.util.Arrays.hashCode(vars);
        long temp;
        temp = Double.doubleToLongBits(rhs);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + sense;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ILPConstraint other = (ILPConstraint) obj;
        if (!java.util.Arrays.equals(coeffs, other.coeffs))
            return false;
        if (!java.util.Arrays.equals(vars, other.vars))
            return false;
        if (Double.doubleToLongBits(rhs) != Double.doubleToLongBits(other.rhs))
            return false;
        if (sense != other.sense)
            return false;
        return true;
    }

    public void addToILP(ILPSolver xmp) {
        if (this.sense == GRB.EQUAL) {
            xmp.addEqualityConstraint(vars, coeffs, rhs);
        } else if (this.sense == GRB.GREATER_EQUAL) {
            xmp.addGreaterThanConstraint(vars, coeffs, rhs);
        } else if (this.sense == GRB.LESS_EQUAL) {
            xmp.addLessThanConstraint(vars, coeffs, rhs);
        } else
            throw new RuntimeException("Invalid constraint of type: " + this.sense);

    }

}
