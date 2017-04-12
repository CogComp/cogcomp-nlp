/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for external annotators
 *
 * @author khashab2
 */
public class ExternalAnnotatorsTest {

    private AnnotatorService service = null;

    @Before
    public void init() throws IOException, AnnotatorException {
        this.service = ExternalAnnotatorServiceFactory.buildPipeline();
    }

    @Test
    public void testExternalAnnotations() throws AnnotatorException {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        service.addView(ta,ViewNames.SRL_VERB_PATH_LSTM);
        assertTrue(ta.hasView(ViewNames.SRL_VERB_PATH_LSTM));
        assertTrue(ta.getView(ViewNames.SRL_VERB_PATH_LSTM).getConstituents().size() > 5);
    }
}
