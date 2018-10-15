/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp.seg;

import edu.illinois.cs.cogcomp.lbjava.nlp.POSBracketToVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;


/**
 * This parser expects labeled data as described in
 * {@link POSBracketToVector}, and it returns {@link Token} objects
 * representing that labeled data.
 *
 * @author Nick Rizzolo
 **/
public class POSBracketToToken extends POSBracketToVector {
    /**
     * A pointer to the most recently returned word is kept for easy access to
     * the next word.
     **/
    public Token currentWord;


    /**
     * Initializes an instance with the named file.
     *
     * @param file The name of the file containing labeled data.
     **/
    public POSBracketToToken(String file) {
        super(file);
    }


    /**
     * Returns the next labeled word in the data.
     */
    public Object next() {
        if (currentWord == null) {
            LinkedVector vector = (LinkedVector) super.next();
            while (vector != null && vector.size() == 0)
                vector = (LinkedVector) super.next();
            if (vector == null) return null;

            Word w = (Word) vector.get(0);
            Token t = currentWord = new Token(w, null, w.partOfSpeech);
            t.partOfSpeech = null;

            while (w.next != null) {
                w = (Word) w.next;
                t.next = new Token(w, t, w.partOfSpeech);
                t.partOfSpeech = null;
                t = (Token) t.next;
            }
        }

        Token result = currentWord;
        currentWord = (Token) currentWord.next;
        return result;
    }
}

