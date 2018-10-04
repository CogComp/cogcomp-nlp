/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.collocations;

/**
 * Refer Dunning 1993
 *
 * @author Vivek Srikumar
 *         <p>
 *         November 14, 2008
 */
public abstract class LogLikelihoodCollocation implements CollocationComputer {

    /**
     * Compute the collocation using a hypothesis test, like Dunning 1993
     */
    public double getCollocationScore(String left, String right) throws Exception {
        double N = this.getTotalNumberOfTokens();

        double c1 = this.getCount(left);
        double c2 = this.getCount(right);

        double c12 = this.getCount(left + " " + right);

        double p = c2 / N;
        double p1 = c12 / c1;

        double p2 = (c2 - c12) / (N - c1);


        return ll(c12, c1, p) + ll(c2 - c12, N - c1, p) - ll(c12, c1, p1)
                - ll(c2 - c12, N - c1, p2);

    }

    private double ll(double k, double n, double p) {
        return k * Math.log(p) + (n - k) * Math.log(1 - p);
    }

    public abstract double getTotalNumberOfTokens();

}
