/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.config.SimpleGazetteerAnnotatorConfigurator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test lazy initialization behavior of Annotator.
 * 
 * @author mssammon
 */
public class AnnotatorLazyInitTest {
    private static ResourceManager defaultRm;
    protected final TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(
            new StatefulTokenizer());

    @BeforeClass
    public static void setUpOnce() {
        Properties props = new Properties();
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.IS_LAZILY_INITIALIZED.key,
                Configurator.FALSE);
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PATH_TO_DICTIONARIES.key,
                "/testgazetteers/");
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PHRASE_LENGTH.key, "6");
        defaultRm = new ResourceManager(props);
    }


    @Test
    public void testNonLazy() {
        SimpleGazetteerAnnotator sga = null;
        try {
            sga = new SimpleGazetteerAnnotator(defaultRm);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(sga.isInitialized());

        assertTrue(sga.dictionaries.size() > 0);
        assertTrue(sga.dictionariesIgnoreCase.size() > 0);
    }

    @Test
    public void testLazy() {
        SimpleGazetteerAnnotator sga = null;
        Properties props = new Properties();
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PATH_TO_DICTIONARIES.key,
                "/testgazetteers/");
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PHRASE_LENGTH.key, "6");
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.IS_LAZILY_INITIALIZED.key,
                SimpleGazetteerAnnotatorConfigurator.TRUE);
        try {
            sga = new SimpleGazetteerAnnotator(new ResourceManager(props));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertFalse(sga.isInitialized());

        assertTrue(null == sga.dictionaries ? true : sga.dictionaries.size() > 0);
        assertTrue(null == sga.dictionariesIgnoreCase ? true
                : sga.dictionariesIgnoreCase.size() > 0);

        TextAnnotation ta =
                tab.createTextAnnotation("The CIA has no London headquarters, though General Electric does.");

        try {
            sga.getView(ta);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(ta.hasView(sga.getViewName()));
        assertTrue(sga.isInitialized());
        assertTrue(null == sga.dictionaries ? true : sga.dictionaries.size() > 0);
        assertTrue(null == sga.dictionariesIgnoreCase ? true
                : sga.dictionariesIgnoreCase.size() > 0);

        assertTrue(ta.hasView(sga.getViewName()));
    }

}
