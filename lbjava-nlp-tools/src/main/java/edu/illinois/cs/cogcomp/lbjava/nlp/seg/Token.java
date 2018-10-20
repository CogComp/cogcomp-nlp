/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp.seg;

import edu.illinois.cs.cogcomp.lbjava.nlp.Word;


/**
 * Simple extension of the {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word} class from LBJava's NLP
 * library.  Two extra fields are provided to store the token's type.  First,
 * {@link #type} is intended to store the type of the token <i>as computed by
 * some classifier</i>.  Second, {@link #label} is intended for use only when
 * the data is labeled.
 *
 * @author Nick Rizzolo
 **/
public class Token extends Word {
    /**
     * This field is used to store a computed type tag.
     */
    public String type;
    /**
     * This field stores the type tag found in labeled data.
     */
    public String label;


    /**
     * A <code>Token</code> can be constructed from a <code>Word</code>
     * object representing the same word, a <code>Token</code> representing
     * the previous word in the sentence, and the type label found in the data.
     *
     * @param w    Represents the same word as the <code>Token</code> being
     *             constructed.
     * @param p    The previous word in the sentence.
     * @param type The type label for this word from the data.
     **/
    public Token(Word w, Token p, String type) {
        super(w.form, w.partOfSpeech, w.lemma, w.wordSense, p, w.start, w.end);
        label = type;
    }

    /**
     * Produces a simple <code>String</code> representation of this word in
     * which the {@link #label} field appears followed by the word's part
     * of speech and finally the form (i.e., spelling) of the word all
     * surrounded by parentheses.
     **/
    public String toString() {
        return "(" + label + " " + partOfSpeech + " " + form + ")";
    }
}

