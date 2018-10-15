/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

/**
 * A small utility class to wrap a pair of labelID and its score
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class LabelScorePair implements Comparable<LabelScorePair> {
    public String labelID;
    double labelScore;

    public LabelScorePair(String labelID, double score) {
        this.labelID = labelID;
        labelScore = score;
    }

    public String getLabelID() {
        return labelID;
    }

    public double getScore() {
        return labelScore;
    }

    public void setLabelID(String labelID) {
        this.labelID = labelID;
    }

    public void setScore(double score) {
        labelScore = score;
    }

    @Override
    public int compareTo(LabelScorePair kvp) {
        return Double.compare(this.labelScore, kvp.labelScore);
    }
}
