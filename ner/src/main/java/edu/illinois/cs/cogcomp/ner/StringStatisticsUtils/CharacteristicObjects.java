/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.StringStatisticsUtils;

import java.util.Vector;


public class CharacteristicObjects {
    int maxSize;
    public Vector<Object> topObjects = new Vector<>();
    public Vector<Double> topScores = new Vector<>();

    public CharacteristicObjects(int capacity) {
        maxSize = capacity;
    }

    public void addElement(Object o, double score) {
        topObjects.addElement(o);
        topScores.addElement(score);
        if (topObjects.size() > maxSize) {
            int minId = 0;
            for (int i = 0; i < topScores.size(); i++)
                if (topScores.elementAt(minId) > topScores.elementAt(i))
                    minId = i;
            topScores.removeElementAt(minId);
            topObjects.removeElementAt(minId);
        }
    }

    public String toString() {
        String res = "";
        for (int i = 0; i < topScores.size(); i++)
            res += (topObjects.elementAt(i).toString() + "\t-\t" + topScores.elementAt(i) + "\n");
        return res;
    }

    public Object getMax() {
        if (topObjects.size() == 0)
            return null;
        double max = topScores.elementAt(0);
        Object res = topObjects.elementAt(0);
        for (int i = 1; i < topObjects.size(); i++)
            if (topScores.elementAt(i) > max) {
                max = topScores.elementAt(i);
                res = topObjects.elementAt(i);
            }
        return res;
    }
}
