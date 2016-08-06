/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

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

import java.util.List;
import java.util.Set;
import java.util.Random;
import java.io.Writer;

import org.apache.log4j.Logger;

/**
 * Test class for SHALLOW PARSER Formpp Feature Extractor
 *
 * @author Paul Vijayakumar, Mazin Bokhari
 */
public class TestWordConjunctionOneTwoThreeGramWindowTwo extends TestCase {
    static Logger log = Logger.getLogger(TestAffixes.class.getName());

    private static List<TextAnnotation> tas;

    static {
        try {
            tas =
                    IOUtils.readObjectAsResource(TestWordConjunctionOneTwoThreeGramWindowTwo.class,
                            "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public final void test() throws EdisonException {

        log.debug("Formpp");
        // Using the first TA and a constituent between span of 30-40 as a test
        TextAnnotation ta = tas.get(1);
        View TOKENS = ta.getView("TOKENS");

        log.debug("GOT TOKENS FROM TEXTAnn");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);

        for (Constituent c : testlist) {
            log.debug(c.getSurfaceForm());
        }

        log.debug("Testlist size is " + testlist.size());

        Constituent test = testlist.get(2);

        log.debug("The constituent we are extracting features from in this test is: "
                + test.getSurfaceForm());

        WordConjunctionOneTwoThreeGramWindowTwo WordConjunctionOneTwoThreeGramWindowTwo =
                new WordConjunctionOneTwoThreeGramWindowTwo(
                        "WordConjunctionOneTwoThreeGramWindowTwo");

        log.debug("Startspan is " + test.getStartSpan() + " and Endspan is " + test.getEndSpan());

        Set<Feature> feats = WordConjunctionOneTwoThreeGramWindowTwo.getFeatures(test);
        String[] expected_outputs =
                {"WordConjunctionOneTwoThreeGramWindowTwo:0_0(I)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:1_0(give)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:2_0(John)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:3_0($900)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:4_0(today)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:0_1(I_give)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:1_1(give_John)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:2_1(John_$900)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:3_1($900_today)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:4_1(today)"};

        if (feats == null) {
            log.debug("Feats are returning NULL.");
        }

        log.debug("Printing Set of Features");
        for (Feature f : feats) {
            log.debug(f.getName());
            assert (ArrayUtils.contains(expected_outputs, f.getName()));
        }

        log.debug("GOT FEATURES YES!");

        // System.exit(0);
    }


}
