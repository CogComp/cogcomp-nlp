package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

/**
 * This class produces a Tokenization with character offsets based on a raw text and a given set of tokens,
 * which must be a sequence of substrings of the raw text in order.
 *
 * @author Xiaotian Le
 */
public class OracleTokenizer {
    public static class IllegalTokenException extends IllegalArgumentException {
        public String rawText;
        public int rawTextOffset;
        public List<String> tokens;
        public int tokensIndex;

        public IllegalTokenException(List<String> tokens, int tokensIndex, String rawText, int rawTextOffset, int displayLength) {
            super("The tokens are not a substring sequence of the raw text: Can't find \"" +
                    tokens.get(tokensIndex) + "\" in \"" +
                    StringUtils.substring(rawText, rawTextOffset, rawTextOffset + displayLength) + "...\".");
            this.tokens = tokens;
            this.tokensIndex = tokensIndex;
            this.rawText = rawText;
            this.rawTextOffset = rawTextOffset;
        }
    }

    private static final int DEFAULT_EXCEPTION_DISPLAY_LENGTH = 32;
    private int exceptionDisplayLength;

    public OracleTokenizer() {
        this(DEFAULT_EXCEPTION_DISPLAY_LENGTH);
    }

    public OracleTokenizer(int exceptionDisplayLength) {
        this.exceptionDisplayLength = exceptionDisplayLength;
    }

    /**
     * Produces a Tokenization with character offsets based on
     * 1. a raw text
     * 2. a given set of tokens, which must be a sequence of substrings of the raw text in order
     * 3. a given set of sentence spans, which will be normalized to cover all tokens
    */
    public Tokenizer.Tokenization tokenize(String rawText, List<String> tokens, List<IntPair> sentences) {
        List<IntPair> charOffsets = new ArrayList<>();

        for (int i = 0, rawTextOffset = 0; i < tokens.size(); ++i) {
            String token = tokens.get(i);

            int tokenStartOffset = rawText.indexOf(token, rawTextOffset);
            if (tokenStartOffset == -1) {
                throw new IllegalTokenException(tokens, i, rawText, rawTextOffset, exceptionDisplayLength);
            }

            int tokenEndOffset = tokenStartOffset + token.length();
            charOffsets.add(new IntPair(tokenStartOffset, tokenEndOffset));

            rawTextOffset = tokenEndOffset;
        }

        int[] sentenceEndTokenIndexes = normalizeSentences(sentences, tokens.size());
        return new Tokenizer.Tokenization(tokens.toArray(new String[0]), charOffsets.toArray(new IntPair[0]), sentenceEndTokenIndexes);
    }

    /**
     * Helper for normalizing a sentence span annotation by creating a sentence for each uncovered token span
     * Input sentence spans must be in order and non-overlapping
     */
    public static int[] normalizeSentences(List<IntPair> sentences, int numberOfTokens) {
        List<Integer> sentenceEndTokenIndexes = new ArrayList<>();

        int lastTokenId = 0;
        for (IntPair sentence : sentences) {
            if (sentence.getFirst() > lastTokenId) {
                sentenceEndTokenIndexes.add(sentence.getFirst());
            }

            lastTokenId = sentence.getSecond();
            sentenceEndTokenIndexes.add(sentence.getSecond());
        }

        if (lastTokenId < numberOfTokens) {
            sentenceEndTokenIndexes.add(numberOfTokens);
        }

        return sentenceEndTokenIndexes.stream().mapToInt(i -> i).toArray();
    }
}
