/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TransliterationAnnotator;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransliterationAnnotatorTest {

    TransliterationAnnotator annotator = null;

    @Before
    public void setUp() throws Exception {
        this.annotator = new TransliterationAnnotator(true, Language.Persian);
    }

    @Test
    public void testTransliterationWorks() throws AnnotatorException {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        annotator.getView(ta);
        assertEquals(true, ta.hasView(ViewNames.TRANSLITERATION));
        List<Constituent> consList = ta.getView(ViewNames.TRANSLITERATION).getConstituents();
        boolean hasJohn = false;
        for (Constituent c : consList) {
            if (c.getLabel().contains("جان")) hasJohn = true; // Persian transliteration of "John"
        }
        assertTrue(hasJohn);
    }
}
