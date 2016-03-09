package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class BasicTextAnnotationBuilderTest extends TestCase {

    String sentA = "This is a text that contains pre-tokenized sentences .";
    String sentB = "For the purposes of this test , tokens are separated by whitespace .";
    String sentC = "Sentences are separated by newline characters .";
    String rawText = sentA + System.lineSeparator() + sentB + System.lineSeparator() + sentC;

    private List<String[]> tokenizedSentences;

    public void setUp() throws Exception {
        super.setUp();
        String[] sentences = rawText.split("\\n");
        tokenizedSentences = new ArrayList<>(sentences.length);
        for (String sentTokens : sentences) {
            tokenizedSentences.add(sentTokens.split("\\s"));
        }
    }

    public void testCreateTextAnnotationFromTokens() throws Exception {
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokenizedSentences);
        assertTrue(ta.hasView(ViewNames.SENTENCE));
        assertTrue(ta.hasView(ViewNames.TOKENS));

        List<Constituent> sentences = ta.getView(ViewNames.SENTENCE).getConstituents();
        assertEquals(3, sentences.size());
        Constituent secondSent = sentences.get(1);
        String newB = secondSent.getTokenizedSurfaceForm();
        assertEquals(sentB, newB);

        List<Constituent> tokens = ta.getView(ViewNames.TOKENS).getConstituents();
        assertEquals(29, tokens.size());

        // Get the sixth constituent
        List<Constituent> tokensCovering =
                ta.getView(ViewNames.TOKENS).getConstituentsCoveringToken(6);
        assertEquals(1, tokensCovering.size());
        assertEquals("pre-tokenized", tokensCovering.get(0).getTokenizedSurfaceForm());
        // Check that the two surface forms agree
        assertEquals("pre-tokenized", tokensCovering.get(0).getSurfaceForm());

        // Get the sixth constituent of the second sentence
        int sentStart = sentences.get(1).getStartSpan();
        tokensCovering = ta.getView(ViewNames.TOKENS).getConstituentsCoveringToken(sentStart + 6);
        assertEquals(1, tokensCovering.size());
        assertEquals(",", tokensCovering.get(0).getTokenizedSurfaceForm());


    }
}
