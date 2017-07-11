package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Created by Xuanyu on 7/10/2017.
 * This is the Tester Class
 * It requires untrained classifiers generated directly by LBJava
 */
public class BIOTester {
    public static String getPath(String mode, int fold){
        if (mode.equals("train")){
            return "data/partition_with_dev/train/" + fold;
        }
        if (mode.equals("eval")){
            return "data/partition_with_dev/eval/" + fold;
        }
        else{
            return "INVALID_PATH";
        }
    }
    public static void test_cv(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        for (int i = 0; i < 5; i++){
            bio_classifier classifier = new bio_classifier();
            Parser train_parser = new BIOReader(getPath("train", i), "ACE05");
            Parser test_parser = new BIOReader(getPath("eval", i), "ACE05");
            bio_label output = new bio_label();
            System.out.println("Start training fold " + i);
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            classifier.setLexiconLocation("tmp/bio_classifier_fold_" + i + ".lex");
            Learner preExtractLearner = trainer.preExtract("tmp/bio_classifier_fold_" + i + ".ex", true, Lexicon.CountPolicy.none);
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
            train_parser.reset();
            classifier.doneWithRound();
            classifier.doneLearning();

            int labeled_mention = 0;
            int predicted_mention = 0;
            int correct_mention = 0;

            System.out.println("Start evaluating fold " + i);

            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                String bioTag = classifier.discreteValue(example);
                if (bioTag.equals("B")){
                    predicted_mention ++;
                }
                String correctTag = output.discreteValue(example);
                if (correctTag.equals("B")){
                    labeled_mention ++;
                    Constituent curToken = (Constituent)example;
                    Constituent pointerToken = curToken;
                    boolean correct_predicted = true;
                    while (!pointerToken.getAttribute("BIO").equals("O")){
                        if (!classifier.discreteValue(pointerToken).equals(output.discreteValue(pointerToken))){
                            correct_predicted = false;
                        }
                        if (pointerToken.getStartSpan() == pointerToken.getTextAnnotation().getSentenceFromToken(pointerToken.getStartSpan()).getEndSpan() - 1){
                            break;
                        }
                        pointerToken = pointerToken.getTextAnnotation().getView("BIO").getConstituentsCoveringToken(pointerToken.getStartSpan() + 1).get(0);
                    }
                    if (correct_predicted){
                        correct_mention ++;
                    }
                }
            }
            total_labeled_mention += labeled_mention;
            total_predicted_mention += predicted_mention;
            total_correct_mention += correct_mention;
        }
        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

    }

    public static void main(String[] args){
        test_cv();
    }
}
