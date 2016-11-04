/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestPOSBaseLineFeatureExtractor {

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestPOSBaseLineFeatureExtractor.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void test() throws Exception {
        System.out.println("POSBaseLine Feature Extractor");
        // Using the first TA and a constituent between span of 30-40 as a test
        TextAnnotation ta = tas.get(2);
        View TOKENS = ta.getView("TOKENS");

        System.out.println("GOT TOKENS FROM TEXTAnn");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);

        for (Constituent c : testlist) {
            System.out.println(c.getSurfaceForm());
        }

        System.out.println("Testlist size is " + testlist.size());

        // Constituent test = testlist.get(1);

        // System.out.println("The constituent we are extracting features from
        // in this test is: " + test.getSurfaceForm());

        // System.out.println(TestPOSBaseLineFeatureExtractor.class.getProtectionDomain().getCodeSource().getLocation());
        // System.out.println(System.getProperty("user.dir"));

        POSBaseLineFeatureExtractor posBaseLine =
                new POSBaseLineFeatureExtractor("posBaseLine", "test_corpus", TestPosHelper.corpus);

        ArrayList<Set<Feature>> featslist = new ArrayList<>();

        for (Constituent test : testlist)
            featslist.add(posBaseLine.getFeatures(test));

        if (featslist.isEmpty()) {
            System.out.println("Feats list is returning NULL.");
        }

        System.out.println("Printing list of Feature set");

        for (Set<Feature> feats : featslist) {
            for (Feature f : feats)
                System.out.println(f.getName());
        }

        /*
         * Set<Feature> feats = posBaseLine.getFeatures(test);
         * 
         * if (feats == null) { System.out.println("Feats are returning NULL."); }
         * 
         * System.out.println("Printing Set of Features");
         * 
         * for (Feature f : feats) { System.out.println(f.getName()); }
         */

        System.out.println("GOT FEATURES YES!");
    }

    private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames)
            throws EdisonException {

        for (TextAnnotation ta : tas) {
            for (String viewName : viewNames)
                if (ta.hasView(viewName))
                    System.out.println(ta.getView(viewName));
        }
    }
}
