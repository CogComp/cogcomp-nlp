/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.StringStatisticsUtils;

import java.util.Vector;

public class CharacteristicWords {
    int maxSize;
    public Vector<String> topWords = new Vector<>();
    public Vector<Double> topScores = new Vector<>();

    public CharacteristicWords(int capacity) {
        maxSize = capacity;
    }

    public void addElement(String w, double score) {
        int i = 0;
        while (i < topWords.size() && score <= topScores.elementAt(i))
            i++;
        topWords.insertElementAt(w, i);
        topScores.insertElementAt(score, i);
        if (topWords.size() > maxSize) {
            topScores.removeElementAt(topScores.size() - 1);
            topWords.removeElementAt(topWords.size() - 1);
        }
    }

    public String toString() {
        String res = "";
        for (int i = 0; i < topScores.size(); i++)
            res += (topWords.elementAt(i) + "\t-\t" + topScores.elementAt(i) + "\n");
        return res;
    }
}
