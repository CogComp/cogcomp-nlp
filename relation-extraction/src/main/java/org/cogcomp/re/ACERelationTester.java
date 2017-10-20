package org.cogcomp.re;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;

import org.cogcomp.re.LbjGen.*;

import java.io.File;
import java.util.*;

import java.lang.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;

public class ACERelationTester {
    public static void processList(List<Object> in, double rate){
        Random rand = new Random();
        List<Object> to_remove = new ArrayList<Object>();
        for (int i = 0; i < in.size(); i++){
            Relation r = (Relation)(in.get(i));
            if (rand.nextDouble() < rate && r.getAttribute("RelationSubtype") == "NOT_RELATED"){
                in.remove(i);
            }
        }
    }

    public static void delete_files(){
        for (int i = 0; i < 5; i++) {
            File f = new File("src/main/java/org/cogcomp/re/classifier_fold_" + i);
            f.delete();
        }
    }

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
    public static void test_cv(){
        int total_correct = 0;
        int total_labeled = 0;
        int total_predicted = 0;

        int null_total_correct = 0;
        int null_total_labeled = 0;
        int null_total_predicted = 0;

        Map<String, Integer> pMap = new HashMap<String, Integer>();
        Map<String, Integer> lMap = new HashMap<String, Integer>();
        Map<String, Integer> cMap = new HashMap<String, Integer>();
        int total_real_relation = 0;
        int real_relation_pm = 0;
        int real_relation_ps = 0;
        int real_relation_pp = 0;
        int real_relation_f = 0;
        int real_relation_all = 0;
        int total_null_relation = 0;
        int null_relation_pm = 0;
        int null_relation_ps = 0;
        int null_relation_pp = 0;
        int null_relation_f = 0;
        int null_relation_all = 0;
        for (int i = 0; i < 5; i++) {
            fine_relation_label output = new fine_relation_label();
            //ACEMentionReader train_parser = new ACEMentionReader("data/partition_with_dev/train/" + i, "relations_mono");
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
                if (predicted_label.equals("NOT_RELATED") == false){
                    if (pMap.containsKey(predicted_label)){
                        pMap.put(predicted_label, pMap.get(predicted_label) + 1);
                    }
                    else{
                        pMap.put(predicted_label, 1);
                    }
                    total_predicted ++;
                }
                else{
                    null_total_predicted ++;
                }

                if (gold_label.equals("NOT_RELATED") == false){
                    if (lMap.containsKey(gold_label)){
                        lMap.put(gold_label, lMap.get(gold_label) + 1);
                    }
                    else{
                        lMap.put(gold_label, 1);
                    }
                    total_labeled ++;
                }
                else {
                    null_total_labeled ++;
                }
                //if (getCoarseType(predicted_label).equals(getCoarseType(gold_label))){
                if (predicted_label.equals(gold_label)){
                    if (predicted_label.equals("NOT_RELATED") == false){
                        if (cMap.containsKey(gold_label)){
                            cMap.put(gold_label, cMap.get(gold_label) + 1);
                        }
                        else{
                            cMap.put(gold_label, 1);
                        }
                        total_correct ++;
                    }
                    else{
                        null_total_correct++;
                    }
                }
            }
            classifier.forget();
        }
        for (String s : lMap.keySet()){
            System.out.println(s + "\t" + lMap.get(s) + "\t" + pMap.get(s) + "\t" + cMap.get(s));
        }
        System.out.println("NOT_RELATED: " + null_total_predicted + " " + null_total_labeled + " " + null_total_correct);
        System.out.println("Real: " + total_real_relation + "; premodifer: " + real_relation_pm + "; possessive: " + real_relation_ps
        + "; preposition: " + real_relation_pp + "; formulaic: " + real_relation_f + "; all: " + real_relation_all);
        System.out.println("Null: " + total_null_relation + "; premodifer: " + null_relation_pm + "; possessive: " + null_relation_ps
                + "; preposition: " + null_relation_pp + "; formulaic: " + null_relation_f + "; all: " + null_relation_all);
        System.out.println("Total labeled: " + total_labeled);
        System.out.println("Total predicted: " + total_predicted);
        System.out.println("Total correct: " + total_correct);
        double p = (double)total_correct * 100.0/ (double)total_predicted;
        double r = (double)total_correct * 100.0/ (double)total_labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
    }

    public static void test_constraint_predicted(){
        int total_correct = 0;
        int total_labeled = 0;
        int total_predicted = 0;

        int total_true_correct = 0;
        int total_true_labeled = 0;
        int total_true_predicted = 0;
        for (int i = 0; i < 5; i++) {
            fine_relation_label output = new fine_relation_label();
            ACEMentionReader train_parser_from_file = IOHelper.readFiveFold(i, "TRAIN");
            relation_classifier classifier = new relation_classifier();
            classifier.setLexiconLocation("models/relation_classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser_from_file);
            Learner preExtractLearner = trainer.preExtract("models/relation_classifier_fold_" + i + ".ex", true, Lexicon.CountPolicy.none);
            preExtractLearner.saveLexicon();
            Lexicon lexicon = preExtractLearner.getLexicon();
            classifier.setLexicon(lexicon);
            int examples = train_parser_from_file.relations_mono.size();
            classifier.initialize(examples, preExtractLearner.getLexicon().size());
            for (Relation r : train_parser_from_file.relations_mono){
                //if (is_null(binary_classifier, r)){
                //continue;
                //}
                classifier.learn(r);
            }
            classifier.doneWithRound();
            classifier.doneLearning();

            ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);
            Parser parser_full = new PredictedMentionReader("data/partition/eval/" + i);
            for (Object example = parser_full.next(); example != null; example = parser_full.next()){
                Relation exampleRelation = (Relation)example;
                boolean isGold = exampleRelation.getAttribute("IsGoldRelation").equals("True");
                String predicted_label = constrainedClassifier.discreteValue(example);
                if (predicted_label.equals("NOT_RELATED") == false){
                    total_predicted ++;
                    if (isGold){
                        total_true_predicted ++;
                    }
                }
                String gold_label = output.discreteValue(example);
                if (gold_label.equals("NOT_RELATED") == false){
                    total_labeled ++;
                    if (isGold){
                        total_true_labeled ++;
                    }
                }
                if (getCoarseType(predicted_label).equals(getCoarseType(gold_label))){
                //if (predicted_label.equals(gold_label)){
                    if (predicted_label.equals("NOT_RELATED") == false){
                        total_correct ++;
                        if (isGold){
                            total_true_correct ++;
                        }
                    }
                }
            }
            classifier.forget();
            parser_full.reset();
        }
        System.out.println("Total True labeled: " + total_true_labeled);
        System.out.println("Total True predicted: " + total_true_predicted);
        System.out.println("Total True correct: " + total_true_correct);
        System.out.println("=================");
        System.out.println("Total labeled: " + total_labeled);
        System.out.println("Total predicted: " + total_predicted);
        System.out.println("Total correct: " + total_correct);
        double p = (double)total_correct * 100.0/ (double)total_predicted;
        double r = (double)total_correct * 100.0/ (double)total_labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
        //delete_files();
    }

    public static void test_ace_gold(){
        int labeled = 0;
        int predicted = 0;
        int correct = 0;

        fine_relation_label output = new fine_relation_label();
        Parser train_parser = new ACEMentionReader("data/all", "relation_full_bi_test");
        relation_classifier classifier = new relation_classifier();
        classifier.setLexiconLocation("models/gold_relation_classifier_all.lex");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Learner preExtractLearner = trainer.preExtract("models/gold_relation_classifier_all.ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);

        Parser parser_full_gold = new ACEMentionReader("data/partition_with_dev/dev", "relation_full_bi_test");
        for (Object example = parser_full_gold.next(); example != null; example = parser_full_gold.next()){
            String predicted_label = constrainedClassifier.discreteValue(example);
            String gold_label = output.discreteValue(example);
            if (gold_label.equals("NOT_RELATED") == false){
                labeled ++;
            }
            if (predicted_label.equals("NOT_RELATED") == false){
                predicted ++;
            }
            if (predicted_label.equals(gold_label) && !gold_label.equals("NOT_RELATED")){
                correct ++;
            }
            if (!predicted_label.equals(gold_label) && !gold_label.equals("NOT_RELATED")){
                Relation r = (Relation)example;
                Constituent source = r.getSource();
                Constituent target = r.getTarget();
                TextAnnotation ta = source.getTextAnnotation();
                Constituent sh = RelationFeatureExtractor.getEntityHeadForConstituent(source, ta, "A");
                Constituent th = RelationFeatureExtractor.getEntityHeadForConstituent(target, ta, "B");
                System.out.println(ta.getSentenceFromToken(source.getStartSpan()));
                System.out.println(source.toString() + " || " + target.toString());
                System.out.println(sh.toString() + " || " + th.toString());
                System.out.println("Gold: " + gold_label + " Predicted: " + predicted_label);
                System.out.println();
            }
        }
        System.out.println("====Gold Mention Results====");
        System.out.println("Total Labeled Mention ACE: " + labeled);
        System.out.println("Total Predicted Mention ACE: " + predicted);
        System.out.println("Total Correct Mention ACE: " + correct);
        double p = (double)correct / (double)predicted;
        double r = (double)correct / (double)labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p * 100.0);
        System.out.println("Recall: " + r * 100.0);
        System.out.println("F1: " + f * 100.0);
    }

    public static void test_ace_predicted(){
        int labeled = 0;
        int predicted_predicted = 0;
        int predicted_correct = 0;

        fine_relation_label output = new fine_relation_label();
        Parser train_parser = new ACEMentionReader("data/all", "relation_full_bi_test");
        relation_classifier classifier = new relation_classifier();
        classifier.setLexiconLocation("models/predicted_relation_classifier_all.lex");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Learner preExtractLearner = trainer.preExtract("models/predicted_relation_classifier_all.ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        //ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);

        Parser parser_full = new PredictedMentionReader("data/partition_with_dev/dev");
        for (Object example = parser_full.next(); example != null; example = parser_full.next()){
            String gold_label = output.discreteValue(example);
            //String predicted_label = constrainedClassifier.discreteValue(example);
            String predicted_label = classifier.discreteValue(example);
            if (!predicted_label.equals("NOT_RELATED")){
                predicted_predicted ++;
            }
            if (!gold_label.equals("NOT_RELATED")){
                labeled ++;
            }
            if (predicted_label.equals(gold_label) && !gold_label.equals("NOT_RELATED")){
                predicted_correct ++;
            }
        }
        classifier.forget();
        parser_full.reset();
        train_parser.reset();

        System.out.println("====Predicted Mention Results====");
        System.out.println("Total Labeled Mention ACE: " + labeled);
        System.out.println("Total Predicted Mention ACE: " + predicted_predicted);
        System.out.println("Total Correct Mention ACE: " + predicted_correct);
        double p = (double)predicted_correct / (double)predicted_predicted;
        double r = (double)predicted_correct / (double)labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p * 100.0);
        System.out.println("Recall: " + r * 100.0);
        System.out.println("F1: " + f * 100.0);
    }
    public static void main(String[] args){
        test_cv();
    }
}
