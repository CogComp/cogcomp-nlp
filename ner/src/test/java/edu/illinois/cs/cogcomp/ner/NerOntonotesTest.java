/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
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

        assertEquals(3, v.getConstituents().size());
    }
}
