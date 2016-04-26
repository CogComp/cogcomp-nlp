/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

import java.util.HashSet;
import java.util.Set;

public class ConstituentSpanSplittingEvaluator extends Evaluator {

    View gold, prediction;

    public void setViews(View gold, View prediction) {
        this.gold = gold;
        this.prediction = prediction;
    }

    public void evaluate(ClassificationTester tester) {

        Set<IntPair> goldSpans = new HashSet<>();
        for (Constituent cons : gold.getConstituents()) {
            goldSpans.add(cons.getSpan());
        }

        Set<IntPair> predictedSpans = new HashSet<>();
        for (Constituent cons : prediction.getConstituents()) {
            predictedSpans.add(cons.getSpan());
        }

        Set<IntPair> spanIntersection = new HashSet<>(goldSpans);
        spanIntersection.retainAll(predictedSpans);

        tester.recordCount("" /* label doesn't matter */, goldSpans.size(), predictedSpans.size(),
                spanIntersection.size());
    }
}
