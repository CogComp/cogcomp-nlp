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
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestPOSBaseLineFeatureExtractor {

    private static List<TextAnnotation> tas;
    private static Logger logger = LoggerFactory.getLogger(TestPOSBaseLineFeatureExtractor.class);

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestPOSBaseLineFeatureExtractor.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void test() throws Exception {
        logger.info("POSBaseLine Feature Extractor");
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

        // logger.info(TestPOSBaseLineFeatureExtractor.class.getProtectionDomain().getCodeSource().getLocation());
        // logger.info(System.getProperty("user.dir"));

        POSBaseLineFeatureExtractor posBaseLine =
                new POSBaseLineFeatureExtractor("posBaseLine", "test_corpus", TestPosHelper.corpus);

        ArrayList<Set<Feature>> featslist = new ArrayList<>();

        for (Constituent test : testlist)
            featslist.add(posBaseLine.getFeatures(test));

        if (featslist.isEmpty()) {
            logger.info("Feats list is returning NULL.");
        }

        logger.info("Printing list of Feature set");

        for (Set<Feature> feats : featslist) {
            for (Feature f : feats)
                logger.info(f.getName());
        }

        /*
         * Set<Feature> feats = posBaseLine.getFeatures(test);
         * 
         * if (feats == null) { logger.info("Feats are returning NULL."); }
         * 
         * logger.info("Printing Set of Features");
         * 
         * for (Feature f : feats) { logger.info(f.getName()); }
         */

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
