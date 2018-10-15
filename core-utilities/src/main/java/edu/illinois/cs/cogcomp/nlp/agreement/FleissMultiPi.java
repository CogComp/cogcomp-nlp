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
public class FleissMultiPi extends AnnotatorAgreement {

    public FleissMultiPi(int numAnnotators, int numItems, int numLabels) {
        super(numAnnotators, numItems, numLabels);
    }

    @Override
    public double getExpectedAgreement() {
        double Ae = 0;
        for (int k = 0; k < numLabels; k++) {
            Ae += (this.nk[k] * this.nk[k]);
        }

        Ae /= (this.numItems * this.numAnnotators * this.numItems * this.numAnnotators);

        return Ae;
    }

    @Override
    public double getObservedAgreement() {
        double Ao = 0;
        for (int i = 0; i < numItems; i++) {
            for (int k = 0; k < numLabels; k++) {
                Ao += (nik[i][k] * (nik[i][k] - 1));
            }
        }

        Ao /= (numItems * numAnnotators * (numAnnotators - 1));
        return Ao;
    }
}
