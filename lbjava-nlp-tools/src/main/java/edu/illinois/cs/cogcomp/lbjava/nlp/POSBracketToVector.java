/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.LineByLine;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;


/**
 * Use this parser to return <code>LinkedVector</code> objects representing
 * sentences given file names of POS bracket form files to parse.  These
 * files are expected to have one sentence per line, and the format of each
 * line is as follows: <br><br>
 * <p/>
 * <code>(pos1 spelling1) (pos2 spelling2) ... (posN spellingN)</code>
 * <br><br>
 * <p/>
 * It is also expected that there will be exactly one space between a part of
 * speech and the corresponding spelling and between a closing parenthesis
 * and an opening parenthesis.
 *
 * @author Nick Rizzolo
 **/
public class POSBracketToVector extends LineByLine {
    /**
     * Creates the parser.
     *
     * @param file The file to parse.
     **/
    public POSBracketToVector(String file) {
        super(file);
    }


    /**
     * Retrieves the next <code>LinkedVector</code> from the files being
     * parsed.
     **/
    public Object next() {
        String line = readLine();
        if (line == null) return null;
        return parsePOSBracketForm(line);
    }


    /**
     * Given a single line of textual input (containing all and only the words
     * in a single sentence) in the format shown above, this method parses and
     * returns a <code>LinkedVector</code>.
     *
     * @param line A single line of text.
     * @return A <code>LinkedVector</code> representing the input text.
     **/
    public static LinkedVector parsePOSBracketForm(String line) {
        String[] tokens = line.trim().split(" ");
        if (tokens.length == 0
                || tokens.length == 1
                && (tokens[0] == null || tokens[0].length() == 0))
            return new LinkedVector();

        int spaceIndex = line.indexOf(' ');
        spaceIndex = line.indexOf(' ', spaceIndex + 1);
        Word w = new Word(tokens[1].substring(0, tokens[1].length() - 1),
                tokens[0].substring(1),
                0,
                spaceIndex - 1);

        for (int i = 2; i < tokens.length; i += 2) {
            int start = spaceIndex + 1;
            spaceIndex = line.indexOf(' ', spaceIndex + 1);
            spaceIndex = line.indexOf(' ', spaceIndex + 1);

            w.next =
                    new Word(tokens[i + 1].substring(0, tokens[i + 1].length() - 1),
                            tokens[i].substring(1),
                            w,
                            start,
                            spaceIndex - 1);
            w = (Word) w.next;
        }

        return new LinkedVector(w);
    }


    /**
     * Given textual input in the format shown below, this method parses and
     * returns the <code>Word</code> that the text represents.  Expected
     * format: <br><br>
     * <p/>
     * <code>(pos spelling)</code>
     *
     * @param text     Text representing a word in POS bracket form.
     * @param previous The word that came before this word in the sentence.
     * @return A <code>Word</code> represented by the input text or
     * <code>null</code> if the input does not represent a
     * <code>Word</code>.
     **/
    public static Word parsePOSBracketForm(String text, Word previous) {
        if (text.charAt(0) != '(' || text.charAt(text.length() - 1) != ')')
            return null;
        String[] tokens = text.split(" ");
        if (tokens.length != 2) return null;
        return new Word(tokens[1].substring(0, tokens[1].length() - 1),
                tokens[0].substring(1),
                previous);
    }
}

