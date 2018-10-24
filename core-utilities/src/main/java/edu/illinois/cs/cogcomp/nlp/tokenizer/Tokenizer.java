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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Specifies behavior for Tokenizers integrated into Cognitive Computation Group NLP software
 *
 * @author Mark Sammons
 * @author Vivek Srikumar
 * @author Christos Christodoulopoulos
 */
public interface Tokenizer {

    class Tokenization {
        private final String[] tokens;
        private final IntPair[] characterOffsets;
        private final int[] sentenceEndTokenIndexes;

        public Tokenization(String[] tokens, IntPair[] characterOffsets,
                int[] sentenceEndTokenIndexes) {
            this.tokens = tokens;
            this.characterOffsets = characterOffsets;
            this.sentenceEndTokenIndexes = sentenceEndTokenIndexes;
        }

        public String[] getTokens() {
            return tokens;
        }

        public int[] getSentenceEndTokenIndexes() {
            return sentenceEndTokenIndexes;
        }

        public IntPair[] getCharacterOffsets() {
            return characterOffsets;
        }
    }

    /**
     * given a sentence, return a set of tokens and their character offsets
     * 
     * @param sentence The sentence string
     * @return A {@link Pair} containing the array of tokens and their character offsets
     */
    Pair<String[], IntPair[]> tokenizeSentence(String sentence);


    /**
     * given a span of text, return a list of Pair{@literal < String[], IntPair[] >} corresponding
     * to tokenized sentences, where the String[] is the ordered list of sentence tokens and the
     * IntPair[] is the corresponding list of character offsets with respect to <b>the original
     * text</b>.
     */
    Tokenization tokenizeTextSpan(String textSpan);

}
