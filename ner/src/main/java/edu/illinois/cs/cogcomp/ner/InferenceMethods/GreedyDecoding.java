/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.InferenceMethods;

import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;

import java.util.ArrayList;

class GreedyDecoding {
    /**
     * This is the simplest greedy left-to right annotation.
     */
    public static void annotateGreedy(Data data, SparseNetworkLearner tagger, int inferenceLayer)
            throws IllegalArgumentException {
        if (inferenceLayer != 1 && inferenceLayer != 2) {
            throw new IllegalArgumentException("Terrible error- nonexisting inference layer");
        }
        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (LinkedVector sentence : sentences) {
                for (int i = 0; i < sentence.size(); ++i) {
                    NEWord w = (NEWord) sentence.get(i);
                    // this will set the label of w.neTypeLevel1 to the max prediction...
                    if (inferenceLayer == 1)
                        PredictionsToProbabilities.getAndSetPredictionConfidences(tagger, w,
                                NEWord.LabelToLookAt.PredictionLevel1Tagger);
                    else
                        PredictionsToProbabilities.getAndSetPredictionConfidences(tagger, w,
                                NEWord.LabelToLookAt.PredictionLevel2Tagger);
                }
            }
        }
    }
}
