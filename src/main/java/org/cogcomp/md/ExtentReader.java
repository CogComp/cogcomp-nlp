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
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Gazetteers gazetteers = GazetteersFactory.get();
        for (TextAnnotation ta : taList){
            View mentionView = ta.getView(ViewNames.MENTION_ACE);
            View tokenView = ta.getView(ViewNames.TOKENS);
            for (Constituent mention : mentionView){
                Constituent head = ACEReader.getEntityHeadForConstituent(mention, ta, "HEADS");
                for (int i = head.getStartSpan(); i < head.getEndSpan(); i++) {
                    head.addAttribute("GAZ" + i, ((FlatGazetteers) gazetteers).annotateConstituent(tokenView.getConstituentsCoveringToken(i).get(0), false));
                }
                head.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(head));
                for (int i = mention.getStartSpan(); i < head.getStartSpan(); i++){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                    curToken.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotateConstituent(curToken, false));
                    Relation leftR = new Relation("true", curToken, head, 1.0f);
                    ret.add(leftR);
                }
                for (int i = head.getEndSpan(); i < mention.getEndSpan(); i++){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                    curToken.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotateConstituent(curToken, false));
                    Relation rightR = new Relation("true", curToken, head, 1.0f);
                    ret.add(rightR);
                }
                if (mention.getStartSpan() > 0){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(mention.getStartSpan() - 1).get(0);
                    curToken.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotateConstituent(curToken, false));
                    Relation falseR = new Relation("false", curToken, head, 1.0f);
                    ret.add(falseR);
                }
                if (mention.getEndSpan() < tokenView.getEndSpan()){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(mention.getEndSpan()).get(0);
                    curToken.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotateConstituent(curToken, false));
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
