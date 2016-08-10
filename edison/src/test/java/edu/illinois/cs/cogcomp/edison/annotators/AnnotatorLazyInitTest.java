package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test lazy initialization behavior of Annotator.
 * @author mssammon
 */
public class AnnotatorLazyInitTest
{
    protected final TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());

    @Test
    public void testNonLazy()
    {
        SimpleGazetteerAnnotator sga = null;
        try {
            sga = new SimpleGazetteerAnnotator(6, "/testgazetteers/", false);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue( sga.isInitialized() );

        assertTrue( sga.dictionaries.size() > 0 );
        assertTrue( sga.dictionariesIgnoreCase.size() > 0 );
    }

    @Test
    public void testLazy()
    {
        SimpleGazetteerAnnotator sga = null;
        try {
            sga = new SimpleGazetteerAnnotator(6, "/testgazetteers/", true);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertFalse( sga.isInitialized() );

        assertTrue( sga.dictionaries.size() == 0 );
        assertTrue( sga.dictionariesIgnoreCase.size() == 0 );

        TextAnnotation ta = tab.createTextAnnotation("The CIA has no London headquarters, though General Electric does." );

        try {
            sga.addView( ta );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assertTrue( sga.isInitialized() );
        assertTrue( sga.dictionaries.size() > 0 );
        assertTrue( sga.dictionariesIgnoreCase.size() > 0 );

        assertTrue(ta.hasView( sga.getViewName() ));
    }

}
