package edu.illinois.cs.cogcomp.lbj.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


/**
  * This parser returns only words that have been observed less than or equal
  * to {@link #threshold} times according to {@link baselineTarget}.
  *
  * @author Nick Rizzolo
 **/
public class POSLabeledUnknownWordParser extends POSBracketToToken
{
  /**
    * A reference to the classifier that knows how often words were observed
    * during training.
   **/
  private static final baselineTarget baseline = new baselineTarget();
  /** Only words that were observed this many times or fewer are returned. */
  public static int threshold = 3;


  /**
    * Initializes an instance with the named file.
    *
    * @param file The name of the file containing labeled data.
   **/
  public POSLabeledUnknownWordParser(String file) { super(file); }


  /** Returns the next labeled word in the data. */
  public Object next()
  {
    Token result = (Token) super.next();
    while (result != null && baseline.observedCount(result.form) > threshold)
      result = (Token) super.next();
    return result;
  }
}

