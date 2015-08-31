package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.CachingAnnotatorService;
import edu.illinois.cs.cogcomp.annotation.handler.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TextAnnotationBuilder;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Create a pipeline simply from a set of handlers (Annotators) and
 *    CachingAnnotatorService.
 *
 * Created by mssammon on 4/14/15.
 */
public class IllinoisNewPipelineTest
{
    private static final String CONFIG = "src/test/resources/testConfig.properties";
    private static final String NER_CONLL_CONFIG = "config/ner-conll-config.properties";
    private static final String LEMMA_CONFIG = "config/lemmatizer-config.properties";

    private static Logger logger = LoggerFactory.getLogger( IllinoisNewPipelineTest.class );

    private CachingAnnotatorService prep;

    private String text;

    @Before
    public void setUp() throws Exception
    {
        ResourceManager rm = null;
        try
        {
            rm = new ResourceManager( CONFIG );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }

        TextAnnotationBuilder taBuilder = new TextAnnotationBuilder( new IllinoisTokenizer() );

        IllinoisPOSHandler pos = new IllinoisPOSHandler();
        IllinoisChunkerHandler chunk = new IllinoisChunkerHandler();
        IllinoisNerHandler ner = new IllinoisNerHandler( NER_CONLL_CONFIG, ViewNames.NER_CONLL );
        IllinoisLemmatizerHandler lemma = new IllinoisLemmatizerHandler( LEMMA_CONFIG );


        Properties stanfordProps = new Properties();
        stanfordProps.put( "annotators", "pos, parse") ;
        stanfordProps.put("parse.originalDependencies", true);

        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator( "pos", stanfordProps );
        ParserAnnotator parseAnnotator = new ParserAnnotator( "parse", stanfordProps );

        StanfordParseHandler parser = new StanfordParseHandler( posAnnotator, parseAnnotator );
        StanfordDepHandler depParser = new StanfordDepHandler( posAnnotator, parseAnnotator );

        Map< String, Annotator> extraViewGenerators = new HashMap<String, Annotator>();

        extraViewGenerators.put( ViewNames.POS, pos );
        extraViewGenerators.put( ViewNames.SHALLOW_PARSE, chunk );
        extraViewGenerators.put( ViewNames.LEMMA, lemma );
        extraViewGenerators.put( ViewNames.NER_CONLL, ner );
        extraViewGenerators.put( ViewNames.PARSE_STANFORD, parser );
        extraViewGenerators.put( ViewNames.DEPENDENCY_STANFORD, depParser );

        boolean throwExceptionIfUncached = false;

        Map< String, Boolean > requestedViews = new HashMap<String, Boolean>();
        for ( String view : extraViewGenerators.keySet() )
            requestedViews.put( view, false );

        prep = new CachingAnnotatorService(requestedViews, taBuilder, throwExceptionIfUncached, extraViewGenerators );
        //prep = IllinoisCachingPreprocessor.getInstance(CONFIG, taBuilder, extraViewGenerators);
        String cacheDir = rm.getString( IllinoisCachingPreprocessor.CACHE_DIR );

        CachingAnnotatorService.openCache(cacheDir);

        text = "The priest stared at John for a long time, startled at his disclosure. " +
                "The Altman Inn had long been a topic of public vexation for the St. Francis seminary, " +
                "and Ivor McDougal had long maintained that the relative ineffectiveness of Bishop Godfrey " +
                "could be traced to his long unhappy years there." ;
    }

    @After
    public void tearDown() throws Exception
    {
    }


    @Test
    public void testCacheTiming() throws Exception {
        logger.debug("starting testCacheTiming test");

        // so that it is slow!
        prep.removeKeyFromCache(text);

        long start, end, duration, total = 0;

        // Annotate input texts, put stuff in the cache
        start = System.currentTimeMillis();
        prep.getCachedTextAnnotation(text);
        end = System.currentTimeMillis();

        long firsttotal = end - start;
        logger.debug("original annotation cost: " + firsttotal + " milliseconds");

        int n = 5;
        for (int j = 0; j < n; j++) {
            start = System.currentTimeMillis();
            prep.getCachedTextAnnotation(text);
            end = System.currentTimeMillis();

            duration = end - start;
            logger.debug("duration is: " + duration + " milliseconds");
            total += duration;
        }

        long avg = total / n;
        logger.debug("Average = " + avg + " milliseconds");

        assertTrue(avg < firsttotal);

    }

}
