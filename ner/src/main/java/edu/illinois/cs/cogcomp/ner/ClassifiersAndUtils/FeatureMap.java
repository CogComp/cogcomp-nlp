/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Vector;


public class FeatureMap {
    private Logger logger = LoggerFactory.getLogger(FeatureMap.class);

    public Hashtable<String, Integer> wordToFid = new Hashtable<>();
    public Hashtable<Integer, String> fidToWord = new Hashtable<>();
    public int dim = 0;

    public FeatureMap() {
        wordToFid = new Hashtable<>();
        fidToWord = new Hashtable<>();
        dim = 0;
    }

    public void save(String file) {
        OutFile out = new OutFile(file);
        out.println(String.valueOf(dim));
        for (String w : wordToFid.keySet()) {
            out.println(w);
            out.println(String.valueOf(wordToFid.get(w)));
        }
        out.close();
    }

    public FeatureMap(String filename) {
        wordToFid = new Hashtable<>();
        fidToWord = new Hashtable<>();
        InFile in = new InFile(filename);
        dim = Integer.parseInt(in.readLine());
        for (int i = 0; i < dim; i++) {
            String w = in.readLine();
            int fid = Integer.parseInt(in.readLine());
            wordToFid.put(w, fid);
            fidToWord.put(fid, w);
        }
        in.close();
    }

    public FeatureMap(FeatureMap _map) {
        wordToFid = new Hashtable<>();
        fidToWord = new Hashtable<>();
        dim = _map.dim;
        for (String w : _map.wordToFid.keySet()) {
            wordToFid.put(w, _map.wordToFid.get(w));
            fidToWord.put(_map.wordToFid.get(w), w);
        }

    }

    public void readFromFile(String countFiles, int thres) {
        InFile in = new InFile(countFiles);
        Vector<String> tokens = in.readLineTokens(" \n\t");
        while (tokens != null) {
            int count = Integer.parseInt(tokens.elementAt(0));
            if (count >= thres) {
                wordToFid.put(tokens.elementAt(1), dim);
                fidToWord.put(dim, tokens.elementAt(1));
                dim++;
            }
            tokens = in.readLineTokens("\n\t ");
        }
    }

    /*
     * if countRepsWithinDoc is false, we basically require the word to appear in at least
     * appearanceThres documents
     */
    public void addDocs(DocumentCollection docs, int appearanceThres, boolean countRepsWithinDoc) {
        UnigramStatistics stat = new UnigramStatistics(docs, countRepsWithinDoc);
        for (String w : stat.wordCounts.keySet()) {
            if (stat.wordCounts.get(w) >= appearanceThres) {
                wordToFid.put(w, dim);
                fidToWord.put(dim, w);
                dim++;
            }
        }
        /*
         * logger.info("Building a feature map"); for(int i=0;i<docs.docs.size();i++) {
         * Vector<String> words=docs.docs.elementAt(i).words; for(int j=0;j<words.size();j++)
         * if((!wordToFid.containsKey(words.elementAt(j)))&&
         * (stat.wordCounts.get(words.elementAt(j))>=appearanceThres)) {
         * wordToFid.put(words.elementAt(j), dim); fidToWord.put(dim,words.elementAt(j)); dim++; } }
         */
        logger.info("Done building a feature map, the dimension is: " + dim);
    }

    public void addMoreDocsIgnoreAppearanceThres(DocumentCollection docs) {
        for (int i = 0; i < docs.docs.size(); i++) {
            Vector<String> words = docs.docs.elementAt(i).words;
            for (int j = 0; j < words.size(); j++)
                if (!wordToFid.containsKey(words.elementAt(j))) {
                    wordToFid.put(words.elementAt(j), dim);
                    fidToWord.put(dim, words.elementAt(j));
                    dim++;
                }
        }
        logger.debug("Done adding docs to a feature map, the dimension is: " + dim);
    }

    public void addDimension(String dimensionName) {
        if (!wordToFid.containsKey(dimensionName)) {
            wordToFid.put(dimensionName, dim);
            fidToWord.put(dim, dimensionName);
            dim++;
        }
    }
}
