/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.verbsense.experiment.TextPreProcessor;
import junit.framework.TestCase;

public class VerbSenseLabelerTest extends TestCase {
    VerbSenseLabeler labeler;
    TextAnnotation ta, taNoVerb;
    TextPreProcessor preProcessor;
    VerbSenseAnnotator annotator;

    public void setUp() throws Exception {
        super.setUp();
        labeler = new VerbSenseLabeler();
        preProcessor = TextPreProcessor.getInstance();
        annotator = new VerbSenseAnnotator();
        annotator.doInitialize();
    }

    public void testGetPrediction() throws Exception {
        ta = preProcessor.preProcessText("John Smith wrote a book.");
        taNoVerb = preProcessor.preProcessText("Events from the year 1872.");
        assertEquals("John Smith (01 wrote) a book . ", labeler.getPrediction(ta).toString());
        assertEquals("Events from the year 1872 . ", labeler.getPrediction(taNoVerb).toString());
    }

    public void testAnnotator() throws Exception {
        ta = preProcessor.preProcessText("John Smith wrote a book.");
        taNoVerb = preProcessor.preProcessText("Events from the year 1872.");
        annotator.addView(ta);
        annotator.addView(taNoVerb);
        assertEquals("John Smith (01 wrote) a book . ", ta.getView(annotator.getViewName())
                .toString());
        assertEquals("Events from the year 1872 . ", taNoVerb.getView(annotator.getViewName())
                .toString());
    }
}
