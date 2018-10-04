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

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mssammon on 7/23/17.
 */
public class TestRemoveRelationsConstituents {

    @Test
    public void testRemoveRelation() {
        String[] views = new String[]{ViewNames.SRL_VERB};
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(views, false, 2);

        PredicateArgumentView srl  = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
        Relation r = srl.getPredicates().get(0).getOutgoingRelations().get(0);

        Constituent source = r.getSource();
        Constituent target = r.getTarget();

        assertTrue(source.getOutgoingRelations().contains(r));
        assertTrue(target.getIncomingRelations().contains(r));

        srl.removeRelation(r);

        assertFalse(srl.getRelations().contains(r));
        assertFalse(source.getOutgoingRelations().contains(r));
        assertFalse(target.getIncomingRelations().contains(r));
    }


    @Test
    public void testRemoveConstituent() {
        String[] views = new String[]{ViewNames.SRL_VERB};
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(views, false, 2);

        PredicateArgumentView srl  = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
        Relation r = srl.getPredicates().get(0).getOutgoingRelations().get(0);

        Constituent source = r.getSource();
        Constituent target = r.getTarget();

        srl.removeConstituent(target);

        assertFalse(srl.containsConstituent(target));
        assertFalse(srl.getRelations().contains(r));

    }


}
