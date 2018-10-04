/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.constants.Language;
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
import org.cogcomp.md.LbjGen.bio_classifier_nam;
import org.cogcomp.md.LbjGen.bio_classifier_nom;
import org.cogcomp.md.LbjGen.bio_classifier_pro;
import org.cogcomp.md.LbjGen.extent_classifier;

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

    private bio_classifier_nam classifier_nam = null;
    private bio_classifier_nom classifier_nom = null;
    private bio_classifier_pro classifier_pro = null;
    String fileName_NAM = "";
    String fileName_NOM = "";
    String fileName_PRO = "";
    String fileName_EXTENT = "";
    private extent_classifier classifier_extent;
    private Learner[] candidates;
    private FlatGazetteers gazetteers;
    private BrownClusters brownClusters;
    private WordNetManager wordNet;

    private String _mode;
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
        _mode = mode;
    }

    /**
     *
     * @param nam_model_path NAM model file path (excluding the extension)
     * @param nom_model_path NOM model file path (excluding the extension)
     * @param pro_model_path PRO model file path (excluding the extension)
     * @param extent_model_path EXTENT model file path (excluding the extension)
     * @param mode Useless in this case
     */
    public MentionAnnotator(String nam_model_path, String nom_model_path, String pro_model_path, String extent_model_path, String mode){
        super(ViewNames.MENTION, new String[]{ViewNames.POS}, true);
        _mode = mode;
        if (fileName_NAM != null) {
            fileName_NAM = nam_model_path;
        }
        if (fileName_NOM != null) {
            fileName_NOM = nom_model_path;
        }
        if (fileName_PRO != null) {
            fileName_PRO = pro_model_path;
        }
    }

    public void initialize(ResourceManager rm){
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            if (_mode.contains("ACE")) {
                File extentFile = ds.getDirectory("org.cogcomp.mention", "ACE_EXTENT", 1.0, false);
                fileName_EXTENT = extentFile.getPath() + File.separator + "ACE_EXTENT" + File.separator + "EXTENT_ACE";
                if (_mode.contains("NON")){
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
            else if (_mode.contains("ERE")){
                File extentFile = ds.getDirectory("org.cogcomp.mention", "ERE_EXTENT", 1.0, false);
                fileName_EXTENT = extentFile.getPath() + File.separator + "ERE_EXTENT" + File.separator + "EXTENT_ERE";
                if (_mode.contains("NON")){
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
        if (!fileName_NAM.equals("")) {
            classifier_nam = new bio_classifier_nam(fileName_NAM + ".lc", fileName_NAM + ".lex");
        }
        if (!fileName_NOM.equals("")) {
            classifier_nom = new bio_classifier_nom(fileName_NOM + ".lc", fileName_NOM + ".lex");
        }
        if (!fileName_PRO.equals("")) {
            classifier_pro = new bio_classifier_pro(fileName_PRO + ".lc", fileName_PRO + ".lex");
        }
        if (!fileName_EXTENT.equals("")) {
            classifier_extent = new extent_classifier(fileName_EXTENT + ".lc", fileName_EXTENT + ".lex");
        }
        else {
            try {
                Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
                File extentFile = ds.getDirectory("org.cogcomp.mention", "ACE_EXTENT", 1.0, false);
                fileName_EXTENT = extentFile.getPath() + File.separator + "ACE_EXTENT" + File.separator + "EXTENT_ACE";
                classifier_extent = new extent_classifier(fileName_EXTENT + ".lc", fileName_EXTENT + ".lex");
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            gazetteers = (FlatGazetteers) GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", true, Language.English);
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

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException{
        if (!isInitialized()){
            doInitialize();
        }
        if (!ta.hasView(ViewNames.POS)){
            throw new AnnotatorException("Missing required view POS");
        }
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

    /**
     *
     * @param c The input full extent constituent
     * @param viewName The expected view name that you want the head constituent to have
     * @return A constituent that is only the head
     */
    public static Constituent getHeadConstituent(Constituent c, String viewName){
        if (c.getAttribute("EntityHeadStartSpan") == null || c.getAttribute("EntityHeadEndSpan") == null){
            return null;
        }
        int cStart = Integer.parseInt(c.getAttribute("EntityHeadStartSpan"));
        int cEnd = Integer.parseInt(c.getAttribute("EntityHeadEndSpan"));
        Constituent ret = new Constituent(c.getLabel(), viewName, c.getTextAnnotation(), cStart, cEnd);
        for (String attributeKey : c.getAttributeKeys()) {
            ret.addAttribute(attributeKey, c.getAttribute(attributeKey));
        }
        return ret;
    }
}
