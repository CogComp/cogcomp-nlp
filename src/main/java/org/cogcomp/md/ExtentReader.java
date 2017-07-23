package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

import java.util.ArrayList;
import java.util.List;

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
        try {
            aceReader = new ACEReader(_path, false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        for (TextAnnotation ta : aceReader){
            ret.add(ta);
        }
        return ret;
    }
    public List<Relation> getPairs(){
        List<Relation> ret = new ArrayList<>();
        for (TextAnnotation ta : taList){
            View mentionView = ta.getView(ViewNames.MENTION_ACE);
            View tokenView = ta.getView(ViewNames.TOKENS);
            for (Constituent mention : mentionView){
                Constituent head = ACEReader.getEntityHeadForConstituent(mention, ta, "HEADS");
                for (int i = mention.getStartSpan(); i < head.getStartSpan(); i++){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                    Relation leftR = new Relation("true", curToken, head, 1.0f);
                    ret.add(leftR);
                }
                for (int i = head.getEndSpan(); i < mention.getEndSpan(); i++){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                    Relation rightR = new Relation("true", curToken, head, 1.0f);
                    ret.add(rightR);
                }
                if (mention.getStartSpan() > 0){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(mention.getStartSpan() - 1).get(0);
                    Relation falseR = new Relation("false", curToken, head, 1.0f);
                    ret.add(falseR);
                }
                if (mention.getEndSpan() < tokenView.getEndSpan()){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(mention.getEndSpan()).get(0);
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
