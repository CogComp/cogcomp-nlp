/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.TokenizerStateMachine.State;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * This is the entry point to the tokenizer state machine. This class is 
 * thread-safe, the {@link TokenizerStateMachine} is not.
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
        return new Tokenization(tokens, wordOffsets, sentenceEnds);
    }
    
    /**
     * Just for testing.
     * @param args
     */
    static public void main(String[] args) {
        String issue1 = "AP The United Nations Security Council agreed today to send military observers back to Angola if a peace treaty is signed as expected this weekend, diplomats said. A Council resolution authorizes 350 military observers and 126 police observers. Observers were first sent to Angola in 1991 to prepare for elections. Only a few remained after civil war resumed in 1992, when the rebel leader, Jonas Savimbi, rejected his election loss and took up arms again.\"";
        String issue2 = "John is 'a good boy' and he is 'not a dog' or a dogs' or a dog's eye.";
        String issue3 = "At 1:30 a.m. Sunday, the troops moved out: 40 U.S. soldiers in a convoy of Humvees mounted with heavy machine guns, and 60 Afghan National Army troops in pickup trucks.";
        String issue4 = "And John said \"There are those who would.\" So ended his reign.";
        String issue5 = "\"I said, 'what're you? Crazy?'\" said Sandowsky. \"I can't afford to do that.";
        String [] ss = {issue1, issue2, issue3, issue4, issue5};
        for (String issue : ss) {
            final TextAnnotationBuilder stab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
            TextAnnotation ta = stab.createTextAnnotation(issue);
            System.out.println(ta);
            for (int i = 0; i < ta.getNumberOfSentences(); i++)
                System.out.println(ta.getSentence(i));
    
            final TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
            ta = tab.createTextAnnotation(issue);
            for (int i = 0; i < ta.getNumberOfSentences(); i++)
                System.out.println(ta.getSentence(i));
            System.out.println("\n");
        }
    }
}