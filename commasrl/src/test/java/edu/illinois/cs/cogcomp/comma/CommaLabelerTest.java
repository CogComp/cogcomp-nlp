/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.comma.annotators.PreProcessor;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import junit.framework.TestCase;

import java.util.Collections;

public class CommaLabelerTest extends TestCase {
    private TextAnnotation ta;
    private String untokenizedText;
    private CommaLabeler annotator = new CommaLabeler();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        untokenizedText = "Mary, the clever scientist, was walking.";
        String tokenizedText = "Mary , the clever scientist , was walking .";
        String[] sentence = tokenizedText.split("\\s+");
        ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections
                        .singletonList(sentence));

        TokenLabelView tlv = new TokenLabelView(ViewNames.POS, "Test", ta, 1.0);
        tlv.addTokenLabel(0, "NNP", 1d);
        tlv.addTokenLabel(1, ",", 1d);
        tlv.addTokenLabel(2, "DT", 1d);
        tlv.addTokenLabel(3, "JJ", 1d);
        tlv.addTokenLabel(4, "NN", 1d);
        tlv.addTokenLabel(5, ",", 1d);
        tlv.addTokenLabel(6, "VBD", 1d);
        tlv.addTokenLabel(7, "VBG", 1d);
        tlv.addTokenLabel(8, ". ", 1d);


        TreeView parse = new TreeView(ViewNames.PARSE_STANFORD, "Test", ta, 1.0);
        String treeString =
                "(ROOT (S (NP (NP (NNP Mary)) (, ,) (NP (DT the) (JJ clever) (NN scientist)) (, ,)) "
                        + "(VP (VBD was) (VP (VBG walking)))  (. .)))";
        parse.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(treeString));

        SpanLabelView ner = new SpanLabelView(ViewNames.NER_CONLL, "Test", ta, 1.0);
        ner.addSpanLabel(0, 1, "PER", 1.0);

        SpanLabelView shallowParse = new SpanLabelView(ViewNames.SHALLOW_PARSE, "Test", ta, 1.0);
        shallowParse.addSpanLabel(0, 1, "NP", 1.0);
        shallowParse.addSpanLabel(2, 5, "NP", 1.0);
        shallowParse.addSpanLabel(6, 8, "VP", 1.0);

        // TODO dependency parse
        // TODO SRL view

        ta.addView(tlv.getViewName(), tlv);
        ta.addView(parse.getViewName(), parse);
        ta.addView(ner.getViewName(), ner);
        ta.addView(shallowParse.getViewName(), shallowParse);
    }

    public void testGetCommaSRLFromTA() throws AnnotatorException {
        // Create the Comma structure
        PredicateArgumentView srlView = (PredicateArgumentView) annotator.getView(ta);
        assertEquals(2, srlView.getPredicates().size());
        Constituent pred1 = srlView.getPredicates().get(0);
        assertEquals("Substitute", srlView.getPredicateSense(pred1));
        assertEquals(2, srlView.getArguments(pred1).size());
        assertEquals("Mary", srlView.getArguments(pred1).get(0).getTarget().getSurfaceForm());
        Constituent pred2 = srlView.getPredicates().get(1);
        assertEquals(1, srlView.getArguments(pred2).size());
        assertEquals("LeftOfSubstitute", srlView.getArguments(pred2).get(0).getRelationName());
    }

    public void testGetCommaSRLFromPlainText() throws Exception {
        PreProcessor preProcessor = new PreProcessor();
        TextAnnotation ta = preProcessor.preProcess(untokenizedText);
        PredicateArgumentView srlView = (PredicateArgumentView) annotator.getView(ta);
        assertEquals(2, srlView.getPredicates().size());
        Constituent pred1 = srlView.getPredicates().get(0);
        assertEquals("Substitute", srlView.getPredicateSense(pred1));
        assertEquals(2, srlView.getArguments(pred1).size());
        assertEquals("Mary", srlView.getArguments(pred1).get(0).getTarget().getSurfaceForm());
        Constituent pred2 = srlView.getPredicates().get(1);
        assertEquals(1, srlView.getArguments(pred2).size());
        assertEquals("LeftOfSubstitute", srlView.getArguments(pred2).get(0).getRelationName());
    }

}
