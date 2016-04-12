package edu.illinois.cs.cogcomp.lbj.chunk.tests;

import edu.illinois.cs.cogcomp.chunker.main.TrainedChunker;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        TrainedChunker tagger = new TrainedChunker();
        Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(testFile)));
        String previous = "";
        String sentence = "";
        int sentenceCounter = 0;

        for (Token w = (Token) parser.next(); w != null; w = (Token) parser.next()) {
            String prediction = tagger.discreteValue(w);
            if (prediction.startsWith("B-") || prediction.startsWith("I-")
                    && !previous.endsWith(prediction.substring(2)))
                sentence += ("[" + prediction.substring(2) + " ");

            sentence += ("(" + w.partOfSpeech + " " + w.form + ") ");

            if (!prediction.equals("O")
                    && (w.next == null
                    || tagger.discreteValue((Token)w.next).equals("O")
                    || tagger.discreteValue((Token)w.next).startsWith("B-")
                    || !tagger.discreteValue((Token)w.next)
                    .endsWith(prediction.substring(2))))
                sentence += ("] ");

            if (w.next == null) {
                sentence = sentence.trim();
                String refSentence = refSentences.get(sentenceCounter).trim();
                if (!sentence.equals(refSentence))
                    fail("Produced output doesn't match reference: " +
                            "\nProduced: " + sentence +
                            "\nExpected: " + refSentence);
                sentence = "";
                sentenceCounter++;
            }
            previous = prediction;
        }
    }
}
