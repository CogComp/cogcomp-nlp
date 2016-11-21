/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.nlp.tokenizer.TokenizerStateMachine.State;

/**
 * This is the entry point to the tokenizer state machine. This class is thread-safe, the
 * {@link TokenizerStateMachine} is not.
 * 
 * @author redman
 */
public class StatefulTokenizer implements Tokenizer {
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentence) {
        // parse the test
        TokenizerStateMachine tsm = new TokenizerStateMachine();
        tsm.parseText(sentence);

        // construct the data needed for the tokenization.
        int words = 0;
        for (State s : tsm.completed) {
            int idx = s.stateIndex();
            if (idx != TokenizerState.IN_SENTENCE.ordinal())
                words++;
        }
        IntPair[] wordOffsets = new IntPair[words];
        String[] tokens = new String[words];
        int wordIndex = 0;
        for (State s : tsm.completed) {
            State ms = (State) s;
            if (s.stateIndex() != TokenizerState.IN_SENTENCE.ordinal()) {
                tokens[wordIndex] = new String(tsm.text, ms.start, ms.end - ms.start);
                wordOffsets[wordIndex++] = new IntPair(ms.start, ms.end);
            }
        }
        return new Pair<>(tokens, wordOffsets);
    }

    @Override
    public Tokenization tokenizeTextSpan(String textSpan) {

        // parse the text
        TokenizerStateMachine tsm = new TokenizerStateMachine();
        tsm.parseText(textSpan);

        // construct the data needed for the tokenization.
        int sentences = 0;
        int words = 0;
        for (State s : tsm.completed) {
            int idx = s.stateIndex();
            if (idx == TokenizerState.IN_SENTENCE.ordinal())
                sentences++;
            else
                words++;
        }
        IntPair[] wordOffsets = new IntPair[words];
        int[] sentenceEnds = new int[sentences];
        String[] tokens = new String[words];
        int sentenceIndex = 0;
        int wordIndex = 0;
        for (State s : tsm.completed) {
            State ms = (State) s;
            if (s.stateIndex() == TokenizerState.IN_SENTENCE.ordinal())
                sentenceEnds[sentenceIndex++] = wordIndex;
            else {
                tokens[wordIndex] = new String(tsm.text, ms.start, ms.end - ms.start);
                wordOffsets[wordIndex++] = new IntPair(ms.start, ms.end);
            }
        }

        // Removing any training sentence containing no words.
        if (sentences > 1)
            if (sentenceEnds[sentences - 1] == sentenceEnds[sentences - 2]) {
                int[] temp = new int[sentences - 1];
                System.arraycopy(sentenceEnds, 0, temp, 0, sentences - 1);
                sentenceEnds = temp;
            }
        return new Tokenization(tokens, wordOffsets, sentenceEnds);
    }
}
