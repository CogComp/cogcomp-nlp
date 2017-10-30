/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TransliterationAnnotator;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransliterationAnnotatorTest {

    TransliterationAnnotator annotator = null;
    @Before
    public void setUp() throws Exception {
        this.annotator = new TransliterationAnnotator(true, Language.Persian);
    }

    @Test
    public void testCleanText() throws AnnotatorException {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        annotator.getView(ta);
        assertEquals(true, ta.hasView(ViewNames.TRANSLITERATION));
    }
}
