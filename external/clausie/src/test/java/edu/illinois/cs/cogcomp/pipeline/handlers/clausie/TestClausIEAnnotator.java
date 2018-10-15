/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers.clausie;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestClausIEAnnotator {
    @Ignore
    @Test
    public void testAnnotator() throws AnnotatorException {
        TextAnnotation ta = DummyTextAnnotationGenerator.
                generateAnnotatedTextAnnotation(false, 3);
        ClausIEAnnotator annotator = new ClausIEAnnotator();
        ta.addView(annotator);
        assertTrue(ta.hasView(annotator.getViewName()));
    }
}
