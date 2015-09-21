package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
//    private static final String CONFIG = "src/test/resources/pipeline-full-config.properties";

    private static Logger logger = LoggerFactory.getLogger( IllinoisNewPipelineTest.class );

    private static AnnotatorService prep;

    private static String text;

    @BeforeClass
    public static void setUpOnce() throws Exception
    {
        ResourceManager pipelineRm = new PipelineConfigurator().getDefaultConfig();
        ResourceManager annotatorServiceRm = new AnnotatorServiceConfigurator().getConfig( pipelineRm );

//        try
//        {
//            ResourceManager nonDefaultRm = new ResourceManager( CONFIG );
//            rm = ( new PipelineConfigurator() ).getConfig( nonDefaultRm );
//        }
//        catch ( IOException e )
//        {
//            e.printStackTrace();
//            System.exit( -1 );
//        }

        prep = IllinoisPipelineFactory.buildPipeline( annotatorServiceRm );

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


    /**
     * this text has no punctuation, and caused OOM exception when no limit on sentence length was
     *    passed to the stanford parser on construction. Maybe all our tools should have this
     *    option too...
     */
    @Test
    public void horribleNewsgroupTextNoPunc()
    {
        String inputFile = "src/test/resources/newsgroupNoPunc.txt";

        String text = null;
        try {
            text = LineIO.slurp(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        TextAnnotation basicTextAnnotation = null;
        try {
            basicTextAnnotation = prep.createBasicTextAnnotation("test", "test", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        String key = AnnotatorService.getCacheKey(basicTextAnnotation, ViewNames.PARSE_STANFORD );

        try {
            prep.removeKeyFromCache( key );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        try {
            prep.addView( basicTextAnnotation, ViewNames.PARSE_STANFORD, true );
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
        System.out.println(basicTextAnnotation.toString());

    }

    @Test
    public void stanfordFailTest()
    {
        String inputFile = "src/test/resources/stanfordFailExample.txt";

        String text = null;
        try {
            text = LineIO.slurp(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        TextAnnotation basicTextAnnotation = null;
        try {
            basicTextAnnotation = prep.createBasicTextAnnotation("test", "test", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        String key = AnnotatorService.getCacheKey(basicTextAnnotation, ViewNames.DEPENDENCY_STANFORD );

        try {
            prep.removeKeyFromCache( key );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        try {
            prep.addView( basicTextAnnotation, ViewNames.DEPENDENCY_STANFORD, true );
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println( "Expected exception from stanford. ");
        } catch (AnnotatorException e) {
            e.printStackTrace();
            System.out.println( "maybe not an expected problem: " + e.getMessage() );
            fail( e.getMessage() );
        }
        System.out.println(basicTextAnnotation.toString());
    }

    @Test
    public void testCacheDisabled()
    {
        Properties props = new Properties();
        props.setProperty( AnnotatorServiceConfigurator.DISABLE_CACHE, Configurator.TRUE );

        ResourceManager pipelineRm = new PipelineConfigurator().getConfig( new ResourceManager( props ));


/**
 * instantiating a second pipeline should result in an error if there is already an instance
 *     with an active cache in the same VM
 */
        AnnotatorService secondPipeline = null;
        try {
            secondPipeline = IllinoisPipelineFactory.buildPipeline(pipelineRm);
        } catch (IOException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        boolean forceUpdate = false;
        try {
            secondPipeline.createAnnotatedTextAnnotation( text, forceUpdate );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );

        }

    }

}
