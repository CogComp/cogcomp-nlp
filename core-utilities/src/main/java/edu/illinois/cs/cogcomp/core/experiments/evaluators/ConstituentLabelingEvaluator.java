/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

import java.util.HashSet;
import java.util.Set;

public class ConstituentLabelingEvaluator extends Evaluator {

    public void evaluate(ClassificationTester tester, View gold, View prediction) {
        super.cleanAttributes(gold, prediction);
        Set<Constituent> goldCons = new HashSet<>(gold.getConstituents());
        Set<Constituent> predictionCons = new HashSet<>(prediction.getConstituents());

        Set<Constituent> predictionConsMinusGoldCons = new HashSet<>(predictionCons);
        predictionConsMinusGoldCons.removeAll(goldCons);

        for (Constituent c : goldCons) {
            boolean contains = false;
            for(Constituent cPred: predictionCons)
                if (cPred.equalsWithoutAttributeEqualityCheck(c)) contains = true;

            if (contains)
                tester.recordCount(c.getLabel(), 1/* gold */, 1/* prediction */, 1/* correct */);
            else
                tester.recordCount(c.getLabel(), 1/* gold */, 1/* prediction */, 0/* correct */);
        }

        for (Constituent c : predictionConsMinusGoldCons) {
            tester.recordCount(c.getLabel(), 0/* gold */, 1/* prediction */, 0/* correct */);
        }
    }
}
