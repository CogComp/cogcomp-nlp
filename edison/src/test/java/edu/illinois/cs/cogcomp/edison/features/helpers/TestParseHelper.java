/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestParseHelper {

    private TextAnnotation ta;
    private String parserView;

    @Before
    public void setUp() {
        parserView = ViewNames.PARSE_STANFORD;
        ta = TextAnnotationUtilities.createFromTokenizedString("A man with a bag is walking .");
        TreeView treeView = new TreeView(parserView, "test", ta, 1.0);
        treeView.setParseTree(0, Tree
                .readTreeFromString("(S1 (S (NP (NP (DT A) (NN man)) (PP (IN with) "
                        + "(NP (DT a) (NN bag)))) (VP (VBZ is) (VP (VBG walking))) (. .)))"));
        ta.addView(parserView, treeView);

        PredicateArgumentView prepSRLView =
                new PredicateArgumentView(ViewNames.SRL_PREP, "test", ta, 1.0);
        Constituent predicate = new Constituent("with", ViewNames.SRL_PREP, ta, 2, 3);
        predicate.addAttribute(PredicateArgumentView.LemmaIdentifier, "Attribute");
        Constituent argGov = new Constituent("", ViewNames.SRL_PREP, ta, 0, 2);
        // The object is just the head ("bag")
        Constituent argObj = new Constituent("", ViewNames.SRL_PREP, ta, 4, 5);
        prepSRLView.addConstituent(predicate);
        prepSRLView.addConstituent(argGov);
        prepSRLView.addConstituent(argObj);
        prepSRLView.addRelation(new Relation("Governor", predicate, argGov, 1.0));
        prepSRLView.addRelation(new Relation("Object", predicate, argObj, 1.0));
        ta.addView(ViewNames.SRL_PREP, prepSRLView);
    }

    @Test
    public void testGetPhraseFromHead() {
        PredicateArgumentView prepSRLView =
                new PredicateArgumentView(ViewNames.SRL_PREP, "test", ta, 1.0);
        Constituent predicate = new Constituent("with", ViewNames.SRL_PREP, ta, 2, 3);
        predicate.addAttribute(PredicateArgumentView.LemmaIdentifier, "Attribute");
        Constituent argGov = new Constituent("", ViewNames.SRL_PREP, ta, 0, 2);
        // The object is just the head ("bag")
        Constituent argObj = new Constituent("", ViewNames.SRL_PREP, ta, 4, 5);
        prepSRLView.addConstituent(predicate);
        prepSRLView.addConstituent(argGov);
        prepSRLView.addConstituent(argObj);
        prepSRLView.addRelation(new Relation("Governor", predicate, argGov, 1.0));
        prepSRLView.addRelation(new Relation("Object", predicate, argObj, 1.0));
        ta.addView(ViewNames.SRL_PREP, prepSRLView);

        Constituent argPhrase = ParseHelper.getPhraseFromHead(predicate, argObj, parserView);
        assert argPhrase != null;

        assertEquals("bag", argObj.toString());
        assertEquals("a bag", argPhrase.toString());
    }
}
