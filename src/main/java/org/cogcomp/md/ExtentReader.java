package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.cogcomp.Datastore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by xuany on 7/23/2017.
 */
public class ExtentReader implements Parser
{
    private List<Relation> pairList;
    private List<TextAnnotation> taList;
    private String _path;
    private int pairIdx;

    public ExtentReader(String path){
        _path = path;
        taList = getTextAnnotations();
        pairList = getPairs();
    }

    public String getId(){
        String ret = _path;
        ret = ret.replace("/", "");
        ret = ret.replace("\\", "");
        return ret;
    }

    public List<TextAnnotation> getTextAnnotations(){
        List<TextAnnotation> ret = new ArrayList<>();
        ACEReader aceReader = null;
        POSAnnotator posAnnotator = new POSAnnotator();
        try {
            aceReader = new ACEReader(_path, false);
            for (TextAnnotation ta : aceReader){
                ta.addView(posAnnotator);
                ret.add(ta);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
    public List<Relation> getPairs(){
        List<Relation> ret = new ArrayList<>();
        WordNetManager wordNet = null;
        try {
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
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

        }
        catch (Exception e){
            e.printStackTrace();
        }
        Gazetteers gazetteers = GazetteersFactory.get();
        BrownClusters brownClusters = BrownClusters.get();
        for (TextAnnotation ta : taList){
            View mentionView = ta.getView(ViewNames.MENTION_ACE);
            View tokenView = ta.getView(ViewNames.TOKENS);
            for (Constituent mention : mentionView){
                Constituent head = ACEReader.getEntityHeadForConstituent(mention, ta, "HEADS");
                ExtentTester.addHeadAttributes(head, gazetteers, brownClusters, wordNet);

                for (int i = mention.getStartSpan(); i < mention.getEndSpan(); i++){
                    if (i >= head.getStartSpan() && i < head.getEndSpan()){
                        continue;
                    }
                    Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                    ExtentTester.addExtentAttributes(curToken, gazetteers, brownClusters, wordNet);
                    Relation R = new Relation("true", curToken, head, 1.0f);
                    ret.add(R);
                }
                if (mention.getStartSpan() > 0){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(mention.getStartSpan() - 1).get(0);
                    ExtentTester.addExtentAttributes(curToken, gazetteers, brownClusters, wordNet);
                    Relation falseR = new Relation("false", curToken, head, 1.0f);
                    ret.add(falseR);
                }
                if (mention.getEndSpan() < tokenView.getEndSpan()){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(mention.getEndSpan()).get(0);
                    ExtentTester.addExtentAttributes(curToken, gazetteers, brownClusters, wordNet);
                    Relation falseR = new Relation("false", curToken, head, 1.0f);
                    ret.add(falseR);
                }
            }
        }
        return ret;
    }
    public void close(){

    }
    public Object next(){
        if (pairIdx == pairList.size()){
            return null;
        }
        else{
            pairIdx ++;
            return pairList.get(pairIdx - 1);
        }
    }
    public void reset(){
        pairIdx = 0;
    }
}
