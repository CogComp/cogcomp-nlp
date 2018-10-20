/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer.Tokenization;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple TextAnnotationBuilder that is meant to be used with pre-tokenized text (e.g. for
 * training corpora).
 * <p>
 * To create {@link TextAnnotation}s from plain text, you need a
 * {@link edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer}. See {@code illinois-tokenizer} for
 * CogComp's default TextAnnotationBuilder.
 *
 * @author Mark Sammons
 * @author Christos Christodoulopoulos
 * @author Narender Gupta
 */
public class BasicTextAnnotationBuilder implements TextAnnotationBuilder {

    private static final String NAME = "BasicTextAnnotationBuilder";

    /**
     * The default way to create a {@link TextAnnotation} from pre-tokenized text.
     * 
     * @param tokenizedSentences A list of sentences, each one being an array of tokens
     * @return A {@link TextAnnotation} containing the SENTENCE and TOKENS views.
     */
    public static TextAnnotation createTextAnnotationFromTokens(List<String[]> tokenizedSentences) {
        return createTextAnnotationFromTokens("", "", tokenizedSentences);
    }


    /**
     * A way to create a {@link TextAnnotation} from pre-tokenized text from Python
     *
     * @param tokenizedSentences A list of sentences, each one being an list of tokens
     * @return A {@link TextAnnotation} containing the SENTENCE and TOKENS views.
     */
    public static TextAnnotation createTextAnnotationFromListofListofTokens(List<List<Object>> tokenizedSentences) {
        // This function takes List<List<Object>> to be able to run with cogcomp-nlpy (using pyjnius)
        // Convert the inner lists to String arrays
        // Call the default TextAnnotation builder function

        List<String[]> tokenizedSentences_formatted = new ArrayList<String[]>();

        // Converting inner list to array
        for (List<Object> sentence : tokenizedSentences) {
            String[] sentence_array = new String[sentence.size()];
            int token_idx = 0;
            for (Object w : sentence) {
                sentence_array[token_idx] = (String) w;
                token_idx += 1;
            }
            tokenizedSentences_formatted.add(sentence_array);
        }

        return createTextAnnotationFromTokens("", "", tokenizedSentences_formatted);
    }


    /**
     * The default way to create a {@link TextAnnotation} from pre-tokenized text.
     * 
     * @param tokenizedSentences A list of sentences, each one being a list of tokens
     * @return A {@link TextAnnotation} containing the SENTENCE and TOKENS views.
     */
    public static TextAnnotation createTextAnnotationFromTokens(String corpusId, String textId,
            List<String[]> tokenizedSentences) {
        Tokenization tokenization = tokenizeTextSpan(tokenizedSentences);
        StringBuilder text = new StringBuilder();
        for (String[] sentenceTokens : tokenizedSentences)
            text.append(StringUtils.join(sentenceTokens, ' '))
                    .append(System.lineSeparator());

        return new TextAnnotation(corpusId, textId, text.toString(), tokenization.getCharacterOffsets(),
                tokenization.getTokens(), tokenization.getSentenceEndTokenIndexes());
    }

    /**
     * A stub method that <b>should not</b> be call with this Builder. Please use
     * {@link #createTextAnnotationFromTokens(java.util.List)} instead.
     * <p>
     * To create a {@link TextAnnotation} from raw text, please use {@code illinois-tokenizer}
     */
    @Override
    public TextAnnotation createTextAnnotation(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(
                "Cannot create annotation from raw text using BasicTextAnnotationBuilder");
    }

    /**
     * A stub method that <b>should not</b> be call with this Builder. Please use
     * {@link #createTextAnnotationFromTokens(java.util.List)} instead.
     * <p>
     * To create a {@link TextAnnotation} from raw text, please use {@code illinois-tokenizer}
     */
    @Override
    public TextAnnotation createTextAnnotation(String corpusId, String textId, String text)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(
                "Cannot create annotation from raw text using BasicTextAnnotationBuilder");
    }

    @Override
    public TextAnnotation createTextAnnotation(String corpusId, String textId, String text,
            Tokenization tokenization) throws IllegalArgumentException {
        return new TextAnnotation(corpusId, textId, text, tokenization.getCharacterOffsets(),
                tokenization.getTokens(), tokenization.getSentenceEndTokenIndexes());
    }

    private static Tokenization tokenizeTextSpan(List<String[]> tokenizedSentences) {
        List<String> tokensList = new ArrayList<>();
        List<IntPair> charOffsetsList = new ArrayList<>();

        int[] sentenceEndIndexes = new int[tokenizedSentences.size()];
        int sentIndex = 0;
        int sentStartTokOffset = 0;
        int sentStartCharOffset = 0;

        for (String[] sentenceTokens : tokenizedSentences) {
            sentenceEndIndexes[sentIndex++] = sentStartTokOffset + sentenceTokens.length;

            int tokenStartOffset = 0;
            int nextSentStartCharOffset = 0;

            for (String sentenceToken : sentenceTokens) {
                tokensList.add(sentenceToken);
                int tokenCharStart = sentStartCharOffset + tokenStartOffset;
                int tokenCharEnd = tokenCharStart + sentenceToken.length();

                IntPair translatedCharOffset = new IntPair(tokenCharStart, tokenCharEnd);
                charOffsetsList.add(translatedCharOffset);

                // The next token should start after a single space
                tokenStartOffset += sentenceToken.length() + 1;
                nextSentStartCharOffset = tokenCharEnd + System.lineSeparator().length(); // by end of loop, this should match
                                                            // start of next sentence
            }

            sentStartTokOffset += sentenceTokens.length;
            sentStartCharOffset = nextSentStartCharOffset;
        }

        assert tokensList.size() == charOffsetsList.size();

        String[] tokens = new String[tokensList.size()];
        for (int i = 0; i < tokensList.size(); i++)
            tokens[i] = tokensList.get(i);

        IntPair[] charOffsets = new IntPair[charOffsetsList.size()];
        for (int i = 0; i < charOffsetsList.size(); i++)
            charOffsets[i] = charOffsetsList.get(i);

        return new Tokenization(tokens, charOffsets, sentenceEndIndexes);
    }

    public String getName() {
        return NAME;
    }
}
