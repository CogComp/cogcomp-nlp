package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
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