/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;

import edu.illinois.cs.cogcomp.ner.IO.InFile;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;


public class Document {

    public int classID = -1;
    public Vector<String> words;
    public int[] activeFeatures = null;
    public Document brother = null;// this is a dirty hack remove it ASAP


    public Document(Document d) {
        this.classID = d.classID;
        words = new Vector<>(d.words.size());
        for (int i = 0; i < d.words.size(); i++)
            words.add(d.words.elementAt(i));
    }

    public Document(Vector<String> _words, int _classID) {
        classID = _classID;
        words = _words;
        words.trimToSize();
        // for(int i=0;i<words.size();i++)
        // wordsHash.put(words.elementAt(i), true);
    }

    public Document(String[] _words, int _classID) {
        classID = _classID;
        words = new Vector<>();
        for (String _word : _words)
            words.addElement(_word);
        words.trimToSize();
        // for(int i=0;i<words.size();i++)
        // wordsHash.put(words.elementAt(i), true);
    }

    /*
     * will read all the lines in the file into a single document
     */
    public Document(String filename, int _classID, StopWords stops, String tokenizationDelimiters) {
        InFile in = new InFile(filename);
        this.classID = _classID;
        words = new Vector<>();
        Vector<String> currentWords = in.readLineTokens(tokenizationDelimiters);
        while (currentWords != null) {
            if (stops != null)
                currentWords = stops.filterStopWords(currentWords);
            for (int j = 0; j < currentWords.size(); j++)
                words.addElement(currentWords.elementAt(j));
            currentWords = in.readLineTokens(tokenizationDelimiters);
        }
        words.trimToSize();
    }

    public boolean containsWord(String w) {
        for (int i = 0; i < words.size(); i++)
            if (words.elementAt(i).equalsIgnoreCase(w))
                return true;
        return false;
        // return wordsHash.containsKey(w);
    }

    public int[] getActiveFid(FeatureMap map) {
        Hashtable<Integer, Boolean> activeFids = new Hashtable<>(map.dim);
        for (int i = 0; i < words.size(); i++) {
            if (map.wordToFid.containsKey(words.elementAt(i))) {
                int fid = map.wordToFid.get(words.elementAt(i));
                if (!activeFids.containsKey(fid))
                    activeFids.put(fid, true);
            }
        }
        int[] res = new int[activeFids.size()];
        Iterator<Integer> iter = (activeFids.keySet()).iterator();
        for (int i = 0; i < res.length; i++)
            res[i] = iter.next();
        return res;
    }

    public double[] getFeatureVec(FeatureMap map) {
        double[] res = new double[map.dim];
        for (int i = 0; i < words.size(); i++)
            if (map.wordToFid.containsKey(words.elementAt(i)))
                res[map.wordToFid.get(words.elementAt(i))]++;
        return res;
    }

    public void toCompactFeatureRep(FeatureMap map) {
        if (words == null) {
            activeFeatures = null;
            return;
        }
        this.activeFeatures = getActiveFid(map);
        this.words = null;
    }

    public void tokenize() {
        StringTokenizer st = new StringTokenizer(tokenize(this.toString()));
        words = new Vector<>();
        while (st.hasMoreTokens())
            words.addElement(st.nextToken());
    }

    public static String tokenize(String s) {
        String delims = ",.?!;:<>-*&^%$#[]{}()/\\";
        StringBuffer res = new StringBuffer((int) (s.length() * 1.5));
        for (int i = 0; i < s.length(); i++) {
            if (delims.indexOf(s.charAt(i)) > -1)
                res.append(' ');
            res.append(s.charAt(i));
            if (delims.indexOf(s.charAt(i)) > -1)
                res.append(' ');
        }
        s = res.toString();
        delims = "'`";
        res = new StringBuffer((int) (s.length() * 1.5));
        for (int i = 0; i < s.length(); i++) {
            if (delims.indexOf(s.charAt(i)) > -1)
                res.append(' ');
            res.append(s.charAt(i));
        }
        return res.toString();
    }


    public String toString() {
        StringBuilder res = new StringBuilder(words.size() * 10);
        for (int i = 0; i < words.size(); i++)
            res.append(words.elementAt(i)).append(" ");
        return res.toString();
    }

    public String toString(FeatureMap map, boolean verbose) {
        StringBuilder res = new StringBuilder(words.size() * 10);
        for (int i = 0; i < words.size(); i++) {
            if (map.wordToFid.containsKey(words.elementAt(i)))
                res.append(words.elementAt(i)).append(" ");
            else if (verbose)
                res.append("(?").append(words.elementAt(i)).append("?) ");
        }
        return res.toString();
    }
}
