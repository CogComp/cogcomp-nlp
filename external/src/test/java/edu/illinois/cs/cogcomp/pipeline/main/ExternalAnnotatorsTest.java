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
    private TextAnnotation ta2 = null;

    @Before
    public void init() throws IOException, AnnotatorException {
        this.service = ExternalAnnotatorServiceFactory.buildPipeline();
        this.ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        String sampleText = "Shelly wanted a puppy. She asked her mommy and daddy every day for one. She told them that she would help take care of the puppy, if she could have one. Her mommy and daddy talked it over and said that they would get Shelly a new puppy. Her mommy took her to the dog pound so that she could choose one that she wanted. All the puppies at the dog pound need a loving home. Shelly went to every cage and looked each puppy in the eyes and talked to each one. After each one, she told her mommy, \"No, this isn't the one for me.\" Finally, she saw a black and white spotted one that she fell in love with. She screamed, \"Mommy, this is the one!\" Her mommy asked the worker to take the puppy out so that Shelly could make sure. Shelly and the puppy fell in love with each other right away. Shelly and her mommy took the black and white spotted puppy home with them. Shelly was so excited that she talked all the way home. After thinking hard, Shelly had a name for her new puppy, Spot.";
        this.ta2 = service.createBasicTextAnnotation("", "", sampleText);
    }

    @Test
    public void testExternalAnnotators() throws AnnotatorException {
        // sample text 1
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


        // sample text 2
        service.addView(ta2, StanfordCorefHandler.viewName);
        assertTrue(ta2.getView(StanfordCorefHandler.viewName).getConstituents().size() > 40);
    }
}
