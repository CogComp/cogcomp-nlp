package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

import java.util.HashSet;
import java.util.List;

/**
 * This metric works on pair of mentions. True positives are defined as the number of **mention pairs** that are
 * in the same cluster both in the gold clustering and prediction clustering
 */
public class CorefAccuracyEvaluator extends Evaluator {
    CoreferenceView gold, prediction;

    public void setViews(View gold, View prediction) {
        this.gold = (CoreferenceView) gold;
        this.prediction = (CoreferenceView) prediction;
    }

    public void evaluate(ClassificationTester tester) {
        int overlapCount = 0;
        int predCount = 0;
        int goldCount = 0;
        List<Constituent> allGoldConstituents = gold.getConstituents();
        for(Constituent cons1 : allGoldConstituents) {
            HashSet<Constituent> conferents1gold = gold.getOverlappingChainsCanonicalMentions(cons1);
            HashSet<Constituent> conferents1pred = prediction.getOverlappingChainsCanonicalMentions(cons1);
            for(Constituent c : conferents1gold)
                conferents1gold.addAll(gold.getCoreferentMentionsViaRelations(c));
            for(Constituent c : conferents1gold)
                conferents1pred.addAll(prediction.getCoreferentMentionsViaRelations(c));

            for(Constituent cons2 : allGoldConstituents) {
                // are the two constituents in the same cluster, in gold annotation?
                if(conferents1gold.contains(cons2)) {
                    // are the two constituents in the same cluster, in pred annotation?
                    if(conferents1pred.contains(cons2))
                        overlapCount += 1;
                    goldCount += 1;
                }
                // are the two constituents in the same cluster, in pred annotation?
                if(conferents1pred.contains(cons2))
                    predCount += 1;
            }

            tester.recordCount("coref", goldCount, predCount, overlapCount);
        }
    }
}