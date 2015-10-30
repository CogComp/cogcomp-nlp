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
public interface Tokenizer
{

    class Tokenization {
        private final String[] tokens;
        private final IntPair[] characterOffsets;
        private final int[] sentenceEndTokenIndexes;
        private List<Pair<String[], IntPair[]>> tokenizedSentences;


        public Tokenization(String[] tokens, IntPair[] characterOffsets, int[] sentenceEndTokenIndexes )
        {
            this.tokens = tokens;
            this.characterOffsets = characterOffsets;
            this.sentenceEndTokenIndexes = sentenceEndTokenIndexes;
        }

        /**
         * get a list of pairs, each pair corresponding to a sentence's tokens and their *absolute* character offsets.
         */
        public List<Pair< String[], IntPair[] >> getTokenizedSentences()
        {
            if ( null == tokenizedSentences )
                buildTokenizedSentences();

            return tokenizedSentences;
        }

        private void buildTokenizedSentences()
        {
            this.tokenizedSentences = new ArrayList<>( sentenceEndTokenIndexes.length );
            Set< Integer > sentenceEndIndexes = new HashSet<>();
            int sentenceIndex = 0;
            IntPair[] sentTokenOffsets = null;
            String[] sentTokens = null;
            int lastSentEnd = 0;

            for ( int i = 0; i < this.tokens.length; ++i )
            {
                if ( sentenceEndIndexes.contains( new Integer( i ) ) )
                {
                    if (null == sentTokens)
                        throw new IllegalArgumentException( "Found null token or offset array. " +
                                "Sentence end indexes must be incorrect." );

                    this.tokenizedSentences.add( new Pair<>(sentTokens, sentTokenOffsets ) );
                    sentTokens = null;
                    sentTokenOffsets = null;
                }

                if ( null == sentTokens )
                {
                    int sentEnd = this.sentenceEndTokenIndexes[ sentenceIndex ];
                    sentTokens = new String[ (sentEnd - lastSentEnd ) ];
                    sentTokenOffsets = new IntPair[ (sentEnd - lastSentEnd ) ];

                    lastSentEnd = sentEnd;
                    sentenceEndIndexes.add(lastSentEnd);
                }
            }
        }


        public String[] getTokens()
        {
            return tokens;
        }

        public int[] getSentenceEndTokenIndexes()
        {
            return sentenceEndTokenIndexes;
        }

        public IntPair[] getCharacterOffsets()
        {
            return characterOffsets;
        }
    }

    /**
     * given a sentence, return a set of tokens and their character offsets
     * @param sentence The sentence string
     * @return A {@link Pair} containing the array of tokens and their character offsets
     */
	Pair<String[], IntPair[]> tokenizeSentence(String sentence);


    /**
     * given a span of text, return a list of Pair< String[], IntPair[] > corresponding to
     *    tokenized sentences, where the String[] is the ordered list of sentence tokens and
     *    the IntPair[] is the corresponding list of character offsets with respect to <b>the
     *    original text</b>.
     */
	Tokenization tokenizeTextSpan(String textSpan);

}
