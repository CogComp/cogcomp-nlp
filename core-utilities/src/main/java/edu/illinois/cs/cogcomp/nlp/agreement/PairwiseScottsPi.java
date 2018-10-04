/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.agreement;

/**
 * This assumes that all annotators have the expected distribution for random assignment of labels.
 *
 * @author Vivek Srikumar
 */
public class PairwiseScottsPi extends PairwiseAgreement {

    public PairwiseScottsPi(int numItems, int numLabels) {
        super(numItems, numLabels);
    }

    @Override
    public double getExpectedAgreement() {
        double Ae = 0;
        for (int i = 0; i < this.numLabels; i++) {
            Ae += (this.nk[i] * this.nk[i]);
        }

        Ae /= (4 * this.numItems * this.numItems);

        return Ae;
    }

}
