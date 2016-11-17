package edu.illinois.cs.cogcomp.temporal.normalizer.main;

import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;


import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.LBJavaUtils;


import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.TimexNormalizer;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        initialize(nonDefaultRm);
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

        tagger = new Chunker(
                rm.getString(TemporalChunkerConfigurator.MODEL_PATH),
                rm.getString(TemporalChunkerConfigurator.MODEL_LEX_PATH));
        tagger.readModel(lcPath);
        tagger.readLexicon(lexPath);
        this.heidelTime = new HeidelTimeStandalone(
                Language.ENGLISH,
                DocumentType.valueOf(rm.getString(TemporalChunkerConfigurator.DOCUMENT_TYPE)),
                OutputType.valueOf(rm.getString(TemporalChunkerConfigurator.OUTPUT_TYPE)),
                rm.getString(TemporalChunkerConfigurator.HEIDELTIME_CONFIG),
                POSTagger.valueOf(rm.getString(TemporalChunkerConfigurator.POSTAGGER_TYPE)),
                true
        );
        timexNormalizer = new TimexNormalizer();

        this.dct = new Date();
        timexNormalizer.setTime(this.dct);

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
//                    try {
//                        clabel = heidelTimeNormalize(temp_label);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    Constituent label =
//                            new Constituent(clabel, ViewNames.TIMEX3, record,
//                                    currentChunkStart, currentChunkEnd);
                    Interval normRes = timexNormalizer.normalize(temp_label.toString());
                    Constituent label = new Constituent(normRes==null?"":normRes.toString(),
                            ViewNames.TIMEX3, record,
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
//            try {
//                clabel = heidelTimeNormalize(temp_label);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            Constituent label =
//                    new Constituent(clabel, ViewNames.TIMEX3, record,
//                            currentChunkStart, currentChunkEnd);
            Interval normRes = timexNormalizer.normalize(temp_label.toString());
            Constituent label = new Constituent(normRes==null?"":normRes.toString(),
                    ViewNames.TIMEX3, record,
                    currentChunkStart, currentChunkEnd);
            chunkView.addConstituent(label);
        }
        record.addView(ViewNames.TIMEX3, chunkView);

        return; // chunkView;
    }

    /**
     * Use this function to add specific document creation time
     * The default DCT is current date
     * @param date the DCT you want to set
     */
    public void addDocumentCreationTime(String date) {
        SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            this.dct = f.parse(date);
            timexNormalizer.setTime(this.dct);
        } catch (ParseException e) {
            e.printStackTrace();
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
            timexNormalizer.setTime(this.dct);
        }

        String text = "DAVAO, Philippines, March 4 (AFP). At least 19 people were killed and 114 people were wounded in Tuesday's southern Philippines airport blast, officials said, but reports said the death toll could climb to 30. Radio station DXDC placed the death toll at 30, without giving a source for the figure, which officials could not immediately confirm. The Davao Medical Center, a regional government hospital, recorded 19 deaths with 50 wounded. Medical evacuation workers however said the injured list was around 114, spread out at various hospitals. A powerful bomb tore through a waiting shed at the Davao City international airport at about 5.15 pm (0915 GMT) while another explosion hit a bus terminal at the city. There were no reports of injuries in the second blast. \"It's a very powerful bomb. The waiting shed literally exploded,\" said Vice Mayor Luis Bongoyan, speaking to local radio station. Television footage showed medical teams carting away dozens of wounded victims with fully armed troops on guard. Many of the victims were shown with hastily applied bandages, and teams of nurses and doctors were seen in packed emergency rooms attending to the wounded.";

        //String temp = this.heidelTime.process(text, this.dct);
        //System.out.println(temp);
        String xml_res = this.heidelTime.process(temporal_phrase.toString(), this.dct);
        System.out.println(xml_res);
        int startIndex = xml_res.indexOf("<TimeML>");
        xml_res = xml_res.substring(startIndex);
        Interval interval_res = timexNormalizer.normalize(xml_res);
        System.out.println(timexNormalizer.normalize("march 4"
              ));
        String string_res = interval_res==null?"":interval_res.toString();

        return string_res;
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

}
