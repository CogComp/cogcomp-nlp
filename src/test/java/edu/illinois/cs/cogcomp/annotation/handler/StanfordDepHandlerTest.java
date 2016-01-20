package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class StanfordDepHandlerTest {
    private TextAnnotation ta;

    @Before
    public void setUp() {
        String bigSent = "You all know what Strats sound like and what 5-position selectors do , " +
                "but to get an overall picture of this guitar I lined it up against a regular office hack " +
                "( a Tokai hybrid with an old ‘ 58 Fender neck and Alnico Pro II 's in the middle and bridge " +
                "positions ) and can report that the SRV came out well .";
        ta = TextAnnotationUtilities.createFromTokenizedString(bigSent);
    }

    @Test
    public void testDepParserSizeFail() throws Exception {
        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        stanfordProps.put("parse.maxlen",  "60");
        stanfordProps.put("parse.maxtime", "1000");
        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        StanfordDepHandler depParser = new StanfordDepHandler(posAnnotator, parseAnnotator, 60);

        try {
            depParser.addView(ta);
        } catch (AnnotatorException e) {
            assertTrue(e.getMessage().contains("maximum sentence length"));
        }
        assertFalse(ta.hasView(ViewNames.DEPENDENCY_STANFORD));
    }

    @Test
    public void testDepParserTimeFail() throws Exception {
        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        stanfordProps.put("parse.maxlen",  "100");
        stanfordProps.put("parse.maxtime", "1000");
        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        StanfordDepHandler depParser = new StanfordDepHandler(posAnnotator, parseAnnotator, 100);

        try {
            depParser.addView(ta);
        } catch (AnnotatorException e) {
            assertTrue(e.getMessage().contains("timeout"));
        }
        assertFalse(ta.hasView(ViewNames.DEPENDENCY_STANFORD));
    }

    @Test
    public void testDepParserSuccess() throws Exception {
        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        StanfordDepHandler depParser = new StanfordDepHandler(posAnnotator, parseAnnotator);

        try {
            depParser.addView(ta);
        } catch (AnnotatorException e) {
            fail();
        }
        assertTrue(ta.hasView(ViewNames.DEPENDENCY_STANFORD));
    }
}