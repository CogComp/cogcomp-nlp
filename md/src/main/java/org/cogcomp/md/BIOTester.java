/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReaderWithTrueCaseFixer;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import net.didion.jwnl.JWNLException;

import org.cogcomp.DatastoreException;
import org.cogcomp.md.LbjGen.bio_classifier_nam;
import org.cogcomp.md.LbjGen.bio_classifier_nom;
import org.cogcomp.md.LbjGen.bio_classifier_pro;
import org.cogcomp.md.LbjGen.bio_label;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is for running testers and utilities.
 */

public class BIOTester {

    /**
     * Returns the corpus data paths.
     * @param mode "train/eval/all/dev"
     * @param corpus "ACE/ERE"
     * @param fold The fold index. Not used in mode "all/dev"
     */
    public static String getPath(String mode, String corpus, int fold){
        if (corpus.equals("ERE")) {
            if (mode.equals("train")) {
                return "data/ere/cv/train/" + fold;
            }
            else if (mode.equals("eval")) {
                return "data/ere/cv/eval/" + fold;
            }
            else if (mode.equals("all")){
                return "data/ere/data";
            }
            else {
                return "INVALID_PATH";
            }
        }
        else if (corpus.equals("ACE")){
            if (mode.equals("train")) {
                return "data/partition_with_dev/train/" + fold;
            }
            else if (mode.equals("eval")) {
                return "data/partition_with_dev/eval/" + fold;
            }
            else if (mode.equals("dev")){
                return "data/partition_with_dev/dev";
            }
            else if (mode.equals("all")){
                return "data/all";
            }
            else{
                return "INVALID_PATH";
            }
        }
        else {
            return "INVALID CORPUS";
        }
    }

    /**
     * Extracts the most common predicted type in a given BILOU sequence.
     */
    public static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }

    /**
     * Trainer for the head named entity classifier.
     * @param train_parser The parser containing all training examples
     * @param modelLoc The expected model file destination. Support null.
     */
    public static bio_classifier_nam train_nam_classifier(Parser train_parser, String modelLoc){
        bio_classifier_nam classifier = new bio_classifier_nam();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (int i = 0; i < 1; i++) {
            train_parser.reset();
            for (Object example = train_parser.next(); example != null; example = train_parser.next()) {
                classifier.learn(example);
            }
            classifier.doneWithRound();
        }
        classifier.doneLearning();
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_nam train_nam_classifier(Parser train_parser){
        return train_nam_classifier(train_parser, null);
    }

    /**
     * Trainer for the head nominal classifier.
     * @param train_parser The parser containing all training examples
     * @param modelLoc The expected model file destination. Support null.
     */
    public static bio_classifier_nom train_nom_classifier(Parser train_parser, String modelLoc){
        bio_classifier_nom classifier = new bio_classifier_nom();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
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
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_nom train_nom_classifier(Parser train_parser){
        return train_nom_classifier(train_parser, null);
    }

    /**
     * Trainer for the head pronoun classifier.
     * @param train_parser The parser containing all training examples
     * @param modelLoc The expected model file destination. Support null.
     */
    public static bio_classifier_pro train_pro_classifier(Parser train_parser, String modelLoc){
        bio_classifier_pro classifier = new bio_classifier_pro();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
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
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_pro train_pro_classifier(Parser train_parser){
        return train_pro_classifier(train_parser, null);
    }

    /**
     *
     * @param t The target Consitutent
     * @param candidates The learner array containing 3 Learners.
     *                    candidates[0] : NAM
     *                    candidates[1] : NOM
     *                    candidates[2] : PRO
     * @return a pair of a String and a Integer.
     *          The String: The result of the joint inferencing
     *          The Integer: The index of the selected learner in candidates
     */
    public static Pair<String, Integer> joint_inference(Constituent t, Learner[] candidates){

        double highest_start_score = -10.0;

        Map<Integer, Double> remaining = new ConcurrentHashMap<>();
        String[] preBIOLevel1 = new String[3];
        String[] preBIOLevel2 = new String[3];
        for (int i = 0; i < 3; i++){
            preBIOLevel2[i] = "O";
        }
        int chosen = -1;

        for (int i = 0; i < candidates.length; i++){
            if (candidates[i] == null){
                continue;
            }
            String prediction = candidates[i].discreteValue(t);
            preBIOLevel1[i] = prediction;
            if (prediction.startsWith("B") || prediction.startsWith("U")){
                ScoreSet scores = candidates[i].scores(t);
                Score[] scoresArray = scores.toArray();
                for (Score s : scoresArray){
                    if (s.value.equals(prediction)){
                        remaining.put(i, s.score);
                        if (s.score > highest_start_score){
                            highest_start_score = s.score;
                            chosen = i;
                        }
                    }
                }
            }
        }
        if (chosen == -1){
            return new Pair<>("O", -1);
        }
        else{
            return new Pair<>(candidates[chosen].discreteValue(t), chosen);
        }
    }

    public static String inference(Constituent c, Classifier classifier){
        return classifier.discreteValue(c);
    }

    /**
     *
     * @param curToken The token of the start of a mention (either gold/predicted)
     * @param classifier The selected classifier from joint_inference
     * @param isGold Indicates if getting the gold mention or not
     * @return A constituent of the entire mention head. The size may be larger than 1.
     */
    public static Constituent getConstituent(Constituent curToken, Classifier classifier, boolean isGold) {
        View bioView = curToken.getTextAnnotation().getView("BIO");
        String goldType = "NA";
        if (isGold) {
            if (!curToken.getAttribute("BIO").startsWith("O")) {
                goldType = (curToken.getAttribute("BIO").split("-"))[1];
            }
        }
        List<String> predictedTypes = new ArrayList<>();
        if (!isGold) {
            predictedTypes.add((inference(curToken, classifier).split("-"))[1]);
        }
        int startIdx = curToken.getStartSpan();
        int endIdx = startIdx + 1;
        //Continue to check if the predicted mentions continues to the right
        //if and only if the start predicted BIOLU tag is "B"
        if (inference(curToken, classifier).startsWith("B") && endIdx < bioView.getEndSpan()) {
            String preBIOLevel2_dup = curToken.getAttribute("preBIOLevel1");
            String preBIOLevel1_dup = inference(curToken, classifier);
            Constituent pointerToken = null;
            while (endIdx < bioView.getEndSpan()) {
                pointerToken = bioView.getConstituentsCoveringToken(endIdx).get(0);
                pointerToken.addAttribute("preBIOLevel1", preBIOLevel1_dup);
                pointerToken.addAttribute("preBIOLevel2", preBIOLevel2_dup);
                if (isGold) {
                    String curGold = pointerToken.getAttribute("BIO");
                    if (!(curGold.startsWith("I") || curGold.startsWith("L"))) {
                        break;
                    }
                }
                else {
                    String curPrediction = inference(pointerToken, classifier);
                    if (!(curPrediction.startsWith("I") || curPrediction.startsWith("L"))) {
                        break;
                    }
                    predictedTypes.add(curPrediction.split("-")[1]);
                }
                preBIOLevel2_dup = preBIOLevel1_dup;
                preBIOLevel1_dup = inference(pointerToken, classifier);
                endIdx ++;
            }
        }
        String entityType = goldType;
        String entityMentionType = curToken.getAttribute("EntityMentionType");
        if (!isGold){
            entityType = mostCommon(predictedTypes);
            String className = classifier.getClass().toString();
            //The className variable is in form "...bio_classifier_[TYPE]"
            //Take the last three characters which stands for the mention level.
            entityMentionType = className.substring(className.length() - 3).toUpperCase();
        }
        Constituent wholeMention = new Constituent(entityMentionType + "-" + entityType, 1.0f, "BIO_Mention", curToken.getTextAnnotation(), startIdx, endIdx);
        wholeMention.addAttribute("EntityType", entityType);
        wholeMention.addAttribute("EntityMentionType", entityMentionType);
        return wholeMention;
    }

    /**
     * Cross Validation tester
     * @throws DatastoreException 
     * @throws JWNLException 
     * @throws IOException 
     * @throws InvalidEndpointException 
     * @throws InvalidPortException 
     */
    public static void test_cv() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        boolean isBIO = false;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        int violations = 0;

        for (int i = 0; i < 5; i++){

            Parser test_parser = new BIOCombinedReader(i, "ERE-EVAL", "ALL");
            bio_label output = new bio_label();
            System.out.println("Start training fold " + i);
            Parser train_parser_nam = new BIOCombinedReader(i, "ERE-TRAIN", "NAM");
            Parser train_parser_nom = new BIOCombinedReader(i, "ERE-TRAIN", "NOM");
            Parser train_parser_pro = new BIOCombinedReader(i, "ERE-TRAIN", "PRO");

            bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
            bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
            bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

            Learner[] candidates = new Learner[3];
            candidates[0] = classifier_nam;
            candidates[1] = classifier_nom;
            candidates[2] = classifier_pro;

            int labeled_mention = 0;
            int predicted_mention = 0;
            int correct_mention = 0;

            System.out.println("Start evaluating fold " + i);
            String preBIOLevel1 = "";
            String preBIOLevel2 = "";

            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                System.out.println(((Constituent)example).toString());
                ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

                Pair<String, Integer> cands = joint_inference((Constituent)example, candidates);

                String bioTag = cands.getFirst();
                if (bioTag.equals("I") && !(preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }
                if (bioTag.equals("L") && !(preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }
                if (bioTag.equals("U") && (preBIOLevel1.equals("B") || preBIOLevel1.equals("I"))){
                    violations ++;
                }
                if (bioTag.equals("B") && preBIOLevel1.equals("I")){
                    violations ++;
                }
                if (bioTag.equals("O") && (preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }

                preBIOLevel2 = preBIOLevel1;
                preBIOLevel1 = bioTag;

                boolean goldStart = false;
                boolean predictedStart = false;

                if (bioTag.startsWith("B") || bioTag.startsWith("U")){
                    predicted_mention ++;
                    predictedStart = true;
                }
                String correctTag = output.discreteValue(example);

                if (correctTag.startsWith("B") || correctTag.startsWith("U")){
                    labeled_mention ++;
                    goldStart = true;
                }
                boolean correctBoundary = false;
                if (goldStart && predictedStart) {
                    int candidateIdx = cands.getSecond();
                    Constituent goldMention = getConstituent((Constituent)example, candidates[candidateIdx], true);
                    Constituent predictMention = getConstituent((Constituent)example, candidates[candidateIdx], false);
                    if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                        correctBoundary = true;
                        correct_mention++;
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
        System.out.println("violations: " + violations);
    }

    /**
     * Test set tester
     * @throws JWNLException 
     * @throws IOException 
     * @throws DatastoreException 
     * @throws InvalidEndpointException 
     * @throws InvalidPortException 
     */
    public static void test_ts() throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        boolean isBIO = false;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        int total_correct_nam = 0;
        int total_false_type_nam = 0;
        int total_correct_nom = 0;
        int total_false_type_nom = 0;
        int total_correct_pro = 0;
        int total_false_type_pro = 0;

        Parser test_parser = new BIOReader(getPath("dev", "ACE", 0), "ACE05-EVAL", "ALL", isBIO);
        Parser train_parser_nam = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NAM", isBIO);
        Parser train_parser_nom = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NOM", isBIO);
        Parser train_parser_pro = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "PRO", isBIO);
        bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
        bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
        bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

        Learner[] candidates = new Learner[3];
        candidates[0] = classifier_nam;
        candidates[1] = classifier_nom;
        candidates[2] = classifier_pro;

        String preBIOLevel1 = "";
        String preBIOLevel2 = "";

        for (Object example = test_parser.next(); example != null; example = test_parser.next()){

            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            Pair<String, Integer> cands = joint_inference((Constituent)example, candidates);

            String bioTag = cands.getFirst();
            int learnerIdx = cands.getSecond();

            preBIOLevel2 = preBIOLevel1;
            preBIOLevel1 = bioTag;

            boolean goldStart = false;
            boolean predictedStart = false;

            if (bioTag.startsWith("B") || bioTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            String correctTag = ((Constituent)example).getAttribute("BIO");

            if (correctTag.startsWith("B") || correctTag.startsWith("U")){
                total_labeled_mention ++;
                goldStart = true;
            }

            if (goldStart && predictedStart) {
                Constituent goldMention = getConstituent((Constituent)example, candidates[learnerIdx], true);
                Constituent predictMention = getConstituent((Constituent)example, candidates[learnerIdx], false);
                boolean boundaryCorrect = false;
                boolean typeCorrect = false;
                if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                    boundaryCorrect = true;
                }
                if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                    typeCorrect = true;
                }
                if (boundaryCorrect){
                    total_correct_mention ++;
                    if (learnerIdx == 0){
                        total_correct_nam ++;
                    }
                    if (learnerIdx == 1){
                        total_correct_nom ++;
                    }
                    if (learnerIdx == 2){
                        total_correct_pro ++;
                    }
                }
                if (boundaryCorrect && !typeCorrect){
                    if (learnerIdx == 0){
                        total_false_type_nam ++;
                    }
                    if (learnerIdx == 1){
                        total_false_type_nom ++;
                    }
                    if (learnerIdx == 2){
                        total_false_type_pro ++;
                        System.out.println(goldMention.getTextAnnotation().getSentenceFromToken(goldMention.getStartSpan()).toString());
                        System.out.println(goldMention.toString() + " " + goldMention.getAttribute("EntityType") + " " + predictMention.getAttribute("EntityType"));
                        System.out.println();
                    }
                }
            }
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

        System.out.println("NAM: " + total_false_type_nam + "/" + total_correct_nam);
        System.out.println("NOM: " + total_false_type_nom + "/" + total_correct_nom);
        System.out.println("PRO: " + total_false_type_pro + "/" + total_correct_pro);
    }

    /**
     * ERE corpus tester
     * @throws JWNLException 
     * @throws IOException 
     * @throws DatastoreException 
     * @throws InvalidEndpointException 
     * @throws InvalidPortException 
     */
    public static void test_ere() throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        int total_correct_type_match = 0;

        Parser test_parser = new BIOReader(getPath("all", "ERE", 0), "ERE-EVAL", "ALL", false);
        Parser train_parser_nam = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NAM", false);
        Parser train_parser_nom = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NOM", false);
        Parser train_parser_pro = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "PRO", false);
        bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
        bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
        bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

        String preBIOLevel1 = "";
        String preBIOLevel2 = "";

        Learner[] candidates = new Learner[3];
        candidates[0] = classifier_nam;
        candidates[1] = classifier_nom;
        candidates[2] = classifier_pro;

        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            Pair<String, Integer> prediction = joint_inference((Constituent)example, candidates);
            String goldTag = ((Constituent)example).getAttribute("BIO");
            String predictedTag = prediction.getFirst();
            preBIOLevel2 = preBIOLevel1;
            preBIOLevel1 = predictedTag;
            boolean goldStart = false;
            if (goldTag.startsWith("B") || goldTag.startsWith("U")){
                total_labeled_mention ++;
                goldStart = true;
            }
            boolean predictedStart = false;
            if (predictedTag.startsWith("B") || predictedTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            boolean correct = false;
            boolean type_match = false;
            if (goldStart && predictedStart){
                Constituent goldMention = getConstituent((Constituent)example, candidates[prediction.getSecond()], true);
                Constituent predictedMention = getConstituent((Constituent)example, candidates[prediction.getSecond()], false);
                if (goldMention.getStartSpan() == predictedMention.getStartSpan() && goldMention.getEndSpan() == predictedMention.getEndSpan()){
                    correct = true;
                }
                if (goldMention.getAttribute("EntityType").equals(predictedMention.getAttribute("EntityType"))){
                    type_match = true;
                }
                if (correct){
                    total_correct_mention ++;
                    if (type_match){
                        total_correct_type_match ++;
                    }
                }
            }
        }
        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        System.out.println("Total Correct Type Match: " + total_correct_type_match);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
    }

    public static void test_tac() throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        Parser train_parser = new BIOReader("data/all", "ACE05", "NOM", false);
        Parser test_parser = new BIOReader("data/tac/2016.nom", "ColumnFormat", "ALL", false);
        bio_classifier_nom classifier = train_nom_classifier(train_parser);
        String preLevel1 = "";
        String preLevel2 = "";
        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preLevel2);
            String predictedTag = inference((Constituent)example, classifier);
            String goldTag = ((Constituent)example).getAttribute("BIO");
            boolean predictedStart = false;
            boolean goldStart = false;
            if (predictedTag.startsWith("B") || predictedTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            if (goldTag.startsWith("B") || goldTag.startsWith("U")){
                total_labeled_mention ++;
                goldStart = true;
            }
            if (predictedStart && goldStart){
                Constituent goldMention = getConstituent((Constituent)example, classifier, true);
                Constituent predictedMention = getConstituent((Constituent)example, classifier, false);
                if (goldMention.getStartSpan() == predictedMention.getStartSpan() && goldMention.getEndSpan() == predictedMention.getEndSpan()){
                    total_correct_mention ++;
                }
            }
            preLevel2 = preLevel1;
            preLevel1 = predictedTag;
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

    /**
     * Calculates the average mention head size by type.
     * Research purposes only
     */
    public static void calculateAvgMentionLength(){
        ACEReader aceReader = null;
        try{
            aceReader = new ACEReader(getPath("all", "ERE", 0), false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        double nam = 0.0;
        double nom = 0.0;
        double pro = 0.0;
        double namcount = 0.0;
        double nomcount = 0.0;
        double procount = 0.0;
        for (TextAnnotation ta : aceReader){
            for (Constituent c : ta.getView(ViewNames.MENTION_ACE)){
                Constituent ch = ACEReader.getEntityHeadForConstituent(c, ta, "A");
                if (ch.getAttribute("EntityMentionType").equals("NAM")){
                    nam += (double)(ch.getEndSpan() - ch.getStartSpan());
                    namcount += 1.0;
                }
                if (ch.getAttribute("EntityMentionType").equals("NOM")){
                    nom += (double)(ch.getEndSpan() - ch.getStartSpan());
                    nomcount += 1.0;
                }
                if (ch.getAttribute("EntityMentionType").equals("PRO")){
                    pro += (double)(ch.getEndSpan() - ch.getStartSpan());
                    procount += 1.0;
                }
            }
        }
        System.out.println("NAM LENGTH: " + nam / namcount);
        System.out.println("NOM LENGTH: " + nom / nomcount);
        System.out.println("PRO LENGTH: " + pro / procount);
    }

    /**
     * Test the model trained on hybrid ACE/ERE and evaluated on hybrid ACE/ERE
     * Produce results on separate types
     * @throws DatastoreException 
     * @throws JWNLException 
     * @throws IOException 
     * @throws InvalidEndpointException 
     * @throws InvalidPortException 
     */
    public static void test_hybrid() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        int total_ace_labeled_mention = 0;
        int total_ere_labeled_mention = 0;
        int total_ace_predicted_mention = 0;
        int total_ere_predicted_mention = 0;
        int total_ace_correct_mention = 0;
        int total_ere_correct_mention = 0;
        int total_ace_type_correct = 0;
        int total_ere_type_correct = 0;


        for (int i = 0; i < 5; i++) {
            Parser test_parser = new BIOCombinedReader(i, "ALL-EVAL", "ALL");
            Parser train_parser_nam = new BIOCombinedReader(i, "ALL-TRAIN", "NAM");
            Parser train_parser_nom = new BIOCombinedReader(i, "ALL-TRAIN", "NOM");
            Parser train_parser_pro = new BIOCombinedReader(i, "ALL-TRAIN", "PRO");
            bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
            bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
            bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

            Learner[] candidates = new Learner[3];
            candidates[0] = classifier_nam;
            candidates[1] = classifier_nom;
            candidates[2] = classifier_pro;

            String preBIOLevel1 = "";
            String preBIOLevel2 = "";

            for (Object example = test_parser.next(); example != null; example = test_parser.next()) {

                ((Constituent) example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent) example).addAttribute("preBIOLevel2", preBIOLevel2);

                Pair<String, Integer> cands = joint_inference((Constituent) example, candidates);

                String bioTag = cands.getFirst();
                int learnerIdx = cands.getSecond();

                preBIOLevel2 = preBIOLevel1;
                preBIOLevel1 = bioTag;

                boolean goldStart = false;
                boolean predictedStart = false;

                if (bioTag.startsWith("B") || bioTag.startsWith("U")) {
                    total_predicted_mention++;
                    if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                            ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                        total_ace_predicted_mention ++;
                    }
                    else {
                        total_ere_predicted_mention ++;
                    }
                    predictedStart = true;
                }
                String correctTag = ((Constituent) example).getAttribute("BIO");

                if (correctTag.startsWith("B") || correctTag.startsWith("U")) {
                    total_labeled_mention++;
                    if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                            ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                        total_ace_labeled_mention ++;
                    }
                    else {
                        total_ere_labeled_mention ++;
                    }
                    goldStart = true;
                }

                if (goldStart && predictedStart) {
                    Constituent goldMention = getConstituent((Constituent) example, candidates[learnerIdx], true);
                    Constituent predictMention = getConstituent((Constituent) example, candidates[learnerIdx], false);
                    boolean boundaryCorrect = false;
                    boolean typeCorrect = false;
                    if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                        boundaryCorrect = true;
                    }
                    if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                        typeCorrect = true;
                    }
                    if (boundaryCorrect) {
                        total_correct_mention++;
                        if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                                ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                            total_ace_correct_mention ++;
                        }
                        else {
                            total_ere_correct_mention ++;
                        }
                        if (typeCorrect){
                            if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                                    ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                                total_ace_type_correct ++;
                            }
                            else {
                                total_ere_type_correct ++;
                            }
                        }
                    }
                }
            }
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

        System.out.println("Total Labeled Mention ACE: " + total_ace_labeled_mention);
        System.out.println("Total Predicted Mention ACE: " + total_ace_predicted_mention);
        System.out.println("Total Correct Mention ACE: " + total_ace_correct_mention);
        System.out.println("Total Type Correct ACE: " + total_ace_type_correct);
        p = (double)total_ace_correct_mention / (double)total_ace_predicted_mention;
        r = (double)total_ace_correct_mention / (double)total_ace_labeled_mention;
        f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        System.out.println("Total Labeled Mention ERE: " + total_ere_labeled_mention);
        System.out.println("Total Predicted Mention ERE: " + total_ere_predicted_mention);
        System.out.println("Total Correct Mention ERE: " + total_ere_correct_mention);
        System.out.println("Total Type Correct ERE: " + total_ere_type_correct);
        p = (double)total_ere_correct_mention / (double)total_ere_predicted_mention;
        r = (double)total_ere_correct_mention / (double)total_ere_labeled_mention;
        f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

    }

    public static void statistics(){
        int ace_nam = 0;
        int ace_nom = 0;
        int ace_pro = 0;
        int ere_nam = 0;
        int ere_nom = 0;
        int ere_pro = 0;
        int tac_nam = 0;
        int tac_nom = 0;
        try {
            ACEReaderWithTrueCaseFixer aceReader = new ACEReaderWithTrueCaseFixer("data/all", false);
            for (TextAnnotation ta : aceReader){
                for (Constituent c : ta.getView(ViewNames.MENTION_ACE)){
                    if (c.getAttribute("EntityMentionType").equals("NAM")){
                        ace_nam ++;
                    }
                    if (c.getAttribute("EntityMentionType").equals("NOM")){
                        ace_nom ++;
                    }
                    if (c.getAttribute("EntityMentionType").equals("PRO")){
                        ace_pro ++;
                    }
                }
            }
            EREMentionRelationReader ereReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);
            for (XmlTextAnnotation xta : ereReader){
                TextAnnotation ta = xta.getTextAnnotation();
                for (Constituent c : ta.getView(ViewNames.MENTION_ERE)){
                    if (c.getAttribute("EntityMentionType").equals("NAM")){
                        ere_nam ++;
                    }
                    if (c.getAttribute("EntityMentionType").equals("NOM")){
                        ere_nom ++;
                    }
                    if (c.getAttribute("EntityMentionType").equals("PRO")){
                        ere_pro ++;
                    }
                }
            }
            ColumnFormatReader columnFormatReader = new ColumnFormatReader("data/tac/2016.nam");
            for (TextAnnotation ta : columnFormatReader){
                for (Constituent c : ta.getView("MENTIONS")){
                    tac_nam ++;
                }
            }
            columnFormatReader = new ColumnFormatReader("data/tac/2016.nom");
            for (TextAnnotation ta : columnFormatReader){
                for (Constituent c : ta.getView("MENTIONS")){
                    tac_nom ++;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("ACE_NAM: " + ace_nam);
        System.out.println("ACE_NOM: " + ace_nom);
        System.out.println("ACE_PRO: " + ace_pro);
        System.out.println("ERE_NAM: " + ere_nam);
        System.out.println("ERE_NOM: " + ere_nom);
        System.out.println("ERE_PRO: " + ere_pro);
        System.out.println("TAC_NAM: " + tac_nam);
        System.out.println("TAC_NOM: " + tac_nom);
    }

    public static void TrainModel(String corpus) throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        if (corpus.equals("ACE")) {
            Parser train_parser_nam = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NAM", false);
            Parser train_parser_nom = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NOM", false);
            Parser train_parser_pro = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "PRO", false);
            train_nam_classifier(train_parser_nam, "models/ACE_NAM");
            train_nom_classifier(train_parser_nom, "models/ACE_NOM");
            train_pro_classifier(train_parser_pro, "models/ACE_PRO");
        }
        else if (corpus.equals("ERE")){
            Parser train_parser_nam = new BIOReader(getPath("all", "ERE", 0), "ACE05-TRAIN", "NAM", false);
            Parser train_parser_nom = new BIOReader(getPath("all", "ERE", 0), "ACE05-TRAIN", "NOM", false);
            Parser train_parser_pro = new BIOReader(getPath("all", "ERE", 0), "ACE05-TRAIN", "PRO", false);
            train_nam_classifier(train_parser_nam, "models/ERE_NAM");
            train_nom_classifier(train_parser_nom, "models/ERE_NOM");
            train_pro_classifier(train_parser_pro, "models/ERE_PRO");
        }
    }

    public static void TrainACEModel() throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        TrainModel("ACE");
    }

    public static void TrainEREModel() throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        TrainModel("ERE");
    }

    public static void main(String[] args){
        if (args.length == 0){
            System.out.println("No method call given.");
            return;
        }
        String methodName;
        String methodValue = null;
        Class[] parameters = new Class[]{};
        methodName = args[0];
        try {
            Method m = BIOTester.class.getMethod(methodName, parameters);
            Object ret = m.invoke(methodValue, parameters);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
