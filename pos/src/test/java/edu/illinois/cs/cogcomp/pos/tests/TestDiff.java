/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos.tests;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A sanity check, processing a sample text file and comparing the output to a reference file.
 * 
 * @author Christos Christodoulopoulos
 */
public class TestDiff {

    private static final String testFileName = "testIn.txt";
    private static String testFile;
    private static final String refFileName = "testRefOutput.txt";
    private static List<String> refTags;
    private static List<String[]> refTokens;
    private static final double thresholdAcc = 0.95;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        URL testFileURL = TestDiff.class.getClassLoader().getResource(testFileName);
        assertNotNull("Test file missing", testFileURL);
        testFile = testFileURL.getFile();
        assertNotNull("Reference file missing",
                TestDiff.class.getClassLoader().getResource(refFileName));
        BufferedReader refReader =
                new BufferedReader(new InputStreamReader(TestDiff.class.getClassLoader()
                        .getResourceAsStream(refFileName)));

        refTags = new ArrayList<>();
        refTokens = new ArrayList<>();
        String line;
        while ((line = refReader.readLine()) != null) {
            String[] entries = line.substring(1, line.length() - 1).split("[)] [(]");

            for (int i = 0; i < entries.length; i++) {
                refTags.add(entries[i].split(" ")[0]);
                entries[i] = entries[i].split(" ")[1];
            }
            refTokens.add(entries);
        }
    }

    @Test
    public void testDiff() {
        POSTagger tagger = new POSTagger();
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
        double result = ((double) correctCounter) / tokenCounter;
        if (result < thresholdAcc) {
            fail("Tagger performance is insufficient: " + "\nProduced: " + result + "\nExpected: "
                    + thresholdAcc);
        }
    }

    @Test
    public void testAnnotatorDiff() {
        POSAnnotator annotator = new POSAnnotator();

        TextAnnotation record =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(refTokens);
        try {
            annotator.getView(record);
        } catch (AnnotatorException e) {
            fail("AnnotatorException thrown!\n" + e.getMessage());
        }

        TokenLabelView view = (TokenLabelView) record.getView(ViewNames.POS);
        if (refTags.size() != view.getNumberOfConstituents()) {
            fail("Number of tokens tagged in annotator does not match actual number of tokens!");
        }
        int correctCounter = 0;
        for (int i = 0; i < refTags.size(); i++) {
            if (view.getLabel(i).equals(refTags.get(i))) {
                correctCounter++;
            }
        }
        double result = ((double) correctCounter) / refTags.size();

        if (result < thresholdAcc) {
            fail("Tagger performance is insufficient: " + "\nProduced: " + result + "\nExpected: "
                    + thresholdAcc);
        }

    }

    @Test
    public void testGetTagValues() {
        POSAnnotator annotator = new POSAnnotator();
        String elements[] = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT", "EX", "FW", "IN",
                "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR",
                "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };
        Set<String> set = new HashSet(Arrays.asList(elements));
        assertTrue(annotator.getTagValues().equals(set));
    }
}
