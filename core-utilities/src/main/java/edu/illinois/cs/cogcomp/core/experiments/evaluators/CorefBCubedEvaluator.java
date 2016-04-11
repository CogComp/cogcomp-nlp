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
 * The precision of a collection of documents is the average
 * of the precisions of all mentions in all documents.
 * The precision of a mention m is calculated as the number of mentions
 * correctly predicted to be in the same cluster as m (including m)
 * divided by the number of mentions in the predicted cluster containing m.
 *
 * The recall of a collection of documents is the average
 * of the recalls of all mentions in all documents.
 * The recall of a mention m is calculated as the number of mentions
 * correctly predicted to be in the same cluster as m (including m)
 * divided by the number of mentions in the true cluster containing m.
 *
 * The B-Cubed F-Score is the harmonic mean of the precision and recall
 * defined above.
 * This B-Cubed F-Score is weighted so that every mention's precision and
 * recall gets equal weight.
 *
 * This is the algorithm that Culotta says he used
 * in Culotta, Wick, and McCallum (HLT 2007),
 * modified to accept prediction solutions
 * that contain different mentions than the key solutions,
 * by counting overlap as 0 for mentions not contained in both.
 *
 * See (Amit) Bagga and Baldwin (MUC-7 1998).
 */

public class CorefBCubedEvaluator extends Evaluator {
    CoreferenceView gold, prediction;

    public void setViews(View gold, View prediction) {
        this.gold = (CoreferenceView) gold;
        this.prediction = (CoreferenceView) prediction;
    }

    public void evaluate(ClassificationTester tester) {
        // TODO
    }

    public void evaluate() {
        List<Constituent> allGoldConstituents = gold.getConstituents();
        double precision = 0;
        for(Constituent cons : allGoldConstituents) {
            HashSet<Constituent> overlappingGoldCanonicalCons = gold.getOverlappingChainsCanonicalMentions(cons);
            HashSet<Constituent> overlappingPredCanonicalCons = prediction.getOverlappingChainsCanonicalMentions(cons);

            int numerator = 0;
            int denominator = 0;
            for(Constituent predCanonicalCons : overlappingPredCanonicalCons ) {
                HashSet consInPredCluster =  new HashSet(prediction.getCoreferentMentionsViaRelations(predCanonicalCons));
                for(Constituent goldCanonicalCons : overlappingGoldCanonicalCons) {
                    HashSet consInGoldCluster = new HashSet(gold.getCoreferentMentionsViaRelations(goldCanonicalCons));
                    Set<String> intersection = new HashSet(consInGoldCluster);
                    intersection.retainAll(consInPredCluster);
                    if(!intersection.isEmpty())
                        numerator -= 1; //intersection.size();
                }
                numerator += consInPredCluster.size();
                denominator += consInPredCluster.size();
                System.out.println(numerator);
                System.out.println(denominator);
                if(numerator == 0) {
                    System.out.println("stop here ... ");
                }
                System.out.println("=======");
            }

            precision += 1.0 * numerator / denominator;
            System.out.println("--- precision " + precision);
        }
        System.out.println("*** precision ");
        System.out.println(precision);
        // normalize with the number of mentions
        precision /= allGoldConstituents.size();

        List<Constituent> allPredConstituents = prediction.getConstituents();
        double recall = 0;
        for(Constituent cons : allPredConstituents) {
            HashSet<Constituent> overlappingGoldCanonicalCons = gold.getOverlappingChainsCanonicalMentions(cons);
            HashSet<Constituent> overlappingPredCanonicalCons = prediction.getOverlappingChainsCanonicalMentions(cons);

            int numerator = 0;
            int denominator = 0;
            for(Constituent goldCanonicalCons : overlappingGoldCanonicalCons ) {
                HashSet consInGoldCluster =  new HashSet(gold.getCoreferentMentions(goldCanonicalCons));
                for(Constituent predCanonicalCons : overlappingPredCanonicalCons) {
                    HashSet consInPredCluster = new HashSet(prediction.getCoreferentMentions(predCanonicalCons));
                    Set<String> intersection = new HashSet(consInPredCluster);
                    intersection.retainAll(consInGoldCluster);
//                    numerator -= intersection.size();
                    if(!intersection.isEmpty())
                        numerator -= 1; //intersection.size();
                }
                numerator += consInGoldCluster.size();
                denominator += consInGoldCluster.size();
            }
            recall += 1.0 * numerator / denominator;
        }
        // normalize with the number of mentions
        recall /= allGoldConstituents.size();
        System.out.println("precision = ");
        System.out.println(precision);
        System.out.println("recall = ");
        System.out.println(recall);
    }
}
