/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by tofuwen on 11/26/16.
 */
public class TestGetTokenIdFromCharacterOffset {

    @Test
    public void test() {
        // The construction of the John Smith library finished on time .
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);
        assertEquals(ta.getTokenIdFromCharacterOffset(0), 0); // T
        assertEquals(ta.getTokenIdFromCharacterOffset(1), 0); // h
        assertEquals(ta.getTokenIdFromCharacterOffset(2), 0); // e
        assertEquals(ta.getTokenIdFromCharacterOffset(3), -1); // whitespace after "The"
        assertEquals(ta.getTokenIdFromCharacterOffset(4), 1); // c
        assertEquals(ta.getTokenIdFromCharacterOffset(54), -1); // the white space before "time"
        assertEquals(ta.getTokenIdFromCharacterOffset(60), 10); // .
        assertEquals(ta.getTokenIdFromCharacterOffset(61), -1); // whitespace after .
        // sentence length == 62
        // return the number of tokens + 1, as commented in the TextAnnotation.getTokenIdFromCharacter
        assertEquals(ta.getTokenIdFromCharacterOffset(62), 11);
    }

}
