/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse.io;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import org.junit.Test;

import static org.junit.Assert.*;

public class PreprocessorTest {
    @Test
    public void annotate() throws Exception {
        Preprocessor preprocessor = new Preprocessor();
        String[] sentTokens = {"<root>", "This", "is", "a", "test", "sentence"};
        TextAnnotation ta = preprocessor.annotate("testCorpus", "testSent1", sentTokens);
        assertNotNull(ta);
        assertTrue(ta.hasView(ViewNames.POS));
        assertTrue(ta.hasView(ViewNames.LEMMA));
        assertTrue(ta.hasView(ViewNames.SHALLOW_PARSE));
        assertEquals("NN", ta.getView(ViewNames.POS).getLabelsCoveringToken(4).get(0));
    }
}
