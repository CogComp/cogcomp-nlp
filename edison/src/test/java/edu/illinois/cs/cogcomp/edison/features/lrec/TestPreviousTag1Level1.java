/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.PreviousTag1Level1;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import edu.illinois.cs.cogcomp.ner.ParsingProcessingData.PlainTextReader;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import org.apache.commons.lang.ArrayUtils;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestTAResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.io.Writer;

import org.apache.log4j.Logger;

/**
 *
 * @author Yewen Fan
 */
public class TestPreviousTag1Level1 extends TestCase {
    static Logger log = Logger.getLogger(TestAffixes.class.getName());

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestAffixes.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    private void TestingPreviousTag1Level1(){
        NETaggerLevel1 t1 = null;
        NETaggerLevel2 t2 = null;
        try {
            Parameters.readConfigAndLoadExternalData(new NerBaseConfigurator().getDefaultConfig());
            ParametersForLbjCode.currentParameters.forceNewSentenceOnLineBreaks = false;
            System.out.println("Reading model file : "
                    + ParametersForLbjCode.currentParameters.pathToModelFile + ".level1");
            t1 =
                    new NETaggerLevel1(ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level1", ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level1.lex");
            if (ParametersForLbjCode.currentParameters.featuresToUse
                    .containsKey("PredictionsLevel1")) {
                System.out.println("Reading model file : "
                        + ParametersForLbjCode.currentParameters.pathToModelFile + ".level2");
                t2 =
                        new NETaggerLevel2(ParametersForLbjCode.currentParameters.pathToModelFile
                                + ".level2", ParametersForLbjCode.currentParameters.pathToModelFile
                                + ".level2.lex");
            }
        } catch (Exception e) {
            System.out.println("Cannot initialise the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
        PreviousTag1Level1 ptl1 = new PreviousTag1Level1();
        String test = "By winning the National Football League(NFL) playoff game, the 49ers will host the winner of Sunday's " +
                "Dallas-Green Bay Game on January 15 to decide a berth in the January 29 championship game at Miami.";
        // String test = "JFK has one dog and Newark has a handful, Farbstein said.";
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(test);
        for (LinkedVector lv : sentences) {
            System.out.println(sentences);
            for (int i = 0; i < lv.size(); i++) {
                NEWord neWord = (NEWord) (lv.get(i));
                Data data = new Data(new NERDocument(sentences, "input"));
                try {
                    String output = NETagPlain.tagData(data, t1, t2);
                    // System.out.println("outout");
                    // System.out.println(output);
                } catch (Exception e) {
                    System.out.println("Cannot annotate the test, the exception was: ");
                    e.printStackTrace();
                    fail();
                }
                System.out.println(neWord.form);
                System.out.println(ptl1.classify(neWord).toString());
            }
        }
    }

    public final void test() throws EdisonException {

        TestingPreviousTag1Level1();

        log.debug("TestPreviousTag1Level1 Feature Extractor");
        // Using the first TA and a constituent between span of 30-40 as a test
        TextAnnotation ta = tas.get(9);
        View TOKENS = ta.getView("TOKENS");

        log.debug("Got tokens FROM TextAnnotation");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 50);

        for (Constituent c : testlist) {
            log.debug(c.getSurfaceForm());
            System.out.println(c.getSurfaceForm());
        }

        log.debug("Test Input size is " + testlist.size());

        Constituent test = testlist.get(4);

        log.debug("The constituent we are extracting features from in this test is: "
                + test.getSurfaceForm());

        PreviousTag1Level1Edison afx = new PreviousTag1Level1Edison("PreviousTag1Level1");

        log.debug("Startspan is " + test.getStartSpan() + " and Endspan is " + test.getEndSpan());

        Set<Feature> feats = afx.getFeatures(test);
        String[] expected_outputs =
                {"Affixes:p|(giv)", "Affixes:s|(e)", "Affixes:s|(ve)", "Affixes:s|(ive)"};

        if (feats == null) {
            log.debug("Feats are returning NULL.");
        }

        log.debug("Printing Set of Features");
        for (Feature f : feats) {
            log.debug(f.getName());
            System.out.println(f.getName());
            // assert (ArrayUtils.contains(expected_outputs, f.getName()));
        }

        for (int i = 0; i < testlist.size(); ++i) {
            Constituent testAgain = testlist.get(i);
            System.out.println(testAgain.getSurfaceForm());
            Set<Feature> featsAgain = afx.getFeatures(testAgain);
            for (Feature f : featsAgain) {
                log.debug(f.getName());
                System.out.println(f.getName());
                // assert (ArrayUtils.contains(expected_outputs, f.getName()));
            }
        }

    }

}
