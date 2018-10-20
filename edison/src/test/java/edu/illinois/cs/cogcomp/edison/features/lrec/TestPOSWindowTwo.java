/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

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
import org.apache.commons.lang.ArrayUtils;
import junit.framework.TestCase;

import java.util.List;
import java.util.Set;
import java.util.Random;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Test class for SHALLOW PARSER POSWindow Feature Extractor
 *
 * @author Paul Vijayakumar, Mazin Bokhari
 */
public class TestPOSWindowTwo {
    static Logger log = Logger.getLogger(TestPOSWindowTwo.class.getName());

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestPOSWindowTwo.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void test() throws EdisonException {

        log.debug("POSWindowTwo Feature Extractor");
        // Using the first TA and a constituent between span of 30-40 as a test
        TextAnnotation ta = tas.get(2);
        View TOKENS = ta.getView("TOKENS");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);

        for (Constituent c : testlist) {
            log.debug(c.getSurfaceForm());
        }

        Constituent test = testlist.get(1);

        log.debug("The constituent we are extracting features from in this test is: "
                + test.getSurfaceForm());

        POSWindowTwo POSW = new POSWindowTwo("POSWindowTwo");

        log.debug("Startspan is " + test.getStartSpan() + " and Endspan is " + test.getEndSpan());

        Set<Feature> feats = POSW.getFeatures(test);

        String[] expected_outputs =
                {"POSWindowTwo:0(DT)", "POSWindowTwo:1(VBZ)", "POSWindowTwo:2(DT)",
                        "POSWindowTwo:3(NN)"};

        if (feats == null) {
            log.debug("Feats are returning NULL.");
        }

        log.debug("Printing Set of Features");
        for (Feature f : feats) {
            assert (ArrayUtils.contains(expected_outputs, f.getName()));
        }

        // System.exit(0);
    }

    private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames)
            throws EdisonException {

        for (TextAnnotation ta : tas) {
            for (String viewName : viewNames)
                if (ta.hasView(viewName))
                    log.debug(ta.getView(viewName));
        }
    }
}
