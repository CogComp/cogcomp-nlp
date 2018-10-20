/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.StringStatisticsUtils;

import java.util.Hashtable;
import java.util.Iterator;


public class OccurrenceCounter {
    public Hashtable<String, Double> counts = null;
    public int uniqueTokens = 0;
    public int totalTokens = 0;

    public void addToken(String s) {
        if (counts == null)
            counts = new Hashtable<>();
        totalTokens++;
        if (counts.containsKey(s)) {
            double v = counts.get(s);
            counts.remove(s);
            counts.put(s, v + 1.0);
        } else {
            uniqueTokens++;
            counts.put(s, 1.0);
        }
    }

    public void addToken(String s, double d) {
        if (counts == null)
            counts = new Hashtable<>();
        totalTokens += d;
        if (counts.containsKey(s)) {
            double v = counts.get(s);
            counts.remove(s);
            counts.put(s, v + d);
        } else {
            uniqueTokens++;
            counts.put(s, d);
        }
    }

    public double getCount(String s) {
        if (counts == null)
            counts = new Hashtable<>();
        if (counts.containsKey(s))
            return counts.get(s);
        return 0;
    }

    public Iterator<String> getTokensIterator() {
        if (counts == null)
            counts = new Hashtable<>();
        return counts.keySet().iterator();
    }

    public String[] getTokens() {
        String[] res = new String[uniqueTokens];
        int i = 0;
        for (Iterator<String> iter = getTokensIterator(); iter.hasNext(); res[i++] = iter.next());
        return res;
    }

    public CharacteristicWords getMostFrequentTokens(int numOfTokensToReturn) {
        CharacteristicWords topFeatures = new CharacteristicWords(numOfTokensToReturn);
        Iterator<String> iter = this.getTokensIterator();
        while (iter.hasNext()) {
            String s = iter.next();
            topFeatures.addElement(s, this.getCount(s));
        }
        return topFeatures;
    }

    public String toString() {
        String res = "";
        for (Iterator<String> iter = getTokensIterator(); iter.hasNext();) {
            String s = iter.next();
            res += "\t" + s + "\t-\t" + getCount(s) + "\n";
        }
        return res;
    }
}
