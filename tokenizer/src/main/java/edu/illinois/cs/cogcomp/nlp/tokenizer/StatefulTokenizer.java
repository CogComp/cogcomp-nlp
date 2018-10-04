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
import edu.illinois.cs.cogcomp.nlp.tokenizer.TokenizerStateMachine.State;

/**
 * This is the entry point to the tokenizer state machine. This class is thread-safe, the
 * {@link TokenizerStateMachine} is not.
 * 
 * @author redman
 */
public class StatefulTokenizer implements Tokenizer {
    
    /** this is set to split tokens where dashes are found within words. */
    private boolean splitOnDash;
    
    /** set to split on second consecutive newline. */
    private boolean splitOnSecondNewline;
    
    /**
     * Takes a boolean indicating if we are to split on dash or not. The default
     * constructor assumes we do split on dash.
     */
    public StatefulTokenizer () {
        this(true, false);
    }
    
    /**
     * Takes a boolean indicating if we are to split on dash or not. The default
     * constructor assumes we do split on dash.
     * @param splitOnDash if true, we will split words on a "-".
     * @param splitOnSecondNL split if we encounter two newlines in a row, ignore additional newlines though.
     */
    public StatefulTokenizer (boolean splitOnDash, boolean splitOnSecondNL) {
        super();
        this.splitOnDash = splitOnDash;
        this.splitOnSecondNewline = splitOnSecondNL;
    }
    
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentence) {
        
        // parse the test
        TokenizerStateMachine tsm = new TokenizerStateMachine(splitOnDash, splitOnSecondNewline);
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
        TokenizerStateMachine tsm = new TokenizerStateMachine(splitOnDash, splitOnSecondNewline);
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

    /**
     * @return the splitOnDash to split words when dash encounter or not.
     */
    public boolean isSplitOnDash() {
        return splitOnDash;
    }

    /**
     * @param splitOnDash the splitOnDash to set
     */
    public void setSplitOnDash(boolean splitOnDash) {
        this.splitOnDash = splitOnDash;
    }
    
    /**
     * @return if we split on multiple newlines or not.
     */
    public boolean isSplitOnMultipleNewlines() {
        return splitOnSecondNewline;
    }

    /**
     * @param onnewlines the splitOnDash to set
     */
    public void setSplitOnMultipleNewlines(boolean onnewlines) {
        this.splitOnSecondNewline = onnewlines;
    }
}
