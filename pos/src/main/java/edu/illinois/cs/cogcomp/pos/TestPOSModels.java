/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pos.lbjava.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple test class for testing the POS Tagger with trained models. Currently just tests for
 * accuracy, ideally could break down into much more fine grained reports like that of the
 * TestDiscrete class.
 *
 * Simply tests existing models: Does NOT train/build models, see POSTrain for training.
 *
 * @author James Chen
 */
public class TestPOSModels {
    private static Logger logger = LoggerFactory.getLogger(TestPOSModels.class);

    private static final String NAME = TestPOSModels.class.getCanonicalName();
    private String labeledTestFile;

    private POSTagger tagger;

    /**
     * Constructor for the test class. User specifies models and data.
     *
     * @param labeledTestData The path to the labeled testing data
     */
    public TestPOSModels(String labeledTestData) {
        this.labeledTestFile = labeledTestData;
        this.tagger = new POSTagger();
    }

    /**
     * Tags the unlabeled data and compares the part-of-speech tags with the labeled data, keeping
     * track of and reporting total accuracy at the end.
     */
    public void testAccuracy() {
        WordForm __wordForm = new WordForm();
        Parser labeledParser = new POSBracketToToken(labeledTestFile);
        int numSeen = 0;
        int numEqual = 0;

        Token labeledWord = (Token) labeledParser.next();
        for (; labeledWord != null; labeledWord = (Token) labeledParser.next()) {

            String labeledTag = labeledWord.label;
            String testTag = tagger.discreteValue(labeledWord);

            if (labeledTag.equals(testTag)) {
                numEqual++;
            }
            numSeen++;
        }

        System.out.println("Total accuracy over " + numSeen + " items: "
                + String.format("%.2f", 100.0 * (double) numEqual / (double) numSeen) + "%");
    }

    public static void main(String[] args) {
        ResourceManager rm = new POSConfigurator().getDefaultConfig();
        TestPOSModels test = new TestPOSModels(rm.getString("testData"));

        test.testAccuracy();
    }

}
