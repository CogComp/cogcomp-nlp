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
import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedChild;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.*;

/**
 * Created by mssammon on 7/27/15.
 * @deprecated use StatefulTokenizer instead.
 */
public class IllinoisTokenizer implements Tokenizer {


    /**
     * given a sentence, return a set of tokens and their character offsets
     *
     * @param sentence the plain text sentence to tokenize
     * @return an ordered list of tokens from the sentence, and an ordered list of their start and
     *         end character offsets (one-past-the-end indexing)
     */
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentence) {
        Sentence lbjSentence = new Sentence(sentence);

        LinkedVector wordSplit = lbjSentence.wordSplit();

        String[] output = new String[wordSplit.size()];

        IntPair[] offsets = new IntPair[wordSplit.size()];

        for (int i = 0; i < output.length; i++) {
            LinkedChild linkedChild = wordSplit.get(i);
            output[i] = linkedChild.toString();
            offsets[i] = new IntPair(linkedChild.start, linkedChild.end + 1);
        }
        return new Pair<>(output, offsets);
    }

    /**
     * given a span of text, return a list of {@literal Pair< String[], IntPair[] >} corresponding
     * to tokenized sentences, where the String[] is the ordered list of sentence tokens and the
     * IntPair[] is the corresponding list of character offsets with respect to <b>the original
     * text</b>.
     *
     * @param text an arbitrary span of text.
     * @return a {@link Tokenization} object containing the ordered token strings, their character
     *         offsets, and sentence end positions (as one-past-the-end token offsets)
     */

    @Override
    public Tokenization tokenizeTextSpan(String text) {
        String[] splitterInput = new String[1];
        splitterInput[0] = text;

        SentenceSplitter splitter = new SentenceSplitter(splitterInput);
        Sentence[] sentences = splitter.splitAll();

        List<IntPair> characterOffsets = new LinkedList<>();
        int[] sentenceEndTokenOffsets = new int[sentences.length];
        int sentenceEndTokenIndex = 0;
        int sentIndex = 0;
        List<String> tokens = new LinkedList<>();

        for (Sentence s : splitter.splitAll()) {
            LinkedVector words = s.wordSplit();
            if (s.end >= text.length()) {
                throw new IllegalArgumentException("Error in tokenizer, sentence end ( " + s.end
                        + ") is greater than rawtext length (" + text.length() + ").");
            }

            for (int i = 0; i < words.size(); i++) {
                Word word = (Word) words.get(i);
                IntPair wordOffsets = new IntPair(word.start, word.end + 1);
                characterOffsets.add(wordOffsets);
                tokens.add(text.substring(wordOffsets.getFirst(), wordOffsets.getSecond()));
            }

            sentenceEndTokenIndex += words.size();
            sentenceEndTokenOffsets[sentIndex++] = sentenceEndTokenIndex;
        }
        String[] tokenArray = tokens.toArray(new String[tokens.size()]);
        IntPair[] charOffsetArray = characterOffsets.toArray(new IntPair[characterOffsets.size()]);


        return new Tokenization(tokenArray, charOffsetArray, sentenceEndTokenOffsets);
    }
}
