/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;

import edu.illinois.cs.cogcomp.ner.IO.InFile;

import java.util.Vector;


/*
 * Much like the DocumentCollection class, except it read the doc one by one. This is for the cases
 * when the dataset is too large to fit in memory and i'm using online learning
 */
public class DocumentReader {
    InFile in = null;
    StopWords stops = null;
    boolean discardFirstToken = false;
    String tokenizationDelimiters;

    public DocumentReader(String filename, StopWords _stops, boolean _discardFirstToken,
            String _tokenizationDelimiters) {
        tokenizationDelimiters = _tokenizationDelimiters;
        in = new InFile(filename);
        stops = _stops;
        discardFirstToken = _discardFirstToken;
    }

    /*
     * This code assumes each line in a file contains a new document
     */
    public Document nextDoc(int initClassID) {
        Vector<String> words = in.readLineTokens(tokenizationDelimiters);
        if ((discardFirstToken) && (words != null) && (words.size() > 0))
            words.removeElementAt(0);
        if (stops != null)
            words = stops.filterStopWords(words);
        while (words != null) {
            if (words.size() > 0)
                return new Document(words, initClassID);
            words = in.readLineTokens(tokenizationDelimiters);
            if ((discardFirstToken) && (words != null) && (words.size() > 0))
                words.removeElementAt(0);
            if (stops != null)
                words = stops.filterStopWords(words);
        }
        return null;
    }
}
