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
import java.util.Vector;


public class StopWords {
    public Hashtable<String, Boolean> h = new Hashtable<>();

    public StopWords(String filename) {
        InFile in = new InFile(filename);
        Vector<String> words = in.readLineTokens("\n\t ");
        while (words != null) {
            for (int i = 0; i < words.size(); i++)
                this.h.put(words.elementAt(i).toLowerCase(), true);
            words = in.readLineTokens("\n\t ");
        }
    }

    public Vector<String> filterStopWords(Vector<String> words) {
        if (words == null)
            return null;
        Vector<String> res = new Vector<>();
        for (int i = 0; i < words.size(); i++)
            if (!h.containsKey(words.elementAt(i).toLowerCase()))
                res.addElement(words.elementAt(i));
        return res;
    }

    public boolean isStopWord(String s) {
        return h.containsKey(s.toLowerCase());
    }

    public Vector<String> extractStopWords(Vector<String> words) {
        if (words == null)
            return null;
        Vector<String> res = new Vector<>();
        for (int i = 0; i < words.size(); i++)
            if (h.containsKey(words.elementAt(i).toLowerCase()))
                res.addElement(words.elementAt(i));
        return res;
    }

}
