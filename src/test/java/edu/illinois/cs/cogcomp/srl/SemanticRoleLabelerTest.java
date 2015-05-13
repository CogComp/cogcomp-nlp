package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.edison.sentences.*;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import junit.framework.TestCase;

import java.util.Arrays;

public class SemanticRoleLabelerTest extends TestCase {
    private SemanticRoleLabeler verbSRL, nomSRL;
    private String defaultParser;

    public void setUp() throws Exception {
        super.setUp();
        verbSRL = new SemanticRoleLabeler("src/test/resources/srl-config.properties", SRLType.Verb.name());
        nomSRL = new SemanticRoleLabeler("src/test/resources/srl-config.properties", SRLType.Nom.name());
        defaultParser = SRLProperties.getInstance().getDefaultParser();
    }

    public void testVerbSRL() throws Exception {
        TextAnnotation ta = new TextAnnotation("", "", Arrays.asList("I do ."));

        TokenLabelView tlv = new TokenLabelView(ViewNames.POS, "Test", ta, 1.0);
        tlv.addTokenLabel(0, "PRP", 1d);
        tlv.addTokenLabel(1, "VBP", 1d);
        tlv.addTokenLabel(2, ".", 1d);
        ta.addView(ViewNames.POS, tlv);

        ta.addView(ViewNames.NER, new SpanLabelView(ViewNames.NER, "test", ta, 1d));

        SpanLabelView chunks = new SpanLabelView(ViewNames.SHALLOW_PARSE, "test", ta, 1d);
        chunks.addSpanLabel(0, 1, "NP", 1d);
        chunks.addSpanLabel(1, 2, "VP", 1d);
        ta.addView(ViewNames.SHALLOW_PARSE, chunks);

        String parseView = null;
        if (defaultParser.equals("Charniak")) parseView = ViewNames.PARSE_CHARNIAK;
        if (defaultParser.equals("Stanford")) parseView = ViewNames.PARSE_STANFORD;
        if (defaultParser.equals("Berkeley")) parseView = ViewNames.PARSE_BERKELEY;
        TreeView parse = new TreeView(parseView, defaultParser, ta, 1.0);
        parse.setParseTree(0, TreeParserFactory.getStringTreeParser()
                .parse("(S1 (S (NP (PRP I))       (VP (VPB do))        (. .)))"));
        ta.addView(parse.getViewName(), parse);

        TokenLabelView view = new TokenLabelView(ViewNames.LEMMA, "test", ta, 1d);
        view.addTokenLabel(0, "i", 1d);
        view.addTokenLabel(1, "do", 1d);
        view.addTokenLabel(2, ".", 1d);
        ta.addView(ViewNames.LEMMA, view);

        PredicateArgumentView srl = verbSRL.getSRL(ta);

        assertEquals("do:02\n    A0: I\n", srl.toString());
    }

    public void testNomSRL() throws Exception {
        TextAnnotation ta = new TextAnnotation("", "",
                Arrays.asList("The construction of the library is complete ."));

        TokenLabelView tlv = new TokenLabelView(ViewNames.POS, "Test", ta, 1.0);
        tlv.addTokenLabel(0, "DT", 1d);
        tlv.addTokenLabel(1, "NN", 1d);
        tlv.addTokenLabel(2, "IN", 1d);
        tlv.addTokenLabel(3, "DT", 1d);
        tlv.addTokenLabel(4, "NN", 1d);
        tlv.addTokenLabel(5, "VB", 1d);
        tlv.addTokenLabel(6, "JJ", 1d);
        tlv.addTokenLabel(7, ". ", 1d);

        ta.addView(ViewNames.POS, tlv);
        ta.addView(ViewNames.NER, new SpanLabelView(ViewNames.NER, "test", ta, 1d));
        SpanLabelView chunks = new SpanLabelView(ViewNames.SHALLOW_PARSE, "test", ta, 1d);

        chunks.addSpanLabel(0, 2, "NP", 1d);
        chunks.addSpanLabel(2, 3, "PP", 1d);
        chunks.addSpanLabel(3, 5, "NP", 1d);
        chunks.addSpanLabel(5, 6, "VP", 1d);
        chunks.addSpanLabel(6, 7, "ADJP", 1d);

        ta.addView(ViewNames.SHALLOW_PARSE, chunks);

        String parseView = null;
        if (defaultParser.equals("Charniak")) parseView = ViewNames.PARSE_CHARNIAK;
        if (defaultParser.equals("Stanford")) parseView = ViewNames.PARSE_STANFORD;
        if (defaultParser.equals("Berkeley")) parseView = ViewNames.PARSE_BERKELEY;
        TreeView parse = new TreeView(parseView, defaultParser, ta, 1.0);

        String treeString = "(S1 (S (NP (NP (DT The) (NN construction)) (PP (IN of) (NP (DT the) (NN library)))) " +
                "(VP (AUX is) (ADJP (JJ complete))) (. .)))";
        parse.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(treeString));
        ta.addView(parse.getViewName(), parse);

        TokenLabelView view = new TokenLabelView(ViewNames.LEMMA, "test", ta, 1d);
        view.addTokenLabel(0, "the", 1d);
        view.addTokenLabel(1, "construction", 1d);
        view.addTokenLabel(2, "of", 1d);
        view.addTokenLabel(3, "the", 1d);
        view.addTokenLabel(4, "library", 1d);
        view.addTokenLabel(5, "be", 1d);
        view.addTokenLabel(6, "complete", 1d);
        view.addTokenLabel(7, ".", 1d);
        ta.addView(ViewNames.LEMMA, view);

        PredicateArgumentView srl = nomSRL.getSRL(ta);

        assertEquals("construction:01\n    A1: of the library\n", srl.toString());
    }
}