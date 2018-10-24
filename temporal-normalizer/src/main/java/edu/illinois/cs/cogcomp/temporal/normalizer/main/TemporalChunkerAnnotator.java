/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main;

import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;


import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.AvoidUsing;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.LBJavaUtils;


import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 10/2/16.
 * This class provides an Annotator for temporal normalization.
 * User can choose whether use our implementation of the normalizer
 * or HeidelTime. The results follow TIMEX3 standard.
 * This class also provides a method to write the normalized results
 * to .tml files.
 */
public class TemporalChunkerAnnotator extends Annotator{
    private static final String NAME = TemporalChunkerAnnotator.class.getCanonicalName();
    private static final Logger logger = LoggerFactory.getLogger(TemporalChunkerAnnotator.class);
    private Chunker tagger;
    private String posfield = ViewNames.POS;
    private String tokensfield = ViewNames.TOKENS;
    private String sentencesfield = ViewNames.SENTENCE;
    private HeidelTimeStandalone heidelTime;
    private Date dct;
    private TimexNormalizer timexNormalizer;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Boolean useHeidelTime;
    private List<TimexChunk> timex;
    private TimexChunk placeHolder;

    public List<TimexChunk> getTimex() {
        return timex;
    }
    public void setTimex(List<TimexChunk> timex) {
        this.timex = timex;
    }

    /**
     * default: don't use lazy initialization
     */
    public TemporalChunkerAnnotator() {
        this(true);
    }

    @AvoidUsing(reason = "No config specified")
    /**
     * Constructor parameter allows user to specify whether or not to lazily initialize.
     * PLEASE DO NOT USE THIS CONSTRUCTOR
     *
     * @param lazilyInitialize If set to 'true', models will not be loaded until first call
     *        requiring Chunker annotation.
     */
    public TemporalChunkerAnnotator(boolean lazilyInitialize) {
        super(
                ViewNames.TIMEX3,
                new String[] {ViewNames.POS},
                lazilyInitialize,
                new TemporalChunkerConfigurator().getDefaultConfig()
        );
        //initialize(nonDefaultRm);
    }

    /**
     * DO USE THIS CONSTRUCTOR
     * Refer to main() to see detailed usage
     * @param nonDefaultRm ResourceManager that specifies model paths, etc
     */
    public TemporalChunkerAnnotator (ResourceManager nonDefaultRm) {
        super(ViewNames.TIMEX3, new String[] {ViewNames.POS}, false, nonDefaultRm);
    }

    @Override
    /**
     * Initialize TemporalChunkerAnnotator with the given ResourceManager
     * @param nonDefaultRm ResourceManager that specifies model paths, etc
     */
    public void initialize(ResourceManager rm) {
        URL lcPath =
                IOUtilities.loadFromClasspath(
                        TemporalChunkerAnnotator.class,
                        rm.getString(TemporalChunkerConfigurator.MODEL_PATH)
                );
        URL lexPath =
                IOUtilities.loadFromClasspath(
                        TemporalChunkerAnnotator.class,
                        rm.getString(TemporalChunkerConfigurator.MODEL_LEX_PATH)
                );

        tagger = new Chunker(
                rm.getString(TemporalChunkerConfigurator.MODEL_PATH),
                rm.getString(TemporalChunkerConfigurator.MODEL_LEX_PATH));
        tagger.readModel(lcPath);
        tagger.readLexicon(lexPath);

        this.useHeidelTime =
                rm.getString(TemporalChunkerConfigurator.USE_HEIDELTIME) != "False";

        this.dct = new Date();

        if (this.useHeidelTime) {
            this.heidelTime = new HeidelTimeStandalone(
                    Language.ENGLISH,
                    DocumentType.valueOf(rm.getString(TemporalChunkerConfigurator.DOCUMENT_TYPE)),
                    OutputType.valueOf(rm.getString(TemporalChunkerConfigurator.OUTPUT_TYPE)),
                    rm.getString(TemporalChunkerConfigurator.HEIDELTIME_CONFIG),
                    POSTagger.valueOf(rm.getString(TemporalChunkerConfigurator.POSTAGGER_TYPE)),
                    true
            );
        }
        else {
            timexNormalizer = new TimexNormalizer();
            timexNormalizer.setTime(this.dct);
        }
        this.timex = new ArrayList<>();
        this.factory = DocumentBuilderFactory.newInstance();
        try {
            this.builder = this.factory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Delete the current list of timxes
     */
    public void deleteTimex() {
        this.timex = new ArrayList<>();
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        if (!ta.hasView(tokensfield) || !ta.hasView(sentencesfield)
                || !ta.hasView(posfield)) {
            String msg = "Record must be tokenized, sentence split, and POS-tagged first.";
            logger.error(msg);
            throw new AnnotatorException(msg);
        }

        List<Constituent> tags = ta.getView(posfield).getConstituents();

        List<Token> lbjTokens = LBJavaUtils.recordToLBJTokens(ta);

        View chunkView = new SpanLabelView(ViewNames.TIMEX3, this.NAME, ta, 1.0);

        int currentChunkStart = 0;
        int currentChunkEnd = 0;

        String clabel = null;
        Constituent previous = null;
        int tcounter = 0;
        for (Token lbjtoken : lbjTokens) {
            Constituent current = tags.get(tcounter);
            tagger.discreteValue(lbjtoken);
            logger.debug("{} {}", lbjtoken.toString(), (null == lbjtoken.type) ? "NULL"
                    : lbjtoken.type);

            /*Enforce some rules to avoid silly mistakes of temporal chunker*/
            if(lbjtoken.type.charAt(0) == 'O'){
                DateMapping dateMapping = DateMapping.getInstance();
                String tmp = lbjtoken.form.toLowerCase();
                String prev_token = null;
                if(previous!=null&&previous.getSurfaceForm()!=null)
                    prev_token = previous.getSurfaceForm().toLowerCase();
                if(dateMapping.getHm_month().containsKey(tmp)
                        ||!tmp.equals("sun")&&dateMapping.getHm_dayOfWeek().containsKey(tmp)) {
                    // if this token matches to any month names or day-of-week names (either full names or abbr. names), then force the chunker label to be Begin
                    // "Sun" is a bit tricky since the star "Sun" and the day of week "Sunday" are both NNP. We leave it to chunker now.
                    if(tmp.equals("may")||tmp.equals("sat")){
                        if(current.getLabel().startsWith("NNP"))
                            lbjtoken.type = "B-null";
                    }
                    else
                        lbjtoken.type = "B-null";
                }
                else if(prev_token!=null
                            && dateMapping.getHm_month().containsKey(prev_token)){// previous token was a "month"
                    if(dateMapping.getHm_dayOfMonth().contains(tmp))// curr token is an ordinal number
                        lbjtoken.type = "I-null";
                    else{
                        try {
                            int currint = Integer.valueOf(tmp);
                            if(currint>=1&&currint<=31)// curr token is an int number in [1,31]
                                lbjtoken.type = "I-null";
                        }
                        catch (Exception e){}// nothing needed
                    }
                }
            }

            // what happens if we see an Inside tag -- even if it doesn't follow a Before tag
            if (null != lbjtoken.type && lbjtoken.type.charAt(0) == 'I') {
                if (lbjtoken.type.length() < 3)
                    throw new IllegalArgumentException("Chunker word label '" + lbjtoken.type
                            + "' is too short!");
                if (null == clabel) // we must have just seen an Outside tag and possibly completed
                // a chunk
                {
                    // modify lbjToken.type for later ifs
                    lbjtoken.type = "B" + lbjtoken.type.substring(1);
                } else if (clabel.length() >= 3 && !clabel.equals(lbjtoken.type.substring(2))) {
                    lbjtoken.type = "B" + lbjtoken.type.substring(1);
                }
            }
            if ((lbjtoken.type.charAt(0) == 'B' || lbjtoken.type.charAt(0) == 'O')
                    && clabel != null) {

                if (previous != null) {
                    currentChunkEnd = previous.getEndSpan();
                    Constituent label;
                    Constituent temp_label =
                            new Constituent(clabel, ViewNames.TIMEX3, ta,
                                    currentChunkStart, currentChunkEnd);
                    if (this.useHeidelTime) {
                        try {
                            clabel = heidelTimeNormalize(temp_label);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        label = new Constituent(clabel, ViewNames.TIMEX3, ta,
                                currentChunkStart, currentChunkEnd);
                    }
                    else {
                        String tense = this.getSentenceTense(ta, temp_label.getSpan());
                        TemporalPhrase temporalPhrase = new TemporalPhrase(temp_label.toString(), tense);
                        TimexChunk normRes = timexNormalizer.normalize(temporalPhrase);
                        if (normRes != null) {
                            normRes.setCharStart(temp_label.getStartCharOffset());
                            normRes.setCharEnd(temp_label.getEndCharOffset());
                            this.timex.add(normRes);
                        }
                        else {
                            // Our normalize may produce null result for some extraction
                            // we still need to add a dummy placeholder
                            // so that evaluation will catch this
                            placeHolder = new TimexChunk();
                            placeHolder.setCharStart(temp_label.getStartCharOffset());
                            placeHolder.setCharEnd(temp_label.getEndCharOffset());
                            this.timex.add(placeHolder);
                        }
                        label = new Constituent(normRes==null?"":normRes.toTIMEXTag(),
                                ViewNames.TIMEX3, ta,
                                currentChunkStart, currentChunkEnd);
                        if(normRes!=null)
                            for(String key:normRes.getAttributes().keySet())
                                label.addAttribute(key, normRes.getAttribute(key));
                    }
                    chunkView.addConstituent(label);
                    clabel = null;
                } // else no chunk in progress (we are at the start of the doc)
            }

            if (lbjtoken.type.charAt(0) == 'B') {
                currentChunkStart = current.getStartSpan();
                clabel = lbjtoken.type.substring(2);
            }
            previous = current;
            tcounter++;
        }

        if (clabel != null && null != previous) {
            currentChunkEnd = previous.getEndSpan();
            Constituent label;
            Constituent temp_label =
                    new Constituent(clabel, ViewNames.TIMEX3, ta,
                            currentChunkStart, currentChunkEnd);

            if (this.useHeidelTime) {
                try {
                    clabel = heidelTimeNormalize(temp_label);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                label = new Constituent(clabel, ViewNames.TIMEX3, ta,
                        currentChunkStart, currentChunkEnd);
            }
            else {
                String tense = this.getSentenceTense(ta, temp_label.getSpan());
                TemporalPhrase temporalPhrase = new TemporalPhrase(temp_label.toString(), tense);
                TimexChunk normRes = timexNormalizer.normalize(temporalPhrase);
                label = new Constituent(normRes==null?"":normRes.toTIMEXTag(),
                        ViewNames.TIMEX3, ta,
                        currentChunkStart, currentChunkEnd);
                if (normRes != null){
                    normRes.setCharStart(temp_label.getStartCharOffset());
                    normRes.setCharEnd(temp_label.getEndCharOffset());
                    this.timex.add(normRes);
                }
                else {
                    placeHolder = new TimexChunk();
                    placeHolder.setCharStart(temp_label.getStartCharOffset());
                    placeHolder.setCharEnd(temp_label.getEndCharOffset());
                    this.timex.add(placeHolder);
                }
            }
            chunkView.addConstituent(label);
        }
        ta.addView(ViewNames.TIMEX3, chunkView);
    }

    /**
     * Given a single sentence and a DCT, do normalization
     * @param phrase
     * @param date
     * @return
     * @throws Exception
     */
    public String normalizeSinglePhrase(String phrase, String date) throws Exception {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date dct = f.parse(date);
        if (this.useHeidelTime) {
            return this.heidelTime.process(phrase, dct);
        }
        else {
            return this.timexNormalizer.normalize(phrase, dct).toString();
        }
    }

    /**
     * Use this function to add specific document creation time
     * The default DCT is current date
     * @param date the DCT you want to set
     */
    public void addDocumentCreationTime(String date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.dct = f.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!this.useHeidelTime) {
            timexNormalizer.setTime(this.dct);
        }
    }

    /**
     * Normalize temporal Phrase
     * @param temporal_phrase
     * @return
     * @throws Exception
     */
    private String heidelTimeNormalize(Constituent temporal_phrase) throws Exception {
        // If user didn't specify document creation date, use the current date
        if (this.dct == null) {
            this.dct = new Date();
        }

        String xml_res = this.heidelTime.process(temporal_phrase.toString(), this.dct);

        Document document = builder.parse(new InputSource(new StringReader(xml_res)));

        Element rootElement = document.getDocumentElement();
        String res = recurseNormalizedTimeML(rootElement, temporal_phrase, this.timex);
        return res;
    }

    /**
     * Normalize using HeidelTime, store results to the given list of TimexChunk
     * @param temporal_phrase
     * @param tc the list that user stores normalized timex to
     * @return
     * @throws Exception
     */
    private String heidelTimeNormalize(String temporal_phrase, List<TimexChunk> tc) throws Exception {
        // If user didn't specify document creation date, use the current date
        if (this.dct == null) {
            this.dct = new Date();
        }

        String xml_res = this.heidelTime.process(temporal_phrase, this.dct);

        Document document = builder.parse(new InputSource(new StringReader(xml_res)));

        Element rootElement = document.getDocumentElement();
        String res = recurseNormalizedTimeML(rootElement, temporal_phrase, tc);
        return res;
    }

    /**
     * Normalize phrases given a Node (xml format)
     * @param node
     * @param temporal_phrase
     * @param timex
     * @return
     */
    private String recurseNormalizedTimeML(Node node, String temporal_phrase, List<TimexChunk> timex) {
        // Base case: return empty string
        if (node == null) {
            return "";
        }
        // Iterate over every node, if the node is a TIMEX3 node, then concatenate all its attributes, and recurse
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE && currentNode.getNodeName().indexOf("TIMEX3")!=-1) {
                //calls this method for all the children which is Element
                NamedNodeMap attrs = currentNode.getAttributes();
                String attrPair = "";
                TimexChunk tc = new TimexChunk();
                tc.setContent(temporal_phrase);
                for (int j = 0; j < attrs.getLength(); j++) {
                    String key = attrs.item(j).getNodeName();
                    String value = attrs.item(j).getNodeValue();
                    attrPair += "[" + key + "=" + value + "]" ;
                    if ((tc != null) && (key != null) && (value != null)) {
                        tc.addAttribute(key, value);
                    }
                }
                if (tc.getAttributes().size() != 0) {
                    timex.add(tc);
                }
                return attrPair + recurseNormalizedTimeML(currentNode, temporal_phrase, timex);
            }
        }
        return "";
    }

    /**
     * Recursively read each XML tag of HeidelTime's result.
     * Notice: HeidelTime gives nested TIMEML tags, which TempEval 3 doesn't require.
     * This function only reads the innermost tag.
     * @param node
     * @param temporal_phrase
     * @return
     */
    private String recurseNormalizedTimeML(Node node, Constituent temporal_phrase, List<TimexChunk> timex) {
        // Base case: return empty string
        if (node == null) {
            return "";
        }
        // Iterate over every node, if the node is a TIMEX3 node, then concatenate all its attributes, and recurse
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE && currentNode.getNodeName().indexOf("TIMEX3")!=-1) {
                //calls this method for all the children which is Element
                NamedNodeMap attrs = currentNode.getAttributes();
                String attrPair = "";
                TimexChunk tc = new TimexChunk();
                tc.setContent(temporal_phrase.toString());
                tc.setCharStart(temporal_phrase.getStartCharOffset());
                tc.setCharEnd(temporal_phrase.getEndCharOffset());
                for (int j = 0; j < attrs.getLength(); j++) {
                    String key = attrs.item(j).getNodeName();
                    String value = attrs.item(j).getNodeValue();
                    attrPair += "[" + key + "=" + value + "]" ;
                    if ((tc != null) && (key != null) && (value != null)) {
                        tc.addAttribute(key, value);
                    }
                }
                if (tc.getAttributes().size() != 0) {
                    timex.add(tc);
                }
                return attrPair + recurseNormalizedTimeML(currentNode, temporal_phrase, timex);
            }
        }
        return "";
    }


    public TimexChunk getTimexChunkFromHeidelTime(
            String phrase, HeidelTimeStandalone htTime, Date dct, TextAnnotation ta
    ) throws Exception {
        // This is to compare our normalization and heideltime
        String xml_res = htTime.process(phrase, dct);
        Document document = builder.parse(new InputSource(new StringReader(xml_res)));
        Element rootElement = document.getDocumentElement();
        Constituent dummy = new Constituent(phrase, "", ta, 0, 1);
        List<TimexChunk> tcList = new ArrayList<>();
        String res = recurseNormalizedTimeML(rootElement, dummy, tcList);
        if (tcList.size()>0)
            return tcList.get(0);
        else
            return null;
    }

    /**
     * Get the tense of a given sentence
     * @param ta current TextAnnotation
     * @param currSpan the start and end position of the phrase
     * @return
     */
    public String getSentenceTense(TextAnnotation ta, IntPair currSpan) {
        Sentence currSentence = ta.getSentenceFromToken(currSpan.getFirst());
        IntPair sentenceSpan = currSentence.getSentenceConstituent().getSpan();
        View PosView = ta.getView("POS");
        List<Constituent> sentenceConstituents = PosView.getConstituents();
        String posStr = PosView.toString();
        String[] posList = posStr.split("\\)");
        String tense = "present";
        for (int t = sentenceSpan.getFirst(); t < sentenceSpan.getSecond(); t++) {
            Constituent currConstituent = sentenceConstituents.get(t);
            String prevWord = null;
            String prev2Word = null;
            if (t-1>=0) {
                prevWord = sentenceConstituents.get(t-1).toString().toLowerCase();
            }
            if (t-2>=0) {
                prev2Word = sentenceConstituents.get(t-2).toString().toLowerCase();
            }

            boolean isPerfect = false;
            if ( (prevWord != null && prevWord.matches("have|has|had|was|were|been")) ||
                    (prev2Word != null && prev2Word.matches("have|has|had|was|were|been"))
                    ) {
                if (posList[t].indexOf("VBN")!=-1 ) {
                    isPerfect = true;
                }
            }
            if (posList[t].indexOf("VBD")!=-1 || isPerfect){
                tense = "past";
            }
        }
        return tense;
    }

    /**
     * Extract timex from a TIMEML format text with golden normalization
     * @param text the original timeml text (with golden value)
     * @param content the plain text of the original text
     * @param ta
     * @return
     * @throws Exception
     */
    public List<TimexChunk> extractTimexFromFile(
        String text,
        String content,
        TextAnnotation ta,
        boolean verbose
    ) throws Exception{
        Document document = builder.parse(new InputSource(new StringReader(text)));
        Element rootElement = document.getDocumentElement();
        List<TimexChunk> timex = new ArrayList<>();
        List<TimexChunk> trueTimexs = new ArrayList<>();
        NodeList nodeList=document.getElementsByTagName("*");
        boolean isDct = true;
        HashMap<String, Integer> stringSpanMap = new HashMap<>();
        HashMap<IntPair, TimexChunk> res = new HashMap<>();
        int currPos = 0;

        String docId = "";
        for (int i=0; i<nodeList.getLength(); i++)
        {
            // Get element
            Node currentNode = nodeList.item(i);

            if (currentNode.getNodeName().indexOf("DOCID")!=-1) {
                docId = currentNode.getTextContent();
            }

            if (currentNode.getNodeName().indexOf("EXTRAINFO")!=-1) {
                String info = currentNode.getTextContent();
                String docName = info.split(" ")[0];
                if (!docName.equals(docId)) {
                    Pattern dctPattern = Pattern.compile("(\\d{4}\\-\\d{2}\\-\\d{2})|(\\d{2}/\\d{2}/\\d{4})");
                    Matcher dctMatcher = dctPattern.matcher(info);
                    if (dctMatcher.find()) {
                        if (dctMatcher.group(1)!=null) {
                            this.addDocumentCreationTime(dctMatcher.group(1));
                        }
                        else if (dctMatcher.group(2)!=null) {
                            String []date = dctMatcher.group(2).split("/");
                            String formattedDate = date[2]+"-"+date[0]+"-"+date[1];
                            this.addDocumentCreationTime(formattedDate);
                        }
                        else {
                            System.err.println("CANNOT EXTRACT CORRECT DCT");
                        }
                    }
                }
            }

            if (currentNode.getNodeName().indexOf("TIMEX3")!=-1) {
                // The first TIMEX3 is always DCT, ignore
                if (isDct) {
                    isDct = false;
                    continue;
                }
                TimexChunk trueTc = new TimexChunk();
                NamedNodeMap attrs = currentNode.getAttributes();
                String attrPair = "";
                for (int j = 0; j < attrs.getLength(); j++) {
                    String key = attrs.item(j).getNodeName();
                    String value = attrs.item(j).getNodeValue();
                    attrPair += "[" + key + "=" + value + "]" ;
                    if ((trueTc != null) && (key != null) && (value != null)) {
                        trueTc.addAttribute(key, value);
                    }
                }
                trueTc.setContent(currentNode.getTextContent());

                String currStr = currentNode.getTextContent();
                List<IntPair> startEndPos = ta.getSpansMatching(currStr);

                if (!stringSpanMap.containsKey(currStr)) {
                    stringSpanMap.put(currStr, 0);
                }
                else {
                    stringSpanMap.put(currStr, stringSpanMap.get(currStr)+1);
                }
                IntPair currSpan = new IntPair(0, 0);
                int charStart = 0;
                int charEnd = 0;

                // We use te3-platinum dataset for evaluation, in its AP_20130322.tml
                // file, there's a string "2009-2010". Our tokenizer will tokenize them
                // into a whole. Here we need to split them into separate tokens:
                // 2009 and 2010
                if (currStr.equals("2009") && startEndPos.size()==0) {
                    currSpan = ta.getSpansMatching("2009-2010").get(0);
                    charStart = ta.getTokenCharacterOffset(currSpan.getFirst()).getFirst();
                    charEnd = ta.getTokenCharacterOffset(currSpan.getFirst()).getSecond()-5;
                }

                if (currStr.equals("2010") && startEndPos.size()==0) {
                    currSpan = ta.getSpansMatching("2009-2010").get(0);
                    charStart = ta.getTokenCharacterOffset(currSpan.getFirst()).getFirst()+5;
                    charEnd = ta.getTokenCharacterOffset(currSpan.getFirst()).getSecond();
                }

                for (IntPair match: startEndPos) {
                    int searchStart = ta.getTokenCharacterOffset(match.getFirst()).getFirst();
                    if (searchStart>currPos) {
                        currSpan = match;
                        break;
                    }
                }

                currPos = content.indexOf(currStr, currPos);
                charStart = currPos;
                charEnd = currPos + currStr.length();
                currPos = charEnd + 1;

                String tense = getSentenceTense(ta, currSpan);

                TimexChunk tc = null;
                try {
                    if (useHeidelTime) {
                        List<TimexChunk> htTcs = new ArrayList<>();
                        String htRes = heidelTimeNormalize(
                                currStr,
                                htTcs
                        );
                        if (htTcs.size()==0) {
                            continue;
                        }
                        else {
                            int htStart = charStart;
                            int htEnd = charStart;
                            for (TimexChunk htTc : htTcs) {
                                int subIndex = currStr.indexOf(htTc.getContent());
                                htStart = charStart + subIndex;
                                htEnd = htStart + htTc.getContent().length();
                                htTc.setCharStart(htStart);
                                htTc.setCharEnd(htEnd);
                                timex.add(htTc);
                            }
                        }
                    }
                    else
                        tc = timexNormalizer.normalize(new TemporalPhrase(currStr, tense));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("CANNOT NORMALIZE: " + currStr);
                }
                if (tc!=null) {
                    tc.setContent(currStr);
                    tc.setCharStart(charStart);
                    tc.setCharEnd(charEnd);
                    timex.add(tc);
                    res.put(new IntPair(charStart, charEnd), tc);
                }
                trueTc.setCharStart(charStart);
                trueTc.setCharEnd(charEnd);
                trueTimexs.add(trueTc);
            }

        }

        if (verbose) {
            System.out.println("MISS");
            for (TimexChunk tc : trueTimexs) {
                IntPair key = new IntPair(tc.getCharStart(), tc.getCharEnd());
                if (!res.containsKey(key)) {
                    System.out.println(tc.toTIMEXString());
                    TimexChunk htRes = null;
                    if (htRes != null) {
                        htRes.setContent(tc.getContent());
                        System.out.println("HT:    " + htRes.toTIMEXString());
                    }
                }
            }
            System.out.println();
            System.out.println("WRONG TYPE");
            for (TimexChunk tc : trueTimexs) {
                IntPair key = new IntPair(tc.getCharStart(), tc.getCharEnd());
                if (res.containsKey(key)) {
                    TimexChunk ourTc = res.get(key);
                    if (ourTc.getAttribute(TimexNames.type) == null) {
                        System.out.println("OUR:   " + ourTc.toTIMEXString());
                        System.out.println("TRUE:  " + tc.toTIMEXString());
                        //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                        TimexChunk htRes = null;
                        if (htRes != null) {
                            htRes.setContent(tc.getContent());
                            System.out.println("HT:    " + htRes.toTIMEXString());
                        }
                    } else if (!ourTc.getAttribute(TimexNames.type).equals(tc.getAttribute(TimexNames.type))) {
                        System.out.println("OUR:   " + ourTc.toTIMEXString());
                        System.out.println("TRUE:  " + tc.toTIMEXString());
                        //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                        TimexChunk htRes = null;
                        if (htRes != null) {
                            htRes.setContent(tc.getContent());
                            System.out.println("HT:    " + htRes.toTIMEXString());
                        }
                    }
                }
            }
            System.out.println();
            System.out.println("WRONG VALUE");
            for (TimexChunk tc : trueTimexs) {
                IntPair key = new IntPair(tc.getCharStart(), tc.getCharEnd());
                if (res.containsKey(key)) {
                    TimexChunk ourTc = res.get(key);
                    if (ourTc.getAttribute(TimexNames.value) == null) {
                        System.out.println("OUR:   " + ourTc.toTIMEXString());
                        System.out.println("TRUE:  " + tc.toTIMEXString());
                        //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                        TimexChunk htRes = null;
                        if (htRes != null) {
                            htRes.setContent(tc.getContent());
                            System.out.println("HT:    " + htRes.toTIMEXString());
                        }
                    } else if (!ourTc.getAttribute(TimexNames.value).equals(tc.getAttribute(TimexNames.value))) {
                        System.out.println("OUR:   " + ourTc.toTIMEXString());
                        System.out.println("TRUE:  " + tc.toTIMEXString());
                        //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                        TimexChunk htRes = null;
                        if (htRes != null) {
                            htRes.setContent(tc.getContent());
                            System.out.println("HT:    " + htRes.toTIMEXString());
                        }
                    }
                }
            }
            System.out.println();
        }
        return timex;
    }

    @Override
    public String getViewName() {
        return ViewNames.TIMEX3;
    }

    /**
     * Can be used internally by {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService} to
     * check for pre-requisites before calling any single (external)
     * {@link edu.illinois.cs.cogcomp.annotation.Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by
     *         this ViewGenerator
     */
    @Override
    public String[] getRequiredViews() {
        return new String[] {ViewNames.POS};
    }

    /**
     * Change the format of a text to valid timeml format, such that we can use
     * validator provided by te3-platinum dataset.
     * @param text
     * @param docID
     * @return
     */
    public String formatTempEval3(String text, String docID) {
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat df2 = new SimpleDateFormat("yyyyMMdd");
        String DOCUMENT_FORMAT =
                "<?xml version=\"1.0\" ?>\n" +
                        "<TimeML xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://timeml.org/timeMLdocs/TimeML_1.2.1.xsd\">\n" +
                        "<DOCID>%s</DOCID>\n" +
                        "<DCT><TIMEX3 tid=\"t0\" type=\"DATE\" value=\"%s\" temporalFunction=\"false\" functionInDocument=\"CREATION_TIME\">%s</TIMEX3></DCT>\n" +
                        "<TEXT>%s</TEXT>\n" +
                        "\n</TimeML>";
        return String.format(DOCUMENT_FORMAT,
                docID,
                df1.format(this.dct),
                df2.format(this.dct),
                text.replace("&", "&amp;"));
    }

    /**
     * Write text to a valid timeml format
     * @param outputFilename
     * @param docID
     * @param text
     * @throws IOException
     */
    public void write2Text(String outputFilename, String docID, String text) throws IOException {
        char[] originalDocumentText = text.toCharArray();
        Map<Integer, String> timexInsertionMap = new HashMap<Integer, String>();
        int tidCount = 1;
        for (TimexChunk prediction : this.timex) {
            timexInsertionMap.put(prediction.getCharStart(), prediction.beginAnnotation(tidCount));
            timexInsertionMap.put(prediction.getCharEnd(), prediction.endAnnotation());
            tidCount ++;
        }

        StringBuilder annotatedDocument = new StringBuilder();
        for (int i = 0; i < originalDocumentText.length; i++) {
            if (timexInsertionMap.containsKey(i))
                annotatedDocument.append(timexInsertionMap.get(i));
            annotatedDocument.append(originalDocumentText[i]);
        }

        String outputContent = formatTempEval3(annotatedDocument.toString(), docID);
        try {
            PrintStream ps = new PrintStream(outputFilename);
            ps.print(outputContent);
            ps.close();
        } catch (FileNotFoundException e) {
            System.err.println("Unable to open file");
        }
    }

}
