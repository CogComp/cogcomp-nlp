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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
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
 * Unit test for {@link PosWordConjunctionSizeTwoWindowSizeTwo} extractor, which generates a
 * conjunction of 2-shingles from a window of 2 tokens. The extractor is originally used in
 * illinois-chunker (shallow parser).
 *
 * @author Christos Christodoulopoulos
 */
public class TestPosWordConjunctionSizeTwoWindowSizeTwo {
    private TextAnnotation ta;

    @Before
    public void setUp() throws Exception {
        ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(
                        new String[] {ViewNames.POS}, false, 3);
    }

    @Test
    public final void test() throws EdisonException {
        // / Using the 3rd constituent as a test
        List<Constituent> testList = ta.getView("TOKENS").getConstituents();
        Constituent test = testList.get(3);

        PosWordConjunctionSizeTwoWindowSizeTwo fex =
                new PosWordConjunctionSizeTwoWindowSizeTwo("PosWordConj2Win2");

        Set<Feature> feats = fex.getFeatures(test);
        String[] expected_outputs =
                {"PosWordConjunctionSizeTwoWindowSizeTwo:-2_1(NN-construction)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:-1_1(IN-of)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:0_1(DT-the)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:1_1(NNP-John)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:2_1(NNP-Smith)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:-2_2(NN-construction_IN-of)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:-1_2(IN-of_DT-the)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:0_2(DT-the_NNP-John)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:1_2(NNP-John_NNP-Smith)",
                        "PosWordConjunctionSizeTwoWindowSizeTwo:2_2(NNP-Smith)"};


        if (feats == null)
            fail("Feats are returning NULL.");

        for (Feature f : feats) {
            assertTrue(ArrayUtils.contains(expected_outputs, f.getName()));
        }
    }
}
