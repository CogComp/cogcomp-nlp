/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Vector;


public class UnigramStatistics {

    private Logger logger = LoggerFactory.getLogger(UnigramStatistics.class);

    public Hashtable<String, Integer> wordCounts = new Hashtable<>();
    boolean countRepsWithinDocs = false;
    public int totalWordCount = 0;

    /*
     * if countRepsWithinDoc is false, increase occurrence count only when the word appears in
     * distance documents
     */
    public UnigramStatistics(String filename, FeatureMap map) {
        InFile in = new InFile(filename);
        Vector<String> tokens = in.readLineTokens("\n\t ");
        while (tokens != null) {
            for (int i = 0; i < tokens.size(); i++)
                if (map.wordToFid.containsKey(tokens.elementAt(i)))
                    addWord(tokens.elementAt(i));
            tokens = in.readLineTokens("\n\t ");
        }
        in.close();
    }

    public UnigramStatistics(DocumentCollection docs, boolean _countRepsWithinDocs) {
        countRepsWithinDocs = _countRepsWithinDocs;
        logger.info("Building unigram statistics");
        for (int i = 0; i < docs.docs.size(); i++) {
            addDoc(docs.docs.elementAt(i));
        }
        logger.info("Done building unigram statistics");
    }

    /*
     * if countRepsWithinDoc is false, increase occurrence count only when the word appears in
     * distance documents
     */
    public UnigramStatistics(boolean _countRepsWithinDocs) {
        countRepsWithinDocs = _countRepsWithinDocs;
    }

    public void addDoc(Document doc) {
        Hashtable<String, Boolean> alreadyAppreared = new Hashtable<>();
        Vector<String> words = doc.words;
        for (int j = 0; j < words.size(); j++) {
            if (countRepsWithinDocs || (!alreadyAppreared.containsKey(words.elementAt(j)))) {
                addWord(words.elementAt(j));
                alreadyAppreared.put(words.elementAt(j), true);
            }
        }
    }

    public void addWord(String w) {
        totalWordCount++;
        if (!wordCounts.containsKey(w)) {
            wordCounts.put(w, 1);
        } else {
            int count = wordCounts.get(w);
            wordCounts.remove(w);
            wordCounts.put(w, count + 1);
        }
    }
}
