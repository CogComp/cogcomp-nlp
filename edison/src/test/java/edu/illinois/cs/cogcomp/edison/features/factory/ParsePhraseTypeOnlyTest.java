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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParsePhraseTypeOnlyTest {

    private Constituent predicate, arg1, arg2;

    @Before
    public void setUp() throws Exception {
        String[] viewsToAdd =
                {ViewNames.PARSE_STANFORD, ViewNames.PARSE_CHARNIAK, ViewNames.PARSE_GOLD,
                        ViewNames.SRL_VERB};
        TextAnnotation ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);
        PredicateArgumentView srlView = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
        predicate = srlView.getPredicates().get(0);
        arg1 = srlView.getArguments(predicate).get(0).getTarget();
        arg2 = srlView.getArguments(predicate).get(1).getTarget();
    }

    @Test
    public void testParsePhraseCharniak() throws Exception {
        ParsePhraseTypeOnly charniak = ParsePhraseTypeOnly.CHARNIAK;
        assertEquals("[VBD]", charniak.getFeatures(predicate).toString());
        List<String> sortedList = new ArrayList<String>();
        sortedList.add(charniak.getFeatures(arg1).toString());
        sortedList.add(charniak.getFeatures(arg2).toString());
        Collections.sort(sortedList);
        assertEquals("[NP]", sortedList.get(0));
        assertEquals("[PP]", sortedList.get(1));
    }

    @Test
    public void testParsePhraseStanford() throws Exception {
        ParsePhraseTypeOnly stanford = ParsePhraseTypeOnly.STANFORD;
        assertEquals("[VBD]", stanford.getFeatures(predicate).toString());
        List<String> sortedList = new ArrayList<String>();
        sortedList.add(stanford.getFeatures(arg1).toString());
        sortedList.add(stanford.getFeatures(arg2).toString());
        Collections.sort(sortedList);
        assertEquals("[NP]", sortedList.get(0));
        assertEquals("[PP]", sortedList.get(1));
    }

    @Test
    public void testParsePhraseGold() throws Exception {
        ParsePhraseTypeOnly gold = new ParsePhraseTypeOnly(ViewNames.PARSE_GOLD);
        assertEquals("[VBD]", gold.getFeatures(predicate).toString());
        List<String> sortedList = new ArrayList<String>();
        sortedList.add(gold.getFeatures(arg1).toString());
        sortedList.add(gold.getFeatures(arg2).toString());
        Collections.sort(sortedList);
        assertEquals("[NP]", sortedList.get(0));
        assertEquals("[PP]", sortedList.get(1));
    }
}