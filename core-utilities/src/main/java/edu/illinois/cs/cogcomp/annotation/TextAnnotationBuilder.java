package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

/**
 * The interface that will be used by any TextAnnotation creation method that requires tokenization.
 * An implementation using CogComp's default tokenizer can be found in {@code illinois-tokenizer}
 * <p>
 * A class that implements this interface must create two views:
 * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#SENTENCE} and
 * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#TOKENS}.
 * <p>
 * To create a {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}
 * from pre-tokenized text (e.g. from training corpora) please use
 * {@link BasicTextAnnotationBuilder}.
 */
public interface TextAnnotationBuilder {

    String getName();

    TextAnnotation createTextAnnotation(String text) throws IllegalArgumentException;

    TextAnnotation createTextAnnotation(String corpusId, String textId, String text)
            throws IllegalArgumentException;

    TextAnnotation createTextAnnotation(String corpusId, String textId, String text, Tokenizer.Tokenization
            tokenization) throws IllegalArgumentException;

}
