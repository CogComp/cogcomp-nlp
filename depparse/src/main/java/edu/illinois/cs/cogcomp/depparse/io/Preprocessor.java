/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse.io;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Preprocessor {
    private static Logger logger = LoggerFactory.getLogger(Preprocessor.class);
    private Annotator pos, lemma, chunk;

    public Preprocessor() {
        pos = new POSAnnotator();
        lemma = new IllinoisLemmatizer();
        chunk = new ChunkerAnnotator();

        logger.info("Finished loading preprocessing pipeline");
    }

    TextAnnotation annotate(String corpusId, String sentId, String[] tokens)
            throws AnnotatorException {
        // Ignore the root token
        List<String[]> words =
                Collections.singletonList(Arrays.copyOfRange(tokens, 1, tokens.length));
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(corpusId, sentId, words);
        ta.addView(pos);
        ta.addView(lemma);
        ta.addView(chunk);
        return ta;
    }

    public void annotate(TextAnnotation ta) throws AnnotatorException {
        ta.addView(pos);
        ta.addView(lemma);
        ta.addView(chunk);
    }
}
