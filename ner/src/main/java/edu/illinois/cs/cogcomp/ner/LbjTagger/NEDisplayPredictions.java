/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Vector;

/**
 * This class will show each prediction for each word in each sentence (if there is a prediction).
 * 
 * @author redman
 *
 */
public class NEDisplayPredictions {
    private static Logger logger = LoggerFactory.getLogger(NEDisplayPredictions.class);

    /**
     * Display the predictions, the gazetteer matches and the labels.
     * 
     * @param testDatapath path to test data.
     * @param dataFormat the data format.
     * @param verbose report more.
     * @throws Exception
     */
    public static void test(String testDatapath, String dataFormat, boolean verbose)
            throws Exception {
        Data testData =
                new Data(testDatapath, testDatapath, dataFormat, new String[] {}, new String[] {});
        ExpressiveFeaturesAnnotator.annotate(testData);
        Vector<Data> data = new Vector<>();
        data.addElement(testData);

        NETaggerLevel1 taggerLevel1 =
                new NETaggerLevel1(ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1", ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1.lex");
        NETaggerLevel2 taggerLevel2 = null;
        if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PredictionsLevel1")) {
            taggerLevel2 =
                    new NETaggerLevel2(ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2", ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2.lex");
        }
        for (int i = 0; i < data.size(); i++)
            Decoder.annotateDataBIO(data.elementAt(i), taggerLevel1, taggerLevel2);
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
