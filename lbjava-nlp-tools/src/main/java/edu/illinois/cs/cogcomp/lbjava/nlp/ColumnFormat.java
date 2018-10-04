/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.LineByLine;


/**
 * This parser returns arrays of <code>String</code>s representing the rows
 * of a file in column format.  The input file is assumed to contain fields
 * of non-whitespace characters separated by any amount of whitespace, one
 * line of which is usually used to represent a word in a corpus.  This
 * parser breaks a given line into one <code>String</code> per field,
 * omitting all of the whitespace.  They are then returned in an array via
 * <code>next()</code>.  If the input line is empty or contains only
 * whitespace, a zero length array will be returned.
 *
 * @author Nick Rizzolo
 **/
public class ColumnFormat extends LineByLine {
    /**
     * Creates the parser.
     *
     * @param file The name of the file to parse.
     **/
    public ColumnFormat(String file) {
        super(file);
    }


    /**
     * Returns an array of <code>String</code>s representing the information in
     * the columns of this row.
     **/
    public Object next() {
        String line = readLine();
        if (line == null) return null;
        if (line.length() == 0) return new String[0];
        return line.split("\\s+");
    }
}

