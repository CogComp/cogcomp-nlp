/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;


/**
 * Use this parser to return <code>Word</code> objects given file names of
 * POS bracket form files to parse.  These files are expected to have one
 * sentence per line, and the format of each line is as follows: <br><br>
 * <p/>
 * <code>(pos1 spelling1) (pos2 spelling2) ... (posN spellingN)</code>
 * <br><br>
 * <p/>
 * It is also expected that there will be exactly one space between a part of
 * speech and the corresponding spelling and between a closing parenthesis
 * and an opening parenthesis.
 *
 * @author Nick Rizzolo
 * @deprecated As of LBJava release 2.0.4, the functionality of this class has
 * been superceded by the {@link edu.illinois.cs.cogcomp.lbjava.parse.ChildrenFromVectors}
 * parser used in conjunction with {@link POSBracketToVector}.
 **/
public class POSBracketToWord extends POSBracketToVector {
    /**
     * The next word to return, or <code>null</code> if we need a new sentence.
     **/
    private Word currentWord;


    /**
     * Adds the given file name to the queue.
     *
     * @param file The file name to add to the queue.
     **/
    public POSBracketToWord(String file) {
        super(file);
    }


    /**
     * Retrieves the next sentence from the files being parsed.
     *
     * @return A <code>LinkedVector</code> representation of the next sentence.
     **/
    public Object next() {
        if (currentWord == null) {
            LinkedVector vector = (LinkedVector) super.next();
            if (vector != null) currentWord = (Word) vector.get(0);
        }

        Word result = currentWord;
        if (currentWord != null) currentWord = (Word) currentWord.next;
        return result;
    }
}

