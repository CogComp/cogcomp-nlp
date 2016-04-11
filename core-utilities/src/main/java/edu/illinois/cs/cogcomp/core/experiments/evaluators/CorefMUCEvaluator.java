package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

import java.util.HashSet;
import java.util.Set;

/**
 * Evaluate a Clustering using the MUC evaluation metric. See Marc
 * Vilain, John Burger, John Aberdeen, Dennis Connolly, and Lynette
 * Hirschman. 1995. A model-theoretic coreference scoring scheme. In
 * Proceedings fo the 6th Message Understanding Conference
 * (MUC6). 45--52. Morgan Kaufmann.
 *
 * Note that MUC more or less ignores singleton clusters.
 */
public class CorefMUCEvaluator extends Evaluator {
    CoreferenceView gold, prediction;

    public void setViews(View gold, View prediction) {
        this.gold = (CoreferenceView) gold;
        this.prediction = (CoreferenceView) prediction;
    }

    public void evaluate(ClassificationTester tester) {
        // TODO
    }

     public void evaluate() {
        // Recall = \sum_i [ |si| - |pOfsi| ] / \sum_i [ |si| - 1 ]
        // where si is a true cluster, pOfsi is the set of predicted
        // clusters that contain elements of si (i.e. number of predicted clusters having some overlap with
        // this gold cluster)
        int numerator = 0;
        int denominator = 0;
         for(Constituent goldCanonicalCons : gold.getCanonicalEntitiesViaRelations() ) {
            HashSet consInGoldCluster =  new HashSet(gold.getCoreferentMentions(goldCanonicalCons));
            for(Constituent predCanonicalCons : prediction.getCanonicalEntitiesViaRelations()) {
                HashSet consInPredCluster = new HashSet(prediction.getCoreferentMentions(predCanonicalCons));
                Set<String> intersection = new HashSet(consInGoldCluster);
                intersection.retainAll(consInPredCluster);
                if(!intersection.isEmpty())
                    numerator -= 1;
            }
            numerator += consInGoldCluster.size();
            denominator += consInGoldCluster.size() - 1;
        }
         System.out.println(numerator);
         System.out.println(denominator);
        double recall = 1.0 * numerator / denominator;

        // Precision is defined dually by reversing the roles of gold and prediction
        // Precision = \sum_i [ |siprime| - |pOfsiprime| ] / \sum_i [ |siprime| - 1 ]
        // where siprime is a predicted cluster, pOfsiprime is the set of
        // true clusters that contain elements of siprime.
        numerator = 0;
        denominator = 0;
        for(Constituent predCanonicalCons : prediction.getCanonicalEntitiesViaRelations() ) {
            HashSet consInPredCluster =  new HashSet(prediction.getCoreferentMentions(predCanonicalCons));
            for(Constituent goldCanonicalCons : gold.getCanonicalEntitiesViaRelations()) {
                HashSet consInGoldCluster = new HashSet(gold.getCoreferentMentions(goldCanonicalCons));
                Set<String> intersection = new HashSet(consInPredCluster);
                intersection.retainAll(consInGoldCluster);
                if(!intersection.isEmpty())
                    numerator -= 1;
            }
            numerator += consInPredCluster.size();
            denominator += consInPredCluster.size() - 1;
        }
        double precision = 1.0 * numerator / denominator;
         System.out.println("precision = ");
         System.out.println(precision);
         System.out.println("recall = ");
         System.out.println(recall);
     }
}