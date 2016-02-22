package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class NerOntonotesTest {


    private static final String TEST_INPUT =
            "The CIA likes to wait til June before it has its big Destroy All Monsters "
                    + " Halloween party.";

    @Test
    public void testOntonotesNer() {
        TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
        Properties props = new Properties();
        NERAnnotator nerOntonotes =
                NerAnnotatorManager.buildNerAnnotator(new ResourceManager(props),
                        ViewNames.NER_ONTONOTES);

        TextAnnotation taOnto = tab.createTextAnnotation("", "", TEST_INPUT);

        nerOntonotes.addView(taOnto);
        View v = taOnto.getView(nerOntonotes.getViewName());

        assertEquals(v.getConstituents().size(), 3);
    }
}
