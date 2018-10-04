/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.agreement;

/**
 * This abstract class and its descendants are based on the survey article
 * "Inter-Coder Agreement for Computational Linguistics" by Ron Artstein and Massimo Poessio in
 * Computational Lingusitics, Volume 34, Number 4.
 *
 * @author Vivek Srikumar
 */
public abstract class AnnotatorAgreement {

    protected final int numAnnotators;
    protected final int numItems;
    protected final int numLabels;

    protected int[][] nik, nck;
    protected int[] nk;

    protected int[][] annotation;

    public AnnotatorAgreement(int numAnnotators, int numItems, int numLabels) {
        this.numAnnotators = numAnnotators;
        this.numItems = numItems;
        this.numLabels = numLabels;
        this.nik = new int[numItems][numLabels];
        this.nck = new int[numAnnotators][numLabels];
        this.nk = new int[numLabels];

        this.annotation = new int[this.numItems][this.numAnnotators];
    }

    public void addAnnotation(int annotatorId, int item, int label) {
        nik[item][label]++;
        nck[annotatorId][label]++;
        nk[label]++;

        annotation[item][annotatorId] = label;

    }

    public abstract double getObservedAgreement();

    public abstract double getExpectedAgreement();

    public double getObservedDisagreement() {
        return 1 - getObservedAgreement();
    }

    public double getAgreementCoefficient() {
        return (this.getObservedAgreement() - this.getExpectedAgreement())
                / (1 - this.getExpectedAgreement());
    }

}
