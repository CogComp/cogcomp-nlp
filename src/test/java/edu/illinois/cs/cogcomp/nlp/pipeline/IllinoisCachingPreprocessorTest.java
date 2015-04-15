package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by mssammon on 4/14/15.
 */
public class IllinoisCachingPreprocessorTest
{
    private static final String CONFIG = "src/test/resources/testConfig.txt";

    private static Logger logger = LoggerFactory.getLogger( IllinoisCachingPreprocessorTest.class );

    private IllinoisCachingPreprocessor prep;

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
        prep = IllinoisCachingPreprocessor.getInstance( rm, null );

        IllinoisCachingPreprocessor.openCache();

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
