/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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

    /**
     * define a configuration flag to specify behavior w.r.t. hyphens
     */
    public static final String SPLIT_ON_DASH = "splitOnDash";

    String getName();

    /**
     * A method for creating
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation} by
     * tokenizing the given text string.
     * 
     * @param text Raw text string
     */
    TextAnnotation createTextAnnotation(String text) throws IllegalArgumentException;

    /**
     * An overloaded version of {@link #createTextAnnotation(String)} which takes in a corpus Id and
     * text Id. These strings can be used for bookkeeping.
     * 
     * @param corpusId
     * @param textId
     * @param text Raw text string.
     */
    TextAnnotation createTextAnnotation(String corpusId, String textId, String text)
            throws IllegalArgumentException;

    /**
     * A method for creating
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation} by
     * respecting the pre-tokenization of text passed as an instance of
     * {@link edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer.Tokenization}.
     * 
     * @param text Raw text string
     * @param tokenization An instance containing tokens, character offsets, and sentence
     *        boundaries.
     */
    TextAnnotation createTextAnnotation(String corpusId, String textId, String text,
            Tokenizer.Tokenization tokenization) throws IllegalArgumentException;

}
