package org.cogcomp.md;

/*
 * The reader file which reads B/I/O tag for each word of a certain corpus
 * Supports mode:
 * "ACE05" -> ACE 2005 with ACEReader
 */
import java.io.File;
import java.util.*;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.annotators.GazetteerViewGenerator;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.cogcomp.Datastore;

public class BIOReader implements Parser
{
    private List<Constituent> tokenList;
    private int tokenIndex;
    private List<TextAnnotation> taList;
    private String _path;
    private String _mode;
    private List<Annotator> annotators;

    public BIOReader(String path, String mode){
        _path = path;
        _mode = mode;
        taList = getTextAnnotations();
        tokenList = getTokensFromTAs();
    }

    private List<TextAnnotation> getTextAnnotations(){
        List<TextAnnotation> ret = new ArrayList<>();
        if (_mode.equals("ACE05")){
            ACEReader aceReader = null;
            try{
                aceReader = new ACEReader(_path, false);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            for (TextAnnotation ta : aceReader) {
                POSAnnotator posAnnotator = new POSAnnotator();
                try {
                    ta.addView(posAnnotator);
                    GazetteerViewGenerator.gazetteersInstance.addView(ta);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                ret.add(ta);
            }
        }
        else{
            System.out.println("No defult actions for unknown mode");
        }
        return ret;
    }

    private List<Constituent> getTokensFromTAs(){
        List<Constituent> ret = new ArrayList<>();
        if (_mode.equals("ACE05")){
            for (TextAnnotation ta : taList){
                View tokenView = ta.getView(ViewNames.TOKENS);
                View mentionView = ta.getView(ViewNames.MENTION_ACE);
                String[] token2tags = new String[tokenView.getConstituents().size()];
                for (int i = 0; i < token2tags.length; i++){
                    token2tags[i] = "O";
                }
                for (Constituent c : mentionView.getConstituents()){
                    token2tags[c.getStartSpan()] = "B";
                    for (int i = c.getStartSpan() + 1; i < c.getEndSpan(); i++){
                        token2tags[i] = "I";
                    }
                }
                for (int i = 0; i < token2tags.length; i++){
                    Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                    Constituent newToken = curToken.cloneForNewView("MD");
                    newToken.addAttribute("BIO", token2tags[i]);
                    ret.add(newToken);
                }
            }
        }
        else{
            System.out.println("No defult actions for unknown mode");
        }
        return ret;
    }

    public void close(){}
    public Object next(){
        if (tokenIndex == tokenList.size()) {
            return null;
        } else {
            tokenIndex++;
            return tokenList.get(tokenIndex - 1);
        }
    }

    public void reset(){
        tokenIndex = 0;
    }
}