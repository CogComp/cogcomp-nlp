/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

/**
 * These are the types of tokens we deal with.
 * 
 * @author redman
 */
public enum TokenType {

    /** any kind of punctuation. */
    PUNCTUATION,

    /** printable text characters. */
    TEXT,

    /** whitespace of any kind. */
    WHITESPACE,

    /** unprintable characters. */
    UNPRINTABLE

}
