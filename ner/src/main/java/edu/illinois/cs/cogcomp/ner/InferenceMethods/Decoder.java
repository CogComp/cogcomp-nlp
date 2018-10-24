/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.InferenceMethods;

import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.TwoLayerPredictionAggregationFeatures;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.LbjTagger.TextChunkRepresentationManager;

import java.util.ArrayList;


public class Decoder {

    /**
     * If you don't wanna use some of the classifiers - pass null parameters.
     */
    public static void annotateDataBIO(Data data, ParametersForLbjCode params) throws Exception {
        Decoder.annotateBIO_AllLevelsWithTaggers(data, params);
    }

    /**
     * use taggerLevel2=null if you want to use only one level of inference
     */
    protected static void annotateBIO_AllLevelsWithTaggers(Data data, ParametersForLbjCode params) throws Exception {

        clearPredictions(data);
        NETaggerLevel1.isTraining = false;
        NETaggerLevel2.isTraining = false;


        GreedyDecoding.annotateGreedy(data, params.taggerLevel1, 1);

        TextChunkRepresentationManager.changeChunkRepresentation(
                params.taggingEncodingScheme,
                TextChunkRepresentationManager.EncodingScheme.BIO, data,
                NEWord.LabelToLookAt.PredictionLevel1Tagger);


        PredictionsAndEntitiesConfidenceScores.pruneLowConfidencePredictions(data,
                params.minConfidencePredictionsLevel1,
                NEWord.LabelToLookAt.PredictionLevel1Tagger);

        // this block runs the level2 tagger
        // Previously checked if features included 'PatternFeatures'
        boolean level2 = params.featuresToUse.containsKey("PredictionsLevel1");
        if (params.taggerLevel2 != null && level2) {
            // annotate with patterns
            PredictionsAndEntitiesConfidenceScores.pruneLowConfidencePredictions(data, 0.0,
                    NEWord.LabelToLookAt.PredictionLevel1Tagger);
            TwoLayerPredictionAggregationFeatures.setLevel1AggregationFeatures(data, false);
            GreedyDecoding.annotateGreedy(data, params.taggerLevel2, 2);
            PredictionsAndEntitiesConfidenceScores.pruneLowConfidencePredictions(data,
                    params.minConfidencePredictionsLevel2,
                    NEWord.LabelToLookAt.PredictionLevel2Tagger);
            TextChunkRepresentationManager.changeChunkRepresentation(
                    params.taggingEncodingScheme,
                    TextChunkRepresentationManager.EncodingScheme.BIO, data,
                    NEWord.LabelToLookAt.PredictionLevel2Tagger);
        } else {
            for (int docid = 0; docid < data.documents.size(); docid++) {
                ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
                for (LinkedVector sentence : sentences)
                    for (int i = 0; i < sentence.size(); i++) {
                        NEWord w = (NEWord) sentence.get(i);
                        w.neTypeLevel2 = w.neTypeLevel1;
                    }
            }
        }
    }

    /*
     * Lbj does some pretty annoying caching. We need this method for the beamsearch and the
     * viterbi.
     */
    public static void nullifyTaggerCachedFields(SparseNetworkLearner tagger) {
        NEWord w = new NEWord(new Word("lala1"), null, "O");
        w.parts = new String[0];
        NEWord[] words =
                {new NEWord(w, null, "O"), new NEWord(w, null, "O"), new NEWord(w, null, "O"),
                        new NEWord(w, null, "O"), new NEWord(w, null, "O"),
                        new NEWord(w, null, "O"), new NEWord(w, null, "O")};
        for (int i = 1; i < words.length; i++) {
            words[i].parts = new String[0];
            words[i].previous = words[i - 1];
            words[i].previousIgnoreSentenceBoundary = words[i - 1];
            words[i - 1].next = words[i];
            words[i - 1].nextIgnoreSentenceBoundary = words[i];
        }
        for (NEWord word : words)
            word.neTypeLevel1 = word.neTypeLevel2 = "O";
        tagger.classify(words[3]);
    }

    public static void clearPredictions(Data data) {
        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (LinkedVector sentence : sentences) {
                for (int i = 0; i < sentence.size(); i++) {
                    ((NEWord) sentence.get(i)).neTypeLevel1 = null;
                    ((NEWord) sentence.get(i)).neTypeLevel2 = null;
                }
            }
        }
    }
}
