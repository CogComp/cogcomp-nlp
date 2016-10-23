package edu.illinois.cs.cogcomp.temporal.normalizer.main;

import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;


import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.LBJavaUtils;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zhilifeng on 10/2/16.
 */
public class TemporalChunkerAnnotator extends Annotator{
    private static final String NAME = TemporalChunkerAnnotator.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(TemporalChunkerAnnotator.class);
    private Chunker tagger;
    private String posfield = ViewNames.POS;
    private String tokensfield = ViewNames.TOKENS;
    private String sentencesfield = ViewNames.SENTENCE;
    private HeidelTimeStandalone heidelTime = null;
    private Date dct = null;

    /**
     * default: don't use lazy initialization
     */
    public TemporalChunkerAnnotator() {
        this(false);
    }

    /**
     * Constructor parameter allows user to specify whether or not to lazily initialize.
     *
     * @param lazilyInitialize If set to 'true', models will not be loaded until first call
     *        requiring Chunker annotation.
     */
    public TemporalChunkerAnnotator(boolean lazilyInitialize) {
        super(ViewNames.TIMEX3, new String[] {ViewNames.POS}, lazilyInitialize);

    }

    public TemporalChunkerAnnotator(ResourceManager nonDefaultRm) {
        super(ViewNames.TIMEX3, new String[]{}, nonDefaultRm.getBoolean(
                AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, Configurator.FALSE), nonDefaultRm);

        heidelTime = new HeidelTimeStandalone(Language.ENGLISH,
                DocumentType.COLLOQUIAL,
                OutputType.TIMEML,
                "conf/config.props",
                POSTagger.TREETAGGER, true);
    }

    @Override
    public void initialize(ResourceManager rm) {
        rm = new ChunkerConfigurator().getConfig(nonDefaultRm);
        String model = rm.getString(ChunkerConfigurator.MODEL_PATH);
        tagger = new Chunker(
                rm.getString(ChunkerConfigurator.MODEL_PATH), rm.getString(ChunkerConfigurator.MODEL_LEX_PATH)
        );
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
        // String rawText = record.getText();

        List<Token> lbjTokens = LBJavaUtils.recordToLBJTokens(record);

        View chunkView = new SpanLabelView(ViewNames.TIMEX3, this.NAME, record, 1.0);

        int currentChunkStart = 0;
        int currentChunkEnd = 0;

        String clabel = "";
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
                    currentChunkEnd = previous.getEndSpan();
                    Constituent temp_label =
                            new Constituent(clabel, ViewNames.TIMEX3, record,
                                    currentChunkStart, currentChunkEnd);
                    try {
                        clabel = heidelTimeNormalize(temp_label);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    Constituent label =
                            new Constituent(clabel, ViewNames.TIMEX3, record,
                                    currentChunkStart, currentChunkEnd);
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
            Constituent temp_label =
                    new Constituent(clabel, ViewNames.TIMEX3, record,
                            currentChunkStart, currentChunkEnd);
            try {
                clabel = heidelTimeNormalize(temp_label);
            } catch (Exception e) {
                System.out.println(e);
            }

            Constituent label =
                    new Constituent(clabel, ViewNames.TIMEX3, record,
                            currentChunkStart, currentChunkEnd);
            chunkView.addConstituent(label);
        }
        record.addView(ViewNames.TIMEX3, chunkView);

        return; // chunkView;
    }

    public void addDocumentCreationTime(String date) {
        SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            this.dct = f.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String heidelTimeNormalize(Constituent temporal_phrase) throws Exception {
        // If user didn't specify document creation date, use the current date
        if (this.dct == null) {
            this.dct = new Date();
        }

        String xml_res = heidelTime.process(temporal_phrase.toString(), this.dct);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml_res)));

        Element rootElement = document.getDocumentElement();
        String res = recurseNormalizedTimeML(rootElement);
        return res;
    }


    private String recurseNormalizedTimeML(Node node) {
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
                for (int j = 0; j < attrs.getLength(); j++) {
                    attrPair += "["
                            + attrs.item(j).getNodeName() + "="
                            + attrs.item(j).getNodeValue() + "]" ;
                }
                return attrPair + recurseNormalizedTimeML(currentNode);
            }
        }
        return "";
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


    public static void main(String []args) throws Exception{

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));


        ResourceManager rm = new ResourceManager("./conf/chunker_config.props");
        TemporalChunkerAnnotator tca = new TemporalChunkerAnnotator(rm);


        String path = "./test.txt";
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String text = new String(encoded, StandardCharsets.UTF_8);

        AnnotatorService annotator = CuratorFactory.buildCuratorClient();
        TextAnnotation ta = annotator.createBasicTextAnnotation("corpus", "id", text);
        annotator.addView(ta, ViewNames.POS);

        tca.addView(ta);

        View timexView = ta.getView(ViewNames.TIMEX3);

        String corpId = "IllinoisTimeAnnotator";
        List<Constituent> timeCons = timexView.getConstituents();

        // Keep track of the compressed index of each constituent.
        Span[] compressedSpans = new Span[timeCons.size()];
        int spanStart;

        // Builds a string of the concatenated constituents from a labeled view.
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<timeCons.size(); i++){
            Constituent c = timeCons.get(i);
            spanStart = builder.length();

            builder.append(c.toString());
            builder.append("; ");

            compressedSpans[i] = new Span(spanStart, builder.length()-2);
        }
        String compressedText = builder.toString();

        System.out.println(compressedText);
    }
}
