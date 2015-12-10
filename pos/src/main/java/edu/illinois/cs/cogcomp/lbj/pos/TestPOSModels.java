package edu.illinois.cs.cogcomp.lbj.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
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
  private static final String NAME = POSTrain.class.getCanonicalName();
  private String labeledTestFile;
  private String unlabeledTestFile;

  private POSTaggerKnown taggerKnown;
  private POSTaggerUnknown taggerUnknown;

  /**
   * Constructor for the test class. User specifies models and data.
   *
   * @param modelPath The path to the directory where the models are stored.
   * @param labeledTestData The path to the labeled testing data
   * @param unlabeledTestData The path to the unlabeled testing data
   */
  public TestPOSModels(String modelPath, String labeledTestData, String unlabeledTestData) {
    this.labeledTestFile = labeledTestData;
    this.unlabeledTestFile = unlabeledTestData;

    this.taggerKnown = new POSTaggerKnown(modelPath + Constants.knownName + ".lc",
            modelPath + Constants.knownName + ".lex");
    this.taggerUnknown = new POSTaggerUnknown(modelPath + Constants.unknownName + ".lc",
            modelPath + Constants.unknownName + ".lex");
  }

  /**
   * Tags the unlabeled data and compares the part-of-speech tags with the labeled data,
   * keeping track of and reporting total accuracy at the end.
   */
  public void testAccuracy() {
    wordForm __wordForm = new wordForm();
    Parser unlabeledParser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(unlabeledTestFile)));
    Parser labeledParser = new POSBracketToToken(labeledTestFile);
    int numSeen = 0;
    int numEqual = 0;

    Token unlabeledWord = (Token) unlabeledParser.next();
    Token labeledWord = (Token) labeledParser.next();
    for (; unlabeledWord != null && labeledWord != null;
         unlabeledWord = (Token) unlabeledParser.next(),
                 labeledWord = (Token) labeledParser.next()) {

      String unlabeledTag;
      String labeledTag;

      if (baselineTarget.getInstance().observed(__wordForm.discreteValue(unlabeledWord))) {
        unlabeledTag = taggerKnown.discreteValue(unlabeledWord);
      } else {
        unlabeledTag = taggerUnknown.discreteValue(unlabeledWord);
      }
      labeledTag = labeledWord.label;

      if (labeledTag.equals(unlabeledTag)) {
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
    TestPOSModels test = new TestPOSModelsBuilder()
            .modelPath(args[ 0 ] + "/")
            .unlabeledTestFile("test/testIn.txt")
            .labeledTestFile("test/testRefOutput.txt")
            .buildTestPOSModels();

    test.testAccuracy();
  }

}
