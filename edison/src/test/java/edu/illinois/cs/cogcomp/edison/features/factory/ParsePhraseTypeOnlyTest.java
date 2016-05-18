/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParsePhraseTypeOnlyTest {

    private Constituent predicate, arg1, arg2;

    @Before
    public void setUp() throws Exception {
        String[] viewsToAdd =
                {ViewNames.PARSE_STANFORD, ViewNames.PARSE_CHARNIAK, ViewNames.PARSE_GOLD,
                        ViewNames.SRL_VERB};
        TextAnnotation ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
        PredicateArgumentView srlView = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
        predicate = srlView.getPredicates().get(0);
        arg1 = srlView.getArguments(predicate).get(0).getTarget();
        arg2 = srlView.getArguments(predicate).get(1).getTarget();
    }

    @Test
    public void testParsePhraseCharniak() throws Exception {
        ParsePhraseTypeOnly charniak = ParsePhraseTypeOnly.CHARNIAK;
        assertEquals("[VBD]", charniak.getFeatures(predicate).toString());
        assertEquals("[PP]", charniak.getFeatures(arg1).toString());
        assertEquals("[NP]", charniak.getFeatures(arg2).toString());
    }

    @Test
    public void testParsePhraseStanford() throws Exception {
        ParsePhraseTypeOnly stanford = ParsePhraseTypeOnly.STANFORD;
        assertEquals("[VBD]", stanford.getFeatures(predicate).toString());
        assertEquals("[PP]", stanford.getFeatures(arg1).toString());
        assertEquals("[NP]", stanford.getFeatures(arg2).toString());
    }

    @Test
    public void testParsePhraseGold() throws Exception {
        ParsePhraseTypeOnly gold = new ParsePhraseTypeOnly(ViewNames.PARSE_GOLD);
        assertEquals("[VBD]", gold.getFeatures(predicate).toString());
        assertEquals("[PP]", gold.getFeatures(arg1).toString());
        assertEquals("[NP]", gold.getFeatures(arg2).toString());
    }
}
