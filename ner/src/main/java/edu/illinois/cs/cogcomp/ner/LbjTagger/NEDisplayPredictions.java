/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import java.util.ArrayList;
import java.util.Vector;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;

/**
 * This class will show each prediction for each word in each sentence (if there is a prediction).
 * @author redman
 */
public class NEDisplayPredictions {
    
    /**
     * Display the predictions, the gazetteer matches and the labels.
     * 
     * @param testDatapath path to test data.
     * @param dataFormat the data format.
     * @param verbose report more.
     * @throws Exception
     */
    public static void test(String testDatapath, String dataFormat, boolean verbose, ParametersForLbjCode params)
            throws Exception {
        Data testData =
                new Data(testDatapath, testDatapath, dataFormat, new String[] {}, new String[] {}, params);
        ExpressiveFeaturesAnnotator.annotate(testData, params);
        Vector<Data> data = new Vector<>();
        data.addElement(testData);
        for (int i = 0; i < data.size(); i++)
            Decoder.annotateDataBIO(data.elementAt(i), params);
        reportPredictions(data.get(0));
    }

    /**
     * Report the results.
     * 
     * @param dataSet the parsed and annotated data.
     */
    public static void reportPredictions(Data dataSet) {
        for (int docid = 0; docid < dataSet.documents.size(); docid++) {
            NERDocument doc = dataSet.documents.get(docid);
            System.out.println("\nGetting document " + doc.docname);
            ArrayList<LinkedVector> sentences = doc.sentences;
            for (int k = 0; k < sentences.size(); k++) {
                LinkedVector sentence = sentences.get(k);
                int N = sentence.size();
                for (int i = 0; i < N; ++i) {
                    NEWord word = (NEWord) sentence.get(i);
                    System.out.println(word.form + "\tL:" + word.neLabel + "\t1:"
                            + word.neTypeLevel1 + "\t2:" + word.neTypeLevel2 + "\t(" + doc.docname
                            + ")");
                    for (String h2 : word.gazetteers)
                        System.out.print(" " + h2);
                    System.out.println();
                }
            }
        }
    }
}
