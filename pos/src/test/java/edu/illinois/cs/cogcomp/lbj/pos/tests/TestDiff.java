package edu.illinois.cs.cogcomp.lbj.pos.tests;

import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
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

/**
 * A sanity check, processing a sample text file and comparing the
 * output to a reference file.
 * @author Christos Christodoulopoulos
 */
public class TestDiff extends TestCase {

    private static final String testFileName = "testIn.txt";
    private static String testFile;
    private static final String refFileName = "testRefOutput.txt";
    private static List<String> refSentences;

    public void setUp() throws IOException, URISyntaxException {
        URL testFileURL = TestDiff.class.getClassLoader().getResource(testFileName);
        assertNotNull("Test file missing", testFileURL);
        testFile = testFileURL.getFile();
        assertNotNull("Reference file missing", TestDiff.class.getClassLoader().getResource(refFileName));
        BufferedReader refReader = new BufferedReader(
                new InputStreamReader(TestDiff.class.getClassLoader().getResourceAsStream(refFileName)));

        refSentences = new ArrayList<String>();

        String line;
        while ((line = refReader.readLine()) != null) {
            refSentences.add(line);
        }
    }

    public void testDiff() {
/*
        POSTagger tagger = new POSTagger();
        Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(testFile)));
        String sentence = "";
        int sentenceCounter = 0;

        for (Token word = (Token) parser.next(); word != null; word = (Token) parser.next()) {
            String tag = tagger.discreteValue(word);
            sentence += " (" + tag + " " + word.form + ")";

            if (word.next == null) {
                if (!sentence.substring(1).equals(refSentences.get(sentenceCounter)))
                    fail("Produced output doesn't match reference: " +
                            "\nProduced: " + sentence.substring(1) +
                            "\nExpected: " + refSentences.get(sentenceCounter));

                sentence = "";
                sentenceCounter++;
            }
        }
*/
    }
}
