package org.cogcomp.md;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Vector;

/**
 * Created by admin on 8/3/2017.
 */
public class MentionAnnotator extends Annotator{

    private static final String NAME = MentionAnnotator.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(MentionAnnotator.class);
    private bio_classifier_nam classifier_nam;
    private bio_classifier_nom classifier_nom;
    private bio_classifier_pro classifier_pro;
    private Learner[] candidates;
    private FlatGazetteers gazetteers;
    private BrownClusters brownClusters;
    WordNetManager wordNet;

    public MentionAnnotator(){
        this(true);
    }

    public MentionAnnotator(boolean lazilyInitialize){
        super("MENTION", new String[]{ViewNames.POS}, lazilyInitialize);
        classifier_nam = new bio_classifier_nam("models/ACE_NAM.lc", "models/ACE_NAM.lex");
        classifier_nom = new bio_classifier_nom("models/ACE_NOM.lc", "models/ACE_NOM.lex");
        classifier_pro = new bio_classifier_pro("models/ACE_PRO.lc", "models/ACE_PRO.lex");

        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
            gazetteers = (FlatGazetteers) GazetteersFactory.get();
            Vector<String> bcs = new Vector<>();
            bcs.add("brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
            bcs.add("brown-clusters/brownBllipClusters");
            bcs.add("brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt");
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
        View mentionView = new SpanLabelView("MENTIONS", MentionAnnotator.class.getCanonicalName(), ta, 1.0f, true);
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
            Constituent currentBIO = tokenView.getConstituentsCoveringToken(i).get(0);
            currentBIO.addAttribute("preBIOLevel1", preBIOLevel1);
            currentBIO.addAttribute("preBIOLevel2", preBIOLevel2);
            Pair<String, Integer> prediction = BIOTester.joint_inference(currentBIO, candidates, null);
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
                mentionView.addConstituent(mention);
            }
        }
        ta.addView("MENTION", mentionView);
    }

}
