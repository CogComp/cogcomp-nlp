/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This parser takes the plain, unannotated {@link Sentence}s returned by
 * another parser (e.g., {@link SentenceSplitter}) and splits them into
 * {@link Word} objects.  Entire sentences now represented as
 * {@link edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector}s are then returned one at a time by calls
 * to the <code>next()</code> method.
 * <p/>
 * <p> A {@link #main(String[])} method is also implemented which applies
 * this class to plain text in a straight-forward way.
 *
 * @author Nick Rizzolo
 **/
public class WordSplitter implements Parser {
    private static Logger logger = LoggerFactory.getLogger(WordSplitter.class);
    /**
     * Run this program on a file containing plain text, and it will produce
     * the same text on <code>STDOUT</code> rearranged so that each line
     * contains exactly one sentence, and so that character sequences deemed to
     * be "words" are delimited by whitespace.
     * <p/>
     * <p> Usage:
     * <code> java edu.illinois.cs.cogcomp.lbjava.edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter &lt;file name&gt; </code>
     *
     * @param args The command line arguments.
     **/
    public static void main(String[] args) {
        String filename = null;

        try {
            filename = args[0];
            if (args.length > 1) throw new Exception();
        } catch (Exception e) {
            System.err.println("usage: java edu.illinois.cs.cogcomp.lbjava.edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter <file name>");
            System.exit(1);
        }

        WordSplitter splitter = new WordSplitter(new SentenceSplitter(filename));

        for (LinkedVector s = (LinkedVector) splitter.next(); s != null;
             s = (LinkedVector) splitter.next()) {
            if (s.size() > 0) {
                Word w = (Word) s.get(0);
                System.out.print(w.form);
                for (w = (Word) w.next; w != null; w = (Word) w.next)
                    System.out.print(" " + w.form);
            }

            System.out.println();
        }
    }


    /**
     * The {@link Sentence} returning parser.
     */
    protected Parser parser;


    /**
     * Initializing constructor.
     *
     * @param p The {@link Sentence} returning parser.
     **/
    public WordSplitter(Parser p) {
        parser = p;
    }


    /**
     * Returns {@link edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector}s of {@link Word} objects one at
     * a time.
     **/
    public Object next() {
        Sentence sentence = (Sentence) parser.next();
        if (sentence == null) return null;
        return sentence.wordSplit();
    }


    /**
     * Sets this parser back to the beginning of the raw data.
     */
    public void reset() {
        parser.reset();
    }


    /**
     * Frees any resources this parser may be holding.
     */
    public void close() {
        parser.close();
    }
}

