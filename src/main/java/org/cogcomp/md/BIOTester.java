package org.cogcomp.md;

import EDU.oswego.cs.dl.util.concurrent.FJTask;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.util.ArrayList;
import java.util.List;

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

    public static bio_classifier_nam train_nam_classifier(Parser train_parser){
        bio_classifier_nam classifier = new bio_classifier_nam();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)train_parser).id;
        classifier.setLexiconLocation("tmp/bio_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
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
        return classifier;
    }

    public static bio_classifier_nom train_nom_classifier(Parser train_parser){
        bio_classifier_nom classifier = new bio_classifier_nom();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)train_parser).id;
        classifier.setLexiconLocation("tmp/bio_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
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
        return classifier;
    }

    public static bio_classifier_pro train_pro_classifier(Parser train_parser){
        bio_classifier_pro classifier = new bio_classifier_pro();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)train_parser).id;
        classifier.setLexiconLocation("tmp/bio_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
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
        return classifier;
    }

    public static Constituent add_joint_score(bio_classifier_nam a, bio_classifier_nom b, bio_classifier_pro c, Constituent cur){
        double[] scoresToAdd = new double[9];
        ScoreSet scores_a = a.scores(cur);
        Score[] scoresArray_a = scores_a.toArray();
        ScoreSet scores_b = b.scores(cur);
        Score[] scoresArray_b = scores_b.toArray();
        ScoreSet scores_c = c.scores(cur);
        Score[] scoresArray_c = scores_c.toArray();
        for (Score score : scoresArray_a){
            if (score.value.equals("B")){
                scoresToAdd[0] = score.score;
            }
            else if (score.value.equals("I")){
                scoresToAdd[1] = score.score;
            }
            else{
                scoresToAdd[2] = score.score;
            }
        }
        for (Score score : scoresArray_b){
            if (score.value.equals("B")){
                scoresToAdd[3] = score.score;
            }
            else if (score.value.equals("I")){
                scoresToAdd[4] = score.score;
            }
            else{
                scoresToAdd[5] = score.score;
            }
        }
        for (Score score : scoresArray_c){
            if (score.value.equals("B")){
                scoresToAdd[6] = score.score;
            }
            else if (score.value.equals("I")){
                scoresToAdd[7] = score.score;
            }
            else{
                scoresToAdd[8] = score.score;
            }
        }
        String scoreString = "";
        for (double s : scoresToAdd){
            scoreString += Double.toString(s) + ",";
        }
        cur.addAttribute("BIOScores", scoreString);
        cur.addAttribute("A_prediction", a.discreteValue(cur));
        cur.addAttribute("B_prediction", b.discreteValue(cur));
        cur.addAttribute("C_prediction", c.discreteValue(cur));
        return cur;
    }

    public static bio_joint_classifier train_joint_classifier (bio_classifier_nam a, bio_classifier_nom b, bio_classifier_pro c, Parser parser){
        List<Constituent> examples = new ArrayList<>();
        int exampleCount = 0;
        for (Object example = parser.next(); example != null; example = parser.next()){
            Constituent cur = (Constituent)example;
            examples.add(add_joint_score(a, b, c, cur));
            exampleCount ++;
        }
        Parser train_parser = new ArrayToParser(examples);

        bio_joint_classifier classifier = new bio_joint_classifier();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String parser_id = ((BIOReader)parser).id;
        classifier.setLexiconLocation("tmp/bio_joint_classifier_" + parser_id + ".lex");
        Learner preExtractLearner = trainer.preExtract("tmp/bio_joint_classifier_" + parser_id + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);

        train_parser.reset();
        classifier.initialize(exampleCount, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        return classifier;
    }

    public static String joint_inference(Constituent t, bio_classifier_nam a, bio_classifier_nom b, bio_classifier_pro c, Learner classifier){
        Constituent target = add_joint_score(a, b, c, t);
        String originalTag = classifier.discreteValue(target);
        String inferedTag = originalTag;
        View bioView = target.getTextAnnotation().getView("BIO");
        //TODO: Inference on single "I"
        if (originalTag.equals("O")){
            if (target.getStartSpan() + 1 < bioView.getEndSpan()) {
                Constituent nextTest = bioView.getConstituentsCoveringToken(target.getStartSpan() + 1).get(0);
                nextTest.addAttribute("preBIOLevel1", originalTag);
                nextTest.addAttribute("preBIOLevel2", target.getAttribute("preBIOLevel1"));
                nextTest = add_joint_score(a, b, c, nextTest);
                String nextTag = classifier.discreteValue(nextTest);
                if (nextTag.equals("I")){
                    //inferedTag = "B";
                }
            }
        }
        //TODO: Hard rule on pronouns
        if (!originalTag.equals("B")) {
            if (BIOFeatureExtractor.isInPronounList(target)) {
                //inferedTag = "B";
            }
        }
        return inferedTag;
    }

    public static String inference(Constituent c, Learner classifier){
        return classifier.discreteValue(c);
    }

    public static void test_cv(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        for (int i = 0; i < 5; i++){

            Parser test_parser = new BIOReader(getPath("eval", i), "ACE05", "ALL");
            bio_label output = new bio_label();
            System.out.println("Start training fold " + i);
            Parser train_parser_nam = new BIOReader(getPath("train", i), "ACE05", "NAM");
            Parser train_parser_nom = new BIOReader(getPath("train", i), "ACE05", "NOM");
            Parser train_parser_pro = new BIOReader(getPath("train", i), "ACE05", "PRO");
            Parser train_parser_all = new BIOReader(getPath("train", i), "ACE05", "ALL");

            bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
            bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
            bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);


            //bio_joint_classifier classifier = train_joint_classifier(classifier_nam, classifier_nom, classifier_pro, train_parser_all);
            bio_classifier_nam classifier = train_nam_classifier(train_parser_all);

            int labeled_mention = 0;
            int predicted_mention = 0;
            int correct_mention = 0;

            System.out.println("Start evaluating fold " + i);
            String preBIOLevel1 = "";
            String preBIOLevel2 = "";
            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);
                String mentionType = ((Constituent)example).getAttribute("EntityMentionType");
                //String bioTag = joint_inference((Constituent)example, classifier_nam, classifier_nom, classifier_pro, classifier);
                String bioTag = inference((Constituent)example, classifier);

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
                        //preBIOLevel1_dup = joint_inference(pointerToken, classifier_nam, classifier_nom, classifier_pro, classifier);
                        preBIOLevel1_dup = inference(pointerToken, classifier);
                        preBIOLevel2_dup = preBIOLevel1_dup;
                        wholeMention += pointerToken.toString() + " ";
                        //if (!joint_inference(pointerToken, classifier_nam, classifier_nom, classifier_pro, classifier).equals(output.discreteValue(pointerToken))){
                        if (!inference(pointerToken, classifier).equals(output.discreteValue(pointerToken))){
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
                            //System.out.print(ct.toString() + " " + ct.getAttribute("BIO") + " " + inference(ct, classifier) + ", ");
                        }
                        //System.out.println();
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
