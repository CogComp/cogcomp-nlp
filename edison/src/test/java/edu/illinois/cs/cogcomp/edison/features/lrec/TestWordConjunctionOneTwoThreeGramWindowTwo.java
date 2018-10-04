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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link WordConjunctionOneTwoThreeGramWindowTwo} extractor, which generates a
 * conjunction of 3-shingles from a window of 2 tokens. The extractor is originally used in
 * illinois-chunker (shallow parser).
 *
 * @author Christos Christodoulopoulos
 */
public class TestWordConjunctionOneTwoThreeGramWindowTwo {
    private TextAnnotation ta;

    @Before
    public void setUp() throws Exception {
        ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[] {},
                        false, 3);
    }

    @Test
    public final void test() throws EdisonException {
        // Using the 3rd constituent as a test
        List<Constituent> testList = ta.getView("TOKENS").getConstituents();
        Constituent test = testList.get(3);

        WordConjunctionOneTwoThreeGramWindowTwo fex =
                new WordConjunctionOneTwoThreeGramWindowTwo("WordConj3GramWin2");

        Set<Feature> feats = fex.getFeatures(test);
        String[] expected_outputs =
                {"WordConjunctionOneTwoThreeGramWindowTwo:-2_1(construction)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:-1_1(of)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:0_1(the)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:1_1(John)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:2_1(Smith)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:-2_2(construction_of)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:-1_2(of_the)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:0_2(the_John)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:1_2(John_Smith)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:2_2(Smith)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:-2_3(construction_of_the)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:-1_3(of_the_John)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:0_3(the_John_Smith)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:1_3(John_Smith)",
                        "WordConjunctionOneTwoThreeGramWindowTwo:2_3(Smith)"};

        if (feats == null)
            fail("Feats are returning NULL.");

        for (Feature f : feats) {
            assertTrue(ArrayUtils.contains(expected_outputs, f.getName()));
        }
    }
}
