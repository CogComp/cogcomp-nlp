/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test the Sentence class related methods.
 */
public class TestSentence {
    private static TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);

    /**
     * Test that editing a Sentence instance throws an exeception.
     */
    @Test
    public void testEditingThrowsException() {
        List<Sentence> sentences = new ArrayList<>(3);
        for (int sentenceId = 0; sentenceId < ta.getNumberOfSentences(); sentenceId++) {
            sentences.add(ta.getSentence(sentenceId));
        }

        Throwable err = null;
        for (Sentence sent: sentences) {
            try {
                View tokensView = sent.getView(ViewNames.TOKENS);
                sent.addTopKView("TOKENS_BKP", Collections.singletonList(tokensView));
            } catch (Throwable ex) {
                err = ex;
            }
            finally {
                assertTrue(err instanceof UnsupportedOperationException);
                err = null;
            }

            try {
                View tokensView = sent.getView(ViewNames.TOKENS);
                sent.addViews(new String[] { "TOKENS_BKP" }, new View[] { tokensView });
            } catch (Throwable ex) {
                err = ex;
            }
            finally {
                assertTrue(err instanceof UnsupportedOperationException);
                err = null;
            }

            try {
                View tokensView = sent.getView(ViewNames.TOKENS);
                sent.addView("TOKENS_BKP", tokensView);
            } catch (Throwable ex) {
                err = ex;
            }
            finally {
                assertTrue(err instanceof UnsupportedOperationException);
                err = null;
            }

            try {
                sent.removeView(ViewNames.LEMMA);
            } catch (Throwable ex) {
                err = ex;
            }
            finally {
                assertTrue(err instanceof UnsupportedOperationException);
                err = null;
            }

            try {
                sent.removeAllViews();
            } catch (Throwable ex) {
                err = ex;
            }
            finally {
                assertTrue(err instanceof UnsupportedOperationException);
                err = null;
            }

            try {
                sent.setTokens(new String[] { }, new IntPair[] { });
            } catch (Throwable ex) {
                err = ex;
            }
            finally {
                assertTrue(err instanceof UnsupportedOperationException);
                err = null;
            }
        }
    }

    /**
     * Testing that changes made to the source TextAnnotation are propogated to the Sentence instance.
     */
    @Test
    public void testEditingSourceTextAnnotation() {
        List<Sentence> sentences = new ArrayList<>(3);
        for (int sentenceId = 0; sentenceId < ta.getNumberOfSentences(); sentenceId++) {
            Sentence sent = ta.getSentence(sentenceId);
            sentences.add(sent);
            assertTrue(sent.hasView(ViewNames.POS));
        }

        View posView = ta.getView(ViewNames.POS);
        ta.removeView(ViewNames.POS);

        for (Sentence sent: sentences) {
            assertFalse(sent.hasView(ViewNames.POS));
        }

        ta.addView(ViewNames.POS, posView);

        for (Sentence sent: sentences) {
            assertTrue(sent.hasView(ViewNames.POS));
        }
    }
}
