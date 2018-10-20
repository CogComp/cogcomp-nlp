/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NfoldCrossvalidation {
    private static Logger logger = LoggerFactory.getLogger(NfoldCrossvalidation.class);

    private DocumentCollection[] folds = null;

    public NfoldCrossvalidation(DocumentCollection docs, int foldsNum) {
        folds = new DocumentCollection[foldsNum];
        RandomSubset sample = new RandomSubset(docs);
        for (int i = 0; i < foldsNum; i++)
            folds[i] = sample.getRandomSubset(docs.docs.size() / foldsNum);
    }

    public DocumentCollection getTest(int foldId) {
        return folds[foldId];
    }

    public DocumentCollection getTrain(int foldId) {
        DocumentCollection res = new DocumentCollection();
        for (int i = 0; i < folds.length; i++)
            if (i != foldId)
                res.addDocuments(folds[i].docs);
        return res;
    }

    public void printNfoldCorrssvalidationNbAcc(int classesNum, double thres,
            int minWordsAppearenceCount) {
        double acc = 0;
        double precision = 0;
        double recall = 0;
        double total = 0;
        double confusionMatrix[][] = new double[classesNum][classesNum];
        for (int i = 0; i < folds.length; i++) {
            DocumentCollection train = getTrain(i);
            DocumentCollection test = getTest(i);
            FeatureMap map = new FeatureMap();
            map.addDocs(train, minWordsAppearenceCount, false);
            MemoryEfficientNB nb = new MemoryEfficientNB(train, map, classesNum);
            acc += nb.getAcc(test) / ((double) folds.length);
            for (int j = 0; j < test.docs.size(); j++) {
                total++;
                Document d = test.docs.elementAt(j);
                int label = nb.classify(d, thres);
                if (label > -1) {
                    recall++;
                    confusionMatrix[d.classID][label]++;
                }
                if (label == d.classID)
                    precision++;
            }
        }
        System.out.println("Accuracy: " + acc);
        System.out.println("Within " + thres + " confidence threshold:");
        System.out.println("\tPrecision=" + precision / recall);
        System.out.println("\tRecall=" + recall / total);
        System.out.println("\tConfusion matrix within confidence threshold:");
        for (int i = 0; i < classesNum; i++) {
            for (int j = 0; j < classesNum; j++)
                System.out.print(confusionMatrix[i][j] + "\t");
            System.out.println();
        }
    }
}
