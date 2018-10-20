/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main;

import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;


/**
 * This parser is designed to extract an internal representation of the sentences and words that
 * appear in the Reuters 2003 corpora. It produces <code>LBJ2.parse.LinkedVector</code>s populated
 * by <code>LBJ2.nlp.seg.Token</code>s representing words. Each <code>LinkedVector</code> represents
 * a sentence from the input. More information about <code>LinkedVector</code> can be found in <a
 * href="http://l2r.cs.uiuc.edu/~cogcomp/software/LBJ2/library/LBJ2/parse/LinkedVector.html">LBJ's
 * online Javadoc</a>.
 *
 * @author Nick Rizzolo
 **/
public class Reuters2003Parser extends ColumnFormat {
    /**
     * Constructs this parser to parse the given file.
     *
     * @param file The name of the file to parse.
     **/
    public Reuters2003Parser(String file) {
        super(file);
    }


    /**
     * Produces the next object parsed from the input file; in this case, that object is guaranteed
     * to be a <code>LinkedVector</code> populated by <code>Token</code>s representing a sentence.
     **/
    public Object next() {
        String[] line = (String[]) super.next();
        while (line != null && (line.length < 2 || line[4].equals("-X-")))
            line = (String[]) super.next();
        if (line == null)
            return null;

        if (line[3].charAt(0) == 'I')
            line[3] = "B" + line[3].substring(1);
        Token t = new Token(new Word(line[5], line[4]), null, line[3]);
        String previous = line[3];

        for (line = (String[]) super.next(); line != null && line.length > 0; line =
                (String[]) super.next()) {
            if (line[3].charAt(0) == 'I' && !previous.endsWith(line[3].substring(2)))
                line[3] = "B" + line[3].substring(1);
            t.next = new Token(new Word(line[5], line[4]), t, line[3]);
            t = (Token) t.next;
            previous = line[3];
        }

        return new LinkedVector(t);
    }
}
