package edu.illinois.cs.cogcomp.lbj.chunk;

import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.BIOTester;
import edu.illinois.cs.cogcomp.lbjava.parse.ChildrenFromVectors;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Test class for evaluating the accuracy of the chunker
 * Expects data to be in COLUMN FORMAT
 * @author James Chen
 */
public class TestChunkerModels {
    private static final String NAME = TestChunkerModels.class.getCanonicalName();
    private String modelPath;
    private String labeledData;

    private Chunker chunker;

    /**
     * Constructor for the test class. User specifies models and data.
     * If no args provided, instantiate default chunker.
     * @param modelPath Path to the directory where the models are stored
     * @param labeledData The path to the labeled testing data
     * @param chunkerName The file name of the chunker .lc and .lex models
     */
    public TestChunkerModels(String modelPath, String labeledData, String chunkerName) {
        this.labeledData = labeledData;
        if (null == modelPath || null == chunkerName )
            this.chunker = new Chunker();
        else
            this.chunker = new Chunker(modelPath + chunkerName + ".lc", modelPath + chunkerName + ".lex");
    }

    public void testAccuracy() {
        Parser parser = new ChildrenFromVectors(new CoNLL2000Parser(labeledData));

        int numSeen = 0;
        int numEqual = 0;

        for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) {
            String prediction = chunker.discreteValue(w);
            String raw = w.toString();
            String actualChunk = raw.substring(raw.indexOf('(') + 1, raw.indexOf(' '));
            if (prediction.equals(actualChunk)) {
                numEqual++;
            }
            numSeen++;
        }

        System.out.println("Total accuracy over " + numSeen + " items: " +
                String.format("%.2f", 100.0 * (double) numEqual / (double) numSeen) + "%");

    }

    public static void main(String[] args) {

        TestChunkerModels test = null;

        if ( args.length != 3 ) {
            if (args.length == 1)
                test = new TestChunkerModels(null, args[0], null);
            else {
                System.err.println("Usage: " + NAME + " testData modelDir modelName");
                System.err.println( "OR " + NAME + " testData" );
                System.exit(-1);
            }
        }
        else
             test = new TestChunkerModels(args[ 1 ], args[ 0 ], args[ 2 ]);

        test.testAccuracy();
    }
}
