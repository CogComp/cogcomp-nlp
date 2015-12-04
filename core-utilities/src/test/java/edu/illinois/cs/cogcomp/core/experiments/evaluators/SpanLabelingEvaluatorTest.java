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

public class SpanLabelingEvaluatorTest {
    SpanLabelView gold, predicted;
    ClassificationTester splittingTester;

    @Before
    public void setUp() throws Exception {
        splittingTester = new ClassificationTester();
        String[] viewsToAdd = {ViewNames.POS};
        TextAnnotation goldTA = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
        TextAnnotation predictionTA = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, true);
        gold = (SpanLabelView) goldTA.getView(ViewNames.POS);
        predicted = (SpanLabelView) predictionTA.getView(ViewNames.POS);
    }

    @Test
    public void testEvaluateIdenticalSpanLabels() throws Exception {
        SpanLabelingEvaluator evaluator = new SpanLabelingEvaluator();
        evaluator.setViews(gold, gold);
        evaluator.evaluate(splittingTester);
        assertEquals(1.0, splittingTester.getAverageF1(), 0);
        assertEquals(1.0, splittingTester.getAverageAccuracy(), 0);
    }

    @Test
    public void testEvaluateNoisySpanLabels() throws Exception {
        SpanLabelingEvaluator evaluator = new SpanLabelingEvaluator();
        evaluator.setViews(gold, predicted);
        evaluator.evaluate(splittingTester);
        assertEquals( splittingTester.getAverageF1(), 0.57, 0.01 );
        assertEquals(0.5, splittingTester.getAverageAccuracy(), 0);
    }
}
