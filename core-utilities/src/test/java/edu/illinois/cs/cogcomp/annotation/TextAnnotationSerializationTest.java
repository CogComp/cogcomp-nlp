/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextAnnotationSerializationTest extends TestCase {

    String sentA = "This is a text that contains pre-tokenized sentences .";
    String sentB = "For the purposes of this test , tokens are separated by whitespace .";
    String sentC = "Sentences are separated by newline characters .";
    String rawText = sentA + System.lineSeparator() + sentB + System.lineSeparator() + sentC;

    private List<String[]> tokenizedSentences;

    public void setUp() throws Exception {
        super.setUp();
        System.out.println(rawText);
        String[] sentences = rawText.split(System.lineSeparator());
        tokenizedSentences = new ArrayList<>(sentences.length);
        for (String sentTokens : sentences) {
            tokenizedSentences.add(sentTokens.split("\\s"));
        }
    }

    public void testJsonSerializability() throws Exception {
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokenizedSentences);

        // making sure serialization does not fail, when some views (possibly by mistake) are null
        ta.addView("nullView", null);

        String json = SerializationHelper.serializeToJson(ta);

        TextAnnotation ta2 = SerializationHelper.deserializeFromJson(json);
        assertEquals(ta2.getCorpusId(), ta.getCorpusId());
        assertEquals(ta2.getId(), ta.getId());
        assertEquals(ta2.getNumberOfSentences(), ta.getNumberOfSentences());
        assertEquals(ta2.getSentence(1), ta.getSentence(1));
        assertEquals(ta2.getSentenceFromToken(2), ta.getSentenceFromToken(2));
        assertEquals(ta2.getTokenIdFromCharacterOffset(5), ta.getTokenIdFromCharacterOffset(5));
        assertEquals(ta2.getToken(4), ta.getToken(4));
        assertEquals(ta2.getAvailableViews(), ta.getAvailableViews());
        assertEquals(Arrays.toString(ta2.getTokensInSpan(1, 3)),
                Arrays.toString(ta.getTokensInSpan(1, 3)));
        assertEquals(ta2.getText(), ta.getText());
    }
}
