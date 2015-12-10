package edu.illinois.cs.cogcomp.lbj.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


/**
  * This program uses {@link POSTagger} to tag pre-tokenized text.  All output
  * is sent to <code>STDOUT</code>.
  *
  * <h4>Usage</h4>
  * <blockquote><code>
  *   java edu.illinois.cs.cogcomp.lbj.pos.POSTagTokenized &lt;text file&gt;
  * </code></blockquote>
  *
  * <h4>Input</h4>
  * The lone command line parameter is simply the name of a plain text file,
  * written in natural language with no annotations except that all words are
  * delimited by whitespace (e.g., punctuation is separated with whitespace,
  * etc.).
  *
  * <h4>Output</h4>
  * The output will contain exactly one sentence per line, and each word will
  * be surrounded by parentheses, accompanied by a POS tag.
  *
  * @author Nick Rizzolo
 **/
public class POSTagTokenized
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
          "usage: java edu.illinois.cs.cogcomp.lbj.pos.POSTagTokenized <text file>");
      System.exit(1);
    }

    String testingFile = args[0];

    POSTagger tagger = new POSTagger();
    Parser parser = new ColumnFormat(testingFile);
    String sentence = "";

    for (String[] s = (String[]) parser.next(); s != null;
         s = (String[]) parser.next())
    {
      Token first = null;

      if (s.length > 0)
      {
        Token t = first = new Token(new Word(s[0]), null, null);

        for (int i = 1; i < s.length; ++i)
        {
          t.next = new Token(new Word(s[i]), t, null);
          t = (Token) t.next;
        }
      }

      for (Token word = first; word != null; word = (Token) word.next)
      {
        String tag = tagger.discreteValue(word);
        sentence += " (" + tag + " " + word.form + ")";
      }

      System.out.println(sentence.substring(1));
    }
  }
}

