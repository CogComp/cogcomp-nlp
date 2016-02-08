package edu.illinois.cs.cogcomp.lbj.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;


/**
 * Simple test class for testing the POS Tagger with trained models. Currently just tests
 * for accuracy, ideally could break down into much more fine grained reports like that
 * of the TestDiscrete class.
 *
 * Simply tests existing models: Does NOT train/build models, see POSTrain for training.
 *
 * @author James Chen
 */
public class TestPOSModels {
  private static final String NAME = TestPOSModels.class.getCanonicalName();
  private String labeledTestFile;

  private TrainedPOSTagger tagger;

  /**
   * Constructor for the test class. User specifies models and data.
   *
   * @param modelPath The path to the directory where the models are stored.
   * @param labeledTestData The path to the labeled testing data
   */
  public TestPOSModels(String modelPath, String labeledTestData) {
    this.labeledTestFile = labeledTestData;
    this.tagger = new TrainedPOSTagger(modelPath);
  }

  /**
   * Tags the unlabeled data and compares the part-of-speech tags with the labeled data,
   * keeping track of and reporting total accuracy at the end.
   */
  public void testAccuracy() {
    wordForm __wordForm = new wordForm();
    Parser labeledParser = new POSBracketToToken(labeledTestFile);
    int numSeen = 0;
    int numEqual = 0;

    Token labeledWord = (Token) labeledParser.next();
    for (; labeledWord != null;
         labeledWord = (Token) labeledParser.next()) {
     
      String labeledTag = labeledWord.label;
      String testTag = tagger.discreteValue(labeledWord);

      if (labeledTag.equals(testTag)) {
        numEqual++;
      }
      numSeen++;
    }

    System.out.println("Total accuracy over " + numSeen + " items: " +
            String.format("%.2f", 100.0 * (double) numEqual / (double) numSeen) + "%");
  }

  public static void main(String[] args) {
    if ( args.length != 1 )
    {
      System.err.println( "Usage: " + NAME + " modelPath" );
      System.err.println( "'modelPath' specifies directory from which the learned models will be read." );
      System.exit( -1 );
    }
    TestPOSModels test = new TestPOSModels(args[0], Constants.testData);

    test.testAccuracy();
  }

}
