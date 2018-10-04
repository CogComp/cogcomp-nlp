/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.InferenceMethods;

import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.TwoLayerPredictionAggregationFeatures;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord.RealFeature;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.CharacteristicWords;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;
import java.util.Vector;

/*
 * This class is responsible for handling prediction scores of the entities. That is, this class can
 * prune the entities/predictions on which we're not confident at
 * 
 * This class also allows to annotate the data with gold named entities, where the gold label is
 * sometimes erased. This is for supporting the 2 layered inference procedure.
 */
public class PredictionsAndEntitiesConfidenceScores {

    /*
     * This function assumes that the data is already tagged with the confidence scores
     */
    public static void pruneLowConfidencePredictions(Data data, double minConfScore,
            NEWord.LabelToLookAt predictionsToLookAt) {
        Vector<NamedEntity> entities = getAndMarkEntities(data, predictionsToLookAt);
        for (int i = 0; i < entities.size(); i++) {
            double confStart = -1;
            if (predictionsToLookAt.equals(NEWord.LabelToLookAt.PredictionLevel1Tagger))
                confStart =
                        getMaxConfidence(entities.elementAt(i).tokens.elementAt(0).predictionConfidencesLevel1Classifier);
            if (predictionsToLookAt.equals(NEWord.LabelToLookAt.PredictionLevel2Tagger))
                confStart =
                        getMaxConfidence(entities.elementAt(i).tokens.elementAt(0).predictionConfidencesLevel2Classifier);
            double confEnd = -1;
            if (predictionsToLookAt.equals(NEWord.LabelToLookAt.PredictionLevel1Tagger))
                confEnd =
                        getMaxConfidence(entities.elementAt(i).tokens.elementAt(entities
                                .elementAt(i).tokens.size() - 1).predictionConfidencesLevel1Classifier);
            if (predictionsToLookAt.equals(NEWord.LabelToLookAt.PredictionLevel2Tagger))
                confEnd =
                        getMaxConfidence(entities.elementAt(i).tokens.elementAt(entities
                                .elementAt(i).tokens.size() - 1).predictionConfidencesLevel2Classifier);
            if (Math.sqrt(confEnd * confStart) < minConfScore)
                for (int j = 0; j < entities.elementAt(i).tokens.size(); j++) {
                    if (predictionsToLookAt == NEWord.LabelToLookAt.PredictionLevel1Tagger)
                        entities.elementAt(i).tokens.elementAt(j).neTypeLevel1 = "O";
                    if (predictionsToLookAt == NEWord.LabelToLookAt.PredictionLevel2Tagger)
                        entities.elementAt(i).tokens.elementAt(j).neTypeLevel2 = "O";
                    entities.elementAt(i).tokens.elementAt(j).predictedEntity = null;
                }
        }
    }

    private static double getMaxConfidence(CharacteristicWords confidences) {
        if (confidences.topScores.size() == 0)
            return 0;
        double max = confidences.topScores.elementAt(0);
        for (int i = 0; i < confidences.topScores.size(); i++)
            if (max < confidences.topScores.elementAt(i)) {
                max = confidences.topScores.elementAt(i);
            }
        return max;
    }

    /*
     * Assumes BIO - annotated data
     */
    public static Vector<NamedEntity> getAndMarkEntities(Data data,
            NEWord.LabelToLookAt predictionType) {
        Vector<NamedEntity> res = new Vector<>();
        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (int i = 0; i < sentences.size(); i++)
                for (int j = 0; j < sentences.get(i).size(); j++) {
                    NEWord w = (NEWord) sentences.get(i).get(j);
                    w.predictedEntity = null;
                    w.goldEntity = null;
                }
        }
        int startSentence = -1;
        int startToken = -1;
        int startAbsIndex = -1;
        int absTokenId = 0;
        String type = "O";
        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (int i = 0; i < sentences.size(); i++)
                for (int j = 0; j < sentences.get(i).size(); j++) {
                    NEWord w = (NEWord) sentences.get(i).get(j);
                    String label = w.neLabel;
                    if (predictionType.equals(NEWord.LabelToLookAt.PredictionLevel1Tagger))
                        label = w.neTypeLevel1;
                    if (predictionType.equals(NEWord.LabelToLookAt.PredictionLevel2Tagger))
                        label = w.neTypeLevel2;
                    String nextLabel = "O";
                    if (w.nextIgnoreSentenceBoundary != null) {
                        if (predictionType.equals(NEWord.LabelToLookAt.GoldLabel))
                            nextLabel = w.nextIgnoreSentenceBoundary.neLabel;
                        if (predictionType.equals(NEWord.LabelToLookAt.PredictionLevel1Tagger))
                            nextLabel = w.nextIgnoreSentenceBoundary.neTypeLevel1;
                        if (predictionType.equals(NEWord.LabelToLookAt.PredictionLevel2Tagger))
                            nextLabel = w.nextIgnoreSentenceBoundary.neTypeLevel2;
                    }
                    if (startSentence == -1 && label.startsWith("B-")) {
                        startSentence = i;
                        startToken = j;
                        startAbsIndex = absTokenId;
                        type = label.substring(2);
                    }
                    if (startSentence != -1 && (!nextLabel.startsWith("I-"))) {
                        NamedEntity e =
                                new NamedEntity(sentences, startAbsIndex, startSentence,
                                        startToken, i, j);
                        e.type = type;
                        res.addElement(e);
                        if (predictionType.equals(NEWord.LabelToLookAt.GoldLabel))
                            for (int k = 0; k < e.tokens.size(); k++)
                                e.tokens.elementAt(k).goldEntity = e;
                        if (predictionType.equals(NEWord.LabelToLookAt.PredictionLevel1Tagger)
                                || predictionType
                                        .equals(NEWord.LabelToLookAt.PredictionLevel2Tagger))
                            for (int k = 0; k < e.tokens.size(); k++)
                                e.tokens.elementAt(k).predictedEntity = e;
                        startSentence = startToken = -1;
                    }
                    absTokenId++;
                }
        }
        return res;
    }
}
