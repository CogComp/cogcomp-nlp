/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test functionality of AnnotationFixer.
 */
public class AnnotationFixerTest {

    private static final String VNAME = "TEST";

    @Test
    public void testAnnotationFixer() {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        int sentStart = ta.getView(ViewNames.SENTENCE).getConstituents().get(1).getStartSpan();
        int cStart = sentStart - 2;
        int cEnd = sentStart + 1;

        // first, run Fixer with existing view (which must respect sentence boundaries

        AnnotationFixer.rationalizeBoundaryAnnotations(ta, ViewNames.PSEUDO_PARSE_STANFORD);

        assertEquals(3, ta.getNumberOfSentences());

        View constrainingView = new View(VNAME, VNAME, ta, 1.0);
        ta.addView(VNAME, constrainingView);
        Constituent c = new Constituent("CONSTRAINT", VNAME, ta, cStart, cEnd);
        constrainingView.addConstituent(c);

        AnnotationFixer.rationalizeBoundaryAnnotations(ta, VNAME);
        assertEquals(2, ta.getNumberOfSentences());

    }
}
