/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import junit.framework.TestCase;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;

public class CommaTest extends TestCase {
    private Comma[] commas;
    private CommaProperties properties = CommaProperties.getInstance();

    @Override
    public void setUp() throws Exception {
        String[] tokens =
                "Says Gayle Key , a mathematics teacher , `` Hello world . ''".split("\\s+");
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections
                        .singletonList(tokens));

        TokenLabelView tlv = new TokenLabelView(ViewNames.POS, "Test", ta, 1.0);
        tlv.addTokenLabel(0, "VBZ", 1d);
        tlv.addTokenLabel(1, "NNP", 1d);
        tlv.addTokenLabel(2, "NNP", 1d);
        tlv.addTokenLabel(3, ",", 1d);
        tlv.addTokenLabel(4, "DT", 1d);
        tlv.addTokenLabel(5, "NN", 1d);
        tlv.addTokenLabel(6, "NN", 1d);
        tlv.addTokenLabel(7, ",", 1d);
        tlv.addTokenLabel(8, "``", 1d);
        tlv.addTokenLabel(9, "UH", 1d);
        tlv.addTokenLabel(10, "NN", 1d);
        tlv.addTokenLabel(11, ".", 1d);
        tlv.addTokenLabel(12, "''", 1d);


        TreeView parse = new TreeView(ViewNames.PARSE_STANFORD, "Test", ta, 1.0);
        String treeString =
                "(ROOT" + "  (SINV" + "    (VP (VBZ Says))" + "    (NP (NNP Gayle) (NNP Key))"
                        + "    (, ,)" + "    (S" + "      (NP (DT a) (NNS mathematics))"
                        + "      (VP (VBZ teacher) (, ,) (`` ``)" + "        (NP"
                        + "          (INTJ (UH Hello))" + "          (NP (NN world)))))"
                        + "    (. .) ('' '')))";
        parse.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(treeString));

        SpanLabelView ner = new SpanLabelView(ViewNames.NER_CONLL, "Test", ta, 1.0);
        ner.addSpanLabel(1, 3, "PER", 1.0);

        SpanLabelView shallowParse = new SpanLabelView(ViewNames.SHALLOW_PARSE, "Test", ta, 1.0);
        shallowParse.addSpanLabel(0, 3, "NP", 1.0);
        shallowParse.addSpanLabel(4, 7, "NP", 1.0);
        shallowParse.addSpanLabel(9, 11, "NP", 1.0);

        // TODO dependency parse
        // TODO SRL view

        ta.addView(tlv.getViewName(), tlv);
        ta.addView(parse.getViewName(), parse);
        ta.addView(ner.getViewName(), ner);
        ta.addView(shallowParse.getViewName(), shallowParse);

        List<String> firstCommasRefinedLabels = Collections.singletonList("Substitute");
        List<String> secondCommasRefinedLabels = Arrays.asList("Substitute", "Quotation");

        CommaSRLSentence sentence =
                new CommaSRLSentence(ta, null, Arrays.asList(firstCommasRefinedLabels,
                        secondCommasRefinedLabels));
        List<Comma> sentenceCommas = sentence.getCommas();
        commas = sentenceCommas.toArray(new Comma[sentenceCommas.size()]);
    }


    public void testLabels() {
        assertEquals("Substitute", commas[0].getLabel());
        if (properties.allowMultiLabelCommas()) {
            assertEquals(Arrays.asList("Substitute", "Quotation"), commas[1].getLabels());
        } else {
            assertEquals("Substitute", commas[1].getLabel());
            assertEquals("Quotation", commas[2].getLabel());
        }
    }


    public void testMultiLabelCommas() {
        int expectedNumberOfCommas;
        if (properties.allowMultiLabelCommas())
            expectedNumberOfCommas = 2;
        else
            expectedNumberOfCommas = 3;
        assertEquals(expectedNumberOfCommas, commas.length);
    }


    public void testBayraktarPattern() {
        assertEquals("SINV --> VP NP , S . ''", commas[0].getBayraktarPattern());
        assertEquals("VP --> *** , `` NP", commas[1].getBayraktarPattern());
    }


    public void testGetWord() {
        assertEquals("teacher", commas[1].getWordToLeft(1));
        assertEquals("$$$", commas[1].getWordToLeft(8));
        assertEquals(",", commas[1].getWordToLeft(0));
        assertEquals("``", commas[1].getWordToRight(1));
        assertEquals("Hello", commas[1].getWordToRight(2));
        assertEquals("###", commas[1].getWordToRight(1000));
    }


    public void testGetPOS() {
        assertEquals("NN", commas[1].getPOSToLeft(1));
        assertEquals("NN", commas[1].getPOSToLeft(2));
        assertEquals("``", commas[1].getPOSToRight(1));
        assertEquals("UH", commas[1].getPOSToRight(2));
        assertEquals("", commas[1].getPOSToRight(1000));
    }


    public void testGetChunk() {
        assertEquals("NP", commas[1].getChunkToLeftOfComma(1).getLabel());
        assertEquals("NP", commas[1].getChunkToLeftOfComma(2).getLabel());
        assertEquals("NP", commas[1].getChunkToRightOfComma(1).getLabel());
        assertEquals(null, commas[1].getChunkToRightOfComma(3));
        assertEquals(null, commas[1].getChunkToRightOfComma(0));
        assertEquals(null, commas[1].getChunkToLeftOfComma(-1));
        assertEquals(null, commas[1].getChunkToLeftOfComma(4));
    }


    public void testGetSiblingPhrase() {
        assertEquals("VBZ", commas[1].getPhraseToLeftOfComma(1).getLabel());
        assertEquals("``", commas[1].getPhraseToRightOfComma(1).getLabel());
        assertEquals("NP", commas[1].getPhraseToRightOfComma(2).getLabel());
        assertEquals(null, commas[1].getPhraseToLeftOfComma(2));
        assertEquals(null, commas[1].getPhraseToLeftOfComma(3));
        assertEquals(null, commas[1].getPhraseToLeftOfComma(4));
        assertEquals(null, commas[1].getPhraseToRightOfComma(3));
    }


    public void testGetParentSiblingPhrase() {
        assertEquals("VP", commas[1].getPhraseToLeftOfParent(0).getLabel());
        assertEquals("NP", commas[1].getPhraseToLeftOfParent(1).getLabel());
        assertEquals(null, commas[1].getPhraseToLeftOfParent(2));
        assertEquals(null, commas[1].getPhraseToRightOfParent(1));
    }

    // TODO testGetLeftTORightDependencies
    // TODO testGetRightToLeftDependencies
    // TODO testGetSRLFeatures
}
