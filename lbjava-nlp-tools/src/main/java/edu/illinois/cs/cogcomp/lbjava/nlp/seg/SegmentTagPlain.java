/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp.seg;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.lbjava.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Use this command line program to produce textual segment annotations
 * on an input text using a learned {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word} classifier.
 * <p/>
 * <h4>Usage</h4>
 * <blockquote><code>
 * java edu.illinois.cs.cogcomp.lbjava.edu.illinois.cs.cogcomp.lbjava.nlp.seg.SegmentTagPlain &lt;word classifier&gt;
 * &lt;input file&gt;
 * [&lt;parser&gt;]
 * </code></blockquote>
 * <p/>
 * <h4>Input</h4>
 * The first command line parameter specifies the fully qualified class name
 * (e.g. <code>myPackage.myClass</code>) of a {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word}
 * classifier whose predictions either are equal to <code>"O"</code> or begin
 * with <code>"B-"</code> or <code>"I-"</code>.  The second command line
 * parameter specifies the relative path to a file containing the plain text
 * to be tagged.
 * <p/>
 * <p> The optional third command line parameter specifies the name of a
 * parser which creates an alternative representation of the user's choice
 * for the plain text words in the input.  When this parameter is omitted,
 * {@link PlainToTokenParser} is applied to {@link edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter}
 * applied to {@link edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter} which ends up returning
 * {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word} objects one at a time.  If the parameter is given,
 * the specified parser is used in place of {@link PlainToTokenParser}.  It
 * must be the case that this parser returns objects of a class derived from
 * class {@link edu.illinois.cs.cogcomp.lbjava.nlp.Word}.
 * <p/>
 * <h4>Output</h4>
 * The same text with segment annotations predicted by the specified
 * classifier is produced on <code>STDOUT</code>.  Annotated segments will
 * be surrounded by square brackets.  The type of the segment (as indicated
 * by the <code>"B-"</code> and <code>"I-"</code> labels after removing those
 * prefixes) appears attached to the opening square bracket.
 *
 * @author Nick Rizzolo
 **/
public class SegmentTagPlain {
    private static Logger logger = LoggerFactory.getLogger(SegmentTagPlain.class);
    public static void main(String[] args) {
        String taggerName = null;
        String inputFile = null;
        String parserName = null;

        try {
            taggerName = args[0];
            inputFile = args[1];

            if (args.length > 2) {
                parserName = args[2];
                if (args.length > 3) throw new Exception();
            }
        } catch (Exception e) {
            System.err.println(
                    "usage: java edu.illinois.cs.cogcomp.lbjava.edu.illinois.cs.cogcomp.lbjava.nlp.seg.SegmentTagPlain <word classifier> "
                            + "<input file> \\\n"
                            + "                                         [<parser>]");
            System.exit(1);
        }

        Classifier tagger = ClassUtils.getClassifier(taggerName);
        Parser parser;

        if (parserName == null)
            parser =
                    new PlainToTokenParser(
                            new WordSplitter(new SentenceSplitter(inputFile)));
        else
            parser =
                    ClassUtils.getParser(parserName, new Class[]{Parser.class},
                            new Parser[]{new WordSplitter(new SentenceSplitter(inputFile))});

        String previous = "";

        for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) {
            String prediction = tagger.discreteValue(w);
            if (prediction.startsWith("B-")
                    || prediction.startsWith("I-")
                    && !previous.endsWith(prediction.substring(2)))
                System.out.print("[" + prediction.substring(2) + " ");
            System.out.print(w.form + " ");
            if (!prediction.equals("O")
                    && (w.next == null
                    || tagger.discreteValue(w.next).equals("O")
                    || tagger.discreteValue(w.next).startsWith("B-")
                    || !tagger.discreteValue(w.next)
                    .endsWith(prediction.substring(2))))
                System.out.print("] ");
            if (w.next == null) System.out.println();
            previous = prediction;
        }
    }
}

