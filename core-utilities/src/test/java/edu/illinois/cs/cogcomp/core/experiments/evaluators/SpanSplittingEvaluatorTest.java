package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpanSplittingEvaluatorTest {
    SpanLabelView prediction1, prediction2;
    ClassificationTester splittingTester;

    @Before
    public void setUp() throws Exception {
        splittingTester = new ClassificationTester();
        TextAnnotation prediction1TA = DummyTextAnnotationGenerator.generateBasicTextAnnotation(4);
        TextAnnotation prediction2TA = DummyTextAnnotationGenerator.generateBasicTextAnnotation(1);
        prediction1 = (SpanLabelView) prediction1TA.getView(ViewNames.TOKENS);
        prediction2 = (SpanLabelView) prediction2TA.getView(ViewNames.TOKENS);
    }

    @Test
    public void testIdenticalSplitting() throws Exception {
        SpanSplittingEvaluator evaluator = new SpanSplittingEvaluator();
        evaluator.setViews(prediction1, prediction1);
        evaluator.evaluate(splittingTester);
        assertEquals(1.0, splittingTester.getAverageF1(), 0);
        assertEquals(1.0, splittingTester.getAverageAccuracy(), 0);
    }

    @Test
    public void testShorterPredictionSpans() throws Exception {
        SpanSplittingEvaluator evaluator = new SpanSplittingEvaluator();
        evaluator.setViews(prediction1, prediction2);
        evaluator.evaluate(splittingTester);
        assertEquals(0.4, splittingTester.getAverageF1(), 0);
        assertEquals(1.0, splittingTester.getAverageAccuracy(), 0);
    }

    @Test
    public void testLongerPredictionSpans() throws Exception {
        SpanSplittingEvaluator evaluator = new SpanSplittingEvaluator();
        evaluator.setViews(prediction2, prediction1);
        evaluator.evaluate(splittingTester);
        assertEquals(0.4, splittingTester.getAverageF1(), 0);
        assertEquals(0.25, splittingTester.getAverageAccuracy(), 0);
    }
}