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
import java.util.Set;

/**
 * Evaluate a Clustering using the MUC evaluation metric. See Marc Vilain, John Burger, John
 * Aberdeen, Dennis Connolly, and Lynette Hirschman. 1995. A model-theoretic coreference scoring
 * scheme. In Proceedings fo the 6th Message Understanding Conference (MUC6). 45--52. Morgan
 * Kaufmann.
 *
 * Note that MUC more or less ignores singleton clusters.
 */
public class CorefMUCEvaluator extends Evaluator {
    CoreferenceView gold, prediction;

    public void evaluate(ClassificationTester tester, View goldView, View predictionView) {
        this.gold = (CoreferenceView) goldView;
        this.prediction = (CoreferenceView) predictionView;
        // Recall = \sum_i [ |si| - |pOfsi| ] / \sum_i [ |si| - 1 ]
        // where si is a true cluster, pOfsi is the set of predicted
        // clusters that contain elements of si (i.e. number of predicted clusters having some
        // overlap with
        // this gold cluster)
        int numerator1 = 0;
        int denominator1 = 0;
        for (Constituent goldCanonicalCons : gold.getCanonicalEntitiesViaRelations()) {
            HashSet<Constituent> consInGoldCluster =
                    new HashSet(gold.getCoreferentMentionsViaRelations(goldCanonicalCons));
            for (Constituent predCanonicalCons : prediction.getCanonicalEntitiesViaRelations()) {
                HashSet<Constituent> consInPredCluster =
                        new HashSet(prediction.getCoreferentMentionsViaRelations(predCanonicalCons));
                Set<Constituent> intersection = new HashSet();
                for(Constituent cGold : consInGoldCluster) {
                    for(Constituent cPred: consInPredCluster)
                        if(cPred.equalsWithoutAttributeEqualityCheck(cGold)) intersection.add(cGold);
                }
                if (!intersection.isEmpty())
                    numerator1 -= 1;
            }
            numerator1 += consInGoldCluster.size();
            denominator1 += consInGoldCluster.size() - 1;
        }

        double recall = 1.0 * numerator1 / denominator1;

        // Precision is defined dually by reversing the roles of gold and prediction
        // Precision = \sum_i [ |siprime| - |pOfsiprime| ] / \sum_i [ |siprime| - 1 ]
        // where siprime is a predicted cluster, pOfsiprime is the set of
        // true clusters that contain elements of siprime.
        int numerator2 = 0;
        int denominator2 = 0;
        for (Constituent predCanonicalCons : prediction.getCanonicalEntitiesViaRelations()) {
            HashSet<Constituent> consInPredCluster =
                    new HashSet(prediction.getCoreferentMentionsViaRelations(predCanonicalCons));
            for (Constituent goldCanonicalCons : gold.getCanonicalEntitiesViaRelations()) {
                HashSet<Constituent> consInGoldCluster =
                        new HashSet(gold.getCoreferentMentionsViaRelations(goldCanonicalCons));
                Set<Constituent> intersection = new HashSet();
                for(Constituent cPred: consInPredCluster) {
                    for(Constituent cGold: consInGoldCluster) {
                        if(cGold.equalsWithoutAttributeEqualityCheck(cPred)) intersection.add(cGold);
                    }
                }
                if (!intersection.isEmpty())
                    numerator2 -= 1;
            }
            numerator2 += consInPredCluster.size();
            denominator2 += consInPredCluster.size() - 1;
        }
        double precision = 1.0 * numerator2 / denominator2;

        assert (numerator1 == numerator2);
        tester.recordCount("coref", denominator1, denominator2, numerator1);
    }
}