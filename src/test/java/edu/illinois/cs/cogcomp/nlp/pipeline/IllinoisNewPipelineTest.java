package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.util.EdisonPrintAlignedAnnotation;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Create a pipeline simply from a set of handlers (Annotators) and
 *    CachingAnnotatorService.
 *
 * Created by mssammon on 4/14/15.
 */
public class IllinoisNewPipelineTest
{
    private static final String CONFIG = "src/test/resources/pipeline-full-config.properties";

    private static Logger logger = LoggerFactory.getLogger( IllinoisNewPipelineTest.class );

    private static AnnotatorService prep;

    private static String text;

    @BeforeClass
    public static void setUpOnce() throws Exception
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

        prep = IllinoisPipelineFactory.buildPipeline( rm );

        text = "The priest stared at John for a long time, startled at his disclosure. " +
                "The Altman Inn had long been a topic of public vexation for the St. Francis seminary, " +
                "and Ivor McDougal had long maintained that the relative ineffectiveness of Bishop Godfrey " +
                "could be traced to his long unhappy years there." ;
    }

    @AfterClass
    public static void finalTeardown()
    {
        prep.closeCache();
    }

    @After
    public void tearDown() throws Exception
    {
    }


    @Test
    public void testCacheTiming()
    {
        logger.debug("starting testCacheTiming test");

        // so that it is slow!
        try {
            prep.removeKeyFromCache(text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        long start, end, duration, total = 0;

        // Annotate input texts, put stuff in the cache
        start = System.currentTimeMillis();
        try {
            boolean forceUpdate = true;
            prep.createAnnotatedTextAnnotation(text, forceUpdate);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
        end = System.currentTimeMillis();

        long firsttotal = end - start;
        logger.debug("original annotation cost: " + firsttotal + " milliseconds");

        int n = 5;
        boolean forceUpdate = false;
        for (int j = 0; j < n; j++) {
            start = System.currentTimeMillis();
            try {
                prep.createAnnotatedTextAnnotation( text, forceUpdate );
            } catch (AnnotatorException e) {
                e.printStackTrace();
                fail( e.getMessage() );

            }
            end = System.currentTimeMillis();

            duration = end - start;
            logger.debug("duration is: " + duration + " milliseconds");
            total += duration;
        }


        long avg = total / n;
        logger.debug("Average = " + avg + " milliseconds");

        assertTrue(avg < firsttotal);

    }

    @Test
    public void TestPipelineProcessing()
    {
        try {
            prep.removeKeyFromCache(text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        TextAnnotation ta = null;
        try {
            boolean forceUpdate = false;
            ta = prep.createAnnotatedTextAnnotation(text, forceUpdate );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assertTrue(ta.hasView(ViewNames.NER_CONLL));
        assertTrue(ta.hasView(ViewNames.NER_ONTONOTES));
        assertTrue( ta.hasView( ViewNames.POS ) );
        assertTrue( ta.hasView( ViewNames.PARSE_STANFORD ) );
        assertTrue( ta.hasView( ViewNames.DEPENDENCY_STANFORD ) );
        assertTrue( ta.hasView( ViewNames.SHALLOW_PARSE ) );
        assertTrue( ta.hasView( ViewNames.LEMMA ) );
        assertTrue( ta.hasView( ViewNames.SENTENCE ) );

        assertEquals(ta.getView(ViewNames.NER_CONLL).getConstituents().size(), 5);
    }
}
