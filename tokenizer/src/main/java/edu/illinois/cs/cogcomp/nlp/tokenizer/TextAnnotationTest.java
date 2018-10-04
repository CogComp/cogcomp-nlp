/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * test specific functionalities associated with TextAnnotation and tokenization Created by mssammon
 * on 3/31/16.
 */
public class TextAnnotationTest {
    /**
     * test whether the mapping between character offset and token index is correct.
     */
    @Test
    public void testCharacterOffsetToTokenIndex() {
        String normal = "The ordinary sample.\n\nDon't mess things up.";

        String leadingWaste = "<ignoreme>wastedspace</ignoreme>";
        String postWaste = "   \n<ignoremetoo>aaaargh</ignoremetoo>";
        String other = leadingWaste + normal + postWaste;

        TextAnnotationBuilder tabldr = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());

        TextAnnotation taNormal = tabldr.createTextAnnotation("test", "normal", normal);
        List<Constituent> normalToks = taNormal.getView(ViewNames.TOKENS).getConstituents();

        assertEquals(13, normalToks.get(2).getStartCharOffset());
        assertEquals(24, normalToks.get(5).getStartCharOffset());

        int ignoreUpToOffset = leadingWaste.length();
        IntPair[] characterOffsets = new IntPair[10];
        String[] tokens = taNormal.getTokens();

        for (int i = 0; i < normalToks.size(); ++i) {
            Constituent t = normalToks.get(i);
            characterOffsets[i] =
                    new IntPair(ignoreUpToOffset + t.getStartCharOffset(), ignoreUpToOffset
                            + t.getEndCharOffset());
        }
        List<Constituent> sentences = taNormal.getView(ViewNames.SENTENCE).getConstituents();
        int[] sentenceEndPositions = new int[sentences.size()];
        for (int i = 0; i < sentences.size(); ++i) {
            Constituent s = sentences.get(i);
            sentenceEndPositions[i] = s.getEndSpan();
        }
        // all info should be same except initial char offsets of tokens ignore spans of text
        TextAnnotation taOther =
                new TextAnnotation("test", "other", other, characterOffsets, tokens,
                        sentenceEndPositions);


        List<Constituent> otherToks = taOther.getView(ViewNames.TOKENS).getConstituents();

        int thirdTokNormalStart = normalToks.get(2).getStartCharOffset();

        int thirdTokOtherStart = otherToks.get(2).getStartCharOffset();

        assertEquals(thirdTokOtherStart, (thirdTokNormalStart + leadingWaste.length()));

        int eighthTokNormalStart = normalToks.get(8).getStartCharOffset();
        int eighthTokOtherStart = otherToks.get(8).getStartCharOffset();

        assertEquals(eighthTokOtherStart, (eighthTokNormalStart + leadingWaste.length()));

        int meaninglessStartOffset = taOther.getTokenIdFromCharacterOffset(2);

        assertEquals(-1, meaninglessStartOffset);
        int meaninglessPastEndOffset =
                taOther.getTokenIdFromCharacterOffset(leadingWaste.length() + normal.length() + 5);

        assertEquals(-1, meaninglessPastEndOffset);

        int meaninglessInBetweenToksOffset = taNormal.getTokenIdFromCharacterOffset(20);

        assertEquals(-1, meaninglessInBetweenToksOffset);

    }
}
