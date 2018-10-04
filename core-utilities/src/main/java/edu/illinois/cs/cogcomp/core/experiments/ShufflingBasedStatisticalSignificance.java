/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Uses stratified shuffling to compute statistical significance
 *
 * @author Vivek Srikumar
 */
public class ShufflingBasedStatisticalSignificance {
    private static Logger logger = LoggerFactory.getLogger(ShufflingBasedStatisticalSignificance.class);

    List<EvaluationRecord> output1, output2;
    private int numIterations;
    private Random random;

    private int pDiff;

    private int rDiff;

    private int fDiff;

    private double pSign;

    private double rSign;

    private double fSign;

    private boolean finishedRunning;

    private Pair<EvaluationRecord, EvaluationRecord> perf;

    public ShufflingBasedStatisticalSignificance(int numIterations, Random random) {
        this.numIterations = numIterations;
        this.random = random;
        this.output1 = new ArrayList<>();
        this.output2 = new ArrayList<>();

        finishedRunning = false;
    }

    public static void main(String[] args) {
        ShufflingBasedStatisticalSignificance sig =
                new ShufflingBasedStatisticalSignificance(10000, new Random());

        EvaluationRecord e1 = new EvaluationRecord();
        EvaluationRecord e2 = new EvaluationRecord();

        e1.incrementGold();
        sig.addInstance(e1, e2);
        sig.runSignificanceTest();

        System.out.println(sig.precisionSignificance());
        System.out.println(sig.recallSignificance());
        System.out.println(sig.f1Significance());
    }

    public void addInstance(EvaluationRecord system1, EvaluationRecord system2) {
        output1.add(system1);
        output2.add(system2);
    }

    public EvaluationRecord getSystem1Performance() {
        assert finishedRunning;
        return perf.getFirst();
    }

    public EvaluationRecord getSystem2Performance() {
        assert finishedRunning;
        return perf.getSecond();
    }

    public void runSignificanceTest() {
        logger.debug("Doing random shuffle test for " + numIterations + " iterations");

        perf = evaluate(new Pair<>(output1, output2));

        logger.debug("System 1 performance: " + perf.getFirst().getSummary());
        logger.debug("System 2 performance: " + perf.getSecond().getSummary());

        double p = getPrecisionDiff(perf);
        double r = getRecallDiff(perf);
        double f = getF1Diff(perf);

        pSign = getSign(p);

        rSign = getSign(r);
        fSign = getSign(f);

        pDiff = 1;
        rDiff = 1;
        fDiff = 1;
        for (int iter = 0; iter < numIterations; iter++) {
            Pair<List<EvaluationRecord>, List<EvaluationRecord>> list = shuffle();

            perf = evaluate(list);

            double p1 = getPrecisionDiff(perf);
            if (p >= 0 && p1 > p)
                pDiff++;
            else if (p < 0 && p1 < p)
                pDiff++;

            double r1 = getRecallDiff(perf);
            if (r >= 0 && r1 > r)
                rDiff++;
            else if (r < 0 && r1 < r)
                rDiff++;

            double f1 = getF1Diff(perf);

            if (f >= 0 && f1 > f)
                fDiff++;
            else if (f < 0 && f1 < f)
                fDiff++;

        }

        finishedRunning = true;
    }

    private double getSign(double p) {
        if (Math.abs(p) < 0.0001)
            return 0;
        else
            return Math.signum(p);
    }

    public double precisionSignificance() {
        assert finishedRunning;
        return 1.0 * pDiff / (numIterations + 1);
    }

    public double precisionSign() {
        assert finishedRunning;
        return pSign;
    }

    public double recallSignificance() {
        assert finishedRunning;
        return 1.0 * rDiff / (numIterations + 1);
    }

    public double recallSign() {
        assert finishedRunning;
        return rSign;
    }

    public double f1Significance() {
        assert finishedRunning;
        return 1.0 * fDiff / (numIterations + 1);
    }

    public double f1Sign() {
        assert finishedRunning;
        return fSign;
    }

    private Pair<List<EvaluationRecord>, List<EvaluationRecord>> shuffle() {
        assert output1.size() == output2.size();

        List<EvaluationRecord> r1 = new ArrayList<>();
        List<EvaluationRecord> r2 = new ArrayList<>();

        for (int i = 0; i < output1.size(); i++) {

            if (random.nextBoolean()) {
                r2.add(output1.get(i));
                r1.add(output2.get(i));
            } else {
                r1.add(output1.get(i));
                r2.add(output2.get(i));

            }
        }

        return new Pair<>(r1, r2);
    }

    private Pair<EvaluationRecord, EvaluationRecord> evaluate(
            Pair<List<EvaluationRecord>, List<EvaluationRecord>> list) {

        List<EvaluationRecord> output1 = list.getFirst();
        List<EvaluationRecord> output2 = list.getSecond();
        EvaluationRecord system2Perf = new EvaluationRecord();
        EvaluationRecord system1Perf = new EvaluationRecord();

        for (int i = 0; i < output1.size(); i++) {
            mergeInto(system1Perf, output1.get(i));
            mergeInto(system2Perf, output2.get(i));
        }

        return new Pair<>(system1Perf, system2Perf);

    }

    private void mergeInto(EvaluationRecord systemRecord, EvaluationRecord record) {
        assert record != null;
        assert systemRecord != null;

        systemRecord.incrementCorrect(record.getCorrectCount());
        systemRecord.incrementGold(record.getGoldCount());
        systemRecord.incrementPredicted(record.getPredictedCount());
    }

    private double getPrecisionDiff(Pair<EvaluationRecord, EvaluationRecord> r) {
        EvaluationRecord system1Perf = r.getFirst();
        EvaluationRecord system2Perf = r.getSecond();
        return system2Perf.getPrecision() - system1Perf.getPrecision();
    }

    private double getRecallDiff(Pair<EvaluationRecord, EvaluationRecord> r) {
        EvaluationRecord system1Perf = r.getFirst();
        EvaluationRecord system2Perf = r.getSecond();

        return system2Perf.getRecall() - system1Perf.getRecall();
    }

    private double getF1Diff(Pair<EvaluationRecord, EvaluationRecord> r) {
        EvaluationRecord system1Perf = r.getFirst();
        EvaluationRecord system2Perf = r.getSecond();

        return system2Perf.getF1() - system1Perf.getF1();
    }

    @Override
    public String toString() {
        return " P: " + this.precisionSignificance() + " (" + this.precisionSign() + "), R: "
                + this.recallSignificance() + "(" + this.recallSign() + "), F: "
                + this.f1Significance() + "(" + this.f1Sign() + ")";
    }
}
