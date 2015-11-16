package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CuratorFactoryTest {
    /*private static final String CONFIG_FILE = "src/test/resources/caching-curator.properties";
    private static final String TEXT_FILE = "src/test/resources/curatorTestInput.txt";

    private static final int NUM_TOKS = 19;
    private static final int NUM_CHUNKS = 10;
    private static final int NUM_SRL_FRAMES = 3;
    private static final int NUM_SENTS = 2;

    private String text;

    private AnnotatorService curator;

    @Before
    public void setUp() throws Exception {
        text = LineIO.slurp(TEXT_FILE);
        curator = CuratorFactory.buildCuratorClient();
    }

    @Test
    public void testGetTextAnnotation() {
        TextAnnotation ta = null;
        try {
            ta = curator.createBasicTextAnnotation("test", "0", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (IllegalArgumentException e) {
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

    @Test
    public void testGetIndividualTextAnnotationViews() throws IOException {
        TextAnnotation ta = null;
        try {
            ResourceManager rm = new ResourceManager(CONFIG_FILE);
            ta = curator.createBasicTextAnnotation("test", "0", text);
            for (String viewName : rm.getCommaSeparatedValues(CuratorConfigurator.VIEWS_TO_ADD.key))
                curator.addView(ta, viewName);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            // If this is a "connection timeout" exception we can ignore it
            if (e.getMessage().contains("Connection timed out"))
                return;
        }
        testViews(ta);
    }

    @Test
    public void testGetAllTextAnnotationViews() {
        TextAnnotation ta = null;
        try {
            ta = curator.createAnnotatedTextAnnotation("test", "0", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            // If this is a "connection timeout" exception we can ignore it
            if (e.getMessage().contains("Connection timed out"))
                return;
        }
        testViews(ta);
    }

    private void testViews(TextAnnotation ta) {
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
        // Currently, there is no way to access the number of trees in a TreeView (protected variable, no getter)
        assertEquals(56, ta.getView(ViewNames.PARSE_CHARNIAK).getNumberOfConstituents());
        assertEquals(NUM_SRL_FRAMES, ((PredicateArgumentView) ta.getView(ViewNames.SRL_VERB)).getPredicates().size());
        assertEquals(NUM_CHUNKS, ta.getView(ViewNames.SHALLOW_PARSE).getNumberOfConstituents());
        assertEquals(NUM_TOKS, ta.getView(ViewNames.LEMMA).getNumberOfConstituents());
    }*/
}