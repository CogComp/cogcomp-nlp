/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * Created by mssammon on 8/17/15.
 */
public class IllinoisTokenizerTest {
    private static Logger logger = LoggerFactory.getLogger(IllinoisTokenizerTest.class);

    @Before
    public void setUp() throws Exception {}

    /**
     * Test method for {@link IllinoisTokenizer} .
     */
    @Test
    public void testIllinoisTokenizer() {
        Tokenizer tokenizer = new IllinoisTokenizer();

        String sentence = "This is a   test.";
        String[] tokens = {"This", "is", "a", "test", "."};
        IntPair[] offsets = new IntPair[tokens.length];
        offsets[0] = new IntPair(0, 4);
        offsets[1] = new IntPair(5, 7);
        offsets[2] = new IntPair(8, 9);
        offsets[3] = new IntPair(12, 16);
        offsets[4] = new IntPair(16, 17);

        doTokenizerTest(tokenizer, sentence, tokens, offsets);

        sentence = "Hello, world! I am at UIUC.";
        tokens = new String[] {"Hello", ",", "world", "!", "I", "am", "at", "UIUC", "."};
        offsets = new IntPair[tokens.length];
        offsets[0] = new IntPair(0, 5);
        offsets[1] = new IntPair(5, 6);
        offsets[2] = new IntPair(7, 12);
        offsets[3] = new IntPair(12, 13);
        offsets[4] = new IntPair(14, 15);
        offsets[5] = new IntPair(16, 18);
        offsets[6] = new IntPair(19, 21);
        offsets[7] = new IntPair(22, 26);
        offsets[8] = new IntPair(26, 27);

        doTokenizerTest(tokenizer, sentence, tokens, offsets);
    }

    @Test
    public void testIllinoisTokenizerMultiline() {
        Tokenizer tkr = new IllinoisTokenizer();
        String text =
                "Mr. Dawkins -- a liberal professor -- doesn't like fundamentalists.   "
                        + System.lineSeparator() + "He is intolerant of intolerance!";

        Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
        int[] sentEndOffsets = tknzn.getSentenceEndTokenIndexes();

        assertEquals(2, sentEndOffsets.length);

        assertEquals(12, sentEndOffsets[0]);
        assertEquals(18, sentEndOffsets[1]);

        String[] tokens = tknzn.getTokens();

        assertEquals("--", tokens[6]);
        assertEquals("of", tokens[15]);

        IntPair[] tokenOffsets = tknzn.getCharacterOffsets();
        int notIndex = 8;
        IntPair notOffsets = new IntPair(42, 45);

        assertEquals(notOffsets, tokenOffsets[notIndex]);

        int intolerantIndex = 14;

        IntPair intolerantOffsets = new IntPair(77, 87);

        assertEquals(intolerantOffsets, tokenOffsets[intolerantIndex]);
    }

    private void doTokenizerTest(Tokenizer tokenizer, String sentence, String[] tokens,
            IntPair[] offsets) {
        logger.info(sentence);
        Pair<String[], IntPair[]> tokenize = tokenizer.tokenizeSentence(sentence);

        assertEquals(tokens.length, tokenize.getFirst().length);
        assertEquals(tokens.length, tokenize.getSecond().length);

        for (int i = 0; i < tokens.length; i++) {
            assertEquals(tokens[i], tokenize.getFirst()[i]);
            assertEquals(offsets[i], tokenize.getSecond()[i]);
        }
    }

    @Test
    public void testIllinoisTokenizerEmptyString() {
        Tokenizer tkr = new IllinoisTokenizer();
        String text = "";
        Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
        assertEquals(tknzn.getTokens().length, 0);
    }

    @Test
    public void testIllinoisTokenizerStringWithNewline() {
        Tokenizer tkr = new IllinoisTokenizer();
        String text = "this\nsentence";
        Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
        assertEquals(tknzn.getTokens().length, 2);
    }
}
