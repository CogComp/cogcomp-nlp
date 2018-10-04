/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.stats;

import java.io.Serializable;

/**
 * Statistics of one variable.
 *
 * @author Vivek Srikumar
 */
public class OneVariableStats implements Serializable {

    private static final long serialVersionUID = -2036657383899693962L;
    private double sigmax;
    private int num;
    private double sigmax2;

    private double min, max;

    public void reset() {
        sigmax = 0;
        num = 0;
        sigmax2 = 0;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }

    public OneVariableStats() {
        reset();
    }

    public void add(double d) {
        num++;
        sigmax += d;
        sigmax2 += (d * d);

        if (d > max) {
            max = d;
        }
        if (d < min) {
            min = d;
        }
    }

    public double mean() {
        if (num == 0) {
            return 0;
        }
        return (sigmax) / num;
    }

    /**
     * Calculate the Population Standard Deviation. $\sqrt(\frac{\sum(X-M)^2}{n})$
     *
     * @return Population Standard Deviation
     */
    public double std() {
        if (min == max || num == 0) {
            return 0;
        }
        double m = mean();
        return Math.sqrt(sigmax2 / num - m * m);
    }

    /**
     * Calculate the standard error of the mean (SEM): the standard deviation of the sample-mean's
     * estimate of a population mean.
     *
     * @return The SEM, which is {@link #std()} divided by the square root of the sample size.
     */
    public double stdErr() {
        if (num == 0) {
            return 0;
        }
        return std() / Math.sqrt(num);
    }

    public double min() {
        return this.min;
    }

    public double max() {
        return this.max;
    }
}
