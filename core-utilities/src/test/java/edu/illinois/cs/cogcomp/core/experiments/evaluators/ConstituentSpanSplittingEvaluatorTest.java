/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConstituentSpanSplittingEvaluatorTest {
    SpanLabelView prediction1, prediction2;
    ClassificationTester splittingTester;

    @Before
    public void setUp() throws Exception {
        splittingTester = new ClassificationTester();
        TextAnnotation prediction1TA =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[] {},
                        false, 1);
        TextAnnotation prediction2TA =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[] {},
                        false, 2);
        prediction1 = (SpanLabelView) prediction1TA.getView(ViewNames.TOKENS);
        prediction2 = (SpanLabelView) prediction2TA.getView(ViewNames.TOKENS);
    }

    @Test
    public void testIdenticalSplitting() throws Exception {
        ConstituentSpanSplittingEvaluator evaluator = new ConstituentSpanSplittingEvaluator();
        evaluator.evaluate(splittingTester, prediction1, prediction1);
        assertEquals(1.0, splittingTester.getMicroF1(), 0);
        assertEquals(1.0, splittingTester.getMicroPrecision(), 0);
    }

    @Test
    public void testShorterPredictionSpans() throws Exception {
        ConstituentSpanSplittingEvaluator evaluator = new ConstituentSpanSplittingEvaluator();
        evaluator.evaluate(splittingTester, prediction1, prediction2);
        assertEquals(0.73, splittingTester.getMicroF1(), 0.01);
        assertEquals(0.58, splittingTester.getMicroPrecision(), 0.03);
    }

    @Test
    public void testLongerPredictionSpans() throws Exception {
        ConstituentSpanSplittingEvaluator evaluator = new ConstituentSpanSplittingEvaluator();
        evaluator.evaluate(splittingTester, prediction2, prediction1);
        assertEquals(0.73, splittingTester.getMicroF1(), 0.01);
        assertEquals(1.0, splittingTester.getMicroPrecision(), 0.01);
    }
}
