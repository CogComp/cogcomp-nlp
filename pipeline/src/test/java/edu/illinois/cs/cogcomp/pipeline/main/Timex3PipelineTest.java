/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by zhilifeng on 8/8/17.
 */
public class Timex3PipelineTest {
    private static BasicAnnotatorService timex3Processor;

    @BeforeClass
    public static void init() throws IOException, AnnotatorException {
        ResourceManager rm = new PipelineConfigurator().getConfig(new ResourceManager( "config/pipeline-timex3.properties" ));
        timex3Processor = PipelineFactory.buildPipeline(rm);
    }


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
        assertEquals("<TIMEX3 mod=\"START\" type=\"DATE\" value=\"2016-12\">", constituents.get(0).getLabel());
        assertEquals("<TIMEX3 type=\"DATE\" value=\"2017-07\">", constituents.get(1).getLabel());
    }

}
