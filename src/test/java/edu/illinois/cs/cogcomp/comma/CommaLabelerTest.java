package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.comma.lbj.CommaClassifier;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.edison.sentences.*;
import junit.framework.TestCase;

import java.util.Arrays;

public class CommaLabelerTest extends TestCase {
    private CommaClassifier classifier;
    private TextAnnotation ta;

    public void setUp() throws Exception {
        super.setUp();
        classifier = new CommaClassifier();
        ta = new TextAnnotation("","", Arrays.asList("Mary , the clever scientist , was walking ."));

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
        ta.addView(ViewNames.POS, tlv);

        TreeView parse = new TreeView(ViewNames.PARSE_STANFORD, "Test", ta, 1.0);
        String treeString = "(ROOT (S (NP (NP (NNP Mary)) (, ,) (NP (DT the) (JJ clever) (NN scientist)) (, ,)) " +
                "(VP (VBD was) (VP (VBG walking)))  (. .)))";
        parse.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(treeString));
        ta.addView(parse.getViewName(), parse);
    }

    public void testGetCommaSRL() {
        // Create the Comma structure
        PredicateArgumentView srlView = new PredicateArgumentView("SRL_COMMA", "Test", ta, 1.0d);
        String text = ta.getText();
        for (Constituent comma : ta.getView(ViewNames.POS).getConstituents()) {
            if (!comma.getLabel().equals(",")) continue;
            Comma commaStruct = new Comma(comma.getStartSpan(), text, ta);
            String label = classifier.discreteValue(classifier.classify(commaStruct));
            Constituent predicate = new Constituent(label, "SRL_COMMA", ta, comma.getStartSpan(), comma.getEndSpan());
            srlView.addConstituent(predicate);
            Constituent leftArg = commaStruct.getPhraseToLeftOfComma(1);
            if (leftArg != null) {
                Constituent leftArgConst = new Constituent(leftArg.getLabel(), "SRL_COMMA", ta,
                        leftArg.getStartSpan(), leftArg.getEndSpan());
                srlView.addConstituent(leftArgConst);
                srlView.addRelation(new Relation("LeftOf" + label, predicate, leftArgConst, 1.0d));
            }
            Constituent rightArg = commaStruct.getPhraseToRightOfComma(1);
            if (rightArg != null) {
                Constituent rightArgConst = new Constituent(rightArg.getLabel(), "SRL_COMMA", ta,
                        rightArg.getStartSpan(), rightArg.getEndSpan());
                srlView.addConstituent(rightArgConst);
                srlView.addRelation(new Relation("RightOf" + label, predicate, rightArgConst, 1.0d));
            }
        }
        assertEquals(",:\n    LeftOfOther: Mary\n    RightOfOther: the clever scientist\n,:\n" +
                "    LeftOfOther: the clever scientist\n", srlView.toString());
    }
}