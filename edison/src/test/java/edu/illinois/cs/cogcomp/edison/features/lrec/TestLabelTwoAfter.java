/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.helpers.TestPosHelper;
import edu.illinois.cs.cogcomp.edison.features.lrec.LabelTwoAfter;
import edu.illinois.cs.cogcomp.edison.features.lrec.TestPOSBaseLineFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.POSBaseLineCounter;
import edu.illinois.cs.cogcomp.edison.utilities.POSMikheevCounter;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestLabelTwoAfter extends TestCase {
    private static Logger logger = LoggerFactory.getLogger(TestLabelTwoAfter.class);

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestPOSBaseLineFeatureExtractor.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public final void test() throws Exception {
        logger.info("LabelTwoAfter Feature Extractor");
        // Using the first TA and a constituent between span of 30-40 as a test
        TextAnnotation ta = tas.get(2);
        View TOKENS = ta.getView("TOKENS");

        logger.info("GOT TOKENS FROM TEXTAnn");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);

        for (Constituent c : testlist) {
            logger.info(c.getSurfaceForm());
        }

        logger.info("Testlist size is " + testlist.size());

        // Constituent test = testlist.get(1);

        // logger.info("The constituent we are extracting features from
        // in this test is: " + test.getSurfaceForm());


        POSBaseLineCounter posBaseLine = new POSBaseLineCounter("posBaseLine");
        posBaseLine.buildTable(TestPosHelper.corpus);

        POSMikheevCounter posMikheev = new POSMikheevCounter("posMikheev");
        posMikheev.buildTable(TestPosHelper.corpus);

        LabelTwoAfter l2aPOS = new LabelTwoAfter("l2aPOS");
        LabelTwoAfter l2aPOSBaseLine = new LabelTwoAfter("l2aPOSBaseLine", posBaseLine);
        LabelTwoAfter l2aPOSMikheev = new LabelTwoAfter("l2aPOSMikheev", posMikheev);

        // Test when using POS View
        ArrayList<Set<Feature>> featslist = new ArrayList<>();

        for (Constituent test : testlist)
            featslist.add(l2aPOS.getFeatures(test));

        if (featslist.isEmpty()) {
            logger.info("Feats list is returning NULL.");
        }

        logger.info("\n" + "Test when using POS View");
        logger.info("Printing list of Feature set");

        for (Set<Feature> feats : featslist) {
            for (Feature f : feats)
                logger.info(f.getName());
        }

        // Test when using POS baseline Counting
        featslist.clear();

        for (Constituent test : testlist)
            featslist.add(l2aPOSBaseLine.getFeatures(test));

        if (featslist.isEmpty()) {
            logger.info("Feats list is returning NULL.");
        }

        logger.info("\n" + "Test when using POS baseline Counting");
        logger.info("Printing list of Feature set");

        for (Set<Feature> feats : featslist) {
            for (Feature f : feats)
                logger.info(f.getName());
        }
        // Test when using POS Mikheev Counting
        featslist.clear();

        for (Constituent test : testlist)
            featslist.add(l2aPOSMikheev.getFeatures(test));

        if (featslist.isEmpty()) {
            logger.info("Feats list is returning NULL.");
        }

        logger.info("\n" + "Test when using POS Mikheev Counting");
        logger.info("Printing list of Feature set");

        for (Set<Feature> feats : featslist) {
            for (Feature f : feats)
                logger.info(f.getName());
        }

        logger.info("GOT FEATURES YES!");
    }

    private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames)
            throws EdisonException {

        for (TextAnnotation ta : tas) {
            for (String viewName : viewNames)
                if (ta.hasView(viewName))
                    logger.info(ta.getView(viewName).toString());
        }
    }
}
