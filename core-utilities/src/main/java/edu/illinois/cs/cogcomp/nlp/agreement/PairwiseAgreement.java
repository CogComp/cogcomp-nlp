/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.agreement;

import edu.illinois.cs.cogcomp.core.utilities.AvoidUsing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vivek Srikumar
 */
public abstract class PairwiseAgreement extends AnnotatorAgreement {
    private static Logger logger = LoggerFactory.getLogger(PairwiseAgreement.class);

    public PairwiseAgreement(int numItems, int numLabels) {
        this(2, numItems, numLabels);
    }

    @AvoidUsing(reason = "Superseded by the other agreement methods in the package")
    public PairwiseAgreement(int numAnnotators, int numItems, int numLabels) {
        super(numAnnotators, numItems, numLabels);
        if (numAnnotators != 2)
            throw new IllegalArgumentException(
                    "Number of annotators is not two for an object of class "
                            + this.getClass().toString());
    }

    @Override
    public double getObservedAgreement() {
        double Ao = 0;
        for (int i = 0; i < this.numItems; i++) {
            if (annotation[i][0] == annotation[i][1])
                Ao++;
        }

        Ao /= this.numItems;
        return Ao;
    }

    public void printAgreementMatrix() {
        int[][] matrix = new int[this.numLabels][this.numLabels];
        for (int i = 0; i < this.numItems; i++) {
            matrix[this.annotation[i][0]][this.annotation[i][1]]++;
        }

        for (int i = 0; i < numLabels; i++) {
            for (int j = 0; j < numLabels; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        PairwiseAgreement agreementCalculator = new PairwiseCohensKappa(100, 2);

        for (int i = 0; i < 20; i++) {
            agreementCalculator.addAnnotation(0, i, 0);
            agreementCalculator.addAnnotation(1, i, 0);

        }

        for (int i = 20; i < 40; i++) {
            agreementCalculator.addAnnotation(0, i, 0);
            agreementCalculator.addAnnotation(1, i, 1);

        }

        for (int i = 40; i < 50; i++) {
            agreementCalculator.addAnnotation(0, i, 1);
            agreementCalculator.addAnnotation(1, i, 0);

        }

        for (int i = 50; i < 100; i++) {
            agreementCalculator.addAnnotation(0, i, 1);
            agreementCalculator.addAnnotation(1, i, 1);

        }
        System.out.println(agreementCalculator.getObservedAgreement());
        System.out.println(agreementCalculator.getExpectedAgreement());
        System.out.println(agreementCalculator.getAgreementCoefficient());

    }

}
