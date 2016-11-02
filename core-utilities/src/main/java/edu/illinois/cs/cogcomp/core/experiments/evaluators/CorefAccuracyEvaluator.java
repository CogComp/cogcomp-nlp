/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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
        View goldClone = null;
        View predictionClone = null;
        try {
            goldClone = (View) goldView.clone();
            predictionClone = (View) predictionView.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        super.cleanAttributes(goldClone, predictionClone);

        int overlapCount = 0;
        int predCount = 0;
        int goldCount = 0;
        CoreferenceView gold = (CoreferenceView) goldClone;
        CoreferenceView prediction = (CoreferenceView) predictionClone;
        List<Constituent> allGoldConstituents = gold.getConstituents();
        for (Constituent cons1 : allGoldConstituents) {
            HashSet<Constituent> conferents1gold =
                    gold.getOverlappingChainsCanonicalMentions(cons1);
            HashSet<Constituent> conferents1pred =
                    prediction.getOverlappingChainsCanonicalMentions(cons1);
            for (Constituent c : conferents1gold)
                conferents1gold.addAll(gold.getCoreferentMentionsViaRelations(c));
            for (Constituent c : conferents1gold)
                conferents1pred.addAll(prediction.getCoreferentMentionsViaRelations(c));

            for (Constituent cons2 : allGoldConstituents) {
                // are the two constituents in the same cluster, in gold annotation?
                if (conferents1gold.contains(cons2)) {
                    // are the two constituents in the same cluster, in pred annotation?
                    if (conferents1pred.contains(cons2))
                        overlapCount += 1;
                    goldCount += 1;
                }
                // are the two constituents in the same cluster, in pred annotation?
                if (conferents1pred.contains(cons2))
                    predCount += 1;
            }

            tester.recordCount("coref", goldCount, predCount, overlapCount);
        }
    }
}
