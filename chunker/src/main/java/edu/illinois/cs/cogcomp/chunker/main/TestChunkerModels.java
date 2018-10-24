/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main;

import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.ChildrenFromVectors;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for evaluating the accuracy of the chunker Expects data to be in COLUMN FORMAT
 * 
 * @author James Chen
 */
public class TestChunkerModels {
    private static final String NAME = TestChunkerModels.class.getCanonicalName();
    private String modelPath;
    private String labeledData;

    private Chunker tagger;
    private static final Logger logger = LoggerFactory.getLogger(TestChunkerModels.class);

    /**
     * Constructor for the test class. User specifies models and data. If no args provided,
     * instantiate default chunker.
     * 
     * @param modelPath Path to the directory where the models are stored
     * @param labeledData The path to the labeled testing data
     * @param chunkerName The file name of the chunker .lc and .lex models
     */
    public TestChunkerModels(String modelPath, String labeledData, String chunkerName) {
        this.labeledData = labeledData;
        tagger = new Chunker();
    }

    public void testAccuracy() {
        Parser parser = new ChildrenFromVectors(new CoNLL2000Parser(labeledData));

        int numSeen = 0;
        int numEqual = 0;

        for (Token w = (Token) parser.next(); w != null; w = (Token) parser.next()) {
            String prediction = tagger.discreteValue(w);
            String raw = w.toString();
            String actualChunk = raw.substring(raw.indexOf('(') + 1, raw.indexOf(' '));
            if (prediction.equals(actualChunk)) {
                numEqual++;
            }
            numSeen++;
        }

        logger.info("Total accuracy over " + numSeen + " items: "
                + String.format("%.2f", 100.0 * (double) numEqual / (double) numSeen) + "%");

    }

    public static void main(String[] args) {

        TestChunkerModels test = null;

        if (args.length != 3) {
            if (args.length == 1)
                test = new TestChunkerModels(null, args[0], null);
            else {
                System.err.println("Usage: " + NAME + " testData modelDir modelName");
                System.err.println("OR " + NAME + " testData");
                System.exit(-1);
            }
        } else
            test = new TestChunkerModels(args[1], args[0], args[2]);

        test.testAccuracy();
    }
}
