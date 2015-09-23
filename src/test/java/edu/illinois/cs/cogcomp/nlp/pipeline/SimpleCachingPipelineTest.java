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
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by mssammon on 9/21/15.
 */
public class SimpleCachingPipelineTest
{

    private static AnnotatorService processor;
    private static HashSet<String> activeViews;

    @BeforeClass
    public static void init() throws IOException, AnnotatorException {
        Properties props = new Properties();
        props.setProperty( PipelineConfigurator.ACTIVE_VIEWS, "POS,SHALLOW_PARSE,NER_CONLL" );
        props.setProperty( AnnotatorServiceConfigurator.CACHE_DIR, "simple-annotation-cache" );
        props.setProperty( AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED, Configurator.FALSE );
        activeViews = new HashSet< String >();
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

        String fileName = null;
        try {
            fileName = (( SimpleCachingPipeline ) processor).getSavePath( ( (SimpleCachingPipeline) processor).pathToSaveCachedFiles, text);
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

        boolean forceUpdate = true;
        try {
            TextAnnotation annotatedText = processor.createAnnotatedTextAnnotation( "", "", text, forceUpdate );

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
            ta = processor.createBasicTextAnnotation( "", "", text, forceUpdate );

            boolean isUpdated = processor.addViewsAndCache( ta, activeViews, forceUpdate );

            assertTrue( isUpdated );
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


    }
}
