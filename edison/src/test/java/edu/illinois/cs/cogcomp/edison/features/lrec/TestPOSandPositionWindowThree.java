/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
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
import org.junit.Test;

/**
 * Test class for SHALLOW PARSER Formpp Feature Extractor
 *
 * @author Paul Vijayakumar, Mazin Bokhari
 */
public class TestPOSandPositionWindowThree {
    static Logger log = Logger.getLogger(TestPOSandPositionWindowThree.class.getName());

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestPOSandPositionWindowThree.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void test() throws EdisonException {

        log.debug("POSWindowpp Feature Extractor");
        // Using the first TA and a constituent between span of 30-40 as a test
        TextAnnotation ta = tas.get(2);
        View TOKENS = ta.getView("TOKENS");

        log.debug("GOT TOKENS FROM TEXTAnn");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);

        for (Constituent c : testlist) {
            log.debug(c.getSurfaceForm());
        }

        log.debug("Testlist size is " + testlist.size());

        Constituent test = testlist.get(1);

        log.debug("The constituent we are extracting features from in this test is: "
                + test.getSurfaceForm());

        POSandPositionWindowThree POSWpp =
                new POSandPositionWindowThree("POSandPositionWindowThree");

        // Formpp.initViews(test);

        log.debug("Startspan is " + test.getStartSpan() + " and Endspan is " + test.getEndSpan());

        Set<Feature> feats = POSWpp.getFeatures(test);
        String[] expected_outputs =
                {"POSandPositionWindowThree:0_0(DT)", "POSandPositionWindowThree:1_0(VBZ)",
                        "POSandPositionWindowThree:2_0(DT)", "POSandPositionWindowThree:3_0(NN)",
                        "POSandPositionWindowThree:4_0(.)", "POSandPositionWindowThree:5_0(null)",
                        "POSandPositionWindowThree:6_0(null)",
                        "POSandPositionWindowThree:0_1(DT_VBZ)",
                        "POSandPositionWindowThree:1_1(VBZ_DT)",
                        "POSandPositionWindowThree:2_1(DT_NN)",
                        "POSandPositionWindowThree:3_1(NN_.)",
                        "POSandPositionWindowThree:4_1(._null)",
                        "POSandPositionWindowThree:5_1(null_null)",
                        "POSandPositionWindowThree:6_1(null)",
                        "POSandPositionWindowThree:0_2(DT_VBZ_DT)",
                        "POSandPositionWindowThree:1_2(VBZ_DT_NN)",
                        "POSandPositionWindowThree:2_2(DT_NN_.)",
                        "POSandPositionWindowThree:3_2(NN_._null)",
                        "POSandPositionWindowThree:4_2(._null_null)",
                        "POSandPositionWindowThree:5_2(null_null)",
                        "POSandPositionWindowThree:6_2(null)"};


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
