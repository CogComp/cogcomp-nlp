/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utility;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.TokenUtils;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

/**
 * A set of convenience methods for constructing TextAnnotations. Replaces a morass of specialized
 * constructors in Edison to support use of illinois-core-utilities.
 *
 * @author Mark Sammons
 * @author Narender Gupta
 */
public class TokenizerTextAnnotationBuilder implements TextAnnotationBuilder {
    private static final String NAME = TokenizerTextAnnotationBuilder.class.getSimpleName();

    private static final String DEFAULT_TEXT_ID = "dummyTextId";
    private static final String DEFAULT_CORPUS_ID = "dummyCorpusId";

    private Tokenizer tokenizer;

    /**
     * instantiate a TextAnnotationBuilder.
     * 
     * @param tokenizer The Tokenizer that will split text into sentences and words.
     */
    public TokenizerTextAnnotationBuilder(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    /**
     * instantiate a TextAnnotation using a SentenceViewGenerator to create an explicit Sentence
     * view
     *
     * @param corpusId a field in TextAnnotation that can be used by the client for book-keeping
     *        (e.g. track texts from the same corpus)
     * @param textId a field in TextAnnotation that can be used by the client for book-keeping (e.g.
     *        identify a specific document by some reference string)
     * @param text the plain English text to process
     * @param tokens the token Strings, in order from original text
     * @param sentenceEndPositions token offsets of sentence ends (one-past-the-end indexing)
     * @param sentenceViewGenerator the name of the source of the sentence split
     * @param sentenceViewScore a score that may indicate how reliable the sentence split
     *        information is
     * @return a TextAnnotation object with {@link ViewNames#TOKENS} and {@link ViewNames#SENTENCE}
     *         views.
     */
    public static TextAnnotation buildTextAnnotation(String corpusId, String textId, String text,
            String[] tokens, int[] sentenceEndPositions, String sentenceViewGenerator,
            double sentenceViewScore) {

        if (sentenceEndPositions[sentenceEndPositions.length - 1] != tokens.length)
            throw new IllegalArgumentException(
                    "Invalid sentence boundary. Last element should be the number of tokens");


        IntPair[] offsets = TokenUtils.getTokenOffsets(text, tokens);

        assert offsets.length == tokens.length;

        TextAnnotation ta =
                new TextAnnotation(corpusId, textId, text, offsets, tokens, sentenceEndPositions);

        SpanLabelView view =
                new SpanLabelView(ViewNames.SENTENCE, sentenceViewGenerator, ta, sentenceViewScore);

        int start = 0;
        for (int s : sentenceEndPositions) {
            view.addSpanLabel(start, s, ViewNames.SENTENCE, 1d);
            start = s;
        }
        ta.addView(ViewNames.SENTENCE, view);

        SpanLabelView tokView =
                new SpanLabelView(ViewNames.TOKENS, sentenceViewGenerator, ta, sentenceViewScore);



        for (int tokIndex = 0; tokIndex < tokens.length; ++tokIndex) {
            tokView.addSpanLabel(tokIndex, tokIndex + 1, tokens[tokIndex], 1d);
        }

        ta.addView(ViewNames.TOKENS, tokView);

        return ta;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * create a TextAnnotation for the text argument, using the Tokenizer provided at construction.
     * The text should be free from html/xml tags and non-English characters, assuming you want to
     * process this text with other NLP components.
     *
     * @param text the text to build the TextAnnotation
     * @return a TextAnnotation object with {@link ViewNames#SENTENCE} and {@link ViewNames#TOKENS}
     *         views and default corpus id and text id fields.
     * @throws IllegalArgumentException if the tokenizer has problems processing the text.
     */
    @Override
    public TextAnnotation createTextAnnotation(String text) throws IllegalArgumentException {
        return createTextAnnotation(DEFAULT_CORPUS_ID, DEFAULT_TEXT_ID, text);
    }

    /**
     * Tokenize the input text (split into sentences and "words" within sentences) and populate a
     * TextAnnotation object. Specifies token character offsets with respect to original text. Input
     * text should be English and avoid html and xml tags, and non-English characters may cause
     * problems if you use the TextAnnotation as input to other NLP components.
     *
     * @param corpusId a field in TextAnnotation that can be used by the client for book-keeping
     *        (e.g. track texts from the same corpus)
     * @param textId a field in TextAnnotation that can be used by the client for book-keeping (e.g.
     *        identify a specific document by some reference string)
     * @param text the plain English text to process
     * @return a TextAnnotation object with {@link ViewNames#TOKENS} and {@link ViewNames#SENTENCE}
     *         views.
     * @throws IllegalArgumentException if the tokenizer has problems with the input text.
     */
    @Override
    public TextAnnotation createTextAnnotation(String corpusId, String textId, String text)
            throws IllegalArgumentException {
        Tokenizer.Tokenization tokenization = tokenizer.tokenizeTextSpan(text);
        TextAnnotation ta = new TextAnnotation(corpusId, textId, text, tokenization.getCharacterOffsets(),
                tokenization.getTokens(), tokenization.getSentenceEndTokenIndexes());
        SpanLabelView view =
                new SpanLabelView(ViewNames.SENTENCE, NAME, ta, 1.0);

        int start = 0;
        for (int s : tokenization.getSentenceEndTokenIndexes()) {
            view.addSpanLabel(start, s, ViewNames.SENTENCE, 1d);
            start = s;
        }
        ta.addView(ViewNames.SENTENCE, view);

        return ta;
    }

    /**
     * A stub method that <b>should not</b> be called with this Builder. Please use
     * {@link edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder} if you need to create
     * {@link TextAnnotation} from pre-tokenized text.
     */
    @Override
    public TextAnnotation createTextAnnotation(String corpusId, String textId, String text,
            Tokenizer.Tokenization tokenization) throws IllegalArgumentException {
        throw new IllegalArgumentException(
                "Cannot create annotation from tokenized text using TokenizerTextAnnotationBuilder");
    }

    /**
     * Create a new text annotation using the given text, the tokens and the sentence boundary
     * positions (only the ending positions), specified in terms of the tokens.
     * <p>
     * For example, for the text "Jack went up the hill. So did Jill.", the tokens would be the
     * array {"Jack", "went", "up", "the", "hill", "." ,"So", "did", "Jill", "."} and the array of
     * sentence boundary array would be {6, 11}. If the last element of the sentence boundary array
     * is not equal to the size of the tokens array, an IllegalArgumentException is raised.
     *
     * @param corpusId A string that identifies the corpus
     * @param id A string that identifies this text
     * @param text The text it self
     * @param tokens The array of tokens of this text
     * @param sentenceEndPositions The ending positions of sentences, specified as indices to the
     *        tokens array. Note that the end positions are exclusive -- for example, if the
     *        sentence ends at the 7th token, then the end position for that sentence would be 8.
     */
    public TextAnnotation buildTextAnnotation(String corpusId, String id, String text,
            String[] tokens, int[] sentenceEndPositions) {
        return TokenizerTextAnnotationBuilder.buildTextAnnotation(corpusId, id, text, tokens,
                sentenceEndPositions, "UserSpecified", 1.0d);
    }

}
