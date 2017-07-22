package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.nlp.ColumnFormat;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ColumnFormatReader;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;

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
        for (int i = 0; i < 1; i++) {
            train_parser.reset();
            for (Object example = train_parser.next(); example != null; example = train_parser.next()) {
                classifier.learn(example);
            }
            classifier.doneWithRound();
        }
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

    public static bio_classifier_pro train_pro_classifier(bio_classifier_nam classifier_nam, bio_classifier_nom classifier_nom, Parser train_parser){
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

        if (a.discreteValue(t).equals("B") || b.discreteValue(t).equals("B") || c.discreteValue(t).equals("B")){
            return "B";
        }
        if (a.discreteValue(t).equals("I") || b.discreteValue(t).equals("I") || c.discreteValue(t).equals("I")){
            return "I";
        }
        else{
            return "O";
        }
        /*
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
        */
    }

    public static String inference(Constituent c, Classifier classifier){
        View posView = c.getTextAnnotation().getView(ViewNames.POS);
        List<Constituent> posCons = posView.getConstituentsCoveringToken(c.getStartSpan());
        if (posCons.size() > 0){
            String posLabel = posCons.get(0).getLabel();
            if (posLabel.contains("PRP") || posLabel.contains("WP")){
                //return "B";
            }
        }
        if (!BIOFeatureExtractor.isInPronounList(c).equals("")){
            //return "B";
        }
        return classifier.discreteValue(c);
    }

    public static Constituent getConstituent(Constituent curToken, Classifier classifier, boolean isGold) {
        View bioView = curToken.getTextAnnotation().getView("BIO");
        String goldType = (curToken.getAttribute("BIO").split("-"))[1];
        List<String> predictedTypes = new ArrayList<>();
        System.out.println(inference(curToken, classifier));
        predictedTypes.add((inference(curToken, classifier).split("-"))[1]);
        int startIdx = curToken.getStartSpan();
        int endIdx = startIdx + 1;
        if (endIdx < bioView.getEndSpan()) {
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

        Constituent wholeMention = new Constituent(curToken.getLabel(), 1.0f, "BIO_Mention", curToken.getTextAnnotation(), startIdx, endIdx);
        if (isGold){
            wholeMention.addAttribute("EntityType", goldType);
        }
        else{
            wholeMention.addAttribute("EntityType", mostCommon(predictedTypes));
        }
        return wholeMention;
    }

    public static void test_cv(){
        boolean isBIO = true;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        for (int i = 0; i < 5; i++){

            Parser test_parser = new BIOReader(getPath("eval", i), "ACE05", "ALL", isBIO);
            bio_label output = new bio_label();
            System.out.println("Start training fold " + i);
            Parser train_parser_nam = new BIOReader(getPath("train", i), "ACE05", "NAM", isBIO);
            Parser train_parser_nom = new BIOReader(getPath("train", i), "ACE05", "NOM", isBIO);
            Parser train_parser_pro = new BIOReader(getPath("train", i), "ACE05", "PRO", isBIO);
            Parser train_parser_all = new BIOReader(getPath("train", i), "ACE05", "ALL", isBIO);

            bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
            bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
            bio_classifier_pro classifier_pro = train_pro_classifier(classifier_nam, classifier_nom, train_parser_pro);

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

                String bioTag = inference((Constituent)example, classifier);

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

                if (goldStart && predictedStart) {
                    Constituent goldMention = getConstituent((Constituent)example, classifier, true);
                    Constituent predictMention = getConstituent((Constituent)example, classifier, false);
                    if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                        if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                            correct_mention++;
                        }
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

    public static void test_ts(){
        boolean isBIO = true;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        Parser test_parser = new BIOReader("data/partition_with_dev/dev", "ACE05", "NOM", isBIO);
        Parser train_parser_all = new BIOReader("data/all", "ACE05", "NOM", isBIO);
        bio_classifier_nom classifier = train_nom_classifier(train_parser_all);
        String preBIOLevel1 = "";
        String preBIOLevel2 = "";

        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            String bioTag = inference((Constituent)example, classifier);

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
                Constituent goldMention = getConstituent((Constituent)example, classifier, true);
                Constituent predictMention = getConstituent((Constituent)example, classifier, false);
                if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                    if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                        total_correct_mention++;
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
    }

    public static Pair<String, String> inference_with_type(bio_classifier_nam classifier_nam, bio_classifier_nom classifier_nom, bio_classifier_pro classifier_pro, Constituent c){
        String[] tags = new String[3];
        tags[0] = classifier_nam.discreteValue(c);
        tags[1] = classifier_nom.discreteValue(c);
        tags[2] = classifier_pro.discreteValue(c);
        int b_count = 0;
        int i_count = 0;
        int o_count = 0;
        for (String s : tags){
            if (s.equals("B")){
                b_count ++;
            }
            if (s.equals("I")){
                i_count ++;
            }
            if (s.equals("O")){
                o_count ++;
            }
        }
        if (b_count == 1 && o_count == 2){
            if (tags[0].equals("B")){
                return new Pair<>("B", "NAM");
            }
            else if (tags[1].equals("B")){
                return new Pair<>("B", "NOM");
            }
            else {
                return new Pair<>("B", "PRO");
            }
        }
        if (i_count == 1 && o_count == 2){
            if (tags[0].equals("I")){
                return new Pair<>("I", "NAM");
            }
            else if (tags[1].equals("B")){
                return new Pair<>("I", "NOM");
            }
            else {
                return new Pair<>("I", "PRO");
            }
        }
        double highest_b_score = -10.0;
        double highest_i_score = -10.0;
        String highest_b_tag = "";
        String highest_i_tag = "";
        if (tags[0].equals("B") || tags[0].equals("I")) {
            ScoreSet scores = classifier_nam.scores(c);
            Score[] scoresArray = scores.toArray();
            for (Score score : scoresArray) {
                if (score.value.equals("B") && tags[0].equals("B")) {
                    double b_score = score.score;
                    if (b_score > highest_b_score){
                        highest_b_score = b_score;
                        highest_b_tag = "NAM";
                    }
                } else if (score.value.equals("I") && tags[0].equals("I")) {
                    double i_score = score.score;
                    if (i_score > highest_i_score){
                        highest_i_score = i_score;
                        highest_i_tag = "NAM";
                    }
                }
            }
        }
        if (tags[1].equals("B") || tags[1].equals("I")){
            ScoreSet scores = classifier_nom.scores(c);
            Score[] scoresArray = scores.toArray();
            for (Score score : scoresArray){
                if (score.value.equals("B") && tags[1].equals("B")) {
                    double b_score = score.score;
                    if (b_score > highest_b_score){
                        highest_b_score = b_score;
                        highest_b_tag = "NOM";
                    }
                } else if (score.value.equals("I") && tags[1].equals("I")) {
                    double i_score = score.score;
                    if (i_score > highest_i_score){
                        highest_i_score = i_score;
                        highest_i_tag = "NOM";
                    }
                }
            }
        }
        if (tags[2].equals("B") || tags[2].equals("I")){
            ScoreSet scores = classifier_pro.scores(c);
            Score[] scoresArray = scores.toArray();
            for (Score score : scoresArray){
                if (score.value.equals("B") && tags[2].equals("B")) {
                    double b_score = score.score;
                    if (b_score > highest_b_score){
                        highest_b_score = b_score;
                        highest_b_tag = "PRO";
                    }
                } else if (score.value.equals("I") && tags[2].equals("I")) {
                    double i_score = score.score;
                    if (i_score > highest_i_score){
                        highest_i_score = i_score;
                        highest_i_tag = "PRO";
                    }
                }
            }
        }
        if (b_count == 2 && o_count == 1){
            return new Pair<>("B", highest_b_tag);
        }
        if (i_count == 2 && o_count == 1){
            return new Pair<>("I", highest_i_tag);
        }
        if (b_count >= 1 && i_count >= 1){
            if (b_count > i_count){
                return new Pair<>("B", highest_b_tag);
            }
            else if (i_count > b_count){
                return new Pair<>("I", highest_i_tag);
            }
            else{
                if (highest_b_score > highest_i_score){
                    return new Pair<>("B", highest_b_tag);
                }
                else{
                    return new Pair<>("I", highest_i_tag);
                }
            }
        }
        return new Pair<>("O", "NA");
    }

    public static void test_ere(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        Map<String, Integer> type_map = new HashMap<>();
        type_map.put("l_nam", 0);
        type_map.put("l_nom", 0);
        type_map.put("l_pro", 0);
        type_map.put("p_nam", 0);
        type_map.put("p_nom", 0);
        type_map.put("p_pro", 0);
        type_map.put("c_nam", 0);
        type_map.put("c_nom", 0);
        type_map.put("c_pro", 0);

        Parser test_parser = new BIOReader("data/ere/data", "ERE", "NOM", true);
        Parser train_parser_nam = new BIOReader("data/all", "ACE05", "NAM", true);
        Parser train_parser_nom = new BIOReader("data/all", "ACE05", "NOM", true);
        Parser train_parser_pro = new BIOReader("data/all", "ACE05", "PRO", true);
        bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
        bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
        bio_classifier_pro classifier_pro = train_pro_classifier(null, null, train_parser_pro);
        no_other_type_nom classifier = new no_other_type_nom(classifier_nam, classifier_nom, classifier_pro);

        String preBIOLevel1 = "";
        String preBIOLevel2 = "";
        Set<String> ACEDict = new HashSet<String>();
        try{
            ACEReader aceReader = new ACEReader("data/all", false);
            for (TextAnnotation ta : aceReader){
                for (Constituent c : ta.getView(ViewNames.MENTION_ACE)){
                    if (c.getAttribute("EntityMentionType").equals("NOM")){
                        Constituent cHead = ACEReader.getEntityHeadForConstituent(c, ta, "A");
                        for (int i = cHead.getStartSpan(); i < cHead.getEndSpan(); i++){
                            ACEDict.add(ta.getToken(i).toLowerCase());
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        int total_miss = 0;
        int total_miss_not_in_dict = 0;
        int total_dict_hit = 0;
        Map<String, Integer> mentionTypeErrorMap = new HashedMap();
        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            String predictedTag = inference((Constituent)example, classifier);
            String goldTag = ((Constituent)example).getAttribute("BIO");
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
                Constituent goldMention = getConstituent((Constituent)example, classifier, true);
                Constituent predictedMention = getConstituent((Constituent)example, classifier, false);
                if (goldMention.getStartSpan() == predictedMention.getStartSpan() && goldMention.getEndSpan() == predictedMention.getEndSpan()){
                    correct = true;
                    if (ACEDict.contains(goldMention.getTextAnnotation().getToken(goldMention.getStartSpan()).toLowerCase())) {
                        total_dict_hit++;
                    }
                }
                if (goldMention.getAttribute("EntityType").equals(predictedMention.getAttribute("EntityType"))){
                    type_match = true;
                }
                else {
                    String error = goldMention.getAttribute("EntityType") + " " + predictedMention.getAttribute("EntityType");
                    if (mentionTypeErrorMap.containsKey(error)){
                        mentionTypeErrorMap.put(error, mentionTypeErrorMap.get(error) + 1);
                    }
                    else{
                        mentionTypeErrorMap.put(error, 1);
                    }
                }
                if (correct){
                    total_correct_mention ++;
                }
            }
/*
            if (goldStart && !correct){
                total_miss ++;
                Constituent goldMention = getConstituent((Constituent)example, classifier, true);
                View bioView = goldMention.getTextAnnotation().getView("BIO");
                Sentence sentence = goldMention.getTextAnnotation().getSentenceFromToken(goldMention.getStartSpan());
                for (int i = sentence.getStartSpan(); i < sentence.getEndSpan(); i++){
                    Constituent curC = bioView.getConstituentsCoveringToken(i).get(0);
                    System.out.print(curC.toString() + "(" + curC.getAttribute("BIO") + " " + inference(curC, classifier) + ") ");
                }
                System.out.println();
                System.out.println();
                if (!ACEDict.contains(goldMention.getTextAnnotation().getToken(goldMention.getStartSpan()).toLowerCase())){
                    total_miss_not_in_dict ++;
                }
            }
*/
        }
        System.out.println("Total miss: " + total_miss);
        System.out.println("Miss dict: " + total_miss_not_in_dict);
        System.out.println("Hit dict: " + total_dict_hit);
        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        for (String key : mentionTypeErrorMap.keySet()){
            System.out.println(key + ": " + mentionTypeErrorMap.get(key));
        }
    }

    public static void test_tac(){
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

    public static void main(String[] args){
        /*
        WordNetManager wordNet = null;
        try {
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(BIOFeatureExtractor.getWordNetTags(wordNet, "resorts"));
        */
        test_ere();
    }
}
