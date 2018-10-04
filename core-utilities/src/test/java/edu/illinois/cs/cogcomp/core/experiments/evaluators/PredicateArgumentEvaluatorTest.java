/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
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
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);
        TextAnnotation taPred =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);
        gold = (PredicateArgumentView) taGold.getView(ViewNames.SRL_VERB);
        predicted = (PredicateArgumentView) taPred.getView(ViewNames.SRL_VERB);
    }

    @Test
    public void testEvaluateSense() throws Exception {
        PredicateArgumentEvaluator evaluator = new PredicateArgumentEvaluator();
        evaluator.evaluateSense(senseTester, gold, predicted);
        assertEquals(1.0, senseTester.getMicroF1(), 0);

        // Override the sense
        predicted.getPredicates().get(0).addAttribute(PredicateArgumentView.SenseIdentifer, "02");
        evaluator = new PredicateArgumentEvaluator();
        evaluator.evaluateSense(senseTester, gold, predicted);
        assertEquals(0.875, senseTester.getMicroF1(), 0.01);
    }

    @Test
    public void testEvaluate() throws Exception {
        PredicateArgumentEvaluator evaluator = new PredicateArgumentEvaluator();
        evaluator.evaluate(argLabelTester, gold, predicted);
        assertEquals(1.0, argLabelTester.getMicroF1(), 0);

        // Add a wrong prediction
        Constituent predicate = predicted.getPredicates().get(0);
        Constituent argument =
                new Constituent("test", ViewNames.SRL_VERB, predicate.getTextAnnotation(), 0, 1);
        predicted.addRelation(new Relation("test", predicate, argument, 0));
        evaluator = new PredicateArgumentEvaluator();
        evaluator.evaluate(argLabelTester, gold, predicted);
        assertEquals(0.92, argLabelTester.getMicroF1(), 0.1);
    }
}
