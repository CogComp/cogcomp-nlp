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
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
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
 * The reader file which reads B/I/O/(L/U) tag for each word of a certain corpus
 * @ParserType; Constituent
 * Supports mode:
 * ACE 2005 with ACEReader
 * ERE with EREMentionRelationReader
 * ColumnFormat with any column formatted data
 * It returns Constituents duplicated from Token View, all the returning Constituents should be of size 1
 * The returning Constituents has an attribute "BIO", with value "B","I","O","L" or "U".
 * @Example: Parser parser = new BIOReader("data/", "ACE05-TRAIN", "NAM", false);
 * @Process: The reader first reads TextAnnotations from corpus using corpusreaders,
 *            then annotates the TextAnnotations with required views
 *            then parse the TextAnnotations into tokens.
 */

public class BIOReader implements Parser {

    private List<Constituent> tokenList;
    private int tokenIndex;
    private List<TextAnnotation> taList;
    private String _path;
    private String _mode;
    private String _binary_indicator;
    private String _type;
    private boolean _isBIO;

    public String id;

    public BIOReader(){

    }

    /**
     *
     * @param path The system path to the data
     * @param mode Corpus followed by "-" and indicator (TRAIN/EVAL). e.g., "ACE05-EVAL"
     * @param type The type of mentions that the reader keeps.
     *             "NAM" -> Only named entities
     *             "NOM" -> Only nominals
     *             "PRO" -> Only pronouns
     *             "ALL" -> All mentions
     * @param isBIO Indicates if the tagging schema is "BIO" or "BIOLU"
     * @throws JWNLException 
     * @throws IOException 
     * @throws DatastoreException 
     * @throws InvalidEndpointException 
     * @throws InvalidPortException 
     */
    public BIOReader(String path, String mode, String type, Boolean isBIO) {
        _path = path;
        _mode = mode.split("-")[0];
        _binary_indicator = mode.split("-")[1];
        _type = type;
        _isBIO = isBIO;
        String[] path_group = path.split("/");
        String group = path_group[path_group.length - 1];
        id = group + "_" + type;
        taList = getTextAnnotations();
        annotateTas();
        try {
            tokenList = getTokensFromTAs();
        } catch (Throwable t) {
            throw new RuntimeException("Tokens could not be reproduced form the text annotations.",t);
        }
    }

    public List<TextAnnotation> getTextAnnotations(){
        List<TextAnnotation> ret = new ArrayList<>();
        if (_mode.equals("ACE05")){
            ACEReaderWithTrueCaseFixer aceReader = null;
            try{
                aceReader = new ACEReaderWithTrueCaseFixer(_path, false);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            for (TextAnnotation ta : aceReader) {
                ret.add(ta);
            }
        }
        else if (_mode.equals("ERE")){
            EREMentionRelationReader ereMentionRelationReader = null;
            try {
                ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, _path, false);

            }
            catch (Exception e){
                e.printStackTrace();
            }
            for (XmlTextAnnotation xta : ereMentionRelationReader){
                ret.add(xta.getTextAnnotation());
            }
        }
        else if (_mode.equals("ColumnFormat")){
            ColumnFormatReader columnFormatReader = new ColumnFormatReader(_path);
            for (TextAnnotation ta : columnFormatReader){
                ret.add(ta);
            }
        }
        else{
            System.out.println("No defult actions for unknown mode");
        }
        return ret;
    }

    private void annotateTas(){
        for (TextAnnotation ta : taList){
            POSAnnotator posAnnotator = new POSAnnotator();
            try {
                ta.addView(posAnnotator);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private List<Constituent> getTokensFromTAs() throws InvalidPortException, InvalidEndpointException, DatastoreException, IOException, JWNLException{
        List<Constituent> ret = new ArrayList<>();
        WordNetManager wordNet = null;
        Gazetteers gazetteers = null;
        BrownClusters brownClusters = null;
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
        WordNetManager.loadConfigAsClasspathResource(true);
        wordNet = WordNetManager.getInstance();
        
        String mentionViewName = "";
        if (_mode.equals("ACE05")){
            mentionViewName = ViewNames.MENTION_ACE;
        }
        else if (_mode.equals("ERE")){
            mentionViewName = ViewNames.MENTION_ERE;
        }
        else if (_mode.equals("ColumnFormat")){
            mentionViewName = "MENTIONS";
        }
        else{
            System.out.println("No actions for undefined mode");
        }
        for (TextAnnotation ta : taList){
            View tokenView = ta.getView(ViewNames.TOKENS);
            View mentionView = ta.getView(mentionViewName);
            View bioView = new SpanLabelView("BIO", BIOReader.class.getCanonicalName(), ta, 1.0f);
            String[] token2tags = new String[tokenView.getConstituents().size()];
            for (int i = 0; i < token2tags.length; i++){
                token2tags[i] = "O";
            }
            for (Constituent c : mentionView.getConstituents()){
                if (!_type.equals("ALL")) {
                    String excludeType = _type;
                    if (_type.startsWith("SPE_")){
                        excludeType = _type.substring(4);
                    }
                    if (!c.getAttribute("EntityMentionType").equals(excludeType)) {
                        continue;
                    }
                }
                Constituent cHead = ACEReader.getEntityHeadForConstituent(c, ta, "HEAD");
                if (_mode.equals("ERE")) {
                    c.addAttribute("EntityType", c.getLabel());
                }

                if (cHead == null){
                    continue;
                }
                if (c.getAttribute("EntityType").equals("VEH") || c.getAttribute("EntityType").equals("WEA")){
                    //continue;
                }
                //c.addAttribute("EntityType", "MENTION");

                if (_isBIO) {
                    token2tags[cHead.getStartSpan()] = "B-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                    for (int i = cHead.getStartSpan() + 1; i < cHead.getEndSpan(); i++){
                        token2tags[i] = "I-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                    }
                }
                else {
                    if (cHead.getStartSpan()+1 == cHead.getEndSpan()) {
                        token2tags[cHead.getStartSpan()] = "U-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                    }
                    else {
                        token2tags[cHead.getStartSpan()] = "B-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                        for (int i = cHead.getStartSpan() + 1; i < cHead.getEndSpan() - 1; i++) {
                            token2tags[i] = "I-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                        }
                        token2tags[cHead.getEndSpan() - 1] = "L-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                    }
                }

            }
            for (int i = 0; i < token2tags.length; i++){
                Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                Constituent newToken = curToken.cloneForNewView("BIO");
                if (token2tags[i].equals("O")) {
                    newToken.addAttribute("BIO", token2tags[i]);
                }
                else{
                    String[] group = token2tags[i].split(",");
                    String tag = group[0];
                    String eml = group[1];
                    newToken.addAttribute("BIO", tag);
                    newToken.addAttribute("EntityMentionType", eml);
                }
                newToken.addAttribute("GAZ", ((FlatGazetteers)gazetteers).annotateConstituent(newToken, _isBIO));
                newToken.addAttribute("BC", brownClusters.getPrefixesCombined(newToken.toString()));
                if (!newToken.toString().contains("http")) {
                    newToken.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, newToken));
                    newToken.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, newToken));
                }
                else {
                    newToken.addAttribute("WORDNETTAG", ",");
                    newToken.addAttribute("WORDNETHYM", ",");
                }
                if (_binary_indicator.equals("TRAIN")){
                    newToken.addAttribute("isTraining", "true");
                }
                else{
                    newToken.addAttribute("isTraining", "false");
                }
                bioView.addConstituent(newToken);
            }
            ta.addView("BIO", bioView);
            for (Constituent c : bioView){
                ret.add(c);
            }
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