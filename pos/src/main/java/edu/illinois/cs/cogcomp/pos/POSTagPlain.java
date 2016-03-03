package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.lbjava.*;


/**
 * This program uses {@link POSTagger} to tag plain text. All output is sent to <code>STDOUT</code>.
 *
 * <h4>Usage</h4> <blockquote><code>
 *   java edu.illinois.cs.cogcomp.lbj.pos.POSTagPlain &lt;text file&gt;
 * </code></blockquote>
 *
 * <h4>Input</h4> The lone command line parameter is simply the name of a plain text file, written
 * in natural language (i.e., no annotations, and no preprocessing has been performed).
 *
 * <h4>Output</h4> The output will contain exactly one sentence per line, and each word will be
 * surrounded by parentheses, accompanied by a POS tag.
 *
 * @author Nick Rizzolo
 **/
public class POSTagPlain {
    /**
     * Implements the program described above.
     *
     * @param args The command line parameters.
     **/
    public static void main(String[] args) {
        // Parse the command line
        if (args.length != 1) {
            System.err
                    .println("usage: java edu.illinois.cs.cogcomp.lbj.pos.POSTagPlain <text file>");
            System.exit(1);
        }

        String testingFile = args[0];

        POSTagger tagger = new POSTagger();
        Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(testingFile)));
        String sentence = "";

        for (Token word = (Token) parser.next(); word != null; word = (Token) parser.next()) {
            String tag = tagger.discreteValue(word);
            sentence += " (" + tag + " " + word.form + ")";

            if (word.next == null) {
                System.out.println(sentence.substring(1));
                sentence = "";
            }
        }
    }
}
