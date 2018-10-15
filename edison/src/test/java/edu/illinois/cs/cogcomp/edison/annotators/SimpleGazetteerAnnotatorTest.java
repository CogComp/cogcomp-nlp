/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.config.SimpleGazetteerAnnotatorConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the SimpleGazetteerAnnotator class. There is really only one method to test that is the
 * addView method that creates the gaz view guy.
 * 
 * @author redman
 */
public class SimpleGazetteerAnnotatorTest {
    private static Logger logger = LoggerFactory.getLogger(SimpleGazetteerAnnotatorTest.class);

    private static final boolean IS_LAZILY_INITIALIZED = false;
    /** this helper can create text annotations from text. */
    protected final TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(
            new StatefulTokenizer());


    private static ResourceManager defaultRm;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Properties props = new Properties();
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.IS_LAZILY_INITIALIZED.key,
                Configurator.FALSE);
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PATH_TO_DICTIONARIES.key,
                "/testgazetteers/");
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PHRASE_LENGTH.key, "6");
        defaultRm = new ResourceManager(props);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {}

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {}

    /**
     * Test method for
     * {@link edu.illinois.cs.cogcomp.edison.annotators.SimpleGazetteerAnnotator#addView(edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation)}
     * .
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws AnnotatorException
     */
    @Test
    public void testMultiThreading() throws IOException, URISyntaxException, AnnotatorException {
        final SimpleGazetteerAnnotator sga = new SimpleGazetteerAnnotator(defaultRm);
        class TestThread extends Thread {
            Throwable throwable;

            public void run() {

                long start = System.currentTimeMillis();
                while (true) {
                    final TextAnnotation ta =
                            tab.createTextAnnotation("I hail from the university of illinois at champaign urbana.");
                    try {
                        sga.addView(ta);
                    } catch (AnnotatorException e) {
                        throwable = e;
                        return;
                    }
                    SpanLabelView view = (SpanLabelView) ta.getView(ViewNames.TREE_GAZETTEER);
                    List<Constituent> entities = view.getConstituents();
                    Constituent c1 = entities.get(0);
                    try {
                        assertEquals(c1.toString(), "university of illinois");
                        Constituent c2 = entities.get(1);
                        assertEquals(c2.toString(), "university of illinois at champaign urbana");
                        Constituent c3 = entities.get(2);
                        assertEquals(c3.toString(), "illinois");
                        Constituent c4 = entities.get(3);
                        assertEquals(c4.toString(), "champaign");
                        Constituent c5 = entities.get(4);
                        assertEquals(c5.toString(), "urbana");

                        assertEquals(c1.getLabel(), "organizations(IC)");
                        assertEquals(c2.getLabel(), "organizations(IC)");
                        assertEquals(c3.getLabel(), "places(IC)");
                        assertEquals(c4.getLabel(), "places(IC)");
                        assertEquals(c5.getLabel(), "places(IC)");
                        if ((System.currentTimeMillis() - start) > 10000l) {
                            // run for one minute.
                            throwable = null;
                            return;
                        }
                    } catch (AssertionError ae) {
                        throwable = ae;
                        ae.printStackTrace();
                        return;
                    }
                }
            }
        }
        final int numthreads = 20;
        TestThread[] threads = new TestThread[numthreads];
        for (int i = 0; i < numthreads; i++) {
            threads[i] = new TestThread();
            threads[i].start();
        }
        logger.info("Begin multithreaded test.");
        for (int i = 0; i < numthreads; i++) {
            while (true)
                try {
                    threads[i].join();
                    assertEquals("Exception during multithreading test : " + threads[i].throwable,
                            threads[i].throwable, null);
                    break;
                } catch (InterruptedException e) {
                    continue;
                }
        }

    }

    /**
     * Test method for
     * {@link edu.illinois.cs.cogcomp.edison.annotators.SimpleGazetteerAnnotator#addView(edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation)}
     * .
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws AnnotatorException
     */
    @Test
    public void testAddView() throws IOException, URISyntaxException, AnnotatorException {
        SimpleGazetteerAnnotator sga = new SimpleGazetteerAnnotator(defaultRm);
        assertTrue("Wrong number of dictionaries loaded.", sga.dictionaries.size() == 1);
        assertTrue("Wrong number of dictionaries loaded.", sga.dictionariesIgnoreCase.size() == 1);
        TextAnnotation ta =
                tab.createTextAnnotation("I hail from the university of illinois at champaign urbana.");
        sga.addView(ta);
        SpanLabelView view = (SpanLabelView) ta.getView(ViewNames.TREE_GAZETTEER);
        List<Constituent> entities = view.getConstituents();
        Constituent c1 = entities.get(0);
        assertEquals(c1.toString(), "university of illinois");
        Constituent c2 = entities.get(1);
        assertEquals(c2.toString(), "university of illinois at champaign urbana");
        Constituent c3 = entities.get(2);
        assertEquals(c3.toString(), "illinois");
        Constituent c4 = entities.get(3);
        assertEquals(c4.toString(), "champaign");
        Constituent c5 = entities.get(4);
        assertEquals(c5.toString(), "urbana");

        assertEquals(c1.getLabel(), "organizations(IC)");
        assertEquals(c2.getLabel(), "organizations(IC)");
        assertEquals(c3.getLabel(), "places(IC)");
        assertEquals(c4.getLabel(), "places(IC)");
        assertEquals(c5.getLabel(), "places(IC)");

        Properties props = new Properties();
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PHRASE_LENGTH.key, "4");
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PATH_TO_DICTIONARIES.key,
                "/testgazetteers/");
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.IS_LAZILY_INITIALIZED.key,
                SimpleGazetteerAnnotatorConfigurator.FALSE);
        sga = new SimpleGazetteerAnnotator(new ResourceManager(props));
        assertTrue("Wrong number of dictionaries loaded.", sga.dictionaries.size() == 1);
        assertTrue("Wrong number of dictionaries loaded.", sga.dictionariesIgnoreCase.size() == 1);
        ta =
                tab.createTextAnnotation("I hail from the university of illinois at champaign urbana.");
        sga.addView(ta);
        view = (SpanLabelView) ta.getView(ViewNames.TREE_GAZETTEER);
        entities = view.getConstituents();
        c1 = entities.get(0);
        assertEquals(c1.toString(), "university of illinois");
        c2 = entities.get(1);
        assertEquals(c2.toString(), "illinois");
        c3 = entities.get(2);
        assertEquals(c3.toString(), "champaign");
        c4 = entities.get(3);
        assertEquals(c4.toString(), "urbana");

        assertEquals(c1.getLabel(), "organizations(IC)");
        assertEquals(c2.getLabel(), "places(IC)");
        assertEquals(c3.getLabel(), "places(IC)");
        assertEquals(c4.getLabel(), "places(IC)");

        ta =
                tab.createTextAnnotation("I hail from the University of Illinois at champaign urbana.");
        sga.addView(ta);
        view = (SpanLabelView) ta.getView(ViewNames.TREE_GAZETTEER);
        entities = view.getConstituents();
        c1 = entities.get(0);
        assertEquals(c1.toString(), "University of Illinois");
        assertEquals(c1.getLabel(), "organizations");
        c2 = entities.get(1);
        assertEquals(c1.toString(), "University of Illinois");
        assertEquals(c1.getLabel(), "organizations");
    }

    /**
     * Test method for {@link SimpleGazetteerAnnotator#SimpleGazetteerAnnotator(ResourceManager)}.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testSimpleGazetteerAnnotatorString() throws IOException, URISyntaxException {
        Properties props = new Properties();
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.PATH_TO_DICTIONARIES.key,
                "/testgazetteers/");
        props.setProperty(SimpleGazetteerAnnotatorConfigurator.IS_LAZILY_INITIALIZED.key,
                SimpleGazetteerAnnotatorConfigurator.FALSE);
        ResourceManager localRm =
                new SimpleGazetteerAnnotatorConfigurator().getConfig(new ResourceManager(props));
        SimpleGazetteerAnnotator sga = new SimpleGazetteerAnnotator(localRm);
        assertTrue("Wrong number of dictionaries loaded.", sga.dictionaries.size() == 1);
        assertTrue("Wrong number of dictionaries loaded.", sga.dictionariesIgnoreCase.size() == 1);
    }

    // /**
    // * Test method for {@link
    // edu.illinois.cs.cogcomp.edison.annotators.SimpleGazetteerAnnotator#SimpleGazetteerAnnotator(int,
    // java.lang.String, boolean}.
    // * @throws URISyntaxException
    // * @throws IOException
    // */
    // @Test
    // public void testSimpleGazetteerAnnotatorIntString() throws IOException, URISyntaxException {
    // SimpleGazetteerAnnotator sga = new SimpleGazetteerAnnotator(6, "/testgazetteers/", false);
    // assertTrue ("Wrong number of dictionaries loaded.",sga.dictionaries.size() == 1);
    // assertTrue ("Wrong number of dictionaries loaded.",sga.dictionariesIgnoreCase.size() == 1);
    // }
}
