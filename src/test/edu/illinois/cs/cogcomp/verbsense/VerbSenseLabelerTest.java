package edu.illinois.cs.cogcomp.verbsense;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.verbsense.experiment.TextPreProcessor;
import junit.framework.TestCase;

public class VerbSenseLabelerTest extends TestCase {
    VerbSenseLabeler labeler;
    TextAnnotation ta, taNoVerb;
    TextPreProcessor preProcessor;

    public void setUp() throws Exception {
        super.setUp();
        labeler = new VerbSenseLabeler("config/verbsense-config.properties");
        preProcessor = TextPreProcessor.getInstance();
    }

    public void testGetPrediction() throws Exception {
        ta = preProcessor.preProcessText("John Smith wrote a book.");
        taNoVerb = preProcessor.preProcessText("Events from the year 1872.");
        assertEquals("John Smith (01 wrote) a book . ", labeler.getPrediction(ta).toString());
        assertEquals("Events from the year 1872 . ", labeler.getPrediction(taNoVerb).toString());
    }
}