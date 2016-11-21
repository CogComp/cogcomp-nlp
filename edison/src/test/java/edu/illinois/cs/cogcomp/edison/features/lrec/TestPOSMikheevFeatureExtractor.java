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
import java.util.*;


public class TestPOSMikheevFeatureExtractor {

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestPOSMikheevFeatureExtractor.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void test() throws Exception {

        POSMikheevFeatureExtractor posMikheev =
                new POSMikheevFeatureExtractor("posMikheev", "test_corpus", TestPosHelper.corpus);

        System.out.println("POSMikheev Feature Extractor");
        System.out.println("Only print the features with known tags");
        // Using the first TA and a constituent between span of 30-40 as a test
        int i = 0;
        for (TextAnnotation ta : tas) {
            ArrayList<String> outFeatures = new ArrayList<>();
            View TOKENS = ta.getView("TOKENS");

            for (Constituent TOKEN : TOKENS) {
                Set<Feature> feats = posMikheev.getFeatures(TOKEN);
                if (feats.isEmpty()) {
                    System.out.println("Feats list is returning NULL.");
                }
                for (Feature f : feats)
                    if (!f.getName().contains("UNKNOWN")) {
                        outFeatures.add(f.getName());
                    }
            }

            if (!outFeatures.isEmpty()) {
                System.out.println("-------------------------------------------------------");
                System.out.println("Text Annotation: " + i);
                System.out.println("Text Features: ");

                for (String out : outFeatures)
                    System.out.println(out);

                System.out.println("-------------------------------------------------------");
            }

            i++;
        }
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
