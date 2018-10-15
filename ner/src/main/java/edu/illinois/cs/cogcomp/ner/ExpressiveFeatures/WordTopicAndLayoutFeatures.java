/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils.*;
import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Vector;

/*
 * This will keep the information of whether the word is in the title and to what topic it belongs
 * to.
 */
public class WordTopicAndLayoutFeatures {
    private static Logger logger = LoggerFactory.getLogger(WordTopicAndLayoutFeatures.class);

    public static final String pathToStopWords = "../../Data/TrainByTopic/stopwords_big";
    private static HashMap<NEWord, Integer> wordToTopicIdMap = new HashMap<>();
    private static FeatureMap map = null; // new FeatureMap();
    private static MemoryEfficientNB nb = null;// new MemoryEfficientNB(docs, map, 3);
    private static String[] labelnames = null;

    /*
     * Right now the return values are: {isInTitle}x{TopicId}
     */
    public static String getWordType(NEWord w) {
        return "-" + wordToTopicIdMap.get(w);
    }

    /*
     * Note- this assumes that the data is split by documents. So if we choose to ignore the
     * document boundaries, we're in trouble!!!
     */
    public static void addDatasets(Vector<LinkedVector> sentences, boolean lowercaseData,
            double confidenceThreshold) throws Exception {
        if (nb == null || map == null)
            throw new Exception("Topic classifier not initialized!!!");
        String documentText = "";
        Vector<NEWord> docWords = new Vector<>();
        for (int sid = 0; sid < sentences.size(); sid++) {
            LinkedVector s = sentences.elementAt(sid);
            for (int i = 0; i < s.size(); i++) {
                documentText += " " + ((NEWord) s.get(i)).originalForm + " ";
                docWords.addElement((NEWord) s.get(i));
            }
            if (((NEWord) s.get(s.size() - 1)).nextIgnoreSentenceBoundary == null) {
                // this is the last sentence in the document- move on!
                if (lowercaseData)
                    documentText = documentText.toLowerCase();
                Document doc =
                        new Document(InFile.tokenize(documentText,
                                "\n\t -.,?<>;':\"[]{}\\|`~!@#$%^&*()_+=-0987654321`~"), -1);
                int label = nb.classify(doc, confidenceThreshold);
                logger.info("*********************\n" + labelnames[label + 1]
                        + "\n*********************\n"
                        + documentText.substring(0, Math.min(documentText.length(), 400)));
                for (int i = 0; i < docWords.size(); i++)
                    wordToTopicIdMap.put(docWords.elementAt(i), label);
                documentText = "";
                docWords = new Vector<>();
            }
        }

    }

    public static void initTopicClassifier(String pathToTopicData, String[] fileNames,
            String[] _labelnames) {
        map = new FeatureMap();
        labelnames = new String[_labelnames.length + 1];
        labelnames[0] = "UNKNOWN";
        for (int i = 0; i < _labelnames.length; i++)
            labelnames[1 + i] = _labelnames[i];
        DocumentCollection docs = new DocumentCollection();
        StopWords stops = new StopWords(pathToStopWords);
        for (int i = 0; i < fileNames.length; i++)
            docs.addDocuments(pathToTopicData + "/" + fileNames[i], i, stops, false,
                    "\n\t -.,?<>;':\"[]{}\\|`~!@#$%^&*()_+=-0987654321`~");
        map.addDocs(docs, 20, false);
        NfoldCrossvalidation cv = new NfoldCrossvalidation(docs, 5);
        cv.printNfoldCorrssvalidationNbAcc(fileNames.length, -1, 20);
        // System.exit(0);
        nb = new MemoryEfficientNB(docs, map, fileNames.length);
    }

}
