/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.utils;

import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;


/**
 * This parser is designed to extract an internal representation of the sentences and words that
 * appear in the <a href="http://www.cnts.ua.ac.be/conll2000/">CoNLL 2000</a> shared task corpora.
 * It produces <code>LBJ2.parse.LinkedVector</code>s populated by <code>LBJ2.nlp.seg.Token</code>s
 * representing words. Each <code>LinkedVector</code> represents a sentence from the input. More
 * information about <code>LinkedVector</code> can be found in <a
 * href="http://l2r.cs.uiuc.edu/~cogcomp/software/LBJ2/library/LBJ2/parse/LinkedVector.html">LBJ's
 * online Javadoc</a>.
 *
 * <p>
 * A line of the input file parsed by this parser contains information about a single word of
 * natural language text. The word itself appears first, followed by a part of speech tag and a BIO
 * chunk tag, all separated by whitespace. If a part of speech tag is unknown, a single dash may
 * appear instead. Words appear in the order they were found in the plain text, and sentences are
 * separated by newlines.
 *
 * @author Nick Rizzolo
 **/
public class CoNLL2000Parser extends ColumnFormat {
    /**
     * Constructs this parser to parse the given file.
     *
     * @param file The name of the file to parse.
     **/
    public CoNLL2000Parser(String file) {
        super(file);
    }


    /**
     * Produces the next object parsed from the input file; in this case, that object is guaranteed
     * to be a <code>LinkedVector</code> populated by <code>Token</code>s representing a sentence.
     **/
    public Object next() {
        String[] line = (String[]) super.next();
        while (line != null && line.length == 0)
            line = (String[]) super.next();
        if (line == null)
            return null;

        String pos = line[1];
        if (pos.equals("-"))
            pos = null;
        Token t = new Token(new Word(line[0], pos), null, line[2]);

        for (line = (String[]) super.next(); line != null && line.length > 0; line =
                (String[]) super.next()) {
            pos = line[1];
            if (pos.equals("-"))
                pos = null;
            t.next = new Token(new Word(line[0], pos), t, line[2]);
            t = (Token) t.next;
        }

        return new LinkedVector(t);
    }
}
