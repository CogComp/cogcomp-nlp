/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.prepsrl.data.Preprocessor;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrepSRLAnnotatorTest {
    @Test
    public void addView() throws Exception {
        Annotator annotator = new PrepSRLAnnotator();
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);
        annotator.getView(ta);
        assertEquals("ObjectOfVerb", ta.getView(ViewNames.SRL_PREP).getConstituents().get(0).getLabel());
    }
}