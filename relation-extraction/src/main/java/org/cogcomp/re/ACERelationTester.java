/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.md.MentionAnnotator;
import org.cogcomp.re.LbjGen.fine_relation_label;
import org.cogcomp.re.LbjGen.relation_classifier;

import java.util.Properties;

public class ACERelationTester {

    public static String getCoarseType(String fine_type){
        if (fine_type.equals("Located") || fine_type.equals("Located_OP")
                || fine_type.equals("Near")){
            return "PHYS";
        }
        if (fine_type.equals("Geographical") || fine_type.equals("Geographical_OP")
                || fine_type.equals("Subsidiary") || fine_type.equals("Subsidiary_OP")
                || fine_type.equals("Artifact") || fine_type.equals("Artifact_OP")){
            return "PART-WHOLE";
        }
        if (fine_type.equals("Business")
                || fine_type.equals("Lasting-Personal")
                || fine_type.equals("Family")){
            return "PER-SOC";
        }
        if (fine_type.equals("Employment") || fine_type.equals("Employment_OP")
                || fine_type.equals("Ownership") || fine_type.equals("Ownership_OP")
                || fine_type.equals("Founder") || fine_type.equals("Founder_OP")
                || fine_type.equals("Student-Alum") || fine_type.equals("Student-Alum_OP")
                || fine_type.equals("Sports-Affiliation") || fine_type.equals("Sports-Affiliation_OP")
                || fine_type.equals("Investor-Shareholder") || fine_type.equals("Investor-Shareholder_OP")
                || fine_type.equals("Membership") || fine_type.equals("Membership_OP")){
            return "ORG-AFF";
        }
        if (fine_type.equals("User-Owner-Inventor-Manufacturer") || fine_type.equals("User-Owner-Inventor-Manufacturer_OP")){
            return "ART";
        }
        if (fine_type.equals("Citizen-Resident-Religion-Ethnicity") || fine_type.equals("Citizen-Resident-Religion-Ethnicity_OP")
                || fine_type.equals("Org-Location") || fine_type.equals("Org-Location_OP")){
            return "GEN-AFF";
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
            int examples = train_parser.relations_bi.size();
            classifier.initialize(examples, preExtractLearner.getLexicon().size());
            for (Relation r : train_parser.relations_bi){
                classifier.learn(r);
            }
            classifier.doneWithRound();
            classifier.doneLearning();

            ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);
            ACEMentionReader test_parser = IOHelper.readFiveFold(i, "TEST");
            for (Relation r : test_parser.relations_bi){
                String predicted_label = constrainedClassifier.discreteValue(r);
                String gold_label = output.discreteValue(r);
                Relation oppoR = new Relation("TO_TEST", r.getTarget(), r.getSource(), 1.0f);
                String oppo_predicted_label = constrainedClassifier.discreteValue(oppoR);
                if (!predicted_label.equals(ACEMentionReader.getOppoName(oppo_predicted_label))){
                    ScoreSet scores = classifier.scores(r);
                    Score[] scoresArray = scores.toArray();
                    double score_curtag = 0.0;
                    for (Score score : scoresArray){
                        if (score.value.equals(predicted_label)){
                            score_curtag = score.score;
                        }
                    }
                    scores = classifier.scores((Object)oppoR);
                    scoresArray = scores.toArray();
                    double oppo_score_opptag = 0.0;
                    for (Score score : scoresArray){
                        if (score.value.equals(oppo_predicted_label)){
                            oppo_score_opptag = score.score;
                        }
                    }
                    if (score_curtag < oppo_score_opptag && oppo_score_opptag - score_curtag > 0.005){
                        predicted_label = ACEMentionReader.getOppoName(oppo_predicted_label);
                    }
                }
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

    public static void test_ts_predicted(){
        int total_correct = 0;
        int total_labeled = 0;
        int total_predicted = 0;
        int total_coarse_correct = 0;

        ACEMentionReader train_parser = IOHelper.serializeDataIn("relation-extraction/preprocess/reader/all");
        relation_classifier classifier = new relation_classifier();
        classifier.setLexiconLocation("models/relation_classifier_all");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Learner preExtractLearner = trainer.preExtract("models/relation_classifier_all", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = train_parser.relations_bi.size();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Relation r : train_parser.relations_bi){
            classifier.learn(r);
        }
        classifier.doneWithRound();
        classifier.doneLearning();

        ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(classifier);

        PredictedMentionReader predictedMentionReader = new PredictedMentionReader("data/partition_with_dev/dev");
        total_labeled = predictedMentionReader.size_of_gold_relations;
        for (Object o = predictedMentionReader.next(); o != null; o = predictedMentionReader.next()){
            Relation r = (Relation)o;
            String gold_label = r.getAttribute("RelationSubtype");
            String predicted_label = constrainedClassifier.discreteValue(r);
            if (!predicted_label.equals("NOT_RELATED")){
                total_predicted ++;
            }
            if (!gold_label.equals("NOT_RELATED")) {
                if (gold_label.equals(predicted_label)) {
                    total_correct ++;
                }
                if (getCoarseType(gold_label).equals(getCoarseType(predicted_label))){
                    total_coarse_correct ++;
                }
            }
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
        int total_correct = 0;
        int total_labeled = 0;
        int total_predicted = 0;
        int total_coarse_correct = 0;
        try {
            POSAnnotator pos_annotator = new POSAnnotator();
            ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            ACEReader aceReader = new ACEReader("data/partition_with_dev/dev", false);
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            RelationAnnotator relationAnnotator = new RelationAnnotator();
            for (TextAnnotation ta : aceReader){
                ta.addView(pos_annotator);
                stanfordDepHandler.addView(ta);
                chunker.addView(ta);
                mentionAnnotator.addView(ta);
                relationAnnotator.addView(ta);
                total_labeled += ta.getView(ViewNames.MENTION_ACE).getRelations().size();
                total_predicted += ta.getView(ViewNames.RELATION).getRelations().size();
                for (Relation pr : ta.getView(ViewNames.RELATION).getRelations()){
                    for (Relation gr : ta.getView(ViewNames.MENTION_ACE).getRelations()){
                        Constituent prSourceHead = RelationFeatureExtractor.getEntityHeadForConstituent(pr.getSource(), ta, "");
                        Constituent grSourceHead = RelationFeatureExtractor.getEntityHeadForConstituent(gr.getSource(), ta, "");
                        Constituent prTargetHead = RelationFeatureExtractor.getEntityHeadForConstituent(pr.getTarget(), ta, "");
                        Constituent grTargetHead = RelationFeatureExtractor.getEntityHeadForConstituent(gr.getTarget(), ta, "");
                        if (prSourceHead.getStartSpan() == grSourceHead.getStartSpan() &&
                                prSourceHead.getEndSpan() == grSourceHead.getEndSpan() &&
                                prTargetHead.getEndSpan() == grTargetHead.getEndSpan() &&
                                prTargetHead.getStartSpan() == grTargetHead.getStartSpan()){
                            if (pr.getAttribute("RelationType").equals(gr.getAttribute("RelationType"))){
                                total_coarse_correct ++;
                            }
                            if (pr.getAttribute("RelationSubtype").equals(gr.getAttribute("RelationSubtype"))) {
                                total_correct++;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
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

    public static void testRandomText(String text){
        String corpus = "";
        String textId = "";

        TextAnnotationBuilder stab =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = stab.createTextAnnotation(corpus, textId, text);

        try {
            POSAnnotator pos_annotator = new POSAnnotator();
            ChunkerAnnotator chunker = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            RelationAnnotator relationAnnotator = new RelationAnnotator();
            ta.addView(pos_annotator);
            stanfordDepHandler.addView(ta);
            chunker.addView(ta);
            mentionAnnotator.addView(ta);
            relationAnnotator.addView(ta);
            for (Relation r : ta.getView(ViewNames.RELATION).getRelations()){
                IOHelper.printRelation(r);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void generateModel(String serializedDataInput, String modelLoc){
        ACEMentionReader train_parser = IOHelper.serializeDataIn(serializedDataInput);
        relation_classifier classifier = new relation_classifier();
        classifier.setLexiconLocation(modelLoc + ".lex");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Learner preExtractLearner = trainer.preExtract(modelLoc + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = train_parser.relations_bi.size();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Relation r : train_parser.relations_bi){
            classifier.learn(r);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        classifier.setModelLocation(modelLoc + ".lc");
        classifier.saveModel();
    }

    public static void main(String[] args){
        //generateModel("relation-extraction/preprocess/reader/all", "models/ACE_GOLD_BI_NO_LEXICAL");
        //testAnnotator();
        //test_cv_gold();
        testRandomText("The Italian Ministry of Defense visited Tel Aviv’s company and traveled to Chicago, Illinois, with the President of the US.");
    }
}
