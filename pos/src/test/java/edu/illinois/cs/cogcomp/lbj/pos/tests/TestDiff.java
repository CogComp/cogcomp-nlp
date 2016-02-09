package edu.illinois.cs.cogcomp.lbj.pos.tests;

import edu.illinois.cs.cogcomp.lbj.pos.TrainedPOSTagger;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import junit.framework.TestCase;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
/**
 * A sanity check, processing a sample text file and comparing the
 * output to a reference file.
 * @author Christos Christodoulopoulos
 */
public class TestDiff extends TestCase {

    private static final String testFileName = "testIn.txt";
    private static String testFile;
    private static final String refFileName = "testRefOutput.txt";
    private static List<String> refTags;
    private static final double thresholdAcc = 0.95;

    public void setUp() throws IOException, URISyntaxException {
        URL testFileURL = TestDiff.class.getClassLoader().getResource(testFileName);
        assertNotNull("Test file missing", testFileURL);
        testFile = testFileURL.getFile();
        assertNotNull("Reference file missing", TestDiff.class.getClassLoader().getResource(refFileName));
        BufferedReader refReader = new BufferedReader(
                new InputStreamReader(TestDiff.class.getClassLoader().getResourceAsStream(refFileName)));

        refTags = new ArrayList<String>();
        String line;
        while ((line = refReader.readLine()) != null) {
            String [] entries = line.substring(1, line.length()-1).split("[)] [(]");
            for (int i = 0; i < entries.length; i++) {
                refTags.add(entries[i].split(" ")[0]);
            }
        }
    }

    public void testDiff() {

        TrainedPOSTagger tagger = new TrainedPOSTagger();
        Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(testFile)));
        String sentence = "";
        int sentenceCounter = 0;
        int tokenCounter = 0;
        int correctCounter = 0;
        for (Token word = (Token) parser.next(); word != null; word = (Token) parser.next()) {
            String tag = tagger.discreteValue(word);
            if (refTags.get(tokenCounter).equals(tag)) {
                correctCounter++;
            }
            tokenCounter++;
            
        }
        double result = ((double)correctCounter)/tokenCounter;
        if (result < thresholdAcc) {
            fail("Tagger performance is insufficient: "+
                    "\nProduced: " + result +
                    "\nExpected: " + thresholdAcc);
        }
    }
}
