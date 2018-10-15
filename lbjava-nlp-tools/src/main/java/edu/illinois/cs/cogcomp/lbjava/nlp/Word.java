/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedChild;


/**
 * Implementation of a word for natural language processing.  Please note
 * that in general, one can only count on the <code>form</code> and
 * <code>capitalized</code> fields described below having meaningful values.
 * The <code>form</code> field can be assumed to be filled in because it's
 * hard to imagine a situation in which a <code>Word</code> object should be
 * created without any knowledge of how that word appeared in text.  The
 * <code>capitalized</code> field is computed from the <code>form</code> by
 * this class' constructor.
 * <p/>
 * <p> <i>All other fields must be obtained or computed externally.  Space is
 * provided for them in this class' implementation as a convenience, since we
 * expect the user will make frequent use of these fields.</i>
 * <p/>
 * <p> This class extends from {@link edu.illinois.cs.cogcomp.lbjava.parse.LinkedChild}.  Of course,
 * this means that objects of this class contain references to both the
 * previous and the next word in the sentence.  Constructors are available
 * that take the previous word as an argument, setting that reference.  Thus,
 * a useful technique for constructing all the words in a sentence will
 * involve code that looks like this (where <code>form</code> is a
 * {@link String}):
 * <p/>
 * <blockquote>
 * <code>
 * Word current = new Word(form);<br>
 * <i>a loop of some sort</i><br>
 * {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;current.next = new Word(form, current);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;current = current.next;<br>
 * }<br>
 * </blockquote>
 *
 * @author Nick Rizzolo
 **/
public class Word extends LinkedChild {
    /**
     * The actual text from the corpus that represents the word.
     */
    public String form;
    /**
     * Whether or not the word is capitalized is determined automatically by
     * the constructor.
     **/
    public boolean capitalized;
    /**
     * Names the part of speech of this word.
     */
    public String partOfSpeech;
    /**
     * The base form of the word.
     */
    public String lemma;
    /**
     * An indication of the meaning or usage of this instance of this word.
     */
    public String wordSense;


    /**
     * When all that is known is the spelling of the word.
     *
     * @param f The actual text of the word.
     **/
    public Word(String f) {
        this(f, null, null);
    }

    /**
     * Sets the actual text and the part of speech.
     *
     * @param f   The actual text of the word.
     * @param pos A token representing the word's part of speech.
     **/
    public Word(String f, String pos) {
        this(f, pos, null);
    }

    /**
     * This constructor is useful when the sentence is being parsed forwards.
     *
     * @param f The actual text of the word.
     * @param p The word that came before this one in the sentence.
     **/
    public Word(String f, Word p) {
        this(f, null, p);
    }

    /**
     * This constructor is useful when the sentence is being parsed forwards.
     *
     * @param f   The actual text of the word.
     * @param pos A token representing the word's part of speech.
     * @param p   The word that came before this one in the sentence.
     **/
    public Word(String f, String pos, Word p) {
        this(f, pos, p, -1, -1);
    }

    /**
     * When you have offset information.
     *
     * @param f     The actual text of the word.
     * @param start The offset into the parent document at which the first
     *              character of this word is found.
     * @param end   The offset into the parent document at which the last
     *              character of this word is found.
     **/
    public Word(String f, int start, int end) {
        this(f, null, null, start, end);
    }

    /**
     * When you have offset information.
     *
     * @param f     The actual text of the word.
     * @param pos   A token representing the word's part of speech.
     * @param start The offset into the parent document at which the first
     *              character of this word is found.
     * @param end   The offset into the parent document at which the last
     *              character of this word is found.
     **/
    public Word(String f, String pos, int start, int end) {
        this(f, pos, null, start, end);
    }

    /**
     * This constructor is useful when the sentence is being parsed forwards.
     *
     * @param f     The actual text of the word.
     * @param p     The word that came before this one in the sentence.
     * @param start The offset into the parent document at which the first
     *              character of this word is found.
     * @param end   The offset into the parent document at which the last
     *              character of this word is found.
     **/
    public Word(String f, Word p, int start, int end) {
        this(f, null, p, start, end);
    }

    /**
     * This constructor is useful when the sentence is being parsed forwards.
     *
     * @param f     The actual text of the word.
     * @param pos   A token representing the word's part of speech.
     * @param p     The word that came before this one in the sentence.
     * @param start The offset into the parent document at which the first
     *              character of this word is found.
     * @param end   The offset into the parent document at which the last
     *              character of this word is found.
     **/
    public Word(String f, String pos, Word p, int start, int end) {
        this(f, pos, null, null, p, start, end);
    }

    /**
     * This constructor is useful when the sentence is being parsed forwards.
     *
     * @param f     The actual text of the word.
     * @param pos   A token representing the word's part of speech.
     * @param l     The base form of the word.
     * @param sense The sense of the word.
     * @param p     The word that came before this one in the sentence.
     * @param start The offset into the parent document at which the first
     *              character of this word is found.
     * @param end   The offset into the parent document at which the last
     *              character of this word is found.
     **/
    public Word(String f, String pos, String l, String sense, Word p, int start,
                int end) {
        super(p, start, end);
        form = f;
        capitalized = f != null && f.length() > 0
                && Character.isUpperCase(f.charAt(0));
        partOfSpeech = pos;
        if (partOfSpeech != null) POS.fromToken(partOfSpeech);
        lemma = l;
        wordSense = sense;
    }


    /**
     * The string representation of a word is its POS bracket form, or, if the
     * part of speech is not available, it is just the spelling of the word.
     * Note that the POS bracket form of a word also entails displaying left
     * brackets (<code>"("</code>, <code>"["</code>, and <code>"{"</code>) as
     * <code>"-LRB-"</code> and right brackets (<code>")"</code>,
     * <code>"]"</code>, <code>"}"</code>) as <code>"-RRB-"</code>.
     *
     * @return The POS bracket form of this word, or just the spelling of the
     * word if the part of speech is not available.
     **/
    public String toString() {
        if (partOfSpeech == null) return form;
        String form = this.form;

        if (form.length() == 1) {
            if ("([{".indexOf(form.charAt(0)) != -1) form = "-LRB-";
            if (")]}".indexOf(form.charAt(0)) != -1) form = "-RRB-";
        }

        return "(" + partOfSpeech + " " + form + ")";
    }
}

