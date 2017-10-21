/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import org.cogcomp.re.LbjGen.fine_relation_label;
import org.cogcomp.re.LbjGen.relation_classifier;

public class ACERelationTester {

    public static String getCoarseType(String fine_type){
        if (fine_type.equals("Located") || fine_type.equals("Located_OP")
                || fine_type.equals("Near")){
            return "Physical";
        }
        if (fine_type.equals("Geographical") || fine_type.equals("Geographical_OP")
                || fine_type.equals("Subsidiary") || fine_type.equals("Subsidiary_OP")
                || fine_type.equals("Artifact") || fine_type.equals("Artifact_OP")){
            return "Part-whole";
        }
        if (fine_type.equals("Business")
                || fine_type.equals("Lasting-Personal")
                || fine_type.equals("Family")){
            return "Personal-Social";
        }
        if (fine_type.equals("Employment") || fine_type.equals("Employment_OP")
                || fine_type.equals("Ownership") || fine_type.equals("Ownership_OP")
                || fine_type.equals("Founder") || fine_type.equals("Founder_OP")
                || fine_type.equals("Student-Alum") || fine_type.equals("Student-Alum_OP")
                || fine_type.equals("Sports-Affiliation") || fine_type.equals("Sports-Affiliation_OP")
                || fine_type.equals("Investor-Shareholder") || fine_type.equals("Investor-Shareholder_OP")
                || fine_type.equals("Membership") || fine_type.equals("Membership_OP")){
            return "ORG-Affiliation";
        }
        if (fine_type.equals("User-Owner-Inventor-Manufacturer") || fine_type.equals("User-Owner-Inventor-Manufacturer_OP")){
            return "Agent-Artifact";
        }
        if (fine_type.equals("Citizen-Resident-Religion-Ethnicity") || fine_type.equals("Citizen-Resident-Religion-Ethnicity_OP")
                || fine_type.equals("Org-Location") || fine_type.equals("Org-Location_OP")){
            return "Gen-Affiliation";
        }
        if (fine_type.equals("NOT_RELATED")) {
            return "NOT_RELATED";
        }
        else{
            System.out.print("Err: " + fine_type);
            return "err";
        }
    }

    /*
     * This function only tests the constrained classifier
     * It performs a similar five-fold cv
     */
    public static void test_cv_gold(){
        int total_correct = 0;
        int total_labeled = 0;
        int total_predicted = 0;
        int total_coarse_correct = 0;

        for (int i = 0; i < 5; i++) {
            fine_relation_label output = new fine_relation_label();
            ACEMentionReader train_parser = IOHelper.readFiveFold(i, "TRAIN");

            relation_classifier classifier = new relation_classifier();
            classifier.setLexiconLocation("models/relation_classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            Learner preExtractLearner = trainer.preExtract("models/relation_classifier_fold_" + i + ".ex", true, Lexicon.CountPolicy.none);
            preExtractLearner.saveLexicon();
            Lexicon lexicon = preExtractLearner.getLexicon();
            classifier.setLexicon(lexicon);
            int examples = train_parser.relations_mono.size();
            classifier.initialize(examples, preExtractLearner.getLexicon().size());
            for (Relation r : train_parser.relations_mono){
                classifier.learn(r);
            }
            classifier.doneWithRound();
            classifier.doneLearning();

            ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);
            ACEMentionReader test_parser = IOHelper.readFiveFold(i, "TEST");
            for (Relation r : test_parser.relations_mono){
                String predicted_label = constrainedClassifier.discreteValue(r);
                String gold_label = output.discreteValue(r);
                if (!predicted_label.equals("NOT_RELATED")){
                    total_predicted ++;
                }
                if (!gold_label.equals("NOT_RELATED")){
                    total_labeled ++;
                }
                if (predicted_label.equals(gold_label)){
                    if (!predicted_label.equals("NOT_RELATED")){
                        total_correct ++;
                    }
                }
                if (getCoarseType(predicted_label).equals(getCoarseType(gold_label))){
                    if (!predicted_label.equals("NOT_RELATED")){
                        total_coarse_correct ++;
                    }
                }
            }
            classifier.forget();
        }
        System.out.println("Total labeled: " + total_labeled);
        System.out.println("Total predicted: " + total_predicted);
        System.out.println("Total correct: " + total_correct);
        System.out.println("Total coarse correct: " + total_coarse_correct);
        double p = (double)total_correct * 100.0/ (double)total_predicted;
        double r = (double)total_correct * 100.0/ (double)total_labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("Fine Type F1: " + f);
        System.out.println("Coarse Type F1: " + f * (double)total_coarse_correct / (double)total_correct);
    }

    public static void testAnnotator(){
        try {
            //ACEReader aceReader = new ACEReader()
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void generateModel(){

    }

    public static void main(String[] args){
        test_cv_gold();
    }
}
