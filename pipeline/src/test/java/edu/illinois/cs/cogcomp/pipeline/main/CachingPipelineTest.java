/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import org.junit.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for NLP pipeline
 *
 * @author mssammon
 */
public class CachingPipelineTest {
    private static final String TEST_CACHE_FILE = "test-annotation-cache";
    private static BasicAnnotatorService processor;

    @BeforeClass
    public static void init() throws IOException, AnnotatorException {
        Properties props = new Properties();
        props.setProperty(PipelineConfigurator.USE_STANFORD_DEP.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_NER_CONLL.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_SHALLOW_PARSE.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_POS.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_STANFORD_PARSE.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_QUANTIFIER.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_MENTION.key, Configurator.TRUE);

        props.setProperty(AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key, Configurator.TRUE);
        props.setProperty(AnnotatorServiceConfigurator.CACHE_DIR.key, TEST_CACHE_FILE);
        props.setProperty(AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED.key,
                Configurator.FALSE);
        props.setProperty(PipelineConfigurator.USE_JSON.key, Configurator.FALSE);
        props.setProperty(AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.FALSE);
        processor = PipelineFactory.buildPipeline(new ResourceManager(props));
    }

    @AfterClass
    public static void cleanUp() {
        processor = null;
        try {
            if (IOUtils.exists(TEST_CACHE_FILE))
                IOUtils.rm(TEST_CACHE_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void testSetup() {}

    @After
    public void cleanUpAfterTest() {}

    @Test
    public void testCachingPipeline() {
        TextAnnotation ta = null;
        String newText = "This is some text that the USA hasn't seen from Bill Smith before...";
        try {
            ta = processor.createBasicTextAnnotation("test", "test", newText);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertFalse(ta.hasView(ViewNames.SHALLOW_PARSE));
        assertFalse(ta.hasView(ViewNames.NER_CONLL));

        String[] viewsToAdd = {ViewNames.SHALLOW_PARSE, ViewNames.NER_CONLL};
        Set<String> viewNames = new HashSet<>();
        Collections.addAll(viewNames, viewsToAdd);
        try {
            ta = processor.addViewsAndCache(ta, viewNames, false);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(ta.hasView(ViewNames.SHALLOW_PARSE));
        assertTrue(ta.hasView(ViewNames.NER_CONLL));
        assertTrue(IOUtils.exists(TEST_CACHE_FILE));

        try {
            processor.addView(ta, ViewNames.QUANTITIES);
        } catch (AnnotatorException e) {
            e.printStackTrace();
        }
        assertTrue(ta.hasView(ViewNames.QUANTITIES));
        System.out.println(ta.getView(ViewNames.QUANTITIES));
    }

    @Test
    public void stanfordFailTest() {
        String inputFile = "src/test/resources/stanfordFailExample.txt";

        String text = null;
        try {
            text = LineIO.slurp(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        TextAnnotation basicTextAnnotation = null;
        try {
            basicTextAnnotation = processor.createBasicTextAnnotation("test", "test", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            processor.addView(basicTextAnnotation, ViewNames.DEPENDENCY_STANFORD);
        } catch (RuntimeException | AnnotatorException e) {
            e.printStackTrace();
            System.out.println("Expected exception from stanford.");
        }
        System.out.println(basicTextAnnotation.toString());
    }

    @Test
    public void stanfordParseHandler() {
        String text =
                "In the United States, Cinco de Mayo has taken on a significance beyond that in Mexico. ";

        TextAnnotation basicTextAnnotation = null;
        try {
            basicTextAnnotation = processor.createBasicTextAnnotation("test", "test", text);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            processor.addView(basicTextAnnotation, ViewNames.DEPENDENCY_STANFORD);
            processor.addView(basicTextAnnotation, ViewNames.PARSE_STANFORD);
        } catch (RuntimeException | AnnotatorException e) {
            e.printStackTrace();
            System.out.println("Expected exception from stanford.");
        }
        String predictedDepTree =
                basicTextAnnotation.getView(ViewNames.DEPENDENCY_STANFORD).toString();
        String goldDepTree =
                "(taken (:LABEL:prep In (:LABEL:pobj States :LABEL:det the\n"
                        + "                    :LABEL:nn United))\n"
                        + "       (:LABEL:nsubj Cinco (:LABEL:prep de :LABEL:pobj Mayo))\n"
                        + "       :LABEL:aux has\n"
                        + "       (:LABEL:prep on (:LABEL:pobj significance :LABEL:det a))\n"
                        + "       (:LABEL:prep beyond (:LABEL:pobj that (:LABEL:prep in :LABEL:pobj Mexico))))";
        assertEquals("DEPENDENCY_STANFORD - Dependency parse tree should match gold parse.",
                predictedDepTree.trim(), goldDepTree);

        String predictedParseTree =
                basicTextAnnotation.getView(ViewNames.PARSE_STANFORD).toString();
        String goldParseTree =
                "(ROOT (S (PP (IN In)\n" + "    (NP (DT the)\n" + "        (NNP United)\n"
                        + "        (NNPS States)))\n" + "   (, ,)\n" + "   (NP (NP (NNP Cinco))\n"
                        + "       (PP (IN de)\n" + "           (NP (NNP Mayo))))\n"
                        + "   (VP (VBZ has)\n" + "       (VP (VBN taken)\n"
                        + "           (PP (IN on)\n" + "               (NP (DT a)\n"
                        + "                   (NN significance)))\n"
                        + "           (PP (IN beyond)\n" + "               (NP (NP (DT that))\n"
                        + "                   (PP (IN in)\n"
                        + "                       (NP (NNP Mexico)))))))\n" + "   (. .)))";
        assertEquals(
                "PARSE_STANFORD - Constituency parse tree  generated should match gold parse.",
                predictedParseTree.trim(), goldParseTree);
    }


    @Test
    public void testHyphenSplit() {
        String source =
                "The man said that Jean-Pierre Thibault was only present from 2002-2003.  Jean-Pierre ("
                        + "also known as John-Paul) saw fit to share this only last Tuesday- who knows why.";

        TextAnnotation basicTextAnnotation = null;
        try {
            basicTextAnnotation = processor.createBasicTextAnnotation("test", "test", source);
            processor.addView(basicTextAnnotation, ViewNames.NER_CONLL);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(basicTextAnnotation.hasView(ViewNames.NER_CONLL));
        List<Constituent> nes = basicTextAnnotation.getView(ViewNames.NER_CONLL).getConstituents();
        assertEquals(3, nes.size());
        String tokForm = nes.get(0).getTokenizedSurfaceForm();
        assertEquals("Jean-Pierre Thibault", tokForm);
    }
}
