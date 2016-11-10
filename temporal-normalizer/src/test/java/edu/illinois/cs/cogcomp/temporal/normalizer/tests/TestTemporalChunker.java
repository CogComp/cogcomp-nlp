package edu.illinois.cs.cogcomp.temporal.normalizer.tests;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Parameters;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerAnnotator;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerConfigurator;

import edu.illinois.cs.cogcomp.thrift.base.Span;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by zhilifeng on 11/1/16.
 */
public class TestTemporalChunker {
    private String testFileName = "test.txt";
    private static String testFile;
    private TemporalChunkerAnnotator tca;
    private String testText;
    private static Logger logger = LoggerFactory.getLogger(TestTemporalChunker.class);


    @Before
    public void setUp() throws IOException {
        URL testFileURL = TestTemporalChunker.class.getClassLoader().getResource(testFileName);
        assertNotNull("Test file missing", testFileURL);
        testFile = testFileURL.getFile();
        Properties rmProps = new TemporalChunkerConfigurator().getDefaultConfig().getProperties();

        tca = new TemporalChunkerAnnotator(new ResourceManager(rmProps));
        byte[] encoded = Files.readAllBytes(Paths.get(testFile));
        testText = new String(encoded, StandardCharsets.UTF_8);
    }

    @Test
    public void testTemporalChunker() {
        AnnotatorService annotator = null;
        try {
            annotator = CuratorFactory.buildCuratorClient();
        } catch (Exception e) {
            fail("Exception while creating AnnotatorService " + e.getStackTrace());
        }
        URL modelPath =
                IOUtilities.loadFromClasspath(
                        TemporalChunkerAnnotator.class,
                        "modelDirPath"
                );
        ResourceManager nerRm = new TemporalChunkerConfigurator().getDefaultConfig();
        IOUtilities.existsInClasspath(TemporalChunkerAnnotator.class, nerRm.getString("modelDirPath"));
        TextAnnotation ta = null;
        try {
            ta = annotator.createBasicTextAnnotation("corpus", "id", testText);
        } catch (AnnotatorException e) {
            fail("Exception while creating TextAnnotation" + e.getStackTrace());
        }
        try {
            annotator.addView(ta, ViewNames.POS);
        } catch (AnnotatorException e) {
            fail("Exception while adding POS VIEW " + e.getStackTrace());
        }

        try {
            tca.addView(ta);
        } catch (AnnotatorException e) {
            fail("Exception while adding TIMEX3 VIEW " + e.getStackTrace());
        }

        View timexView = ta.getView(ViewNames.TIMEX3);

        String corpId = "IllinoisTimeAnnotator";
        List<Constituent> timeCons = timexView.getConstituents();

        // Keep track of the compressed index of each constituent.
        Span[] compressedSpans = new Span[timeCons.size()];
        int spanStart;

        // Builds a string of the concatenated constituents from a labeled view.
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<timeCons.size(); i++){
            Constituent c = timeCons.get(i);
            spanStart = builder.length();

            builder.append(c.toString());
            builder.append("; ");

            compressedSpans[i] = new Span(spanStart, builder.length()-2);
        }
        String compressedText = builder.toString();
        logger.info(compressedText);
        assertNotNull(compressedText);

    }

}
