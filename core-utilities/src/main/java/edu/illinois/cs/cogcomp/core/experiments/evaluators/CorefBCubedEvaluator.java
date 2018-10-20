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
import java.util.Set;

/**
 * Computes the within-document B-Cubed F-Score for a collection of documents.
 *
 * The precision of a collection of documents is the average of the precisions of all mentions in
 * all documents. The precision of a mention m is calculated as the number of mentions correctly
 * predicted to be in the same cluster as m (including m) divided by the number of mentions in the
 * predicted cluster containing m.
 *
 * The recall of a collection of documents is the average of the recalls of all mentions in all
 * documents. The recall of a mention m is calculated as the number of mentions correctly predicted
 * to be in the same cluster as m (including m) divided by the number of mentions in the true
 * cluster containing m.
 *
 * The B-Cubed F-Score is the harmonic mean of the precision and recall defined above. This B-Cubed
 * F-Score is weighted so that every mention's precision and recall gets equal weight.
 *
 * This is the algorithm that Culotta says he used in Culotta, Wick, and McCallum (HLT 2007),
 * modified to accept prediction solutions that contain different mentions than the key solutions,
 * by counting overlap as 0 for mentions not contained in both.
 *
 * See (Amit) Bagga and Baldwin (MUC-7 1998).
 */

public class CorefBCubedEvaluator extends Evaluator {
    CoreferenceView gold, prediction;

    /**
     * The result will be populated in a ClassificationTester. Note that you can either use "micro"
     * or "macro" statistics. It is more common to use "macro" statistics for BCubed metric.
     */
    public void evaluate(ClassificationTester tester, View goldView, View predictionView) {
        this.gold = (CoreferenceView) goldView;
        this.prediction = (CoreferenceView) predictionView;
        List<Constituent> allGoldConstituents = gold.getConstituents();
        for (Constituent cons : allGoldConstituents) {
            HashSet<Constituent> overlappingGoldCanonicalCons =
                    gold.getOverlappingChainsCanonicalMentions(cons);
            HashSet<Constituent> overlappingPredCanonicalCons =
                    prediction.getOverlappingChainsCanonicalMentions(cons);

            int overlapCount = 0;
            int predCount = 0;
            int goldCount = 0;
            for (Constituent predCanonicalCons : overlappingPredCanonicalCons) {
                HashSet<Constituent> consInPredCluster =
                        new HashSet(prediction.getCoreferentMentionsViaRelations(predCanonicalCons));
                for (Constituent goldCanonicalCons : overlappingGoldCanonicalCons) {
                    HashSet<Constituent> consInGoldCluster =
                            new HashSet(gold.getCoreferentMentionsViaRelations(goldCanonicalCons));

                    Set<Constituent> intersection = new HashSet();
                    for(Constituent gold: consInGoldCluster) {
                        for(Constituent pred: consInPredCluster) {
                            if(gold.equalsWithoutAttributeEqualityCheck(pred)) intersection.add(pred);
                        }
                    }
                    overlapCount += intersection.size();
                    predCount += consInPredCluster.size();
                    goldCount += consInGoldCluster.size();
                }
            }
            tester.recordCount(cons.toString(), goldCount, predCount, overlapCount);
        }
    }
}