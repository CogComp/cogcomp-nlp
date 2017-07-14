package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
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
            String preBIOLevel1 = "";
            String preBIOLevel2 = "";
            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);
                String bioTag = classifier.discreteValue(example);

                double b_score = 0.0, i_score = 0.0, o_score = 0.0;
                ScoreSet scores = classifier.scores(example);
                Score[] scoresArray = scores.toArray();
                for (Score score : scoresArray){
                    if (score.value.equals("B")){
                        b_score = score.score;
                    }
                    else if (score.value.equals("I")){
                        i_score = score.score;
                    }
                    else if (score.value.equals("O")){
                        o_score = score.score;
                    }
                }
                //TODO: Solve start with "I"
                if (bioTag.equals("I") && !(preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    if (Math.abs(b_score - i_score) < 0.1){
                        //bioTag = "B";
                    }
                }

                preBIOLevel1 = bioTag;
                preBIOLevel2 = preBIOLevel1;
                if (bioTag.equals("B")){
                    predicted_mention ++;
                }
                String correctTag = output.discreteValue(example);
                if (correctTag.equals("B")){
                    labeled_mention ++;
                    Constituent curToken = (Constituent)example;
                    Constituent pointerToken = curToken;
                    boolean correct_predicted = true;
                    String wholeMention = "";
                    int startIdx = pointerToken.getStartSpan();
                    int endIdx = startIdx + 1;
                    String preBIOLevel1_dup = ((Constituent) example).getAttribute("preBIOLevel1");
                    String preBIOLevel2_dup = ((Constituent) example).getAttribute("preBIOLevel2");
                    while (!pointerToken.getAttribute("BIO").equals("O")){
                        if (endIdx - 1 > startIdx){
                            if (pointerToken.getAttribute("BIO").equals("B")){
                                break;
                            }
                        }
                        pointerToken.addAttribute("preBIOLevel1", preBIOLevel1_dup);
                        pointerToken.addAttribute("preBIOLevel2", preBIOLevel2_dup);
                        preBIOLevel1_dup = classifier.discreteValue(pointerToken);
                        preBIOLevel2_dup = preBIOLevel1_dup;
                        wholeMention += pointerToken.toString() + " ";
                        if (!classifier.discreteValue(pointerToken).equals(output.discreteValue(pointerToken))){
                            correct_predicted = false;
                        }
                        if (pointerToken.getStartSpan() == pointerToken.getTextAnnotation().getSentenceFromToken(pointerToken.getStartSpan()).getEndSpan() - 1){
                            break;
                        }
                        pointerToken = pointerToken.getTextAnnotation().getView("BIO").getConstituentsCoveringToken(pointerToken.getStartSpan() + 1).get(0);
                        endIdx = pointerToken.getStartSpan() + 1;
                    }
                    endIdx --;
                    if (correct_predicted){
                        correct_mention ++;
                    }
                    else {
                        View bioView = curToken.getTextAnnotation().getView("BIO");
                        int printStart = startIdx;
                        if (correctTag.equals("B") && bioTag.equals("I")){
                            printStart --;
                        }
                        for (int k = printStart; k < endIdx; k++){
                            Constituent ct = bioView.getConstituentsCoveringToken(k).get(0);
                            System.out.print(ct.toString() + " " + ct.getAttribute("BIO") + " " + classifier.discreteValue(ct) + ", ");
                        }
                        System.out.println();
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
