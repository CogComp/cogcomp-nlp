package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TransliterationAnnotator;
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
        this.annotator = new TransliterationAnnotator();
    }

    @Test
    public void testCleanText() throws AnnotatorException {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        annotator.getView(ta);
        assertEquals(true, ta.hasView(ViewNames.TRANSLITERATION));
        System.out.println(ta.getView(ViewNames.TRANSLITERATION));
    }
}
