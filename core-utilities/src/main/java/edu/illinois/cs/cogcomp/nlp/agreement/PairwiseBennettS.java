/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.agreement;

/**
 * This coefficient is computed by assuming that each label is equally likely. It was first
 * descriebd in (Bennett, Alpert and Goldstein, 1954).
 * <p>
 * <b>Note:</b> The coefficient S is problematic in many respects. The value of the coefficient can
 * be artifically increased by adding spurious categories. For more discussion, refer to (Artstein
 * and Poesio, 2008).
 *
 * @author Vivek Srikumar
 */
public class PairwiseBennettS extends PairwiseAgreement {

    public PairwiseBennettS(int numItems, int numLabels) {
        super(numItems, numLabels);
    }

    @Override
    public double getExpectedAgreement() {
        return 1.0 / numLabels;
    }

}
