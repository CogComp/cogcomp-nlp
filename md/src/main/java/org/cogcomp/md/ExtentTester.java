/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.md;

import org.cogcomp.md.LbjGen.*;

import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import net.didion.jwnl.JWNLException;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This is the tester and utilities for extent classification given heads
 */
public class ExtentTester {

    public static extent_classifier train_extent_classifier(ExtentReader train_parser, String prefix){
        extent_classifier classifier = new extent_classifier();
        String modelFileName = "";
        if (prefix == null){
            String postfix = train_parser.getId();
            modelFileName = "tmp/extent_classifier_" +  postfix;
        }
        else{
            modelFileName = prefix;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Lexicon lexicon = trainer.preExtract(modelFileName + ".ex", true);
        classifier.setLexicon(lexicon);
        classifier.setModelLocation(modelFileName + ".lc");
        trainer.train(1);
        classifier.saveModel();
        return classifier;
    }

    public static extent_classifier train_extent_classifier(ExtentReader train_parser){
        return train_extent_classifier(train_parser, null);
    }

    public static void testSimpleExtent() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        int true_labeled = 0;
        int true_predicted = 0;
        int true_correct = 0;
        int false_labeled = 0;
        int false_predicted = 0;
        int false_correct = 0;
        for (int i = 0; i < 5; i++){
            ExtentReader train_parser = new ExtentReader("data/partition_with_dev/train/"  + i);
            extent_classifier classifier = train_extent_classifier(train_parser);

            extentLabel output = new extentLabel();
            Parser test_parser = new ExtentReader("data/partition_with_dev/eval/" + i);
            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                String pTag = classifier.discreteValue(example);
                String gTag = output.discreteValue(example);
                if (pTag.equals("true")){
                    true_predicted ++;
                }
                else {
                    false_predicted ++;
                }
                if (gTag.equals("true")){
                    true_labeled ++;
                }
                else{
                    false_labeled ++;
                }
                if (pTag.equals(gTag)){
                    if (pTag.equals("true")){
                        true_correct ++;
                    }
                    else {
                        false_correct ++;
                    }
                }
            }
        }
        System.out.println("Total Labeled True: " + true_labeled);
        System.out.println("Total Predicted True: " + true_predicted);
        System.out.println("Total Correct True: " + true_correct);
        double p = (double)true_correct / (double)true_predicted;
        double r = (double)true_correct / (double)true_labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("True Precision: " + p);
        System.out.println("True Recall: " + r);
        System.out.println("True F1: " + f);

        System.out.println("Total Labeled False: " + false_labeled);
        System.out.println("Total Predicted False: " + false_predicted);
        System.out.println("Total Correct False: " + false_correct);
        p = (double)false_correct / (double)false_predicted;
        r = (double)false_correct / (double)false_labeled;
        f = 2 * p * r / (p + r);
        System.out.println("False Precision: " + p);
        System.out.println("False Recall: " + r);
        System.out.println("False F1: " + f);
    }

    public static void addHeadAttributes(Constituent head, Gazetteers gazetteers, BrownClusters brownClusters, WordNetManager wordnet){
        View tokenView = head.getTextAnnotation().getView(ViewNames.TOKENS);
        for (int i = head.getStartSpan(); i < head.getEndSpan(); i++) {
            head.addAttribute("GAZ" + i, ((FlatGazetteers) gazetteers).annotateConstituent(tokenView.getConstituentsCoveringToken(i).get(0), false));
            head.addAttribute("BC" + i, brownClusters.getPrefixesCombined(tokenView.getConstituentsCoveringToken(i).get(0).toString()));
        }
        head.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(head));
    }

    public static void addExtentAttributes(Constituent extent, Gazetteers gazetteers, BrownClusters brownClusters, WordNetManager wordnet){
        View tokenView = extent.getTextAnnotation().getView(ViewNames.TOKENS);
        extent.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotateConstituent(extent, false));
        extent.addAttribute("BC", brownClusters.getPrefixesCombined(extent.toString()));
        if (extent.getStartSpan() - 1 > tokenView.getStartSpan()) {
            extent.addAttribute("BCm1", brownClusters.getPrefixesCombined(tokenView.getConstituentsCoveringToken(extent.getStartSpan() - 1).get(0).toString()));
        }
        else {
            extent.addAttribute("BCm1", ",");
        }
        if (extent.getStartSpan() + 1 < tokenView.getEndSpan()) {
            extent.addAttribute("BCp1", brownClusters.getPrefixesCombined(tokenView.getConstituentsCoveringToken(extent.getStartSpan() + 1).get(0).toString()));
        }
        else {
            extent.addAttribute("BCm1", ",");
        }
        extent.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordnet, extent));
        extent.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordnet, extent));
    }

    /**
     * Gets the full mention of the given  head
     * @param classifier The extent classifier
     * @param head The head Constituent
     * @param gazetteers gazetteers
     * @param brownClusters brownclusters
     * @param wordnet wordnet
     * @return A Constituent of a full mention (extent included)
     * @Note The returned Constituent has Attributes "EntityHeadStartSpan" and "EntityHeadEndSpan"
     */
    public static Constituent getFullMention(extent_classifier classifier, Constituent head, Gazetteers gazetteers, BrownClusters brownClusters, WordNetManager wordnet){
        addHeadAttributes(head, gazetteers, brownClusters, wordnet);
        View tokenView = head.getTextAnnotation().getView(ViewNames.TOKENS);
        int leftIdx = head.getStartSpan() - 1;
        while (leftIdx >= tokenView.getStartSpan()){
            Constituent cur = tokenView.getConstituentsCoveringToken(leftIdx).get(0);
            addExtentAttributes(cur, gazetteers, brownClusters, wordnet);
            Relation candidate = new Relation("UNKNOWN", cur, head, 1.0f);
            String prediction = classifier.discreteValue(candidate);
            if (prediction.equals("false")){
                leftIdx ++;
                break;
            }
            leftIdx --;
        }
        if (leftIdx < tokenView.getStartSpan()){
            leftIdx = tokenView.getStartSpan();
        }
        int rightIdx = head.getEndSpan();
        while (rightIdx < tokenView.getEndSpan()){
            Constituent cur = tokenView.getConstituentsCoveringToken(rightIdx).get(0);
            addExtentAttributes(cur, gazetteers, brownClusters, wordnet);
            Relation candidate = new Relation("UNKNOWN", cur, head, 1.0f);
            String prediction = classifier.discreteValue(candidate);
            if (prediction.equals("false")){
                rightIdx --;
                break;
            }
            rightIdx ++;
        }
        if (rightIdx >= tokenView.getEndSpan()){
            rightIdx = tokenView.getEndSpan() - 1;
        }
        Constituent fullMention = new Constituent(head.getLabel(), 1.0f, ViewNames.MENTION, head.getTextAnnotation(), leftIdx, rightIdx + 1);
        fullMention.addAttribute("EntityHeadStartSpan", Integer.toString(head.getStartSpan()));
        fullMention.addAttribute("EntityHeadEndSpan", Integer.toString(head.getEndSpan()));
        fullMention.addAttribute("EntityType", head.getAttribute("EntityType"));
        fullMention.addAttribute("EntityMentionType", head.getAttribute("EntityMentionType"));
        return fullMention;
    }

    public static void testExtentOnGoldHead() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        int labeled = 0;
        int correct = 0;
        POSAnnotator posAnnotator = null;
        WordNetManager wordNet = null;
        Gazetteers gazetteers = null;
        BrownClusters brownClusters = null;
        try{
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
            posAnnotator = new POSAnnotator();
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            gazetteers = GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", true, Language.English);
            Vector<String> bcs = new Vector<>();
            bcs.add("brown-clusters" + File.separator + "brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
            bcs.add("brown-clusters" + File.separator + "brownBllipClusters");
            bcs.add("brown-clusters" + File.separator + "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt");
            Vector<Integer> bcst = new Vector<>();
            bcst.add(5);
            bcst.add(5);
            bcst.add(5);
            Vector<Boolean> bcsl = new Vector<>();
            bcsl.add(false);
            bcsl.add(false);
            bcsl.add(false);
            brownClusters = BrownClusters.get(bcs, bcst, bcsl);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        for (int i = 0; i < 1; i++) {
            ExtentReader train_parser = new ExtentReader("data/partition_with_dev/train/"  + i, "COMBINED-ALL-TRAIN-" + i);
            extent_classifier classifier = train_extent_classifier(train_parser);
            BIOCombinedReader bioCombinedReader = null;
            try{
                bioCombinedReader = new BIOCombinedReader(i, "ALL-EVAL", "ALL", true);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            for (Object ota = bioCombinedReader.next(); ota != null; ota = bioCombinedReader.next()){
                TextAnnotation ta = (TextAnnotation)ota;
                try {
                    ta.addView(posAnnotator);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                String mentionViewName = ViewNames.MENTION_ERE;
                if (ta.getId().startsWith("bn") || ta.getId().startsWith("nw")){
                    mentionViewName = ViewNames.MENTION_ACE;
                }
                View mentionView = ta.getView(mentionViewName);
                for (Constituent mention : mentionView.getConstituents()){
                    Constituent head = ACEReader.getEntityHeadForConstituent(mention, ta, "HEADS");
                    if (head == null){
                        continue;
                    }
                    labeled ++;
                    Constituent predictedFullMention = getFullMention(classifier, head, gazetteers, brownClusters, wordNet);
                    if (predictedFullMention.getStartSpan() == mention.getStartSpan() &&
                            predictedFullMention.getEndSpan() == mention.getEndSpan()){
                        correct ++;
                    }
                    else {
                        System.out.println("Gold: " + mention.toString());
                        System.out.println("Predicted: " + predictedFullMention.toString());
                    }
                }
            }
        }
        System.out.println("Labeled: " + labeled);
        System.out.println("Correct: " + correct);
        System.out.println("Correctness: " + (double)correct * 100.0 / (double)labeled );
    }

    public static Constituent getPredictedMentionHead(Constituent c){
        return new Constituent(c.getLabel(), "HEAD", c.getTextAnnotation(),
                Integer.parseInt(c.getAttribute("EntityHeadStartSpan")),
                Integer.parseInt(c.getAttribute("EntityHeadEndSpan")));
    }

    public static void testExtentOnPredictedHead() throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        WordNetManager wordNet = null;
        Gazetteers gazetteers = null;
        BrownClusters brownClusters = null;
        try{
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            gazetteers = GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", true, Language.English);
            Vector<String> bcs = new Vector<>();
            bcs.add("brown-clusters" + File.separator + "brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
            bcs.add("brown-clusters" + File.separator + "brownBllipClusters");
            bcs.add("brown-clusters" + File.separator + "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt");
            Vector<Integer> bcst = new Vector<>();
            bcst.add(5);
            bcst.add(5);
            bcst.add(5);
            Vector<Boolean> bcsl = new Vector<>();
            bcsl.add(false);
            bcsl.add(false);
            bcsl.add(false);
            brownClusters = BrownClusters.get(bcs, bcst, bcsl);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        int total_mention_predicted = 0;
        int total_mention_labeled = 0;
        int total_mention_head_correct = 0;
        int total_mention_extent_correct = 0;
        for (int i = 0; i < 5; i++) {
            BIOReader h_train_parser_nam = new BIOReader("data/partition_with_dev/train/" + i, "ACE05-TRAIN", "NAM", false);
            BIOReader h_train_parser_nom = new BIOReader("data/partition_with_dev/train/" + i, "ACE05-TRAIN", "NOM", false);
            BIOReader h_train_parser_pro = new BIOReader("data/partition_with_dev/train/" + i, "ACE05-TRAIN", "PRO", false);

            bio_classifier_nam h_classifier_nam = BIOTester.train_nam_classifier(h_train_parser_nam);
            bio_classifier_nom h_classifier_nom = BIOTester.train_nom_classifier(h_train_parser_nom);
            bio_classifier_pro h_classifier_pro = BIOTester.train_pro_classifier(h_train_parser_pro);
            Learner[] h_candidates = new Learner[3];
            h_candidates[0] = h_classifier_nam;
            h_candidates[1] = h_classifier_nom;
            h_candidates[2] = h_classifier_pro;

            ExtentReader e_train_parser = new ExtentReader("data/partition_with_dev/train/"  + i);
            extent_classifier e_classifier = train_extent_classifier(e_train_parser);

            BIOReader test_parser = new BIOReader("data/partition_with_dev/eval/" + i, "ACE05-EVAL", "ALL", false);
            test_parser.reset();
            String preBIOLevel1 = "";
            String preBIOLevel2 = "";
            List<Constituent> predictedHeads = new ArrayList<>();
            List<Constituent> predictedMentions = new ArrayList<>();
            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);
                Pair<String, Integer> h_prediction = BIOTester.joint_inference((Constituent)example, h_candidates);
                String bioTag = h_prediction.getFirst();
                if (bioTag.startsWith("B") || bioTag.startsWith("U")){
                    Constituent predictMention = BIOTester.getConstituent((Constituent)example, h_candidates[h_prediction.getSecond()], false);
                    predictedHeads.add(predictMention);
                }
                preBIOLevel2 = preBIOLevel1;
                preBIOLevel1 = bioTag;
            }
            for (Constituent head : predictedHeads){
                Constituent mention = getFullMention(e_classifier, head, gazetteers, brownClusters, wordNet);
                predictedMentions.add(mention);
            }

            List<Constituent> goldMentions = new ArrayList<>();
            ACEReader aceReader = null;
            try{
                aceReader = new ACEReader("data/partition_with_dev/eval/" + i, false);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            for (TextAnnotation ta : aceReader){
                goldMentions.addAll(ta.getView(ViewNames.MENTION_ACE).getConstituents());
            }
            total_mention_labeled += goldMentions.size();
            total_mention_predicted += predictedMentions.size();
            for (Constituent p : predictedMentions){
                Constituent ph = getPredictedMentionHead(p);
                for (Constituent g : goldMentions){
                    if (!p.getTextAnnotation().getText().equals(g.getTextAnnotation().getText())){
                        continue;
                    }
                    Constituent gh = ACEReader.getEntityHeadForConstituent(g, g.getTextAnnotation(), "TESTG");
                    try {
                        if (ph.getStartSpan() == gh.getStartSpan() && ph.getEndSpan() == gh.getEndSpan()) {
                            total_mention_head_correct++;
                            if (g.getStartSpan() == p.getStartSpan() && g.getEndSpan() == p.getEndSpan()) {
                                total_mention_extent_correct++;
                            }
                            break;
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Total labeled mention: " + total_mention_labeled);
        System.out.println("Total predicted mention: " + total_mention_predicted);
        System.out.println("Total head correct: " + total_mention_head_correct);
        System.out.println("Total extent correct: " + total_mention_extent_correct);
    }

    public static void TrainModel(String corpus) throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        if (corpus.equals("ACE")){
            ExtentReader e_train_parser = new ExtentReader("data/all", "ACE");
            train_extent_classifier(e_train_parser, "models/EXTENT_ACE_TYPE");
        }
        if (corpus.equals("ERE")){
            ExtentReader e_train_parser = new ExtentReader("data/ere/data", "ERE");
            train_extent_classifier(e_train_parser, "models/EXTENT_ERE_TYPE");
        }
    }

    public static void TrainACEModel() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        TrainModel("ACE");
    }

    public static void TrainEREModel() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException {
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
            Method m = ExtentTester.class.getMethod(methodName, parameters);
            Object ret = m.invoke(methodValue, parameters);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
