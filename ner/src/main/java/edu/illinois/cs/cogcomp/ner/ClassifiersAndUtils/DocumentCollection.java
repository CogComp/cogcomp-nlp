/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;


import edu.illinois.cs.cogcomp.ner.IO.InFile;

import java.io.File;
import java.util.Vector;


public class DocumentCollection {
    public Vector<Document> docs = new Vector<>();

    public DocumentCollection() {}

    public void addDoc(Document doc) {
        docs.addElement(doc);
    }

    public void addDocuments(Vector<Document> _docs) {
        for (int i = 0; i < _docs.size(); i++)
            this.docs.addElement(_docs.elementAt(i));
    }

    /*
     * This code assumes each line in a file contains a new document
     */
    public void addDocuments(String filename, int classID, StopWords stops,
            boolean discardFirstToken, String tokenizationDelimiters) {
        InFile in = new InFile(filename);
        Vector<String> words = in.readLineTokens(tokenizationDelimiters);
        if ((discardFirstToken) && (words != null) && (words.size() > 0))
            words.removeElementAt(0);
        if (stops != null)
            words = stops.filterStopWords(words);
        while (words != null) {
            if (words.size() >= 0)
                docs.addElement(new Document(words, classID));
            words = in.readLineTokens(tokenizationDelimiters);
            if ((discardFirstToken) && (words != null) && (words.size() > 0))
                words.removeElementAt(0);
            if (stops != null)
                words = stops.filterStopWords(words);
        }
    }

    /*
     * This format assumes that the folder contains a bunch of files. each files is a single doc
     */
    public void addFolder(String path, int classID, StopWords stops, boolean discardFirstToken,
            String tokenizationDelimiters) {
        String[] files = (new File(path)).list();
        for (String file : files) {
            InFile in = new InFile(path + "/" + file);
            Vector<String> allWords = new Vector<>();
            Vector<String> words = in.readLineTokens(tokenizationDelimiters);
            if ((discardFirstToken) && (words != null) && (words.size() > 0))
                words.removeElementAt(0);
            if (stops != null)
                words = stops.filterStopWords(words);
            while (words != null) {
                for (int j = 0; j < words.size(); j++)
                    allWords.addElement(words.elementAt(j));
                words = in.readLineTokens(tokenizationDelimiters);
                if ((discardFirstToken) && (words != null) && (words.size() > 0))
                    words.removeElementAt(0);
                if (stops != null)
                    words = stops.filterStopWords(words);
            }
            docs.addElement(new Document(allWords, classID));
        }
    }

    public void tokenize() {
        for (int i = 0; i < docs.size(); i++)
            docs.elementAt(i).tokenize();
    }
}
