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
