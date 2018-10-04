/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CuratorFactoryTest {
    private static final String CONFIG_FILE = "src/test/resources/caching-curator.properties";
    private static final String TEXT_FILE = "src/test/resources/curatorTestInput.txt";

    private static final int NUM_TOKS = 19;
    private static final int NUM_CHUNKS = 10;
    private static final int NUM_SRL_FRAMES = 3;
    private static final int NUM_SENTS = 2;

    private String text;

    private static AnnotatorService curator;

    @Before
    public void setUp() throws Exception {
        text = LineIO.slurp(TEXT_FILE);
        Properties settings = new Properties();
        settings.setProperty(CuratorConfigurator.DISABLE_CACHE.key, Configurator.FALSE);
        ResourceManager rm = new ResourceManager(settings);
        ResourceManager config = new CuratorConfigurator().getConfig(rm);
        if (curator == null) curator = CuratorFactory.buildCuratorClient(config);
    }

    @Test
    public void testGetTextAnnotation() {
        // if we are running the test on Semaphore, ignore this test, since Gurobi is not provided on Semaphore.
        if (System.getenv().containsKey("CI") && System.getenv().get("CI").equals("true")
                && System.getenv().containsKey("SEMAPHORE") && System.getenv().get("SEMAPHORE").equals("true")
                && System.getenv().containsKey("CIRCLECI") && System.getenv().get("CIRCLECI").equals("true")) {
            System.out.println("Running the test on Semaphore. Skipping this test  . . . ");
        } else {
            TextAnnotation ta = null;
            try {
                ta = curator.createBasicTextAnnotation("test", "0", text);
            } catch (AnnotatorException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } catch (IllegalArgumentException e) {
                // If this is a "connection timeout" exception we can ignore it
                if (e.getMessage().contains("Connection timed out"))
                    return;
            }

            assertTrue(ta.hasView(ViewNames.SENTENCE));
            assertTrue(ta.hasView(ViewNames.TOKENS));

            List<Constituent> tokens = ta.getView(ViewNames.TOKENS).getConstituents();
            List<Constituent> sentences = ta.getView(ViewNames.SENTENCE).getConstituents();
            assertEquals(NUM_TOKS, tokens.size());
            assertEquals(NUM_SENTS, sentences.size());
        }
    }

    @Test
    public void testGetIndividualTextAnnotationViews() throws IOException {
        // if we are running the test on Semaphore, ignore this test, since Gurobi is not provided on Semaphore.
        if (System.getenv().containsKey("CI") && System.getenv().get("CI").equals("true")
                && System.getenv().containsKey("SEMAPHORE") && System.getenv().get("SEMAPHORE").equals("true")
                && System.getenv().containsKey("CIRCLECI") && System.getenv().get("CIRCLECI").equals("true")) {
            System.out.println("Running the test on Semaphore. Skipping this test  . . . ");
        } else {
            TextAnnotation ta = null;
            try {
                ResourceManager rm = new ResourceManager(CONFIG_FILE);
                ta = curator.createBasicTextAnnotation("test", "0", text);
                for (String viewName : rm.getCommaSeparatedValues(CuratorConfigurator.VIEWS_TO_ADD.key))
                    curator.addView(ta, viewName);
            } catch (AnnotatorException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } catch (IllegalArgumentException e) {
                // If this is a "connection timeout" exception we can ignore it
                if (e.getMessage().contains("Connection timed out"))
                    return;
            }
            testViews(ta);
        }
    }

    @Test
    public void testGetAllTextAnnotationViews() {
        // if we are running the test on Semaphore, ignore this test, since Gurobi is not provided on Semaphore.
        if (System.getenv().containsKey("CI") && System.getenv().get("CI").equals("true")
                && System.getenv().containsKey("SEMAPHORE") && System.getenv().get("SEMAPHORE").equals("true")
                && System.getenv().containsKey("CIRCLECI") && System.getenv().get("CIRCLECI").equals("true")) {
            System.out.println("Running the test on Semaphore. Skipping this test  . . . ");
        } else {
            TextAnnotation ta = null;
            try {
                ta = curator.createAnnotatedTextAnnotation("test", "0", text);
            } catch (AnnotatorException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } catch (IllegalArgumentException e) {
                // If this is a "connection timeout" exception we can ignore it
                if (e.getMessage().contains("Connection timed out"))
                    return;
            }
            testViews(ta);
        }
    }

    private void testViews(TextAnnotation ta) {
        // if we are running the test on Semaphore, ignore this test, since Gurobi is not provided on Semaphore.
        if (System.getenv().containsKey("CI") && System.getenv().get("CI").equals("true")
                && System.getenv().containsKey("SEMAPHORE") && System.getenv().get("SEMAPHORE").equals("true")
                && System.getenv().containsKey("CIRCLECI") && System.getenv().get("CIRCLECI").equals("true")) {
            System.out.println("Running the test on Semaphore. Skipping this test  . . . ");
        } else {
            assertTrue(ta.hasView(ViewNames.POS));
            assertTrue(ta.hasView(ViewNames.TOKENS));
            assertTrue(ta.hasView(ViewNames.SHALLOW_PARSE));
            assertTrue(ta.hasView(ViewNames.LEMMA));
            assertTrue(ta.hasView(ViewNames.NER_CONLL));
            assertTrue(ta.hasView(ViewNames.NER_ONTONOTES));
            assertTrue(ta.hasView(ViewNames.PARSE_CHARNIAK));
            assertTrue(ta.hasView(ViewNames.SRL_VERB));
            assertTrue(ta.hasView(ViewNames.SRL_NOM));
            assertTrue(ta.hasView(ViewNames.COREF));
            assertTrue(ta.hasView(ViewNames.WIKIFIER));
            assertTrue(ta.hasView(ViewNames.DEPENDENCY_STANFORD));
            assertTrue(ta.hasView(ViewNames.PARSE_STANFORD));

            assertEquals(NUM_TOKS, ta.getView(ViewNames.TOKENS).getNumberOfConstituents());
            assertEquals(NUM_TOKS, ta.getView(ViewNames.POS).getNumberOfConstituents());
            // Currently, there is no way to access the number of trees in
            // a TreeView (protected variable, no getter)
            assertEquals(56, ta.getView(ViewNames.PARSE_CHARNIAK).getNumberOfConstituents());
            assertEquals(NUM_SRL_FRAMES, ((PredicateArgumentView) ta.getView(ViewNames.SRL_VERB))
                    .getPredicates().size());
            assertEquals(NUM_CHUNKS, ta.getView(ViewNames.SHALLOW_PARSE).getNumberOfConstituents());
            assertEquals(NUM_TOKS, ta.getView(ViewNames.LEMMA).getNumberOfConstituents());
        }
    }
}
