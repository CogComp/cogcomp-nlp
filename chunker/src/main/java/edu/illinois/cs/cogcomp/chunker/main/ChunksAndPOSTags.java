/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main;

import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This command line program takes a plain, unannotated text file as input and annotates the text
 * with chunk and part of speech tags. Both the Illinois POS tagger and the Illinois Chunker must be
 * on the <code>CLASSPATH</code> for this command to execute.
 *
 * <h3>Usage</h3> <blockquote><code>
 *   java edu.illinois.cs.cogcomp.chunker.main.ChunksAndPOSTags
 *          &lt;input file&gt;
 * </code></blockquote>
 *
 * <h3>Input</h3> The only command line parameter specifies the relative path to a file containing
 * the plain text to be annotated.
 *
 * <h3>Output</h3> The input text with predicted chunk and POS annotations is produced on
 * <code>STDOUT</code>. Annotated segments will be surrounded by square brackets. The type of the
 * segment (as indicated by the <code>"B-"</code> and <code>"I-"</code> labels after removing those
 * prefixes) appears attached to the opening square bracket.
 *
 * @author Nick Rizzolo
 **/
public class ChunksAndPOSTags {
    private static final Logger logger = LoggerFactory.getLogger(ChunksAndPOSTags.class);

    public static void main(String[] args) {
        String filename = null;

        try {
            filename = args[0];
            if (args.length > 1)
                throw new Exception();
        } catch (Exception e) {
            System.err
                    .println("usage: java edu.illinois.cs.cogcomp.chunker.main.ChunksAndPOSTags <input file>");
            System.exit(1);
        }

        Chunker chunker = new Chunker();
        Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(filename)));
        String previous = "";

        for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) {
            String prediction = chunker.discreteValue(w);
            if (prediction.startsWith("B-") || prediction.startsWith("I-")
                    && !previous.endsWith(prediction.substring(2)))
                logger.info("[" + prediction.substring(2) + " ");
            logger.info("(" + w.partOfSpeech + " " + w.form + ") ");
            if (!prediction.equals("O")
                    && (w.next == null || chunker.discreteValue(w.next).equals("O")
                            || chunker.discreteValue(w.next).startsWith("B-") || !chunker
                            .discreteValue(w.next).endsWith(prediction.substring(2))))
                logger.info("] ");
            if (w.next == null)
                logger.info("\n");
            previous = prediction;
        }
    }
}
