package edu.illinois.cs.cogcomp.lbj.pos;


/**
  * Constant values used by the LBJ source file.
  *
  * @author Nick Rizzolo
 **/
public class Constants
{
  /** A configurable prefix. */
  public static final String prefix = "/shared/corpora/corporaWeb/written/eng/POS/";
  /** The file containing the training set. */
  public static final String trainingData = prefix + "00-18.br";
  /** The file containing the development set. */
  public static final String devData = prefix + "19-21.br";
  /** The file containing the test set. */
  public static final String testData = prefix + "22-24.br";
  /** The file containing the training <i>and</i> development sets. */
  public static final String trainingAndDevData = prefix + "00-21.br";

  public static final String baselineName = "baselineTarget";
  public static final String mikheevName = "MikheevTable";
  public static final String knownName = "POSTaggerKnown";
  public static final String unknownName = "POSTaggerUnknown";

  public static final String modelPath = "models/edu/illinois/cs/cogcomp/lbj/pos/";
  public static final String baselineModelPath = modelPath + baselineName + ".lc";
  public static final String mikheevModelPath = modelPath + mikheevName + ".lc";
  public static final String knownModelPath = modelPath + knownName + ".lc";
  public static final String knownLexPath = modelPath + knownName + ".lex";
  public static final String unknownModelPath = modelPath + unknownName + ".lc";
  public static final String unknownLexPath = modelPath + unknownName + ".lex";

}

