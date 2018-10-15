/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

import java.util.HashSet;
import java.util.List;

/**
 * This metric works on pair of mentions. True positives are defined as the number of **mention
 * pairs** that are in the same cluster both in the gold clustering and prediction clustering
 */
public class CorefAccuracyEvaluator extends Evaluator {
    public void evaluate(ClassificationTester tester, View goldView, View predictionView) {
        int overlapCount = 0;
        int predCount = 0;
        int goldCount = 0;
        CoreferenceView gold = (CoreferenceView) goldView;
        CoreferenceView prediction = (CoreferenceView) predictionView;
        List<Constituent> allGoldConstituents = gold.getConstituents();
        for (Constituent cons1 : allGoldConstituents) {
            HashSet<Constituent> coreferents1gold =
                    gold.getOverlappingChainsCanonicalMentions(cons1);
            HashSet<Constituent> coreferents1pred =
                    prediction.getOverlappingChainsCanonicalMentions(cons1);
            for (Constituent c : coreferents1gold)
                coreferents1gold.addAll(gold.getCoreferentMentionsViaRelations(c));
            for (Constituent c : coreferents1gold)
                coreferents1pred.addAll(prediction.getCoreferentMentionsViaRelations(c));

            for (Constituent cons2 : allGoldConstituents) {
                // are the two constituents in the same cluster, in gold annotation?

                boolean coreferents1goldContains = false;
                for(Constituent goldC:  coreferents1gold)
                    if(goldC.equalsWithoutAttributeEqualityCheck(cons2)) coreferents1goldContains = true;

                if(coreferents1goldContains) {
                    // are the two constituents in the same cluster, in pred annotation?
                    boolean coreferents1predContains = false;
                    for(Constituent predC:  coreferents1pred)
                        if(predC.equalsWithoutAttributeEqualityCheck(cons2)) coreferents1predContains = true;
                    if(coreferents1predContains) {
                        overlapCount += 1;
                    }
                    goldCount += 1;
                }

                // are the two constituents in the same cluster, in pred annotation?
                boolean coreferents1predContainsCons2 = false;
                for(Constituent predC:  coreferents1pred)
                    if(predC.equalsWithoutAttributeEqualityCheck(cons2)) coreferents1predContainsCons2 = true;

                if (coreferents1predContainsCons2)
                    predCount += 1;
            }

            tester.recordCount("coref", goldCount, predCount, overlapCount);
        }
    }
}