/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.InferenceMethods;

import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.CharacteristicWords;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PredictionsToProbabilities {
    private static Logger logger = LoggerFactory.getLogger(PredictionsToProbabilities.class);

    private static final String NAME = PredictionsToProbabilities.class.getCanonicalName();

    public static CharacteristicWords getAndSetPredictionConfidences(SparseNetworkLearner c,
            NEWord w, NEWord.LabelToLookAt predictionType) {
        if (null == c) {
            logger.error("ERROR: PredictionsToProbabilities.CharacteristicWords(): null learner.");
        }
        Score[] scores = c.scores(w).toArray();

        if (logger.isDebugEnabled()) {
            logger.debug("## {}.getAndSetPredictionConfidences(): c.scores: {}", NAME, c.scores(w));
        }
        double[] correctedScores = new double[scores.length];
        double min = scores[0].score;
        int maxScoreIdx = 0;
        double maxScore = scores[maxScoreIdx].score;
        String maxLabel = scores[maxScoreIdx].value;
        for (int i = 0; i < scores.length; i++) {
            if (min > scores[i].score)
                min = scores[i].score;
            if (maxScore < scores[i].score) {
                maxScore = scores[i].score;
                maxScoreIdx = i;
                maxLabel = scores[i].value;
            }
        }
        for (int i = 0; i < scores.length; i++)
            correctedScores[i] = scores[i].score - min;
        double sum = 0;
        for (int i = 0; i < correctedScores.length; i++) {
            correctedScores[i] = Math.exp(correctedScores[i]);
            sum += correctedScores[i];
        }
        if (sum > 0) {
            for (int i = 0; i < correctedScores.length; i++)
                correctedScores[i] /= sum;
        }

        for (int i = 0; i < correctedScores.length; i++)
            correctedScores[i] = correctedScores[i];

        CharacteristicWords res = new CharacteristicWords(scores.length);

        for (int i = 0; i < scores.length; i++)
            res.addElement(scores[i].value, correctedScores[i]);

        if (predictionType.equals(NEWord.LabelToLookAt.PredictionLevel1Tagger)) {
            w.neTypeLevel1 = maxLabel;
            w.predictionConfidencesLevel1Classifier = res;
        }
        if (predictionType.equals(NEWord.LabelToLookAt.PredictionLevel2Tagger)) {
            w.neTypeLevel2 = maxLabel;
            w.predictionConfidencesLevel2Classifier = res;
        }
        return res;
    }
}
