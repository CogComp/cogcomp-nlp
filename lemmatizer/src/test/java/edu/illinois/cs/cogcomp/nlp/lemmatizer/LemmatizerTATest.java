/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class LemmatizerTATest {
    private static final String TEST_TEXT_ANNOTATION_FILE = "src/test/resources/serializedTA.ser";
    private TextAnnotation inputTa;
    private IllinoisLemmatizer lem;

    @Test
    public void simpleTest() {
        String lemma = lem.getLemma("putting", "VBG");
        assertTrue(lemma.equals("put"));

        lemma = lem.getLemma("men", "NNS");
        assertTrue(lemma.equals("man"));

        lemma = lem.getLemma("retakes", "VBZ");
        assertTrue(lemma.equals("retake"));
    }

    @Test
    public void stanfordTest() {
        Properties props = new Properties();
        // set non-default lemmatizer constructor params
        props.setProperty(LemmatizerConfigurator.USE_STNFRD_CONVENTIONS.key,
                LemmatizerConfigurator.TRUE);
        // since we are calling
        props.setProperty(LemmatizerConfigurator.IS_LAZILY_INITIALIZED.key,
                LemmatizerConfigurator.FALSE);

        ResourceManager rm = new LemmatizerConfigurator().getConfig(new ResourceManager(props));

        IllinoisLemmatizer lem = new IllinoisLemmatizer(new LemmatizerConfigurator().getConfig(rm));

        String lemma = lem.getLemma("me", "PRP");
        assertTrue(lemma.equals("i"));
    }

    @Before
    public void setUp() throws Exception {
        lem = new IllinoisLemmatizer();
        inputTa = SerializationHelper.deserializeTextAnnotationFromFile(TEST_TEXT_ANNOTATION_FILE);
    }

    @After
    public void tearDown() throws Exception {
        inputTa = null;
    }

    @Test
    public void testCreateWnLemmaView() {
        View lemmaView = null;

        try {
            lemmaView = lem.createLemmaView(inputTa);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        Constituent posC = inputTa.getView(ViewNames.POS).getConstituents().get(0);

        assertEquals(0, posC.getStartSpan());
        assertEquals(1, posC.getEndSpan());

        boolean isTested = false;

        if (null != lemmaView) {
            List<Constituent> spans = inputTa.getView(ViewNames.LEMMA).getConstituents();

            String the = spans.get(0).getLabel(); // orig 'The'
            String CIA = spans.get(1).getLabel(); // orig 'men'
            String thought = spans.get(2).getLabel(); // orig 'have'
            String had = spans.get(6).getLabel(); // orig 'had'
            String were = spans.get(15).getLabel(); // orig 'examinations'

            assertEquals(the, "the");
            assertEquals(CIA, "cia");
            assertEquals(thought, "think");
            assertEquals(had, "have");
            assertEquals(were, "be");
            isTested = true;
        }

        assertTrue(isTested);
    }

    private void printConstituents(PrintStream out, List<Constituent> spans) {
        for (Constituent c : spans)
            out.print(c.getLabel() + ", ");
        out.println();
    }

    @Test
    public void testCreateTextAnnotationLemmaView() {
        View lemmaView = null;
        TextAnnotation ta = inputTa;

        try {
            lemmaView = lem.createLemmaView(ta);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        boolean isTested = false;

        if (null != lemmaView) {
            List<Constituent> spans = lemmaView.getConstituents();

            printConstituents(System.out, spans);

            String the = spans.get(0).getLabel(); // orig 'The'
            String CIA = spans.get(1).getLabel(); // orig 'men'
            String thought = spans.get(2).getLabel(); // orig 'have'
            String had = spans.get(6).getLabel(); // orig 'had'
            String were = spans.get(15).getLabel(); // orig 'examinations'

            assertEquals(the, "the");
            assertEquals(CIA, "cia");
            assertEquals(thought, "think");
            assertEquals(had, "have");
            assertEquals(were, "be");

            isTested = true;
        }
        assertTrue(isTested);
    }
}
