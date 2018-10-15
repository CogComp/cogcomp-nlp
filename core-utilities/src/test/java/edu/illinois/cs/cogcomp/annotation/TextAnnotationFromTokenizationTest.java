/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This testcase is for ensuring that BasicTextAnnotationBuilder can create TextAnnotation from a
 * Tokenization object.
 *
 * @author Narender Gupta
 */
public class TextAnnotationFromTokenizationTest {

    String sentA = "A pre-tokenized sentence .";
    String sentB = "Separated by whitespaces .";
    String sentC = "For the purpose of testing .";
    String rawText = sentA + System.lineSeparator() + sentB + System.lineSeparator() + sentC;
    private Tokenizer.Tokenization tokenization;

    private Tokenizer.Tokenization getTokenization(String rawText) {
        String[] sentences = this.rawText.split("\\n");
        String[] tokens = new String[0];
        List<IntPair> characterOffsets = new ArrayList<>();
        int[] sentenceEndArray = new int[sentences.length];
        int sentenceCharOffset = 0;
        int lastTokenCount = 0;
        for (int iSentence = 0; iSentence < sentences.length; iSentence++) {
            String sentence = sentences[iSentence];
            String[] sentenceTokens = sentence.split("\\s");
            tokens = (String[]) ArrayUtils.addAll(tokens, sentenceTokens);
            int charOffsetBegin = sentenceCharOffset;
            int charOffsetEnd = sentenceCharOffset;
            for (int i = 0; i < sentence.length(); i++) {
                char c = sentence.charAt(i);
                if (Character.isWhitespace(c)) {
                    charOffsetEnd = sentenceCharOffset + i;
                    IntPair tokenOffsets = new IntPair(charOffsetBegin, charOffsetEnd);
                    characterOffsets.add(tokenOffsets);
                    charOffsetBegin = charOffsetEnd + 1;
                }
            }
            charOffsetEnd = sentenceCharOffset + sentence.length();
            IntPair tokenOffsets = new IntPair(charOffsetBegin, charOffsetEnd);
            characterOffsets.add(tokenOffsets);
            sentenceCharOffset = charOffsetEnd + 1;
            lastTokenCount += sentenceTokens.length;
            sentenceEndArray[iSentence] = lastTokenCount;
        }
        IntPair[] charOffsetArray = new IntPair[characterOffsets.size()];
        for (int i = 0; i < characterOffsets.size(); i++) {
            charOffsetArray[i] = characterOffsets.get(i);
        }
        Tokenizer.Tokenization tokenization =
                new Tokenizer.Tokenization(tokens, charOffsetArray, sentenceEndArray);
        return tokenization;
    }

    @Before
    public void init() throws Exception {
        this.tokenization = getTokenization(this.rawText);
    }

    @Test
    public void testCreateTextAnnotationFromTokenization() throws Exception {
        TextAnnotationBuilder taBuilder = new BasicTextAnnotationBuilder();
        TextAnnotation ta = taBuilder.createTextAnnotation("", "", this.rawText, this.tokenization);
        assertTrue(ta.hasView(ViewNames.SENTENCE));
        assertTrue(ta.hasView(ViewNames.TOKENS));

        List<Constituent> sentences = ta.getView(ViewNames.SENTENCE).getConstituents();
        assertEquals(3, sentences.size());
        Constituent secondSent = sentences.get(1);
        String newB = secondSent.getTokenizedSurfaceForm();
        assertEquals(sentB, newB);

        List<Constituent> tokens = ta.getView(ViewNames.TOKENS).getConstituents();
        assertEquals(14, tokens.size());

        // Get the second constituent
        List<Constituent> tokensCovering =
                ta.getView(ViewNames.TOKENS).getConstituentsCoveringToken(1);
        assertEquals(1, tokensCovering.size());
        assertEquals("pre-tokenized", tokensCovering.get(0).getTokenizedSurfaceForm());
        // Check that the two surface forms agree
        assertEquals("pre-tokenized", tokensCovering.get(0).getSurfaceForm());

        // Get the fourth constituent of the second sentence
        int sentStart = sentences.get(1).getStartSpan();
        tokensCovering = ta.getView(ViewNames.TOKENS).getConstituentsCoveringToken(sentStart + 3);
        assertEquals(1, tokensCovering.size());
        assertEquals(".", tokensCovering.get(0).getTokenizedSurfaceForm());
    }
}
