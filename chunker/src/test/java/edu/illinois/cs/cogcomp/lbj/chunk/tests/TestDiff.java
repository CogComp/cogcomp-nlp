/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbj.chunk.tests;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * A sanity check, processing a sample text file and comparing the output to a reference file.
 * 
 * @author Christos Christodoulopoulos
 */
public class TestDiff {
    private static final String testFileName = "testIn.txt";
    private static String testFile;
    private static final String refFileName = "testRefOut.txt";
    private static List<String> refSentences;

    @Before
    public void init() throws IOException, URISyntaxException {
        URL testFileURL = TestDiff.class.getClassLoader().getResource(testFileName);
        assertNotNull("Test file missing", testFileURL);
        testFile = testFileURL.getFile();
        assertNotNull("Reference file missing",
                TestDiff.class.getClassLoader().getResource(refFileName));
        BufferedReader refReader =
                new BufferedReader(new InputStreamReader(TestDiff.class.getClassLoader()
                        .getResourceAsStream(refFileName)));

        refSentences = new ArrayList<>();

        String line;
        while ((line = refReader.readLine()) != null) {
            refSentences.add(line);
        }
    }

    @Test
    public void testDiff() {
        Chunker tagger = new Chunker();
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
                    && (w.next == null || tagger.discreteValue(w.next).equals("O")
                            || tagger.discreteValue(w.next).startsWith("B-") || !tagger
                            .discreteValue(w.next).endsWith(prediction.substring(2))))
                sentence += ("] ");

            if (w.next == null) {
                sentence = sentence.trim();
                String refSentence = refSentences.get(sentenceCounter).trim();
                if (!sentence.equals(refSentence))
                    fail("Produced output doesn't match reference: " + "\nProduced: " + sentence
                            + "\nExpected: " + refSentence);
                sentence = "";
                sentenceCounter++;
            }
            previous = prediction;
        }
    }

    @Test
    public void testGetTagValues() {
        ChunkerAnnotator annotator = new ChunkerAnnotator(true, new ChunkerConfigurator().getDefaultConfig());
        String elements[] = { "B-ADJP", "B-ADVP", "B-CONJP", "B-INTJ", "B-LST", "B-NP", "B-PP", "B-PRT", "B-SBAR",
                "B-UCP", "B-VP", "I-ADJP", "I-ADVP", "I-CONJP", "I-INTJ", "I-NP", "I-PP", "I-PRT", "I-SBAR", "I-UCP",
                "I-VP", "O"};
        Set<String> set = new HashSet(Arrays.asList(elements));
        assertTrue(annotator.getTagValues().equals(set));
    }
}
