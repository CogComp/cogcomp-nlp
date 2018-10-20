/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.core.utilities.Table;

import java.util.*;

/**
 * A class for recording results of experiments. This can be used to generate a result table with
 * one row per label and a confusion table.
 *
 * @author Vivek Srikumar
 */
public class ClassificationTester {

    // average statistics
    EvaluationRecord evalRecord;

    // per-label statistics
    Map<String, EvaluationRecord> labelWiseRecords;
    Counter<String> counter;

    Set<String> ignore = new HashSet<>();
    private Map<String, ShufflingBasedStatisticalSignificance> significance;

    public ClassificationTester() {
        counter = new Counter<>();

        evalRecord = new EvaluationRecord();
        labelWiseRecords = new HashMap<>();
    }

    public void ignoreLabelFromSummary(String label) {
        ignore.add(label);
    }

    public void reset() {
        counter.reset();
        ignore.clear();

        evalRecord = new EvaluationRecord();
        labelWiseRecords.clear();
    }

    private void addLabelRecord(String label) {
        if (!labelWiseRecords.containsKey(label))
            labelWiseRecords.put(label, new EvaluationRecord());
    }

    public EvaluationRecord getEvaluationRecord() {
        return evalRecord;
    }

    public EvaluationRecord getEvaluationRecord(String label) {
        return labelWiseRecords.get(label);
    }

    public Set<String> getLabels() {
        return labelWiseRecords.keySet();
    }

    public void recordCount(String label, int goldCount, int predictedCount, int correctCount) {
        addLabelRecord(label);

        if (!ignore.contains(label)) {
            evalRecord.incrementCorrect(correctCount);
            evalRecord.incrementGold(goldCount);
            evalRecord.incrementPredicted(predictedCount);
        }

        EvaluationRecord record = labelWiseRecords.get(label);
        record.incrementCorrect(correctCount);
        record.incrementGold(goldCount);
        record.incrementPredicted(predictedCount);
    }

    public void record(String goldLabel, String predictedLabel) {
        addLabelRecord(goldLabel);
        addLabelRecord(predictedLabel);

        if (!ignore.contains(goldLabel))
            evalRecord.incrementGold();

        if (!ignore.contains(predictedLabel))
            evalRecord.incrementPredicted();

        labelWiseRecords.get(predictedLabel).incrementPredicted();
        labelWiseRecords.get(goldLabel).incrementGold();

        if (goldLabel.equals(predictedLabel)) {
            labelWiseRecords.get(goldLabel).incrementCorrect();

            if (!ignore.contains(goldLabel))
                evalRecord.incrementCorrect();
        }
        counter.incrementCount(goldLabel + "." + predictedLabel);
    }

    public void recordGoldOnly(String goldLabel) {
        addLabelRecord(goldLabel);

        if (!ignore.contains(goldLabel))
            evalRecord.incrementGold();
        labelWiseRecords.get(goldLabel).incrementGold();

        counter.incrementCount(goldLabel + ".<null>");

    }

    public void recordPredictionOnly(String predictedLabel) {
        addLabelRecord(predictedLabel);

        if (!ignore.contains(predictedLabel))
            evalRecord.incrementPredicted();
        labelWiseRecords.get(predictedLabel).incrementPredicted();
        counter.incrementCount("<null>." + predictedLabel);
    }

    public Pair<Table, List<String>> getConfusionTable() {
        Table table = new Table();
        Set<String> set = new HashSet<>();
        for (String item : counter.items()) {
            String[] split = item.split("\\.");
            set.add(split[0]);
            set.add(split[1]);
        }

        List<String> sortSet = new ArrayList<>(set);
        Collections.sort(sortSet);

        table.addColumn("Gold");

        for (int i = 0; i < sortSet.size(); i++) {
            table.addColumn(i + "");
        }

        int id = 0;
        for (String label : sortSet) {
            List<String> s = new ArrayList<>();
            s.add(id + "");
            id++;

            for (String pLabel : sortSet) {
                int count = (int) counter.getCount(label + "." + pLabel);
                s.add(Integer.toString(count));
            }
            table.addRow(s.toArray(new String[s.size()]));
        }

        return new Pair<>(table, sortSet);
    }

    // Micro statistics, returns the F1, P and R for all instances of any labels
    public double getMicroF1() {
        return evalRecord.getF1();
    }

    public double getMicroPrecision() {
        return evalRecord.getPrecision();
    }

    public double getMicroRecall() {
        return evalRecord.getRecall();
    }

    // Average of macro statistics, returns average of the F1, P and R across different labels
    public double getMacroF1() {
        double sumF1 = 0;
        for (EvaluationRecord e : labelWiseRecords.values()) {
            sumF1 += e.getF1();
        }
        return sumF1 / labelWiseRecords.size();
    }

    public double getMacroPrecision() {
        double sumPrecision = 0;
        for (EvaluationRecord e : labelWiseRecords.values()) {
            sumPrecision += e.getPrecision();
        }
        return sumPrecision / labelWiseRecords.size();
    }

    public double getMacroRecall() {
        double sumRecall = 0;
        for (EvaluationRecord e : labelWiseRecords.values()) {
            sumRecall += e.getRecall();
        }
        return sumRecall / labelWiseRecords.size();
    }

    public Table getPerformanceTable() {
        return getPerformanceTable(true);
    }

    public Table getPerformanceTable(boolean printCounts) {
        return getPerformanceTable(printCounts, labelWiseRecords, evalRecord, significance);
    }

    private Table getPerformanceTable(boolean printCounts,
            Map<String, EvaluationRecord> labelWiseRecords, EvaluationRecord evalRecord,
            Map<String, ShufflingBasedStatisticalSignificance> significance) {
        Table table = new Table();
        table.addColumn("Label");

        if (printCounts) {
            table.addColumn("Total Gold");
            table.addColumn("Total Predicted");
            table.addColumn("Correct Prediction");
        } else {
            table.addColumn("Correct");
            table.addColumn("Excess");
            table.addColumn("Missed");

        }

        table.addColumn("Precision");
        table.addColumn("Recall");
        table.addColumn("F1");

        for (String label : Sorters.sortSet(labelWiseRecords.keySet())) {
            if (label.equals("<null>"))
                continue;

            EvaluationRecord record = labelWiseRecords.get(label);
            table.addRow(getRow(label, record, printCounts, significance));
        }

        table.addSeparator();

        table.addRow(getRow("All", evalRecord, printCounts, significance));

        table.addSeparator();

        return table;
    }

    private String[] getRow(String label, EvaluationRecord record, boolean printCounts,
            Map<String, ShufflingBasedStatisticalSignificance> significance) {

        String c2;
        String c3;
        String c4;

        if (printCounts) {
            c2 = record.getGoldCount() + "";
            c3 = "" + record.getPredictedCount();
            c4 = "" + record.getCorrectCount();
        } else {
            c2 = record.getCorrectCount() + "";
            c3 = record.getExtraCount() + "";
            c4 = record.getMissedCount() + "";
        }

        String prec, rec, f1;
        if (significance != null && significance.containsKey(label)) {
            ShufflingBasedStatisticalSignificance sig = significance.get(label);

            assert sig != null;

            prec =
                    attachSignificance(record.getPrecision(), sig.precisionSignificance(),
                            sig.precisionSign());

            rec =
                    attachSignificance(record.getRecall(), sig.recallSignificance(),
                            sig.recallSign());

            f1 = attachSignificance(record.getF1(), sig.f1Significance(), sig.f1Sign());

        } else {
            prec = StringUtils.getFormattedString(record.getPrecision() * 100, 2);
            rec = StringUtils.getFormattedString(record.getRecall() * 100, 2);
            f1 = StringUtils.getFormattedString(record.getF1() * 100, 2);

        }

        return new String[] {label, c2, c3, c4, prec, rec, f1};
    }

    private String attachSignificance(double e, double d, double sign) {
        String sig = "";

        if (e != 0 && sign != 0) {
            if (d <= 0.01)
                sig = "^{**}";
            else if (d <= 0.05)
                sig = ("^{*}");
        }

        String out = StringUtils.getFormattedString(e * 100, 2);
        if (sign > 0)
            out = "=" + out + "=";
        else if (sign < 0)
            out = "+" + out + "+";

        return out + sig;
    }

    /**
     * Statistical significance information. Takes map from label to p-values for {P, R, F} for that
     * label.
     */
    public void setSignificanceInfo(Map<String, ShufflingBasedStatisticalSignificance> significance) {
        this.significance = significance;
    }

}
