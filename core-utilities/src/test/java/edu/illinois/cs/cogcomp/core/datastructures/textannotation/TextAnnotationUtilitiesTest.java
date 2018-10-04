/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * for test of
 * mapTransformedTextAnnotationToSource(TextAnnotation ta,
 StringTransformation st)
 *
 * see corpusreaders test:
 * edu.illinois.cs.cogcomp.nlp.corpusreaders.EREReaderTest
 */
public class TextAnnotationUtilitiesTest {

    /**
     * note that this test checks getSubTextAnnotation()'s behavior when a sentence has no constituents in
     *   a particular view for a particular sentence (view is not generated in sentence level version)
     */
    @Test
    public void test() {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        TextAnnotation subTA = TextAnnotationUtilities.getSubTextAnnotation(ta, 2);
        assertTrue(subTA.getText().equals("The paving commenced Monday and will finish in June ."));
        assertTrue(Objects.equals(subTA.getAvailableViews().toString(), "[SRL_VERB, PSEUDO_PARSE_STANFORD, POS, NER_CONLL, LEMMA, SHALLOW_PARSE, TOKENS, SENTENCE, PARSE_GOLD]"));
        assertTrue(Objects.equals(subTA.getView(ViewNames.SHALLOW_PARSE).toString(), "[NP The paving ] [VP commenced ] [NP Monday ] [VP will finish ] [PP in ] [NP June ] "));
        String parse = "(S1 (S (NP (DT The)\n" +
                "    (NN paving))\n" +
                "   (VP (VP (VBD commenced)\n" +
                "    (NP (NNP Monday)))\n" +
                "       (CC and)\n" +
                "       (VP (MD will)\n" +
                "           (VP (VB finish)\n" +
                "               (PP (IN in)\n" +
                "                   (NP (NNP June))))))\n" +
                "   (. .)))";

        String subTaStr = subTA.getView(ViewNames.PARSE_GOLD).toString().trim();

        int subTaOffset = ta.getSentence(2).getStartSpan();

        assertTrue(Objects.equals(subTaStr, parse));

        String[] viewsToAdd = new String[]{};

        TextAnnotation emptyTa = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);

        Set<IntPair> srlSpans = new HashSet<>();
        for (Constituent c : subTA.getView(ViewNames.SRL_VERB).getConstituents())
            srlSpans.add(c.getSpan());

        TextAnnotationUtilities.mapSentenceAnnotationsToText(subTA, emptyTa, 2);

        View srlView = emptyTa.getView(ViewNames.SRL_VERB);
        for (Constituent c : srlView.getConstituents()) {
            IntPair cSpan = c.getSpan();
            IntPair adjustedSpan = new IntPair(cSpan.getFirst() - subTaOffset, cSpan.getSecond() - subTaOffset);
            assertTrue(srlSpans.contains(adjustedSpan));
        }

        TreeView parseView = (TreeView) emptyTa.getView(ViewNames.PARSE_GOLD);
        String mappedParse = parseView.toString().trim();
        assertEquals(parse, mappedParse);
    }
}
