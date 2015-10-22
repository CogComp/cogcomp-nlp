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
