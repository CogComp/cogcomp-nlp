/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.core.utilities.Table;

public class EvaluationRecord {
    private int goldCount;
    private int correctCount;
    private int predictedCount;

    public EvaluationRecord() {
        goldCount = 0;
        predictedCount = 0;
        correctCount = 0;
    }

    public void incrementGold() {
        goldCount++;
    }

    public void incrementPredicted() {
        predictedCount++;
    }

    public void incrementCorrect() {
        correctCount++;
    }

    public void incrementGold(int c) {
        goldCount += c;
    }

    public void incrementPredicted(int c) {
        predictedCount += c;
    }

    public void incrementCorrect(int c) {
        correctCount += c;
    }

    public int getGoldCount() {
        return goldCount;
    }

    public int getPredictedCount() {
        return predictedCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getMissedCount() {
        return goldCount - correctCount;
    }

    public int getExtraCount() {
        return predictedCount - correctCount;
    }

    public double getPrecision() {
        if (predictedCount > 0)
            return 1.0 * correctCount / predictedCount;
        else
            return 0.0;
    }

    public double getRecall() {
        if (goldCount > 0)
            return 1.0 * correctCount / goldCount;
        else
            return 0.0;
    }

    public double getF1() {
        if (predictedCount + goldCount > 0)
            return 2.0 * correctCount / (predictedCount + goldCount);
        else
            return 0.0;
    }

    public String getSummary() {
        String s =
                "  - Predicted = " + this.predictedCount + ", Gold = " + this.goldCount
                        + " Correct = " + this.correctCount;

        s += "\n";
        s +=
                "  - Precision = " + getPrecision() + ", Recall = " + getRecall() + ", F1 = "
                        + getF1();

        return s;
    }

    @Override
    public String toString() {
        return getSummary();
    }

    public Table getSummaryTable() {
        Table table = new Table();

        table.addColumn("Precision");
        table.addColumn("Recall");
        table.addColumn("F1");

        table.addRow(new String[] {StringUtils.getFormattedString(getPrecision(), 3),
                StringUtils.getFormattedString(getRecall(), 3),
                StringUtils.getFormattedString(getF1(), 3)});

        return table;
    }
}
