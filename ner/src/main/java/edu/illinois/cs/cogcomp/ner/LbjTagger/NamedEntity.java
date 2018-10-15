/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;
import java.util.Vector;


/*
 * This class was created to support confidence-based predictions
 */
public class NamedEntity {
    public static String[] possibleLabels = null;
    NEWord firstWord = null;
    NEWord lastWord = null;
    public Vector<NEWord> tokens = null; // this will link to a token span within data.
    public String form = null;
    public String type = null;// should be filled with the type of the top prediction: PER/LOC/ORG
                              // etc.
    public int absoluteStartWordIndex = -1;// what is the index of the starting word in the data, if
                                           // we ignore sentence boundaries
    public int startTokenSentenceId = -1;// the sentence number of the first token in the data;
    public int startTokenWordInSentenceId = -1;// the offset of the word within the sentence.
    public int endTokenSentenceId = -1;// the sentence number of the first token in the data;
    public int endTokenWordInSentenceId = -1;// the offset of the word within the sentence.
    public double[] confidences = null;// confidences[i] is the confidence of predicting label
                                       // possibleLabels[i];

    public NamedEntity(ArrayList<LinkedVector> sentences, int _absoluteStartWordIndex,
            int _startTokenSentenceId, int _startTokenWordInSentenceId, int _endTokenSentenceId,
            int _endTokenWordInSentenceId) {
        absoluteStartWordIndex = _absoluteStartWordIndex;
        startTokenSentenceId = _startTokenSentenceId;
        startTokenWordInSentenceId = _startTokenWordInSentenceId;
        endTokenSentenceId = _endTokenSentenceId;
        endTokenWordInSentenceId = _endTokenWordInSentenceId;

        int i = startTokenSentenceId;
        int j = startTokenWordInSentenceId;

        firstWord = (NEWord) sentences.get(startTokenSentenceId).get(startTokenWordInSentenceId);
        lastWord = (NEWord) sentences.get(endTokenSentenceId).get(endTokenWordInSentenceId);

        tokens = new Vector<>();
        while (i <= endTokenSentenceId && (i < endTokenSentenceId || j <= endTokenWordInSentenceId)) {
            LinkedVector sentence = sentences.get(i);
            if (i < endTokenSentenceId)
                while (j < sentence.size()) {
                    tokens.addElement((NEWord) sentence.get(j));
                    ((NEWord) sentence.get(j)).predictedEntity = this;
                    j++;
                }
            else
                while (j <= endTokenWordInSentenceId) {
                    tokens.addElement((NEWord) sentence.get(j));
                    ((NEWord) sentence.get(j)).predictedEntity = this;
                    j++;
                }
            i++;
        }
        form = "";
        for (i = 0; i < tokens.size() - 1; i++)
            form += tokens.elementAt(i).form + " ";
        form += tokens.elementAt(tokens.size() - 1).form;
    }

    public boolean equals(NamedEntity other) {
        if (other == null)
            return false;
        if (!this.type.equals(other.type))
            return false;
        if (this.tokens.size() != other.tokens.size())
            return false;
        for (int i = 0; i < this.tokens.size(); i++)
            if (this.tokens.elementAt(i) != other.tokens.elementAt(i))
                return false;
        return true;
    }
}
