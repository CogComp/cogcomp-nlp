package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
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
// MUC: P:	0.9  -  R:	1.0  - F:   0.947
// b3Element:  P:	0.583 -  R:  1.0 - F:  0.737
// b3Cluster:  P:   0.75  -  R:  1.0 - F:  0.857
public class CorefMUCEvaluatorTest {
    CoreferenceView gold, predicted;
    ClassificationTester senseTester, argLabelTester;

    @Before
    public void setUp() throws Exception {
        String[] toks = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C"};
        List<String[]> tokens = new ArrayList<>();
        tokens.add(toks);
        TextAnnotation dummyTextAnnotation = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
        List<Constituent> allCons = dummyTextAnnotation.getView(ViewNames.TOKENS).getConstituents();
        //System.out.println();
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
        System.out.println(predicted);
        System.out.println(predicted.canonicalEntitiesMap);
        System.out.println(predicted.getCanonicalEntitiesViaRelations());
        System.out.println(predicted.canonicalEntitiesMap);
    }

    @Test
    public void testScores() throws Exception {

        for(Constituent c : predicted.getConstituents()) {
            System.out.println(predicted.getCoreferentMentionsViaRelations(c));
        }
        CorefMUCEvaluator mucEvaluator = new CorefMUCEvaluator();
        mucEvaluator.setViews(gold, predicted);
        mucEvaluator.evaluate();

        CorefBCubedEvaluator bcubedEvaluator = new CorefBCubedEvaluator();
        bcubedEvaluator.setViews(gold, predicted);
        bcubedEvaluator.evaluate();
    }
}
