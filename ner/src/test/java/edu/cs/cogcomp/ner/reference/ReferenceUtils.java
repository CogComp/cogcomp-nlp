/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.ner.reference;

import edu.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.cs.cogcomp.ner.LbjTagger.Data;
import edu.cs.cogcomp.ner.LbjTagger.NERDocument;

import java.util.ArrayList;

/**
 * Utilities to support comparison of NER LBJava feature extractors with reference implementations
 * from Edison
 */
public class ReferenceUtils {

    public Data createNerDataStructuresForText(TextAnnotation ta) {
        ArrayList<LinkedVector> sentences = new ArrayList<>();
        String[] tokens = ta.getTokens();
        int[] tokenindices = new int[tokens.length];
        int tokenIndex = 0;
        int neWordIndex = 0;
        for (int i = 0; i < ta.getNumberOfSentences(); i++) {
            Sentence sentence = ta.getSentence(i);
            String[] wtoks = sentence.getTokens();
            LinkedVector words = new LinkedVector();
            for (String w : wtoks) {
                if (w.length() > 0) {
                    NEWord.addTokenToSentence(words, w, "unlabeled");
                    tokenindices[neWordIndex] = tokenIndex;
                    neWordIndex++;
                } else {
                    throw new IllegalStateException("Bad (zero length) token.");
                }
                tokenIndex++;
            }
            if (words.size() > 0)
                sentences.add(words);
        }

        // Do the annotation.
        Data data = new Data(new NERDocument(sentences, "input"));
        return data;
    }
}
