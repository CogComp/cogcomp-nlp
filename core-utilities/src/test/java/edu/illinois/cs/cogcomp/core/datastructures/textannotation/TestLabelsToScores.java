/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * tests that exercise Constituent and Relation functionality relating to labels and scores.
 */
public class TestLabelsToScores {

    @Test
    public void testExistingLabels()
    {
        TextAnnotation ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[]{ViewNames.SRL_VERB}, false, 1);

        Constituent pred = ((PredicateArgumentView) ta.getView(ViewNames.SRL_VERB)).getPredicates().get(0);

        Map<String, Double> labelsToScores = pred.getLabelsToScores();

        assertNull(labelsToScores);

        Relation rel = pred.getOutgoingRelations().get(0);

        labelsToScores = rel.getLabelsToScores();

        assertNull(labelsToScores);

    }


    @Test
    public void testNewLabels()
    {
        TextAnnotation ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[]{ViewNames.POS}, false, 1);

        View rhymeView = new View("rhyme", "test", ta, 0.4 );

        Map< String, Double > newLabelsToScores = new TreeMap< String, Double >();
        String[] labels = { "eeny", "meeny", "miny", "mo" };
        double[] scores = { 0.15, 0.15, 0.3, 0.4 };

        for ( int i = 0; i < labels.length; ++i )
            newLabelsToScores.put(labels[i], scores[i]);

        Constituent first = new Constituent( newLabelsToScores, "rhyme", ta, 2, 4 );

        assertEquals( "mo", first.getLabel() );
        /**
         * check constituent has own copy of label dist
         */

        newLabelsToScores.clear();

        Map<String, Double> storedLabelsToScores = first.getLabelsToScores();

        assertEquals( storedLabelsToScores.size(), 4 );

        assertEquals( storedLabelsToScores.get("eeny"), 0.15, 0.01);

        /**
         * verify that changing returned map doesn't affect original
         */
        storedLabelsToScores.put( "MAURICE", 10000.0 );
        storedLabelsToScores.put( "eeny", -1.0 );

        assertEquals( "mo", first.getLabel() );

        storedLabelsToScores = first.getLabelsToScores();

        assertEquals( storedLabelsToScores.size(), 4 );

        assertEquals( storedLabelsToScores.get("eeny"), 0.15, 0.01);

        rhymeView.addConstituent(first);

        /**
         * no constraint on scores -- don't have to sum to 1.0
         */
        for ( int i = labels.length -1; i > 0; --i )
            newLabelsToScores.put( labels[i], scores[3-i] );

        Constituent second = new Constituent( newLabelsToScores, "rhyme", ta, 2, 4 );

        storedLabelsToScores = second.getLabelsToScores();

        assertEquals( storedLabelsToScores.size(), 3 );

        assertEquals( storedLabelsToScores.get("meeny"), 0.3, 0.01 );

        rhymeView.addConstituent(second);

        Map<String, Double> relLabelsToScores = new TreeMap<>();
        relLabelsToScores.put( "Yes", 0.8 );
        relLabelsToScores.put( "No", 0.2 );

        Relation rel = new Relation( relLabelsToScores, first, second );
        rhymeView.addRelation(rel);

        relLabelsToScores.clear();

        storedLabelsToScores = rel.getLabelsToScores();

        assertEquals( storedLabelsToScores.size(), 2 );

        assertEquals(storedLabelsToScores.get("No"), 0.2, 0.001 );

        assertEquals( "Yes", rel.getRelationName() );

        storedLabelsToScores.put( "MAYBE", 3.0 );

        assertEquals( "Yes", rel.getRelationName() );

        storedLabelsToScores = rel.getLabelsToScores();

        assertEquals( storedLabelsToScores.size(), 2 );

        assertEquals( storedLabelsToScores.get("No"), 0.2, 0.001 );
    }


    @Test
    public void testHashCode()
    {
        TextAnnotation ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[]{ViewNames.POS}, false, 1);

        Map< String, Double > newLabelsToScores = new TreeMap< String, Double >();
        String[] labels = { "eeny", "meeny", "miny", "mo" };
        double[] scores = { 0.15, 0.15, 0.3, 0.4 };

        for ( int i = 0; i < labels.length; ++i )
            newLabelsToScores.put(labels[i], scores[i]);

        Constituent first = new Constituent( newLabelsToScores, "rhyme", ta, 2, 4 );
        Constituent firstDup = new Constituent( newLabelsToScores, "rhyme", ta, 2, 4 );

        assertEquals(first, firstDup);
        assertEquals(first.hashCode(), firstDup.hashCode());

        /**
         * no constraint on scores -- don't have to sum to 1.0
         */
        for ( int i = 0; i < labels.length - 1; ++i )
            newLabelsToScores.put( labels[i], scores[i] );

        newLabelsToScores.put( "mo", 0.41 );

        Constituent second = new Constituent( newLabelsToScores, "rhyme", ta, 2, 4 );

        assertFalse(second.equals(first));

        assertFalse(second.hashCode() == first.hashCode());
    }
}
