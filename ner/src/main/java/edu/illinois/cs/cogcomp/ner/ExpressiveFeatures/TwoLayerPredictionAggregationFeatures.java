/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NamedEntity;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.OccurrenceCounter;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class TwoLayerPredictionAggregationFeatures {
    private static Logger logger = LoggerFactory
            .getLogger(TwoLayerPredictionAggregationFeatures.class);

    public enum Direction {
        RIGHT, LEFT
    }// are we aggregating to the right or to the left

    public static void setLevel1AggregationFeatures(Data data, boolean useGoldData) {
        logger.debug("Extracting features for level 2 inference");

        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;

            for (LinkedVector twords : sentences) {
                for (int j = 0; j < twords.size(); j++) {
                    setLevel1AggregationFeatures((NEWord) twords.get(j), useGoldData);
                }
            }
        }
        logger.debug("Done - Extracting features for level 2 inference");

    }

    /*
     * If our confidence in predicting the named entity is higher than minConfidenceThreshold, we're
     * going to use the predictions as features
     */
    private static void setLevel1AggregationFeatures(NEWord word, boolean useGoldData) {
        ParametersForLbjCode parameters = word.params;
        // this used to be hard-coded to 0.1
        double omissionRate = parameters.omissionRate;
        // this used to be hard-coded to 0.2 for right direction and 0.1 for left
        // now this is approximated by halving the rate set in the properties
        double noiseRate = parameters.randomNoiseLevel;

        String wordForm = word.form;
        String wordFormLC = wordForm.toLowerCase();

        word.resetLevel1AggregationFeatures();

        NamedEntity currentNE = word.predictedEntity;
        // these counters will keep the distribution of the features around the current word
        OccurrenceCounter featuresCounts = new OccurrenceCounter();

        if (useGoldData)
            currentNE = word.goldEntity;
        HashMap<NamedEntity, Boolean> confidentEntitiesInTheArea = new HashMap<>();
        HashMap<NamedEntity, Boolean> confidentEntitiesInTheAreaLeft = new HashMap<>();
        HashMap<NamedEntity, Boolean> confidentEntitiesInTheAreaRight = new HashMap<>();
        NEWord w = word.previousIgnoreSentenceBoundary;
        for (int i = 0; i < 1000 && w != null; i++) {
            if (useGoldData && w.goldEntity != null && (!w.goldEntity.equals(currentNE))) {
                confidentEntitiesInTheArea.put(w.goldEntity, true);
                confidentEntitiesInTheAreaLeft.put(w.goldEntity, true);
            }
            if (w.predictedEntity != null && (!w.predictedEntity.equals(currentNE)) && !useGoldData) {
                confidentEntitiesInTheArea.put(w.predictedEntity, true);
                confidentEntitiesInTheAreaLeft.put(w.predictedEntity, true);
            }
            if (w != word && w.form.equals(wordForm)) {
                if (useGoldData) {
                    // when facing left the noise rate is half of the right
                    // we're typically better with entities to the left....
                    if (parameters.level1AggregationRandomGenerator.nextDouble() < (noiseRate / 2))
                        featuresCounts.addToken("leftTokenLevel"
                                + parameters.level1AggregationRandomGenerator.randomLabel());
                    else
                        featuresCounts.addToken("leftTokenLevel" + w.neLabel);
                } else {
                    featuresCounts.addToken("leftTokenLevel" + w.neTypeLevel1);
                }
            }
            w = w.previousIgnoreSentenceBoundary;
        }
        w = word.nextIgnoreSentenceBoundary;
        for (int i = 0; i < 1000 && w != null; i++) {
            if (useGoldData && w.goldEntity != null && (!w.goldEntity.equals(currentNE))) {
                confidentEntitiesInTheArea.put(w.goldEntity, true);
                confidentEntitiesInTheAreaRight.put(w.goldEntity, true);
            }
            if (w.predictedEntity != null && (!w.predictedEntity.equals(currentNE)) && !useGoldData) {
                confidentEntitiesInTheArea.put(w.predictedEntity, true);
                confidentEntitiesInTheAreaRight.put(w.predictedEntity, true);
            }
            if (w != word && w.form.equals(wordForm)) {
                if (useGoldData) {
                    if (parameters.level1AggregationRandomGenerator.nextDouble() < noiseRate)
                        featuresCounts.addToken("rightTokenLevel"
                                + parameters.level1AggregationRandomGenerator.randomLabel());
                    else
                        featuresCounts.addToken("rightTokenLevel" + w.neLabel);
                } else {
                    featuresCounts.addToken("rightTokenLevel" + w.neTypeLevel1);
                }
            }
            w = w.nextIgnoreSentenceBoundary;
        }
        for (NamedEntity ne : confidentEntitiesInTheArea.keySet()) {
            String neForm = ne.form;
            String neFormLC = neForm.toLowerCase();

            // check if we should just omit this NE
            if (parameters.level1AggregationRandomGenerator.nextDouble() > omissionRate) {
                // this is if the direction is right. If the direction is left- we have to modify
                // this
                String direction = Direction.RIGHT.toString();
                // please be careful with updating the direction values
                if (confidentEntitiesInTheAreaLeft.containsKey(ne)) {
                    direction = Direction.LEFT.toString();
                    // we're typically better with entities to the left....
                    noiseRate = omissionRate / 2;
                }
                String neType = ne.type;
                if (parameters.level1AggregationRandomGenerator.nextDouble() < noiseRate) {
                    String randomLabelType =
                            parameters.level1AggregationRandomGenerator.randomType();
                    while (randomLabelType.equalsIgnoreCase("O") || randomLabelType.equals(neType))
                        randomLabelType = parameters.level1AggregationRandomGenerator.randomType();
                    neType = randomLabelType;
                }

                if ((!confidentEntitiesInTheAreaLeft.containsKey(ne))
                        && (!confidentEntitiesInTheAreaRight.containsKey(ne)))
                    throw new IllegalArgumentException(
                            "Fatal error: the NE is neither on the left or the right?!");
                boolean neEqWord = neForm.equals(wordForm);
                boolean neEqWordLC = neFormLC.equals(wordFormLC);
                boolean neStartsWithWord = neForm.startsWith(wordForm);
                boolean neStartsWithWordLC = neFormLC.startsWith(wordFormLC);
                boolean neEndsWithWord = neForm.endsWith(wordForm);
                boolean neEndsWithWordLC = neFormLC.endsWith(wordFormLC);
                boolean neContainsWord = neForm.contains(wordForm);
                if (currentNE != null) {
                    String curNEForm = currentNE.form;
                    String curNEFormLC = curNEForm.toLowerCase();
                    if (curNEForm.length() > 3) {
                        boolean neEqCurNE = neForm.equals(curNEForm);
                        boolean neEqCurNELC = neFormLC.equals(curNEFormLC);
                        boolean neStartsWithCurNE = neForm.startsWith(curNEForm);
                        boolean neStartsWithCurNELC = neFormLC.startsWith(curNEFormLC);
                        boolean neEndsWithCurNE = neForm.endsWith(curNEForm);
                        boolean neEndsWithCurNELC = neFormLC.endsWith(curNEFormLC);
                        boolean neContainsCurNE = neForm.contains(curNEForm);

                        if (neEqCurNE)
                            featuresCounts.addToken(direction + "NE_Also_Exact_Match_NE_Type:\t"
                                    + neType);
                        if ((!neEqCurNE) && (!neStartsWithCurNE) && (!neEndsWithCurNE)
                                && neContainsCurNE)
                            featuresCounts.addToken(direction + "NE_Also_Substring_In_NE_Type:\t"
                                    + neType);
                        if ((!neEqCurNE) && neStartsWithCurNE)
                            featuresCounts.addToken(direction + "NE_Also_Starts_NE_Type:\t"
                                    + neType);
                        if ((!neEqCurNE) && neEndsWithCurNE)
                            featuresCounts.addToken(direction + "NE_Also_Ends_NE_Type:\t" + neType);
                        if ((!neEqCurNE) && neEqCurNELC)
                            featuresCounts.addToken(direction + "NE_Also_Exact_Match_NE_Type_IC:\t"
                                    + neType);
                        if ((!((!neEqCurNE) && (!neStartsWithCurNE) && (!neEndsWithCurNE) && neContainsCurNE))
                                && ((!neEqCurNELC) && (!neStartsWithCurNELC)
                                        && (!neEndsWithCurNELC) && neFormLC.contains(curNEFormLC)))
                            featuresCounts.addToken(direction
                                    + "NE_Also_Substring_In_NE_Type_IC:\t" + neType);
                        if ((!((!neEqCurNE) && neStartsWithCurNE)) && (!neEqCurNELC)
                                && neStartsWithCurNELC)
                            featuresCounts.addToken(direction + "NE_Also_Starts_NE_Type_IC:\t"
                                    + neType);
                        if ((!((!neEqCurNE) && neEndsWithCurNE)) && (!neEqCurNELC)
                                && neEndsWithCurNELC)
                            featuresCounts.addToken(direction + "NE_Also_Ends_NE_Type_IC:\t"
                                    + neType);
                    }
                    // if we cannot match complete NEs, it's not the end of the work yet.
                    // in cases such as "Bank of Australia" and "Bank of Illinoils", we want to be
                    // able to say something about the word "Bank"
                    if (wordForm.length() > 3) {
                        if (neEqWord)
                            featuresCounts.addToken(direction
                                    + "labeledTokenExactMatchInExpression:\t" + neType);
                        if ((!neEqWord) && (!neStartsWithWord) && (!neEndsWithWord)
                                && neContainsWord)
                            featuresCounts.addToken(direction
                                    + "labeledTokenSubstringInExpression:\t" + neType);
                        if ((!neEqWord) && neStartsWithWord)
                            featuresCounts.addToken(direction + "labeledTokenStartsExpression:\t"
                                    + neType);
                        if ((!neEqWord) && neEndsWithWord)
                            featuresCounts.addToken(direction + "unlabeledTokenEndsExpression:\t"
                                    + neType);
                        if ((!neEqWord) && neEqWordLC)
                            featuresCounts.addToken(direction
                                    + "labeledTokenExactMatchInExpression_IC:\t" + neType);
                        if ((!((!neEqWord) && (!neStartsWithWord) && (!neEndsWithWord) && neContainsWord))
                                && ((!neEqWordLC) && (!neStartsWithWordLC) && (!neEndsWithWordLC) && neFormLC
                                        .contains(wordFormLC)))
                            featuresCounts.addToken(direction
                                    + "labeledTokenSubstringInExpression_IC:\t" + neType);
                        if ((!((!neEqWord) && neStartsWithWord))
                                && ((!neEqWordLC) && neStartsWithWordLC))
                            featuresCounts.addToken(direction
                                    + "labeledTokenStartsExpression_IC:\t" + neType);
                        if ((!((!neEqWord) && neEndsWithWord))
                                && ((!neEqWordLC) && neEndsWithWordLC))
                            featuresCounts.addToken(direction + "labeledTokenEndsExpression_IC:\t"
                                    + neType);
                    }

                } else {
                    // this form is not a part of named entity
                    if (wordForm.length() > 3) {
                        if (neEqWord)
                            featuresCounts.addToken(direction
                                    + "unlabeledTokenExactMatchInExpression:\t" + neType);
                        if ((!neEqWord) && (!neStartsWithWord) && (!neEndsWithWord)
                                && neContainsWord)
                            featuresCounts.addToken(direction
                                    + "unlabeledTokenSubstringInExpression:\t" + neType);
                        if ((!neEqWord) && neStartsWithWord)
                            featuresCounts.addToken(direction + "unlabeledTokenStartsExpression:\t"
                                    + neType);
                        if ((!neEqWord) && neEndsWithWord)
                            featuresCounts.addToken(direction + "unlabeledTokenEndsExpression:\t"
                                    + neType);
                        if ((!neEqWord) && neEqWordLC)
                            featuresCounts.addToken(direction
                                    + "unlabeledTokenExactMatchInExpression_IC:\t" + neType);
                        if ((!((!neEqWord) && (!neStartsWithWord) && (!neEndsWithWord) && neContainsWord))
                                && ((!neEqWordLC) && (!neStartsWithWordLC) && (!neEndsWithWordLC) && neFormLC
                                        .contains(wordFormLC)))
                            featuresCounts.addToken(direction
                                    + "unlabeledTokenSubstringInExpression_IC:\t" + neType);
                        if ((!((!neEqWord) && neStartsWithWord))
                                && ((!neEqWordLC) && neStartsWithWordLC))
                            featuresCounts.addToken(direction
                                    + "unlabeledTokenStartsExpression_IC:\t" + neType);
                        if ((!((!neEqWord) && neEndsWithWord))
                                && ((!neEqWordLC) && neEndsWithWordLC))
                            featuresCounts.addToken(direction
                                    + "unlabeledTokenEndsExpression_IC:\t" + neType);
                    }
                }
            }
        }
        double max = -1;
        for (Iterator<String> i = featuresCounts.getTokensIterator(); i.hasNext();) {
            String s = i.next();
            if (max < featuresCounts.getCount(s))
                max = featuresCounts.getCount(s);
        }
        if (max == 0)
            max = 1;
        ArrayList<NEWord.RealFeature> newag = word.resetLevel1AggregationFeatures();
        for (Iterator<String> i = featuresCounts.getTokensIterator(); i.hasNext();) {
            String s = i.next();
            newag.add(new NEWord.RealFeature(featuresCounts.getCount(s) / max, s));
        }
    }
}
