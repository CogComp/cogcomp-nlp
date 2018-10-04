/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp.seg;

import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;


/**
 * This parser takes the {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word}s in the representation created
 * by another {@link edu.illinois.cs.cogcomp.lbjava.parse.Parser} and creates a new representation
 * consisting of {@link Token}s.  The input parser is actually expected to
 * return a {@link edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector} populated by
 * {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word}s with each call to {@link edu.illinois.cs.cogcomp.lbjava.parse.Parser#next()}.
 * The {@link Token}s returned by calls to this class's {@link #next()}
 * method are also contained in {@link edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector}s representing
 * sentences which are accessible via the
 * {@link edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector#parent} field.
 *
 * @author Nick Rizzolo
 **/
public class PlainToTokenParser implements Parser {
    /**
     * A parser creating a representation consisting of {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word}s.
     **/
    protected Parser parser;
    /**
     * The next token to return.
     */
    protected Token next;


    /**
     * The only constructor.
     *
     * @param p A parser creating a representation consisting of
     *          {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word}s.
     **/
    public PlainToTokenParser(Parser p) {
        parser = p;
    }


    /**
     * This method returns {@link Token}s until the input is exhausted, at
     * which point it returns <code>null</code>.
     **/
    public Object next() {
        while (next == null) {
            LinkedVector words = (LinkedVector) parser.next();
            if (words == null) return null;
            Word w = (Word) words.get(0);
            Token t = new Token(w, null, null);

            for (w = (Word) w.next; w != null; w = (Word) w.next) {
                t.next = new Token(w, t, null);
                t = (Token) t.next;
            }

            LinkedVector tokens = new LinkedVector(t);
            next = (Token) tokens.get(0);
        }

        Token result = next;
        next = (Token) next.next;
        return result;
    }


    /**
     * Sets this parser back to the beginning of the raw data.
     */
    public void reset() {
        parser.reset();
        next = null;
    }


    /**
     * Frees any resources this parser may be holding.
     */
    public void close() {
        parser.close();
    }
}

