/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
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

    public static void main(String args[]) {
        String input = null;
        try {
            input = LineIO.slurp(textFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("input from " + textFile + " is " + input.length() + " characters long.");

        AnnotatorService as = null;
        try {
            as = PipelineFactory.buildPipeline(ViewNames.POS);
        } catch (IOException | AnnotatorException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        TextAnnotation ta = null;
        try {
            ta = as.createAnnotatedTextAnnotation("test", "test", input);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("found " + ta.getView(ViewNames.POS).getConstituents() + " POS constituents." );
    }

    /**
     * NOTE: this test cannot be run as part of test suite as it tries to
     *   open a default cache already used elsewhere by other tests, which will
     *   not be closed properly. This test is useful to verify that the particular
     *   factory method works, but must be run separately.
     */
//    @Test
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
