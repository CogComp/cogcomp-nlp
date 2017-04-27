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
import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test the new pipeline constructor specifying only the views you need.
 * Created after user had problems with POS view not, apparently, being created.
 * @author mssammon
 */
public class ViewConstructorPipelineTest {
    private static final String textFile = "src/test/resources/pipelinePosText.txt";

    @Test
    public void testPosPipeline() {
        String input = null;
        try {
            input = LineIO.slurp(textFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertFalse(input.length() == 0);

        AnnotatorService as = null;
        try {
            as = PipelineFactory.buildPipeline(ViewNames.POS);
        } catch (IOException | AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            as.createAnnotatedTextAnnotation("test", "test", input);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
