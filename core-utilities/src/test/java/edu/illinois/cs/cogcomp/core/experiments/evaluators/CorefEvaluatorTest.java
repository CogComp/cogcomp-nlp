/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

// test borrowed from here:
// http://alias-i.com/lingpipe/docs/api/com/aliasi/cluster/ClusterScore.html
// gold = { {1, 2, 3, 4, 5}, {6, 7}, {8, 9, A, B, C} }
// prediction = { { 1, 2, 3, 4, 5, 8, 9, A, B, C }, { 6, 7} }
// MUC: P: 0.9 - R: 1.0 - F: 0.947
// b3Element: P: 0.583 - R: 1.0 - F: 0.737
// b3Cluster: P: 0.75 - R: 1.0 - F: 0.857
public class CorefEvaluatorTest {
    CoreferenceView gold, predicted;

    @Before
    public void setUp() throws Exception {
        String[] toks = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C"};
        List<String[]> tokens = new ArrayList<>();
        tokens.add(toks);
        TextAnnotation dummyTextAnnotation =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
        List<Constituent> allCons = dummyTextAnnotation.getView(ViewNames.TOKENS).getConstituents();
        gold = new CoreferenceView("gold", dummyTextAnnotation);
        gold.addCorefEdges(allCons.get(0), allCons.subList(1, 5));
        gold.addCorefEdges(allCons.get(5), allCons.subList(6, 7));
        gold.addCorefEdges(allCons.get(7), allCons.subList(8, 12));
        dummyTextAnnotation.addView("gold", gold);
        predicted = new CoreferenceView("predicted", dummyTextAnnotation);
        predicted.addCorefEdges(allCons.get(0), allCons.subList(1, 5));
        predicted.addCorefEdges(allCons.get(0), allCons.subList(7, 12));
        predicted.addCorefEdges(allCons.get(5), allCons.subList(6, 7));
        dummyTextAnnotation.addView("predicted", predicted);
    }

    @Test
    public void testScores() throws Exception {
        ClassificationTester mucTester = new ClassificationTester();
        CorefMUCEvaluator mucEvaluator = new CorefMUCEvaluator();
        mucEvaluator.evaluate(mucTester, gold, predicted);
        assertEquals(mucTester.getMacroF1(), 0.94, 0.01);
        assertEquals(mucTester.getMacroPrecision(), 0.9, 0.01);
        assertEquals(mucTester.getMacroRecall(), 1.0, 0.01);

        ClassificationTester bcubedTester = new ClassificationTester();
        CorefBCubedEvaluator bcubedEvaluator = new CorefBCubedEvaluator();
        bcubedEvaluator.evaluate(bcubedTester, gold, predicted);
        assertEquals(bcubedTester.getMacroF1(), 0.73, 0.01);
        assertEquals(bcubedTester.getMacroPrecision(), 0.58, 0.01);
        assertEquals(bcubedTester.getMacroRecall(), 1.0, 0.01);

        ClassificationTester accuracyTester = new ClassificationTester();
        CorefAccuracyEvaluator accuracyEvaluator = new CorefAccuracyEvaluator();
        accuracyEvaluator.evaluate(accuracyTester, gold, predicted);
        assertEquals(accuracyTester.getMacroF1(), 0.68, 0.01);
        assertEquals(accuracyTester.getMacroPrecision(), 0.51, 0.01);
        assertEquals(accuracyTester.getMacroRecall(), 1.0, 0.01);
    }
}
