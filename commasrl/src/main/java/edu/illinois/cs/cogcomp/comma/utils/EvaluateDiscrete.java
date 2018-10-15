/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.lbjava.util.TableFormat;

/**
 * Provides all the functionality of TestDiscrete and additionally a confusion matrix
 * 
 * @author navari
 *
 */
public class EvaluateDiscrete extends TestDiscrete {
    HashMap<String, HashMap<String, Integer>> confusionMatrix;

    public EvaluateDiscrete() {
        confusionMatrix = new HashMap<>();
    }

    @Override
    public void reportPrediction(String p, String l) {
        super.reportPrediction(p, l);
        HashMap<String, Integer> predictionHistogramForL = confusionMatrix.get(l);
        if (predictionHistogramForL == null) {
            predictionHistogramForL = new HashMap<>();
            confusionMatrix.put(l, predictionHistogramForL);
        }
        histogramAdd(predictionHistogramForL, p, 1);
    }

    public void printConfusion(PrintStream out) {
        List<String> labels = new ArrayList<>(confusionMatrix.keySet());
        Collections.sort(labels);
        int numLabels = labels.size();
        Double[][] confusion = new Double[numLabels][];
        for (int x = 0; x < numLabels; x++) {
            confusion[x] = new Double[numLabels];
            HashMap<String, Integer> predictionHistogramForL = confusionMatrix.get(labels.get(x));
            for (int y = 0; y < numLabels; y++) {
                Integer count = predictionHistogramForL.get(labels.get(y));
                double fraction =
                        ((count == null) ? 0 : count) / (double) getLabeled(labels.get(x));
                confusion[x][y] = fraction;
            }
        }

        String[] columnLabels = new String[numLabels + 1];
        columnLabels[0] = "Confusion";
        String[] rowLabels = labels.toArray(new String[numLabels]);
        System.arraycopy(rowLabels, 0, columnLabels, 1, numLabels);
        TableFormat.printTableFormat(out, columnLabels, rowLabels, confusion);
    }

    public void reportAll(EvaluateDiscrete ed) {
        super.reportAll(ed);
        matrixAddAll(confusionMatrix, ed.confusionMatrix);
    }

    public void matrixAddAll(HashMap<String, HashMap<String, Integer>> addTo,
            HashMap<String, HashMap<String, Integer>> addFrom) {
        for (String label : addFrom.keySet()) {
            HashMap<String, Integer> addToLabelHist = addTo.get(label);
            HashMap<String, Integer> addFromLabelHist = addFrom.get(label);
            if (addToLabelHist == null) {
                addToLabelHist = new HashMap<>();
                addTo.put(label, addToLabelHist);
            }
            histogramAddAll(addToLabelHist, addFromLabelHist);
        }
    }

    public static EvaluateDiscrete evaluateDiscrete(Classifier classifier, Classifier oracle,
            Parser parser) {
        return (EvaluateDiscrete) testDiscrete(new EvaluateDiscrete(), classifier, oracle, parser,
                false, 0);
    }
}
