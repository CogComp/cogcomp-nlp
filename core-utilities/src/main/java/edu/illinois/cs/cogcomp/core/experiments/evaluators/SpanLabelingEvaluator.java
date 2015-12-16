package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpanLabelingEvaluator extends Evaluator {

    SpanLabelView gold, prediction;

    public void setViews(View gold, View prediction) {
        this.gold = (SpanLabelView) gold;
        this.prediction = (SpanLabelView) prediction;
    }

    public void evaluate(ClassificationTester tester) {

        Set<Constituent> goldCons = new HashSet<>(gold.getConstituents());
        Set<Constituent> predictionCons = new HashSet<>(prediction.getConstituents());

        Set<Constituent> predictionConsMinusGoldCons = new HashSet<>(predictionCons);
        predictionConsMinusGoldCons.removeAll(goldCons);

        for (Constituent c : goldCons) {
            if (predictionCons.contains(c))
                tester.recordCount(c.getLabel(), 1/* gold */, 1/* prediction */, 1/* correct */);
            else
                tester.recordCount(c.getLabel(), 1/* gold */, 1/* prediction */, 0/* correct */);
        }

        for (Constituent c : predictionConsMinusGoldCons) {
            tester.recordCount(c.getLabel(), 0/* gold */, 1/* prediction */, 0/* correct */);
        }
    }
}
