package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.nlp.util.SimpleCachingPipeline;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

import static org.junit.Assert.*;

public class SimpleCachingPipelineTest
{

    private static AnnotatorService processor;
    private static HashSet<String> activeViews;

    @BeforeClass
    public static void init() throws IOException, AnnotatorException {
        Properties props = new Properties();
        props.setProperty( PipelineConfigurator.USE_NER_ONTONOTES, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_STANFORD_DEP, Configurator.TRUE );
        props.setProperty( PipelineConfigurator.USE_STANFORD_PARSE, Configurator.FALSE );
        props.setProperty( AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE, Configurator.TRUE );

        props.setProperty( AnnotatorServiceConfigurator.CACHE_DIR, "simple-annotation-cache" );
        props.setProperty( AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED, Configurator.FALSE );
        activeViews = new HashSet<>();
        activeViews.add( ViewNames.POS );
        activeViews.add( ViewNames.SHALLOW_PARSE );
        activeViews.add( ViewNames.NER_CONLL );
        processor = new SimpleCachingPipeline( new ResourceManager( props ) );
    }


    @Test
    public void testSimpleCachingPipeline()
    {
        String text = "The only way to limit a dog's creativity is to place a foul-smelling bone under its nose. " +
                "For a cat, substitute a laser pointer for the bone.";

        String corpusId = "test";
        String textId = "testText";

        String fileName = null;
        try {
            fileName = SimpleCachingPipeline.getSavePath(((SimpleCachingPipeline) processor).pathToSaveCachedFiles, text);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        if ( IOUtils.exists( fileName ) ) // from previous run of this test
            try {
                IOUtils.rm( fileName );
            } catch (IOException e) {
                e.printStackTrace();
                fail( e.getMessage() );
            }

        assertTrue( !( new File( fileName ) ).exists() );

		try {
            TextAnnotation annotatedText = processor.createAnnotatedTextAnnotation( "", "", text );

            assertNotNull( annotatedText );
            assertTrue(annotatedText.hasView(ViewNames.POS));
            assertTrue( annotatedText.hasView(ViewNames.SHALLOW_PARSE ) );
            System.out.println( "checking file '" + fileName + "' now exists..." );
            assertTrue(IOUtils.exists(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }


        assertTrue( new File( fileName ).exists() );



        TextAnnotation ta = null;
        try {
            ta = SerializationHelper.deserializeTextAnnotationFromFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assertTrue( ta.hasView( ViewNames.NER_CONLL ) );

        try {
            IOUtils.rm( fileName );
        } catch (IOException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assertTrue( !( new File( fileName ) ).exists() );

        try {
            TextAnnotation newTa = processor.createAnnotatedTextAnnotation(corpusId, textId, text );
            assertTrue( newTa.hasView( ViewNames.DEPENDENCY_STANFORD ));
        } catch (AnnotatorException e) {
            e.printStackTrace();
        }

        ta = null;

        assertTrue( new File( fileName ).exists() );

        try {
            ta = SerializationHelper.deserializeTextAnnotationFromFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assertTrue( ta.hasView( ViewNames.NER_CONLL ) );

		// checks that inactive components are not applied...
        assertFalse( ta.hasView( ViewNames.PARSE_STANFORD ) );

    }

}
