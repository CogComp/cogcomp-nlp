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
 * Tests for NLP pipeline
 *
 * @author khashabi
 */
public class CachingPipelineTest {

    private AnnotatorService service = null;

    @Before
    public void init() throws IOException, AnnotatorException {
        this.service = ExternalAnnotatorServiceFactory.buildPipeline();
    }

    @Test
    public void testExternalAnnotations() throws AnnotatorException {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        String vuName = ViewNames.SRL_VERB + "_PathLSTM";
        service.addView(ta,vuName);
        assertTrue(ta.hasView(vuName));
        assertTrue(ta.getView(vuName).getConstituents().size() > 5);
    }
}
