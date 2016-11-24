package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import org.junit.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for SimpleCachingPipeline
 * Created by mssammon on 9/21/15.
 */
public class CachingPipelineTest
{
    private static final String TEST_CACHE_DIR = "test-annotation-cache";
    private static BasicAnnotatorService processor;
    private static HashSet<String> activeViews;

    @BeforeClass
    public static void init() throws IOException, AnnotatorException {
        Properties props = new Properties();
        props.setProperty( PipelineConfigurator.USE_NER_ONTONOTES.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_SRL_VERB.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_SRL_NOM.key, Configurator.FALSE );

        props.setProperty( AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key, Configurator.TRUE );
        props.setProperty( AnnotatorServiceConfigurator.CACHE_DIR.key, TEST_CACHE_DIR );
        props.setProperty( AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_JSON.key, Configurator.FALSE );
        props.setProperty( AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.FALSE );

        processor = PipelineFactory.buildPipeline(new ResourceManager(props));
    }

    @AfterClass
    public static void cleanUp()
    {
        processor = null;
    }


    @Before
    public void removeCacheFiles()
    {
        try {
            if (IOUtils.exists(TEST_CACHE_DIR))
                IOUtils.rm(TEST_CACHE_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanUpAfterTest()
    {
//        removeCacheFiles();
    }

    @Test
    public void testCachingPipeline()
    {
        TextAnnotation ta = null;
        String newText = "This is some text that the USA hasn't seen from Bill Smith before...";
        try {
            ta = processor.createBasicTextAnnotation("test", "test", newText);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
        assertFalse(ta.hasView(ViewNames.SHALLOW_PARSE));
        assertFalse(ta.hasView(ViewNames.NER_CONLL));

        String[] viewsToAdd =  { ViewNames.SHALLOW_PARSE, ViewNames.NER_CONLL };
        Set< String > viewNames = new HashSet<>();
        Collections.addAll(viewNames, viewsToAdd);
        try {
            ta = processor.addViewsAndCache( ta, viewNames, false );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
        assertTrue(ta.hasView(ViewNames.SHALLOW_PARSE));
        assertTrue(ta.hasView(ViewNames.NER_CONLL));
//        assertTrue(IOUtils.exists(TEST_CACHE_DIR));
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
            basicTextAnnotation = processor.createBasicTextAnnotation("test", "test", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        try {
            processor.addView(basicTextAnnotation, ViewNames.DEPENDENCY_STANFORD);
        } catch (RuntimeException | AnnotatorException e) {
            e.printStackTrace();
            System.out.println("Expected exception from stanford.");
        }
        System.out.println(basicTextAnnotation.toString());
    }
}
