/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author zhilifeng
 */
public class Timex3PipelineTest {
    private static BasicAnnotatorService timex3Processor;

    @BeforeClass @Ignore
    public static void init() throws IOException, AnnotatorException {
        timex3Processor = PipelineFactory.buildPipeline(ViewNames.POS, ViewNames.TIMEX3);
    }

    /** this test seems to be unreliable, ignore until fixed. */
    @Ignore
    @Test
    public void testSentencePipeline() {
        TextAnnotation ta = null;
        try {
            ta = timex3Processor.createAnnotatedTextAnnotation(
                    "dummy",
                    "dummy",
                    "This flu season started in early December, a month earlier than usual, and peaked by the end of year."
            );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            timex3Processor.addView(ta, ViewNames.TIMEX3);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(ta.hasView(ViewNames.TIMEX3));
        View temporalViews = ta.getView(ViewNames.TIMEX3);
        List<Constituent> constituents = temporalViews.getConstituents();
        DateTime now = DateTime.now();
        int curYear = now.year().get();
        int curMonth = now.monthOfYear().get();
        assertEquals("<TIMEX3 mod=\"START\" type=\"DATE\" value=\"" + Integer.toString(curYear - 1) + "-12\">", constituents.get(0).getLabel());
        String ccy = String.format("%02d", (curMonth - 2));
        String tx1 = "<TIMEX3 type=\"DATE\" value=\"" + Integer.toString(curYear) + "-" + ccy + "\">";
        String tx2 = constituents.get(1).getLabel();
        assertEquals(tx1, tx2);
    }
}
