/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import edu.illinois.cs.cogcomp.ner.ParsingProcessingData.PlainTextReader;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class NerTest {
    private static Logger logger = LoggerFactory.getLogger(NerTest.class);

    private static final String TEST_INPUT =
            "JFK has one dog and Newark has a handful, Farbstein said.";
    private static final String TEST_OUTPUT =
            "[PERSON JFK] has [CARDINAL one] dog and [GPE Newark] has a handful , [PERSON Farbstein] said . ";

    private static NETaggerLevel1 t1;
    private static NETaggerLevel2 t2 = null;
    ParametersForLbjCode params = null;
    @Before
    public void setUp() throws Exception {
        try {
            ResourceManager rm = new NerBaseConfigurator().getDefaultConfig();
            params = Parameters.readConfigAndLoadExternalData(rm);
            params.forceNewSentenceOnLineBreaks = false;
            ModelLoader.load(rm, ViewNames.NER_ONTONOTES, false, params);
            t1 = (NETaggerLevel1) params.taggerLevel1;
            t2 = (NETaggerLevel2) params.taggerLevel2;
        } catch (Exception e) {
            System.err.println("Cannot initialise the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testTaggers() {
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(TEST_INPUT, params);
        Data data = new Data(new NERDocument(sentences, "input"));
        String output = null;
        try {
            output = NETagPlain.tagData(data, params);
        } catch (Exception e) {
            logger.info("Cannot annotate the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
        assertTrue(output.equals(TEST_OUTPUT));
    }

    @After
    public void tearDown() throws Exception {}
}
