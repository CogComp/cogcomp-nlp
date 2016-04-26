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
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple test to verify TextAnnotationBuilder behavior.
 *
 * Created by mssammon on 9/4/15.
 */
public class TokenizerTextAnnotationBuilderTest {


    @Test
    public void testBuilder() {

        TokenizerTextAnnotationBuilder bldr =
                new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());

        final String sentA =
                "Mr. Dawkins -- a liberal professor -- doesn't like fundamentalists.   ";
        final String sentB = "He is intolerant of intolerance!";

        final int refSentStartOffset = 71;
        final int refSentEndOffset = 103;

        final int refTokStartOffset = 77;
        final int refTokEndOffset = refTokStartOffset + 10;

        final String text = sentA + System.lineSeparator() + sentB;

        TextAnnotation ta = bldr.createTextAnnotation("test", "test", text);

        assertTrue(ta.hasView(ViewNames.SENTENCE));
        assertTrue(ta.hasView(ViewNames.TOKENS));

        Constituent sent = ta.getView(ViewNames.SENTENCE).getConstituents().get(1); // second
                                                                                    // sentence

        assertEquals(sentB, sent.getSurfaceForm());
        assertEquals(refSentStartOffset, sent.getStartCharOffset());
        assertEquals(refSentEndOffset, sent.getEndCharOffset());

        Constituent tok = ta.getView(ViewNames.TOKENS).getConstituents().get(14);

        assertEquals("intolerant", tok.getSurfaceForm());
        assertEquals(refTokStartOffset, tok.getStartCharOffset());
        assertEquals(refTokEndOffset, tok.getEndCharOffset());
    }

}
