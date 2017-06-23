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
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.LBJavaUtils;


import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.TemporalPhrase;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.TimexChunk;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.TimexNames;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.TimexNormalizer;
import javafx.geometry.Pos;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 10/2/16.
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

//        tagger = new Chunker(
//                rm.getString(TemporalChunkerConfigurator.MODEL_PATH),
//                rm.getString(TemporalChunkerConfigurator.MODEL_LEX_PATH));
//        tagger.readModel(lcPath);
//        tagger.readLexicon(lexPath);
        tagger = new Chunker(
//                "/Users/zhilifeng/Desktop/DanRothResearch/illinois-cogcomp-nlp/temporal-normalizer/src/" +
//                        "main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/" +
//                        "TBAQ_full_1label_corr_temp50/TBAQ_full_1label_corr_temp50.lc",
//                "/Users/zhilifeng/Desktop/DanRothResearch/illinois-cogcomp-nlp/temporal-normalizer/src/" +
//                        "main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/" +
//                        "TBAQ_full_1label_corr_temp50/TBAQ_full_1label_corr_temp50.lex"
                "/Users/zhilifeng/Desktop/DanRothResearch/illinois-cogcomp-nlp/temporal-normalizer/src/" +
                        "main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/" +
                        "prev_TB_full_1label_corr_bestit/prev_TB_full_1label_corr_bestit.lc",
                "/Users/zhilifeng/Desktop/DanRothResearch/illinois-cogcomp-nlp/temporal-normalizer/src/" +
                        "main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/" +
                        "prev_TB_full_1label_corr_bestit/prev_TB_full_1label_corr_bestit.lex"
                );
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

    public void deleteTimex() {
        this.timex = new ArrayList<>();
    }

    @Override
    public void addView(TextAnnotation record) throws AnnotatorException {
        if (!record.hasView(tokensfield) || !record.hasView(sentencesfield)
                || !record.hasView(posfield)) {
            String msg = "Record must be tokenized, sentence split, and POS-tagged first.";
            logger.error(msg);
            throw new AnnotatorException(msg);
        }

        List<Constituent> tags = record.getView(posfield).getConstituents();

        List<Token> lbjTokens = LBJavaUtils.recordToLBJTokens(record);

        View chunkView = new SpanLabelView(ViewNames.TIMEX3, this.NAME, record, 1.0);

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
                    // trying to avoid mysterious null pointer exception...
                    lbjtoken.type = "B" + lbjtoken.type.substring(1);
                }
            }
            if ((lbjtoken.type.charAt(0) == 'B' || lbjtoken.type.charAt(0) == 'O')
                    && clabel != null) {

                if (previous != null) {
                    int curSentenceId = current.getSentenceId();
                    Sentence curSentence = record.getSentence(curSentenceId);

                    currentChunkEnd = previous.getEndSpan();
                    Constituent label;
                    Constituent temp_label =
                            new Constituent(clabel, ViewNames.TIMEX3, record,
                                    currentChunkStart, currentChunkEnd);
                    if (this.useHeidelTime) {
                        try {
                            clabel = heidelTimeNormalize(temp_label);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        label = new Constituent(clabel, ViewNames.TIMEX3, record,
                                currentChunkStart, currentChunkEnd);
                    }
                    else {
                        String tense = this.getSentenceTense(record, temp_label.getSpan());
                        TemporalPhrase temporalPhrase = new TemporalPhrase(temp_label.toString(), tense);
                        TimexChunk normRes = timexNormalizer.normalize(temporalPhrase);
                        if (normRes != null) {
                            normRes.setCharStart(temp_label.getStartCharOffset());
                            normRes.setCharEnd(temp_label.getEndCharOffset());
                            this.timex.add(normRes);
                        }
                        else {
                            TimexChunk dummy = new TimexChunk();
                            dummy.setCharStart(temp_label.getStartCharOffset());
                            dummy.setCharEnd(temp_label.getEndCharOffset());
                            this.timex.add(dummy);
                        }
                        label = new Constituent(normRes==null?"":normRes.toTIMEXString(),
                                ViewNames.TIMEX3, record,
                                currentChunkStart, currentChunkEnd);
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
                    new Constituent(clabel, ViewNames.TIMEX3, record,
                            currentChunkStart, currentChunkEnd);

            if (this.useHeidelTime) {
                try {
                    clabel = heidelTimeNormalize(temp_label);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                label = new Constituent(clabel, ViewNames.TIMEX3, record,
                        currentChunkStart, currentChunkEnd);
            }
            else {
                String tense = this.getSentenceTense(record, temp_label.getSpan());
                TemporalPhrase temporalPhrase = new TemporalPhrase(temp_label.toString(), tense);
                TimexChunk normRes = timexNormalizer.normalize(temporalPhrase);

                label = new Constituent(normRes==null?"":normRes.toTIMEXString(),
                        ViewNames.TIMEX3, record,
                        currentChunkStart, currentChunkEnd);
                if (normRes != null){
                    normRes.setCharStart(temp_label.getStartCharOffset());
                    normRes.setCharEnd(temp_label.getEndCharOffset());
                    this.timex.add(normRes);
                }
                else {
                    TimexChunk dummy = new TimexChunk();
                    dummy.setCharStart(temp_label.getStartCharOffset());
                    dummy.setCharEnd(temp_label.getEndCharOffset());
                    this.timex.add(dummy);
                }
            }
            chunkView.addConstituent(label);
        }
        record.addView(ViewNames.TIMEX3, chunkView);

        return; // chunkView;
    }

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
     * Normalize temporal phrase
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

    public String getSentenceTense(TextAnnotation ta, IntPair currSpan) {
//        Sentence currSentence = ta.getSentenceFromToken(currSpan.getFirst());
//        IntPair sentenceSpan = currSentence.getSentenceConstituent().getSpan();
//        View PosView = ta.getView("POS");
//        List<Constituent> sentenceConstituents = PosView.getConstituents();
//        String posStr = PosView.toString();
//        String[] posList = posStr.split("\\)");
//        String tense = "present";
//        for (int t = sentenceSpan.getFirst(); t < sentenceSpan.getSecond(); t++) {
//            Constituent currConstituent = sentenceConstituents.get(t);
//            //System.out.println(currConstituent.getView());
//            if (posList[t].indexOf("VBD")!=-1 || posList[t].indexOf("VBN")!=-1){
//                tense = "past";
//            }
//        }
//        return tense;

        Sentence currSentence = ta.getSentenceFromToken(currSpan.getFirst());
        IntPair sentenceSpan = currSentence.getSentenceConstituent().getSpan();
        View PosView = ta.getView("POS");
        List<Constituent> sentenceConstituents = PosView.getConstituents();
        String posStr = PosView.toString();
        String[] posList = posStr.split("\\)");
        String tense = "present";
        for (int t = sentenceSpan.getFirst(); t < sentenceSpan.getSecond(); t++) {
            Constituent currConstituent = sentenceConstituents.get(t);
            //System.out.println(currConstituent.getView());
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

    public List<TimexChunk> extractTimexFromFile(String text, String content, TextAnnotation ta) throws Exception{
        Document document = builder.parse(new InputSource(new StringReader(text)));
        Element rootElement = document.getDocumentElement();
        List<TimexChunk> timex = new ArrayList<>();
        List<TimexChunk> trueTimexs = new ArrayList<>();
        NodeList nodeList=document.getElementsByTagName("*");
        boolean isDct = true;
        HashMap<String, Integer> stringSpanMap = new HashMap<>();
        HashMap<IntPair, TimexChunk> res = new HashMap<>();
        int currPos = 0;
//        HeidelTimeStandalone htTime = new HeidelTimeStandalone(
//                Language.ENGLISH,
//                DocumentType.NEWS,
//                OutputType.TIMEML,
//                "src/main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/conf/heideltime_config.props",
//                POSTagger.NO,
//                false
//        );
//        java.util.logging.Logger.getLogger("HeidelTimeStandalone").setLevel(Level.OFF);

        String docId = "";
        for (int i=0; i<nodeList.getLength(); i++)
        {
            // Get element
            Node currentNode = nodeList.item(i);

            if (currentNode.getNodeName().indexOf("DOCID")!=-1) {
                docId = currentNode.getTextContent();
            }

//            if (currentNode.getNodeName().indexOf("DCT")!=-1) {
//                Node dctNode = currentNode.getChildNodes().item(0);
//                NamedNodeMap dctAttrs = dctNode.getAttributes();
//                for (int j = 0; j < dctAttrs.getLength(); j++) {
//                    if (dctAttrs.item(j).getNodeName().equals("value")) {
//                        //DCTs.add(dctAttrs.item(j).getNodeValue());
//                        System.out.println(dctAttrs.item(j).getNodeValue());
//                    }
//                }
//            }

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

//                Sentence currSentence = ta.getSentenceFromToken(currSpan.getFirst());
//                IntPair sentenceSpan = currSentence.getSentenceConstituent().getSpan();
//                View PosView = ta.getView("POS");
//                List<Constituent> sentenceConstituents = PosView.getConstituents();
//                String posStr = PosView.toString();
//                String[] posList = posStr.split("\\)");
//                String tense = "present";
//                for (int t = sentenceSpan.getFirst(); t < sentenceSpan.getSecond(); t++) {
//                    Constituent currConstituent = sentenceConstituents.get(t);
//                    //System.out.println(currConstituent.getView());
//                    String prevWord = null;
//                    String prev2Word = null;
//                    if (t-1>=0) {
//                        prevWord = sentenceConstituents.get(t-1).toString().toLowerCase();
//                    }
//                    if (t-2>=0) {
//                        prev2Word = sentenceConstituents.get(t-2).toString().toLowerCase();
//                    }
//
//                    boolean isPerfect = false;
//                    if ( (prevWord != null && prevWord.matches("have|has|had")) ||
//                            (prev2Word != null && prev2Word.matches("have|has|had"))
//                            ) {
//                        if (posList[t].indexOf("VBN")!=-1 ) {
//                            isPerfect = true;
//                        }
//                    }
//                    if (posList[t].indexOf("VBD")!=-1 || isPerfect){
//                        tense = "past";
//                    }
//                }

                String tense = getSentenceTense(ta, currSpan);
                //System.out.println(trueTc.toTIMEXString());

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
                            if (htTcs.size()>1)
                                System.out.println(docId + " " + currStr);
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
                    System.err.println("CANNOT NORMALIZE: " + currStr);
                }
                if (tc!=null) {
                    tc.setContent(currStr);
                    tc.setCharStart(charStart);
                    tc.setCharEnd(charEnd);

                    //                tc.setInterval(normInterval);
                    timex.add(tc);
                    res.put(new IntPair(charStart, charEnd), tc);
                }
                //System.out.println(currStr + " " + normInterval);
                trueTc.setCharStart(charStart);
                trueTc.setCharEnd(charEnd);


                trueTimexs.add(trueTc);
            }

        }

        System.out.println("MISS");
        for (TimexChunk tc:trueTimexs) {
            IntPair key = new IntPair(tc.getCharStart(), tc.getCharEnd());
            if (!res.containsKey(key)) {
                System.out.println(tc.toTIMEXString());
                //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                TimexChunk htRes = null;
                if (htRes!=null) {
                    htRes.setContent(tc.getContent());
                    System.out.println("HT:    " + htRes.toTIMEXString());
                }
            }
        }
        System.out.println();
        System.out.println("WRONG TYPE");
        for (TimexChunk tc:trueTimexs) {
            IntPair key = new IntPair(tc.getCharStart(), tc.getCharEnd());
            if (res.containsKey(key)) {
                TimexChunk ourTc = res.get(key);
                if (ourTc.getAttribute(TimexNames.type)==null) {
                    System.out.println("OUR:   " + ourTc.toTIMEXString());
                    System.out.println("TRUE:  " + tc.toTIMEXString());
                    //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                    TimexChunk htRes = null;
                    if (htRes!=null) {
                        htRes.setContent(tc.getContent());
                        System.out.println("HT:    " + htRes.toTIMEXString());
                    }
                }
                else if (!ourTc.getAttribute(TimexNames.type).equals(tc.getAttribute(TimexNames.type))) {
                    System.out.println("OUR:   " + ourTc.toTIMEXString());
                    System.out.println("TRUE:  " + tc.toTIMEXString());
                    //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                    TimexChunk htRes = null;
                    if (htRes!=null) {
                        htRes.setContent(tc.getContent());
                        System.out.println("HT:    " + htRes.toTIMEXString());
                    }
                }
            }
        }
        System.out.println();
        System.out.println("WRONG VALUE");
        for (TimexChunk tc:trueTimexs) {
            IntPair key = new IntPair(tc.getCharStart(), tc.getCharEnd());
            if (res.containsKey(key)) {
                TimexChunk ourTc = res.get(key);
                if (ourTc.getAttribute(TimexNames.value)==null) {
                    System.out.println("OUR:   " + ourTc.toTIMEXString());
                    System.out.println("TRUE:  " + tc.toTIMEXString());
                    //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                    TimexChunk htRes = null;
                    if (htRes!=null) {
                        htRes.setContent(tc.getContent());
                        System.out.println("HT:    " + htRes.toTIMEXString());
                    }
                }
                else if (!ourTc.getAttribute(TimexNames.value).equals(tc.getAttribute(TimexNames.value))) {
                    System.out.println("OUR:   " + ourTc.toTIMEXString());
                    System.out.println("TRUE:  " + tc.toTIMEXString());
                    //TimexChunk htRes = this.getTimexChunkFromHeidelTime(tc.getContent(), htTime, this.dct, ta);
                    TimexChunk htRes = null;
                    if (htRes!=null) {
                        htRes.setContent(tc.getContent());
                        System.out.println("HT:    " + htRes.toTIMEXString());
                    }
                }
            }
        }

        System.out.println();

        return timex;
    }

//    /**
//     * Normalize temporal phrase using Illini-time
//     * @param temporal_phrase
//     * @return
//     * @throws Exception
//     */
//    private String illiniNormalize(Constituent temporal_phrase) throws Exception {
//        // If user didn't specify document creation date, use the current date
//        if (this.dct == null) {
//            this.dct = new Date();
//            timexNormalizer.setTime(this.dct);
//        }
//
//        //String temp = this.heidelTime.process(text, this.dct);
//        //System.out.println(temp);
//        String xml_res = this.heidelTime.process(temporal_phrase.toString(), this.dct);
//        System.out.println(xml_res);
//        int startIndex = xml_res.indexOf("<TimeML>");
//        xml_res = xml_res.substring(startIndex);
//        Interval interval_res = timexNormalizer.normalize(temporal_phrase.toString());
//
//        String string_res = interval_res==null?"":interval_res.toString();
//
//        return string_res;
//    }


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

    public void write2Text(String outputFilename, String docID, String text) throws IOException {
//        File f = new File(System.getProperty("user.dir"), outputFilename);
//        if(!f.exists())
//            f.createNewFile();
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
