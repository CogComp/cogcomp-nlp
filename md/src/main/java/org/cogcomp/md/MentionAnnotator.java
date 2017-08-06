/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.md;

import org.cogcomp.md.LbjGen.*;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import org.cogcomp.Datastore;

import java.io.File;
import java.util.Vector;

/**
 * This class gives a given TextAnnotation a new View ViewNames.MENTION
 * The View contains Constituents that are annotated mentions of the given TextAnnotation
 * The annotator requires POS View to work.
 *
 * The Constituents in ViewNames.MENTION is the full mention includes extent
 * To get the head of a Constituent, use the Attribute "EntityHeadStartSpan" and "EntityHeadEndSpan"
 */
public class MentionAnnotator extends Annotator{

    private bio_classifier_nam classifier_nam;
    private bio_classifier_nom classifier_nom;
    private bio_classifier_pro classifier_pro;
    private extent_classifier classifier_extent;
    private Learner[] candidates;
    private FlatGazetteers gazetteers;
    private BrownClusters brownClusters;
    private WordNetManager wordNet;

    /**
     * By default, the initializer set mode to "ACE_NONTYPE"
     */
    public MentionAnnotator(){
        this(true, "ACE_NONTYPE");
    }

    /**
     *
     * @param mode Indicates the model expected to lead
     *             "ACE_NONTYPE" -> model trained on ACE without EntityType
     *             "ACE_TYPE" -> model trained on ACE with EntityType
     *             "ERE_NONTYPE" -> model trained on ERE without EntityType
     *             "ERE_TYPE" -> model trained on ERE with EntityType
     */
    public MentionAnnotator(String mode) {
        this(true, mode);
    }

    public MentionAnnotator(boolean lazilyInitialize, String mode){
        super(ViewNames.MENTION, new String[]{ViewNames.POS}, lazilyInitialize);
        String fileName_NAM = "";
        String fileName_NOM = "";
        String fileName_PRO = "";
        String fileName_EXTENT = "";
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            if (mode.contains("ACE")) {
                File extentFlie = ds.getDirectory("org.cogcomp.mention", "ACE_EXTENT", 1.0, false);
                fileName_EXTENT = extentFlie.getPath() + File.separator + "ACE_EXTENT" + File.separator + "EXTENT_ACE";
                if (mode.contains("NON")){
                    File headFile = ds.getDirectory("org.cogcomp.mention", "ACE_HEAD_NONTYPE", 1.0, false);
                    fileName_NAM = headFile.getPath() + File.separator + "ACE_HEAD_NONTYPE" + File.separator + "ACE_NAM";
                    fileName_NOM = headFile.getPath() + File.separator + "ACE_HEAD_NONTYPE" + File.separator + "ACE_NOM";
                    fileName_PRO = headFile.getPath() + File.separator + "ACE_HEAD_NONTYPE" + File.separator + "ACE_PRO";
                }
                else {
                    File headFile = ds.getDirectory("org.cogcomp.mention", "ACE_HEAD_TYPE", 1.0, false);
                    fileName_NAM = headFile.getPath() + File.separator + "ACE_HEAD_TYPE" + File.separator + "ACE_NAM_TYPE";
                    fileName_NOM = headFile.getPath() + File.separator + "ACE_HEAD_TYPE" + File.separator + "ACE_NOM_TYPE";
                    fileName_PRO = headFile.getPath() + File.separator + "ACE_HEAD_TYPE" + File.separator + "ACE_PRO_TYPE";
                }
            }
            else if (mode.contains("ERE")){
                File extentFlie = ds.getDirectory("org.cogcomp.mention", "ERE_EXTENT", 1.0, false);
                fileName_EXTENT = extentFlie.getPath() + File.separator + "ERE_EXTENT" + File.separator + "EXTENT_ERE";
                if (mode.contains("NON")){
                    File headFile = ds.getDirectory("org.cogcomp.mention", "ERE_HEAD_NONTYPE", 1.0, false);
                    fileName_NAM = headFile.getPath() + File.separator + "ERE_HEAD_NONTYPE" + File.separator + "ERE_NAM";
                    fileName_NOM = headFile.getPath() + File.separator + "ERE_HEAD_NONTYPE" + File.separator + "ERE_NOM";
                    fileName_PRO = headFile.getPath() + File.separator + "ERE_HEAD_NONTYPE" + File.separator + "ERE_PRO";
                }
                else {
                    File headFile = ds.getDirectory("org.cogcomp.mention", "ERE_HEAD_TYPE", 1.0, false);
                    fileName_NAM = headFile.getPath() + File.separator + "ERE_HEAD_TYPE" + File.separator + "ERE_NAM_TYPE";
                    fileName_NOM = headFile.getPath() + File.separator + "ERE_HEAD_TYPE" + File.separator + "ERE_NOM_TYPE";
                    fileName_PRO = headFile.getPath() + File.separator + "ERE_HEAD_TYPE" + File.separator + "ERE_PRO_TYPE";
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        classifier_nam = new bio_classifier_nam(fileName_NAM + ".lc", fileName_NAM + ".lex");
        classifier_nom = new bio_classifier_nom(fileName_NOM+".lc", fileName_NOM + ".lex");
        classifier_pro = new bio_classifier_pro(fileName_PRO + ".lc", fileName_PRO + ".lex");
        classifier_extent = new extent_classifier(fileName_EXTENT + ".lc", fileName_EXTENT + ".lex");

        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
            gazetteers = (FlatGazetteers) GazetteersFactory.get();
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
            BrownClusters.init(bcs, bcst, bcsl);
            brownClusters = BrownClusters.get();
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        candidates = new Learner[3];
        candidates[0] = classifier_nam;
        candidates[1] = classifier_nom;
        candidates[2] = classifier_pro;
    }

    public void initialize(ResourceManager rm){

    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException{
        View mentionView = new SpanLabelView(ViewNames.MENTION, MentionAnnotator.class.getCanonicalName(), ta, 1.0f, true);
        View bioView = new SpanLabelView("BIO", BIOReader.class.getCanonicalName(), ta, 1.0f);
        View tokenView = ta.getView(ViewNames.TOKENS);
        for (int i = tokenView.getStartSpan(); i < tokenView.getEndSpan(); i++){
            Constituent currentToken = tokenView.getConstituentsCoveringToken(i).get(0).cloneForNewView("BIO");
            currentToken.addAttribute("GAZ", gazetteers.annotateConstituent(currentToken, false));
            currentToken.addAttribute("BC", brownClusters.getPrefixesCombined(currentToken.toString()));
            if (!currentToken.toString().contains("http")) {
                currentToken.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, currentToken));
                currentToken.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, currentToken));
            }
            else {
                currentToken.addAttribute("WORDNETTAG", ",");
                currentToken.addAttribute("WORDNETHYM", ",");
            }
            currentToken.addAttribute("isTraining", "false");
            bioView.addConstituent(currentToken);
        }
        ta.addView("BIO", bioView);
        String preBIOLevel1 = "";
        String preBIOLevel2 = "";
        for (int i = bioView.getStartSpan(); i < bioView.getEndSpan(); i++){
            Constituent currentBIO = bioView.getConstituentsCoveringToken(i).get(0);
            currentBIO.addAttribute("preBIOLevel1", preBIOLevel1);
            currentBIO.addAttribute("preBIOLevel2", preBIOLevel2);
            Pair<String, Integer> prediction = BIOTester.joint_inference(currentBIO, candidates);
            String predictedTag = prediction.getFirst();
            preBIOLevel2 = preBIOLevel1;
            preBIOLevel1 = predictedTag;
            if (predictedTag.startsWith("B") || predictedTag.startsWith("U")){
                int canIdx = prediction.getSecond();
                Constituent mention = BIOTester.getConstituent(currentBIO, candidates[canIdx], false);
                if (canIdx == 0){
                    mention.addAttribute("EntityMentionType", "NAM");
                }
                if (canIdx == 1){
                    mention.addAttribute("EntityMentionType", "NOM");
                }
                if (canIdx == 2){
                    mention.addAttribute("EntityMentionType", "PRO");
                }
                Constituent fullMention = ExtentTester.getFullMention(classifier_extent, mention, gazetteers, brownClusters, wordNet);
                mentionView.addConstituent(fullMention);
            }
        }
        ta.addView(ViewNames.MENTION, mentionView);
    }

}
