package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Test;

import java.util.Objects;

public class TextAnnotationUtilitiesTest {
    @Test
    public void test() {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        TextAnnotation subTA = TextAnnotationUtilities.getSubTextAnnotation(ta, 2);
        assert subTA.getText().equals("The paving commenced Monday and will finish in June .");
        assert Objects.equals(subTA.getAvailableViews().toString(), "[SRL_VERB, PSEUDO_PARSE_STANFORD, POS, NER_CONLL, LEMMA, SHALLOW_PARSE, TOKENS, SENTENCE, PARSE_GOLD]");
        assert Objects.equals(subTA.getView(ViewNames.SHALLOW_PARSE).toString(), "[NP The paving ] [VP commenced ] [NP Monday ] [VP will finish ] [PP in ] [NP June ] ");
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
        assert Objects.equals(subTA.getView(ViewNames.PARSE_GOLD).toString().trim(), parse);
    }
}
