/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Daniel Khashabi
 */
public class TestCorelexFeatureExtractor {

    @Test
    public final void test() throws EdisonException {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        assertEquals(CorelexFeatureExtractor.instance.getWordFeatures(ta, 1).toString(), "[cae]");  // construction
        assertEquals(CorelexFeatureExtractor.instance.getWordFeatures(ta, 20).toString(), "[avt]"); // paving
        assertEquals(CorelexFeatureExtractor.instance.getWordFeatures(ta, 24).toString(), "[com]"); // will
        assertEquals(CorelexFeatureExtractor.instance.getWordFeatures(ta, 27).toString(), "[tme]"); // June
    }
}
