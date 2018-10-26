/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * Created by mssammon on 8/17/15.
 * 
 * @author t-redman adapted from original tokenizer tests to test the StatefulTokenizer.
 */
public class StatefullTokenizerTest {
    
    /** input test file. */
    private static final String INFILE =
            "src/test/resources/edu/illinois/cs/cogcomp/nlp/tokenizer/splitterWhitespaceTest.txt";

    /**
     * test whether the mapping between character offset and token index is correct.
     */
    @Test
    public void testCharacterOffsetToTokenIndex() {
        String normal = "The ordinary sample.\n\nDon't mess things up.";
        String leadingWaste = "<ignoreme>wastedspace</ignoreme>";
        String postWaste = "   \n<ignoremetoo>aaaargh</ignoremetoo>";
        String other = leadingWaste + normal + postWaste;
        TextAnnotationBuilder tabldr = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation taNormal = tabldr.createTextAnnotation("test", "normal", normal);
        List<Constituent> normalToks = taNormal.getView(ViewNames.TOKENS).getConstituents();
        assertEquals(13, normalToks.get(2).getStartCharOffset());
        assertEquals(24, normalToks.get(5).getStartCharOffset());
        int ignoreUpToOffset = leadingWaste.length();
        IntPair[] characterOffsets = new IntPair[10];
        String[] tokens = taNormal.getTokens();

        for (int i = 0; i < normalToks.size(); ++i) {
            Constituent t = normalToks.get(i);
            characterOffsets[i] =
                    new IntPair(ignoreUpToOffset + t.getStartCharOffset(), ignoreUpToOffset
                            + t.getEndCharOffset());
        }
        List<Constituent> sentences = taNormal.getView(ViewNames.SENTENCE).getConstituents();
        int[] sentenceEndPositions = new int[sentences.size()];
        for (int i = 0; i < sentences.size(); ++i) {
            Constituent s = sentences.get(i);
            sentenceEndPositions[i] = s.getEndSpan();
        }
        // all info should be same except initial char offsets of tokens ignore spans of text
        TextAnnotation taOther =
                new TextAnnotation("test", "other", other, characterOffsets, tokens,
                        sentenceEndPositions);
        List<Constituent> otherToks = taOther.getView(ViewNames.TOKENS).getConstituents();
        int thirdTokNormalStart = normalToks.get(2).getStartCharOffset();
        int thirdTokOtherStart = otherToks.get(2).getStartCharOffset();
        assertEquals(thirdTokOtherStart, (thirdTokNormalStart + leadingWaste.length()));
        int eighthTokNormalStart = normalToks.get(8).getStartCharOffset();
        int eighthTokOtherStart = otherToks.get(8).getStartCharOffset();
        assertEquals(eighthTokOtherStart, (eighthTokNormalStart + leadingWaste.length()));
        int meaninglessStartOffset = taOther.getTokenIdFromCharacterOffset(2);
        assertEquals(-1, meaninglessStartOffset);
        int meaninglessPastEndOffset =
                taOther.getTokenIdFromCharacterOffset(leadingWaste.length() + normal.length() + 5);
        assertEquals(-1, meaninglessPastEndOffset);
        int meaninglessInBetweenToksOffset = taNormal.getTokenIdFromCharacterOffset(20);
        assertEquals(-1, meaninglessInBetweenToksOffset);
    }

    /**
     * Test method for {@link IllinoisTokenizer} .
     */
    @Test
    public void testStatefulTokenizer() {
        Tokenizer tokenizer = new StatefulTokenizer();

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

    /**
     * Test the stateful tokenizer doing multi line tests.
     */
    @Test
    public void testStatefulTokenizerMultiline() {
        Tokenizer tkr = new StatefulTokenizer();
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
        int lineSepLength = System.lineSeparator().length();
        IntPair intolerantOffsets = new IntPair(76 + lineSepLength, 86 + lineSepLength);
        assertEquals(intolerantOffsets, tokenOffsets[intolerantIndex]);
    }
    
    /**
     * Test a tokenizers ability to tokenize sentences.
     * 
     * @param tokenizer the tokenizer to use.
     * @param sentence the sentence to process.
     * @param tokens the set of word tokens.
     * @param offsets the offsets.
     */
    private void doTokenizerTest(Tokenizer tokenizer, String sentence, String[] tokens,
            IntPair[] offsets) {
        Pair<String[], IntPair[]> tokenize = tokenizer.tokenizeSentence(sentence);

        assertEquals(tokens.length, tokenize.getFirst().length);
        assertEquals(tokens.length, tokenize.getSecond().length);

        for (int i = 0; i < tokens.length; i++) {
            assertEquals(tokens[i], tokenize.getFirst()[i]);
            assertEquals(offsets[i], tokenize.getSecond()[i]);
        }
    }


    /**
     * Test Splitter behavior on text with leading/trailing whitespace. Example is use case where
     * xml markup has been replaced with whitespace of equal span.
     */
    @Test
    public void testWhitespaceBehavior() {
        String origText = null;
        try {
            origText = LineIO.slurp(INFILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        Pattern xmlTagPattern = Pattern.compile("(<[^>\\r\\n]+>)");

        Matcher xmlMatcher = xmlTagPattern.matcher(origText);
        StringBuilder cleanTextBldr = new StringBuilder();
        int lastAppendedCharOffset = 0;

        while (xmlMatcher.find()) {
            int start = xmlMatcher.start();
            int end = xmlMatcher.end();
            cleanTextBldr.append(origText.substring(lastAppendedCharOffset, start));
            for (int i = start; i < end; ++i)
                cleanTextBldr.append(" ");
            lastAppendedCharOffset = end;
        }
        cleanTextBldr.append(origText.substring(lastAppendedCharOffset));
        String cleanText = cleanTextBldr.toString();

        // count whitespace chars in string

        // check token offsets in tokens returned by SentenceSplitter

        Pattern sun = Pattern.compile("\\w*Sun\\w*");
        Matcher sunMatcher = sun.matcher(cleanText);

        Set<IntPair> sunSpans = new HashSet<>();
        while (sunMatcher.find())
            sunSpans.add(new IntPair(sunMatcher.start(), sunMatcher.end()));


        SentenceSplitter splitter = new SentenceSplitter(new String[] {cleanText});
        Sentence[] sents = splitter.splitAll();
        Sentence s = sents[0];
        LinkedVector words = s.wordSplit();
        for (int i = 0; i < words.size(); ++i) {

            Word firstWord = (Word) words.get(0);
            if ("Sun".equals(firstWord.form)) {
                IntPair tokenCharOffsets = new IntPair(firstWord.start, firstWord.end);

                assertTrue(sunSpans.contains(tokenCharOffsets));
            }
        }

        StatefulTokenizer statefulTokenizer = new StatefulTokenizer();
        Tokenizer.Tokenization tokenInfo = statefulTokenizer.tokenizeTextSpan(cleanText);

        assertEquals(tokenInfo.getCharacterOffsets().length, tokenInfo.getTokens().length);
        for (int i = 0; i < tokenInfo.getTokens().length; ++i) {
            String tok = tokenInfo.getTokens()[i];
            if (tok.equals("Sun")) {
                IntPair tokCharOffsets = tokenInfo.getCharacterOffsets()[i];

                if (!sunSpans.contains(tokCharOffsets)) {
                    String origTextSubstring =
                            cleanText.substring(tokCharOffsets.getFirst(),
                                    tokCharOffsets.getSecond());
                    System.err.println("ERROR: tokenizer has form '" + tok
                            + "', but offsets refer to substring '" + origTextSubstring + "'.");
                }
                assertTrue(sunSpans.contains(tokCharOffsets));
            }
        }

        TextAnnotation statefulTa =
                new TextAnnotation("test", "test", cleanText, tokenInfo.getCharacterOffsets(),
                        tokenInfo.getTokens(), tokenInfo.getSentenceEndTokenIndexes());
        assertNotNull(statefulTa);
    }

    /**
     * Parse an empty string.
     */
    @Test
    public void testEmptyString() {
        Tokenizer tkr = new StatefulTokenizer();
        String text = "";
        Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
        assertEquals(tknzn.getTokens().length, 0);
    }

    /**
     * Parse an empty string.
     */
    @Test
    public void testSentenceSplitOnMultipleNewlines() {
        TokenizerTextAnnotationBuilder bldr =
                        new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, true));
        String text = "Mary loves Dick. Dick loves Jane.";
        TextAnnotation taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 2);
        text = "Mary loves Dick\n\nDick loves Jane.";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 2);
        text = "Mary loves Dick\n\n\nDick loves Jane.";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 2);
        text = "Mary loves Dick\n\n\n\nDick loves Jane.\n\n";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 2);
        text = "\n\nMary loves Dick\n\n\n\nDick loves Jane.\n\n";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 2);
    }

    /**
     * Parse out a date, which will hopefully look like a date.
     */
    @Test
    public void testDateTokenization() {
        TokenizerTextAnnotationBuilder bldr =
                        new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, true));
        String tmp = "One two, three-four-five 10/23/2018 at 5:20pm one? Of course not! Be well, stranger. Bye-bye!";
        TextAnnotation taA = bldr.createTextAnnotation("test", "test", tmp);
        String[] toks = taA.getTokens();
        assertEquals(toks[8], "10/23/2018");
    }
    
    /**
     * Parse an empty string.
     */
    @Test
    public void testDecimalNotation() {
        TokenizerTextAnnotationBuilder bldr =
                        new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, true));
        String text = "$1.09 percent like me.";
        TextAnnotation taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 1);
        String[] toks = taA.getTokens();
        assertEquals(toks[0], "$1.09");
        
        text = "Take the $.10 tour.";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 1);
        toks = taA.getTokens();
        assertEquals(toks[2], "$.10");

        text = "Take the $10B tour.";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 1);
        toks = taA.getTokens();
        assertEquals(toks[2], "$10B");
        text = "\n(\n$.10)";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 1);
        toks = taA.getTokens();
        assertEquals(toks[1], "$.10");
        assertEquals(toks[0], "(");
        assertEquals(toks[2], ")");
        assertEquals(taA.getNumberOfSentences(), 1);
    }

    /**
     * Parse an empty string.
     */
    @Test
    public void testEmail() {
        TokenizerTextAnnotationBuilder bldr =
                        new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, true));
        String text = "Although tomredman@mchsi.com is an email address.";
        TextAnnotation taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 1);
        String[] toks = taA.getTokens();
        assertEquals(toks[1], "tomredman@mchsi.com");
        
        text = "JLama@summat To robert_serafin@kmz.com";
        taA = bldr.createTextAnnotation("test", "test", text);
        assertEquals(taA.getNumberOfSentences(), 1);
        toks = taA.getTokens();
        assertEquals(toks[0], "JLama@summat");
        assertEquals(toks[2], "robert_serafin@kmz.com");

    }

    /**
     * Make sure newline is recognized.
     */
    @Test
    public void testStringWithNewline() {
        Tokenizer tkr = new StatefulTokenizer();
        String text = "this\nsentence";
        Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
        assertEquals(tknzn.getTokens().length, 2);
    }
    
    /**
     * Test that splitting on dash works correctly.
     */
    @Test
    public void testSplitOnDash() {
        Tokenizer tkr = new StatefulTokenizer();
        String text = "IAEA Director-General Mohamed ElBaradei ";
        Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
        assertEquals(tknzn.getTokens().length, 6);
    }

    /**
     * Test sentence splitter behavior when a there is a lower cased acronym followed immediately by a dot.
     */
    @Test
    public void testLowerCaseAcronymEndWithDot(){
        TokenizerTextAnnotationBuilder tab =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, true));
        String text = "I was born in Urbana, Il. in 1992.";
        TextAnnotation ta = tab.createTextAnnotation(text);
        assertEquals(ta.getNumberOfSentences(), 1);
    }
    
    /**
     * This can be used to just quickly debug when a sentence produces an error.
     * @param args
     */
    public static void main(String [] args) {
        Tokenizer tkr = new StatefulTokenizer(false, false);
        String text = "We going to--. ";
        tkr.tokenizeTextSpan(text);
    }

    /**
     * Test the ending with a couple dashes.
     */
    @Test
    public void testSplitPeriodEnd() {
        Tokenizer tkr = new StatefulTokenizer(false, false);
        String text = "You see always, oh we're going to do this, we're going to--. ";
        Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
        assertEquals(tknzn.getTokens().length, 17);
        tkr = new StatefulTokenizer(true, false);
        tknzn = tkr.tokenizeTextSpan(text);
        assertEquals(tknzn.getTokens().length, 18);
    }
}
