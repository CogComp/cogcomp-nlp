package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.pos.lbjava.*;

/**
  * This program tests {@link POSTagger} on labeled data and reports its
  * performance.  All output is sent to <code>STDOUT</code>.
  *
  * <h4>Usage</h4>
  * <blockquote><code>
  *   java edu.illinois.cs.cogcomp.lbj.pos.TestPOS &lt;text file&gt;
  * </code></blockquote>
  *
  * <h4>Input</h4>
  * The lone command line parameter is the name of a text file containing
  * annotated natural language text.  This file is expected to have one
  * sentence per line, and the format of each line is as follows: <br><br>
  *
  * <code>(pos1 spelling1) (pos2 spelling2) ... (posN spellingN)</code>
  * <br><br>
  *
  * It is also expected that there will be exactly one space between a part of
  * speech and the corresponding spelling and between a closing parenthesis
  * and an opening parenthesis.
  *
  * <h4>Output</h4>
  * An ASCII table is written to <code>STDOUT</code> reporting precision,
  * recall, and F<sub>1</sub> scores itemized by the POS tags discovered
  * either in the labeled data or in the predictions.  The two rightmost
  * columns are named <code>"LCount"</code> and <code>"PCount"</code>
  * (standing for "labeled count" and "predicted count" respectively), and
  * they report the number of times the data contained each label and the
  * number of times the classifier predicted each label respectively.  In the
  * last row, overall accuracy is reported in the precision column.  In the
  * count column, the total number of predictions (or labels, equivalently) is
  * reported.
  *
  * @author Nick Rizzolo
 **/
public class TestPOS
{
  /**
    * Implements the program described above.
    *
    * @param args The command line parameters.
   **/
  public static void main(String[] args)
  {
    // Parse the command line
    if (args.length != 1)
    {
      System.err.println(
          "usage: java edu.illinois.cs.cogcomp.lbj.pos.TestPOS <text file>");
      System.exit(1);
    }

    String testingFile = args[0];

    TestDiscrete.testDiscrete(
        new TestDiscrete(),
        new POSTagger(),
        new POSLabel(),
        new POSBracketToToken(testingFile),
        true, 0);
  }
}

