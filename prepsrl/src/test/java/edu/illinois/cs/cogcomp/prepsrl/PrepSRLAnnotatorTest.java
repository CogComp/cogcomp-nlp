/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.prepsrl.data.Preprocessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrepSRLAnnotatorTest {
    private static Annotator annotator;
    private static Preprocessor preprocessor;

    @BeforeClass
    public static void initialize() throws Exception {
        annotator = new PrepSRLAnnotator();
        preprocessor = new Preprocessor(PrepSRLConfigurator.defaults());
    }

    @Test
    public void testDummy() throws Exception {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);
        View view = annotator.getView(ta);
        assertEquals("ObjectOfVerb", view.getConstituents().get(0).getLabel());
    }

    @Test
    public void testShort() throws Exception {
        String shortSentence = "Mary left her food in the fridge .";
        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(shortSentence);
        preprocessor.annotate(ta);
        View view = annotator.getView(ta);
        assertEquals("Location", view.getConstituents().get(0).getLabel());
    }

    @Test
    public void testLong() throws Exception {
        String longSentence =
                "introducing genes into an afflicted individual for therapeutic purposes : "
                        + "holds great potential for treating the relatively small number of disorders "
                        + "traceable to a single defective gene .";
        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(longSentence);
        preprocessor.annotate(ta);
        View view = annotator.getView(ta);
        assertEquals("Source", view.getConstituents().get(0).getLabel());
    }
}
