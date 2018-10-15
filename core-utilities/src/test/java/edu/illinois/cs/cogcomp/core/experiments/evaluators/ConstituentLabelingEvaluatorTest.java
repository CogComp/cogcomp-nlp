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
import static org.junit.Assert.assertTrue;

public class ConstituentLabelingEvaluatorTest {
    SpanLabelView gold, predicted;
    ClassificationTester splittingTester;

    @Before
    public void setUp() throws Exception {
        splittingTester = new ClassificationTester();
        String[] viewsToAdd = {ViewNames.POS};
        TextAnnotation goldTA =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);
        TextAnnotation predictionTA =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, true, 3);
        gold = (SpanLabelView) goldTA.getView(ViewNames.POS);
        predicted = (SpanLabelView) predictionTA.getView(ViewNames.POS);
    }

    @Test
    public void testEvaluateIdenticalSpanLabels() throws Exception {
        ConstituentLabelingEvaluator evaluator = new ConstituentLabelingEvaluator();
        evaluator.evaluate(splittingTester, gold, gold);
        assertEquals(1.0, splittingTester.getMicroF1(), 0);
        assertEquals(1.0, splittingTester.getMicroPrecision(), 0);
    }

    @Test
    public void testEvaluateNoisySpanLabels() throws Exception {
        ConstituentLabelingEvaluator evaluator = new ConstituentLabelingEvaluator();
        evaluator.evaluate(splittingTester, gold, predicted);
        assertEquals(0.636, splittingTester.getMicroF1(), 0.01);
        assertEquals(0.568, splittingTester.getMicroPrecision(), 0.01);
    }
}
