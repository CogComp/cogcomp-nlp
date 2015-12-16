package edu.illinois.cs.cogcomp.nlp.agreement;

/**
 * This coefficient is computed by assuming that each label is equally likely. It was first
 * descriebd in (Bennett, Alpert and Goldstein, 1954).
 * <p/>
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
