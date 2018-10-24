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
import java.util.LinkedHashSet;
import java.util.Random;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Test class for SHALLOW PARSER WordTypeInformation Feature Extractor
 *
 * @author Paul Vijayakumar, Mazin Bokhari
 */
public class TestWordTypeInformation {
    static Logger log = Logger.getLogger(TestAffixes.class.getName());

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestWordTypeInformation.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void test() throws EdisonException {

        log.debug("WordTypeInformation");
        // Using the first TA and a constituent between span of 0 - 20 as a test
        TextAnnotation ta = tas.get(1);
        View TOKENS = ta.getView("TOKENS");

        log.debug("GOT TOKENS FROM TEXTAnn");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);
        String[] teststrings = new String[5];

        int i = 0, start = 1, end = 6;

        for (Constituent c : testlist) {
            log.debug(c.getSurfaceForm());
            if (i >= start && i < end) {
                teststrings[i - start] = c.getSurfaceForm();
            }
            i++;
        }

        log.debug("Testlist size is " + testlist.size());

        Constituent test = testlist.get(3);

        log.debug("The constituent we are extracting features from in this test is: "
                + test.getSurfaceForm());

        WordTypeInformation wti = new WordTypeInformation("WordTypeInformation");

        log.debug("Startspan is " + test.getStartSpan() + " and Endspan is " + test.getEndSpan());

        Set<Feature> feats = wti.getFeatures(test);
        String[] expected_outputs =
                {"WordTypeInformation:c0(false)", "WordTypeInformation:d0(false)",
                        "WordTypeInformation:c1(false)", "WordTypeInformation:d1(false)",
                        "WordTypeInformation:c2(false)", "WordTypeInformation:d2(false)",
                        "WordTypeInformation:c2(true)", "WordTypeInformation:c3(false)",
                        "WordTypeInformation:d3(false)", "WordTypeInformation:c4(false)",
                        "WordTypeInformation:d4(false)", "WordTypeInformation:c4(true)"};


        Set<String> __result = new LinkedHashSet<String>();
        String __id;
        String __value;
        String classifier = "WordTypeInformation";

        if (feats == null) {
            log.debug("Feats are returning NULL.");
            assertFalse(true);
        }

        log.debug("Printing Set of Features");
        for (Feature f : feats) {
            log.debug(f.getName());
            assert (ArrayUtils.contains(expected_outputs, f.getName()));
        }

        for (; (start < end && teststrings[start - 1] != null); start++) {

            boolean allCapitalized = true, allDigits = true, allNonLetters = true;

            for (int j = 0; j < teststrings[start - 1].length(); ++j) {

                allCapitalized &= Character.isUpperCase(teststrings[start - 1].charAt(j));
                allDigits &= Character.isDigit(teststrings[start - 1].charAt(j));
                allNonLetters &= !Character.isLetter(teststrings[start - 1].charAt(j));

            }
            __id = classifier + ":" + ("c" + (start - 1));
            __value = "(" + (allCapitalized) + ")";
            __result.add(__id + __value);
            __id = classifier + ":" + ("d" + (start - 1));
            __value = "(" + (allDigits) + ")";
            __result.add(__id + __value);
            __id = classifier + ":" + ("c" + (start - 1));
            __value = "(" + (allNonLetters) + ")";
            __result.add(__id + __value);
        }

        for (Feature feat : feats) {
            if (!__result.contains(feat.getName())) {
                assertFalse(true);
            }
        }

        // System.exit(0);
    }

}
