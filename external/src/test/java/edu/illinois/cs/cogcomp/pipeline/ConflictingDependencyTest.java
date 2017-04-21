package edu.illinois.cs.cogcomp.pipeline;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.PathLSTMHandler;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test combination of regular pipeline and external annotators, which have conflicting versions of Stanford
 *    libraries. See if shade plugin resolves the problem.
 *
 * @author mssammon
 */
public class ConflictingDependencyTest {

    @Test
    public void testConflict() {

        BasicAnnotatorService pipeline = null;

        Properties props = new Properties();
        props.setProperty(PipelineConfigurator.USE_STANFORD_PARSE.key, PipelineConfigurator.TRUE);

        try {
            pipeline = PipelineFactory.buildPipeline(new ResourceManager(props));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AnnotatorException e) {
            e.printStackTrace();
        }

        PathLSTMHandler pathSRL = new PathLSTMHandler(true);

        try {
            pipeline.addAnnotator(pathSRL);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        String text = "Perfection of a kind was what he was after, and the poetry he invented " +
                "was easy to understand. He knew human folly like the back of his hand, " +
                "and was greatly interested in armies and fleets.";

        TextAnnotation ta = null;
        try {
            ta = pipeline.createAnnotatedTextAnnotation("test", "test", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(ta.hasView(pathSRL.getViewName()));
    }
}
