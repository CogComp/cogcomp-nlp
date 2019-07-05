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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * The user may specify in their configuration file the labels they want to keep, if they choose not to 
 * keep all labels. For example if you use a 4 class model yet have no interest in MISC or ORG, you can 
 * specify "PER" and "LOC" in a configuration parameter ("labelsToKeep") to discard the models for MISC and
 * ORG. This will result in a significant performance improvement.
 * @author redman
 *
 */
public class NerLabelPruningTest {

    private static final String TEST_INPUT =
            "JFK has one dog and Newark has a handful, Farbstein said.";

    @Test
    public void testOntonotesNer() {
        TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        Properties props = new Properties();
        // TIME LAW GPE NORP LANGUAGE PERCENT FAC PRODUCT ORDINAL LOC PERSON WORK_OF_ART MONEY DATE EVENT QUANTITY ORG CARDINAL
        props.put(NerBaseConfigurator.LABELS_TO_KEEP, "LOC GPE NORP LANGUAGE FAC PRODUCT PERSON EVENT ORG");
        NERAnnotator nerOntonotes = NerAnnotatorManager.buildNerAnnotator(new ResourceManager(props),
                        ViewNames.NER_ONTONOTES);
        TextAnnotation taOnto = tab.createTextAnnotation("", "", TEST_INPUT);
        try {
            nerOntonotes.getView(taOnto);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        View v = taOnto.getView(nerOntonotes.getViewName());
        for (Constituent c : v.getConstituents()) {
        	System.out.println(c+" = "+c.getLabel()+" : "+c.getConstituentScore());
        }
        assertEquals(3, v.getConstituents().size());
    }
}