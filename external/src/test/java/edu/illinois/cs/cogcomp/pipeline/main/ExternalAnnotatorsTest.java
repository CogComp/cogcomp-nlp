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
import edu.illinois.cs.cogcomp.pipeline.handlers.PathLSTMHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordCorefHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordOpenIEHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordRelationsHandler;
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
    private TextAnnotation ta = null;

    @Before
    public void init() throws IOException, AnnotatorException {
        this.service = ExternalAnnotatorServiceFactory.buildPipeline();
        this.ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
    }

    @Test
    public void testExternalAnnotators() throws AnnotatorException {
        service.addView(ta, PathLSTMHandler.SRL_VERB_PATH_LSTM);
        assertTrue(ta.hasView(PathLSTMHandler.SRL_VERB_PATH_LSTM));
        assertTrue(ta.getView(PathLSTMHandler.SRL_VERB_PATH_LSTM).getConstituents().size() > 5);

        service.addView(ta, StanfordOpenIEHandler.viewName);
        assertTrue(ta.hasView(StanfordOpenIEHandler.viewName));
        assertTrue(ta.getView(StanfordOpenIEHandler.viewName).getConstituents().size() >= 5);

        service.addView(ta, StanfordRelationsHandler.viewName);
        assertTrue(ta.hasView(StanfordRelationsHandler.viewName));
        assertTrue(ta.getView(StanfordRelationsHandler.viewName).getConstituents().size() >= 5);

        service.addView(ta, StanfordCorefHandler.viewName);
        assertTrue(ta.hasView(StanfordCorefHandler.viewName));
    }
}
