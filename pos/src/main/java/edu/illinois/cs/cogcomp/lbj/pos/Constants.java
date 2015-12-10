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

//	public static final String DATA_DIR = "dataDir";
//	public static final String TRAIN_DATA = "trainingData";
//	public static final String DEV_DATA = "devData";
//	public static final String TEST_DATA = "testData";
//	public static final String TRAIN_AND_DEV_DATA = "trainingAndDevData";
}

