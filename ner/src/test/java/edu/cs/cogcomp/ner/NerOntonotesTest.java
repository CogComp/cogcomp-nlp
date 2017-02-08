/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.ner;

import edu.cs.cogcomp.annotation.AnnotatorException;
import edu.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.cs.cogcomp.core.datastructures.ViewNames;
import edu.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.cs.cogcomp.core.datastructures.textannotation.View;
import edu.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NerOntonotesTest {


    private static final String TEST_INPUT =
            "The CIA likes to wait til June before it has its big Destroy All Monsters "
                    + " Halloween party.";

    @Test
    public void testOntonotesNer() {
        TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        Properties props = new Properties();
        NERAnnotator nerOntonotes =
                NerAnnotatorManager.buildNerAnnotator(new ResourceManager(props),
                        ViewNames.NER_ONTONOTES);

        TextAnnotation taOnto = tab.createTextAnnotation("", "", TEST_INPUT);

        try {
            nerOntonotes.getView(taOnto);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        View v = taOnto.getView(nerOntonotes.getViewName());

        assertEquals(v.getConstituents().size(), 4);
    }
}
