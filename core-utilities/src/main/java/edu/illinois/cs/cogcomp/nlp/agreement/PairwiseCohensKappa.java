/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.agreement;

/**
 * @author Vivek Srikumar
 */
public class PairwiseCohensKappa extends PairwiseAgreement {

    public PairwiseCohensKappa(int numItems, int numLabels) {
        super(numItems, numLabels);
    }

    @Override
    public double getExpectedAgreement() {
        double Ae = 0;

        for (int i = 0; i < numLabels; i++) {
            Ae += (this.nck[0][i] * this.nck[1][i]);
        }

        Ae /= (this.numItems * this.numItems);
        return Ae;
    }
}
