package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PredicateArgumentEvaluatorTest {
    PredicateArgumentView gold, predicted;
    ClassificationTester senseTester, argLabelTester;

    @Before
    public void setUp() throws Exception {
        senseTester = new ClassificationTester();
        argLabelTester = new ClassificationTester();

        String[] viewsToAdd = {ViewNames.SRL_VERB};
        TextAnnotation taGold =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
        TextAnnotation taPred =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
        gold = (PredicateArgumentView) taGold.getView(ViewNames.SRL_VERB);
        predicted = (PredicateArgumentView) taPred.getView(ViewNames.SRL_VERB);
    }

    @Test
    public void testEvaluateSense() throws Exception {
        PredicateArgumentEvaluator evaluator = new PredicateArgumentEvaluator();
        evaluator.setViews(gold, predicted);
        evaluator.evaluateSense(senseTester);
        assertEquals(1.0, senseTester.getAverageF1(), 0);

        // Override the sense
        predicted.getPredicates().get(0).addAttribute(CoNLLColumnFormatReader.SenseIdentifer, "02");
        evaluator = new PredicateArgumentEvaluator();
        evaluator.setViews(gold, predicted);
        evaluator.evaluateSense(senseTester);
        assertEquals(0.5, senseTester.getAverageF1(), 0);
    }

    @Test
    public void testEvaluate() throws Exception {
        PredicateArgumentEvaluator evaluator = new PredicateArgumentEvaluator();
        evaluator.setViews(gold, predicted);
        evaluator.evaluate(argLabelTester);
        assertEquals(1.0, argLabelTester.getAverageF1(), 0);

        // Add a wrong prediction
        Constituent predicate = predicted.getPredicates().get(0);
        Constituent argument =
                new Constituent("test", ViewNames.SRL_VERB, predicate.getTextAnnotation(), 0, 1);
        predicted.addRelation(new Relation("test", predicate, argument, 0));
        evaluator = new PredicateArgumentEvaluator();
        evaluator.setViews(gold, predicted);
        evaluator.evaluate(argLabelTester);
        assertEquals(0.92, argLabelTester.getAverageF1(), 0.1);
    }
}
