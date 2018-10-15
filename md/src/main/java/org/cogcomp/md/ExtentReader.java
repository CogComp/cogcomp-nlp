/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReaderWithTrueCaseFixer;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import net.didion.jwnl.JWNLException;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This is the reader for extends
 * Given a head, it reads all tokens to either head's left and right
 * with the head itself into a Relation
 * It stops until reading one token-head pair where the token is not part of the head's extent
 */
public class ExtentReader implements Parser
{
    private List<Relation> pairList;
    private List<TextAnnotation> taList;
    private String _path;
    private String _corpus;
    private int pairIdx;

    /**
     *
     * @param path The data pth
     * @param corpus The corpus "ACE/ERE"
     * @throws DatastoreException 
     * @throws JWNLException 
     * @throws IOException 
     * @throws InvalidEndpointException 
     * @throws InvalidPortException 
     */
    public ExtentReader(String path, String corpus) throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        _path = path;
        _corpus = corpus;
        taList = getTextAnnotations();
        pairList = getPairs();
    }

    /**
     *  When no corpus is selected, it is set to "ACE"
     */
    public ExtentReader(String path) {
        _path = path;
        _corpus = "ACE";
        try {
            taList = getTextAnnotations();
            pairList = getPairs();
        } catch (Throwable t) {
            throw new RuntimeException("TextAnnotation generation failed",t);
        }
    }

    /**
     * Produce an ID for model naming
     */
    public String getId(){
        String ret = _path;
        ret = ret.replace("/", "");
        ret = ret.replace("\\", "");
        return ret;
    }

    public List<TextAnnotation> getTextAnnotations() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        List<TextAnnotation> ret = new ArrayList<>();
        if (_corpus.equals("ACE")) {
            ACEReaderWithTrueCaseFixer aceReader = null;
            POSAnnotator posAnnotator = new POSAnnotator();
            try {
                aceReader = new ACEReaderWithTrueCaseFixer(_path, false);
                for (TextAnnotation ta : aceReader) {
                    ta.addView(posAnnotator);
                    ret.add(ta);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (_corpus.equals("ERE")){
            EREMentionRelationReader ereMentionRelationReader = null;
            POSAnnotator posAnnotator = new POSAnnotator();
            try {
                ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, _path, false);
                for (XmlTextAnnotation xta : ereMentionRelationReader){
                    TextAnnotation ta = xta.getTextAnnotation();
                    ta.addView(posAnnotator);
                    ret.add(ta);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        if (_corpus.startsWith("COMBINED")){
            String realCorpus = _corpus.split("-")[1];
            String mode = _corpus.split("-")[2];
            int fold = Integer.parseInt(_corpus.split("-")[3]);
            BIOCombinedReader bioCombinedReader = new BIOCombinedReader(fold, realCorpus + "-" + mode, "ALL", true);
            for (Object ta = bioCombinedReader.next(); ta != null; ta = bioCombinedReader.next()){
                ret.add((TextAnnotation)ta);
            }
        }
        return ret;
    }
    public List<Relation> getPairs(){
        List<Relation> ret = new ArrayList<>();
        WordNetManager wordNet = null;
        Gazetteers gazetteers = null;
        BrownClusters brownClusters = null;
        try {
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            gazetteers = GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", true, Language.English);
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
            brownClusters = BrownClusters.get(bcs, bcst, bcsl);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        for (TextAnnotation ta : taList){
            String mentionViewName = ViewNames.MENTION_ERE;
            if (ta.getId().startsWith("bn") || ta.getId().startsWith("nw")){
                mentionViewName = ViewNames.MENTION_ACE;
            }
            View mentionView = ta.getView(mentionViewName);
            View tokenView = ta.getView(ViewNames.TOKENS);
            for (Constituent mention : mentionView){
                Constituent head = ACEReader.getEntityHeadForConstituent(mention, ta, "HEADS");
                if (head == null){
                    continue;
                }
                if (!head.hasAttribute("EntityType")){
                    head.addAttribute("EntityType", head.getLabel());
                }
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
