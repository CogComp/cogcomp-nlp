/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;


import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class NETesterMultiDataset {
    private static Logger logger = LoggerFactory.getLogger(NETesterMultiDataset.class);

    /**
     * NB: assuming column format
     */
    public static void test(String testDatapath, boolean verbose,  Vector<String> labelsToIgnoreInEvaluation, 
        Vector<String> labelsToAnonymizeInEvaluation, ParametersForLbjCode params)
            throws Exception {
        test(testDatapath,verbose, "-c", labelsToIgnoreInEvaluation, labelsToAnonymizeInEvaluation, params);
    }

    /**
     * Allows format to be specified.
     * @param testDatapath
     * @param verbose
     * @param dataFormat
     * @param labelsToIgnoreInEvaluation
     * @param labelsToAnonymizeInEvaluation
     * @throws Exception
     */
    public static void test(String testDatapath, boolean verbose, String dataFormat, Vector<String> labelsToIgnoreInEvaluation,
        Vector<String> labelsToAnonymizeInEvaluation, ParametersForLbjCode params)
            throws Exception {
        Data testData =
                new Data(testDatapath, testDatapath, dataFormat, new String[] {}, new String[] {}, params);
        ExpressiveFeaturesAnnotator.annotate(testData, params);
        Vector<Data> data = new Vector<>();
        data.addElement(testData);

        if (labelsToIgnoreInEvaluation != null)
            data.elementAt(0).setLabelsToIgnore(labelsToIgnoreInEvaluation);
        if (labelsToAnonymizeInEvaluation != null)
            data.elementAt(0).setLabelsToAnonymize(labelsToAnonymizeInEvaluation);
        NETaggerLevel1 taggerLevel1 = (NETaggerLevel1) params.taggerLevel1;
        NETaggerLevel2 taggerLevel2 = (NETaggerLevel2) params.taggerLevel2;
        SparseAveragedPerceptron sap1 = (SparseAveragedPerceptron)taggerLevel1.getBaseLTU();
        System.out.println("L1 SparseAveragedPerceptron learning rate = "+sap1.getLearningRate()+", thickness = "+sap1.getPositiveThickness());
        if (params.featuresToUse.containsKey("PredictionsLevel1")) {
            SparseAveragedPerceptron sap2 = (SparseAveragedPerceptron)taggerLevel2.getBaseLTU();
            System.out.println("L2 SparseAveragedPerceptron learning rate = "+sap2.getLearningRate()+", thickness = "+sap2.getPositiveThickness());
        }
        printTestResultsByDataset(data, taggerLevel1, taggerLevel2, verbose, params);
    }

    /**
     * NB: assuming column format
     */
    public static void dumpFeaturesLabeledData(String testDatapath, String outDatapath, ParametersForLbjCode params)
            throws Exception {
        FeaturesLevel1SharedWithLevel2 features1 = new FeaturesLevel1SharedWithLevel2();
        FeaturesLevel2 features2 = new FeaturesLevel2();
        NETaggerLevel1 taggerLevel1 = (NETaggerLevel1) params.taggerLevel1;
        NETaggerLevel2 taggerLevel2 = (NETaggerLevel2) params.taggerLevel2;
        File f = new File(testDatapath);
        Vector<String> inFiles = new Vector<>();
        Vector<String> outFiles = new Vector<>();
        if (f.isDirectory()) {
            String[] files = f.list();
            for (String file : files)
                if (!file.startsWith(".")) {
                    inFiles.addElement(testDatapath + "/" + file);
                    outFiles.addElement(outDatapath + "/" + file);
                }
        } else {
            inFiles.addElement(testDatapath);
            outFiles.addElement(outDatapath);
        }
        for (int fileId = 0; fileId < inFiles.size(); fileId++) {
            Data testData =
                    new Data(inFiles.elementAt(fileId), inFiles.elementAt(fileId), "-c",
                            new String[] {}, new String[] {}, params);
            ExpressiveFeaturesAnnotator.annotate(testData, params);
            Decoder.annotateDataBIO(testData, params);
            OutFile out = new OutFile(outFiles.elementAt(fileId));
            for (int docid = 0; docid < testData.documents.size(); docid++) {
                ArrayList<LinkedVector> sentences = testData.documents.get(docid).sentences;
                for (LinkedVector sentence : sentences) {
                    for (int j = 0; j < sentence.size(); j++) {
                        NEWord w = (NEWord) sentence.get(j);
                        out.print(w.neLabel + "\t" + w.form + "\t");
                        FeatureVector fv1 = features1.classify(w);
                        FeatureVector fv2 = features2.classify(w);
                        for (int k = 0; k < fv1.size(); k++) {
                            String s = fv1.getFeature(k).toString();
                            out.print(" " + s.substring(s.indexOf(':') + 1, s.length()));
                        }
                        for (int k = 0; k < fv2.size(); k++) {
                            String s = fv2.getFeature(k).toString();
                            out.print(" " + s.substring(s.indexOf(':') + 1, s.length()));
                        }
                        out.println("");
                    }
                    out.println("");
                }
            }
            out.close();
        }
    }

    public static Vector<TestDiscrete[]> printTestResultsByDataset(Vector<Data> dataCollection,
            NETaggerLevel1 tagger1, NETaggerLevel2 tagger2, boolean verbose, ParametersForLbjCode params) throws Exception {
        for (int i = 0; i < dataCollection.size(); i++)
            Decoder.annotateDataBIO(dataCollection.elementAt(i), params);
        return printTestResultsByDataset(dataCollection, verbose, params);
    }

    public static TestDiscrete[] printAllTestResultsAsOneDataset(Vector<Data> dataCollection,
            NETaggerLevel1 tagger1, NETaggerLevel2 tagger2, boolean verbose, ParametersForLbjCode params) throws Exception {
        for (int i = 0; i < dataCollection.size(); i++)
            Decoder.annotateDataBIO(dataCollection.elementAt(i), params);
        return printAllTestResultsAsOneDataset(dataCollection, verbose, params);
    }


    /*
     * assumes that the data has been annotated by both levels of taggers
     */
    public static Vector<TestDiscrete[]> printTestResultsByDataset(Vector<Data> dataCollection,
            boolean verbose, ParametersForLbjCode params) {
        Vector<TestDiscrete[]> res = new Vector<>();
        for (int dataSetId = 0; dataSetId < dataCollection.size(); dataSetId++) {
            TestDiscrete resultsPhraseLevel1 = new TestDiscrete();
            resultsPhraseLevel1.addNull("O");
            TestDiscrete resultsTokenLevel1 = new TestDiscrete();
            resultsTokenLevel1.addNull("O");

            TestDiscrete resultsPhraseLevel2 = new TestDiscrete();
            resultsPhraseLevel2.addNull("O");
            TestDiscrete resultsTokenLevel2 = new TestDiscrete();
            resultsTokenLevel2.addNull("O");

            TestDiscrete resultsByBILOU = new TestDiscrete();
            TestDiscrete resultsSegmentation = new TestDiscrete();
            resultsByBILOU.addNull("O");
            resultsSegmentation.addNull("O");

            reportPredictions(dataCollection.elementAt(dataSetId), resultsTokenLevel1,
                    resultsTokenLevel2, resultsPhraseLevel1, resultsPhraseLevel2, resultsByBILOU,
                    resultsSegmentation);
            TestDiscrete[] resCurrentData = {resultsPhraseLevel1, resultsPhraseLevel2};
            res.addElement(resCurrentData);
            if (verbose) {
                System.out.println("------------------------------------------------------------");
                System.out.println("******	Performance on dataset "
                        + dataCollection.elementAt(dataSetId).datasetPath + "  **********");
                System.out.println("------------------------------------------------------------");
                if (params.featuresToUse
                        .containsKey("PredictionsLevel1")) {
                    System.out.println("Phrase-level Acc Level2:");
                    resultsPhraseLevel2.printPerformance(System.out);
                    System.out.println("Token-level Acc Level2:");
                    resultsTokenLevel2.printPerformance(System.out);
                    System.out.println("Level2 BILOU Accuracy, letter-by-letter:");
                    resultsByBILOU.printPerformance(System.out);
                    System.out.println("Level2 BILOU PHRASE/BOUNDARY DETECTION Accuracy");
                    resultsSegmentation.printPerformance(System.out);
                }
                System.out.println("Phrase-level Acc Level1:");
                resultsPhraseLevel1.printPerformance(System.out);
                System.out.println("Token-level Acc Level1:");
                resultsTokenLevel1.printPerformance(System.out);
                System.out.println("------------------------------------------------------------");
                System.out.println("****** (END)	Performance on dataset "
                        + dataCollection.elementAt(dataSetId).datasetPath + "  **********");
                System.out.println("------------------------------------------------------------");
            } else {
                System.out.println(">>>>>>>>>	Phrase-level F1 on the dataset: "
                        + dataCollection.elementAt(dataSetId).datasetPath);
                System.out.println("\t Level 1: " + resultsPhraseLevel1.getOverallStats()[2]);
                if (params.featuresToUse
                        .containsKey("PredictionsLevel1"))
                    System.out.println("\t Level 2: " + resultsPhraseLevel2.getOverallStats()[2]);
            }
        }
        return res;
    }

    /**
     * assumes that the data has been annotated by both levels of taggers
     */
    public static TestDiscrete[] printAllTestResultsAsOneDataset(Vector<Data> dataCollection,
            boolean verbose, ParametersForLbjCode params) {
        TestDiscrete resultsPhraseLevel1 = new TestDiscrete();
        resultsPhraseLevel1.addNull("O");
        TestDiscrete resultsTokenLevel1 = new TestDiscrete();
        resultsTokenLevel1.addNull("O");

        TestDiscrete resultsPhraseLevel2 = new TestDiscrete();
        resultsPhraseLevel2.addNull("O");
        TestDiscrete resultsTokenLevel2 = new TestDiscrete();
        resultsTokenLevel2.addNull("O");

        TestDiscrete resultsByBILOU = new TestDiscrete();
        TestDiscrete resultsSegmentation = new TestDiscrete();
        resultsByBILOU.addNull("O");
        resultsSegmentation.addNull("O");

        for (int dataSetId = 0; dataSetId < dataCollection.size(); dataSetId++)
            reportPredictions(dataCollection.elementAt(dataSetId), resultsTokenLevel1,
                    resultsTokenLevel2, resultsPhraseLevel1, resultsPhraseLevel2, resultsByBILOU,
                    resultsSegmentation);

        System.out.println("------------------------------------------------------------");
        System.out.println("******	Combined performance on all the datasets :");
        for (int i = 0; i < dataCollection.size(); i++)
            System.out.println("\t>>> Dataset path : \t" + dataCollection.elementAt(i).datasetPath);
        System.out.println("------------------------------------------------------------");
        if (verbose) {
            if (params.featuresToUse
                    .containsKey("PredictionsLevel1")) {
                System.out.println("Phrase-level Acc Level2:");
                resultsPhraseLevel2.printPerformance(System.out);
                System.out.println("Token-level Acc Level2:");
                resultsTokenLevel2.printPerformance(System.out);
                System.out.println("Level2 BILOU Accuracy, letter-by-letter:");
                resultsByBILOU.printPerformance(System.out);
                System.out.println("Level2 BILOU PHRASE/BOUNDARY DETECTION Accuracy");
                resultsSegmentation.printPerformance(System.out);
            }
            System.out.println("Phrase-level Acc Level1:");
            resultsPhraseLevel1.printPerformance(System.out);
            System.out.println("Token-level Acc Level1:");
            resultsTokenLevel1.printPerformance(System.out);
        } else {
            System.out.println("\t Level 1: " + resultsPhraseLevel1.getOverallStats()[2]);
            if (params.featuresToUse
                    .containsKey("PredictionsLevel1"))
                System.out.println("\t Level 2: " + resultsPhraseLevel2.getOverallStats()[2]);
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("************************************************************");
        System.out.println("------------------------------------------------------------");

        return new TestDiscrete[] {resultsPhraseLevel1, resultsPhraseLevel2};
    }

    public static void reportPredictions(Data dataSet, TestDiscrete resultsTokenLevel1,
            TestDiscrete resultsTokenLevel2, TestDiscrete resultsPhraseLevel1,
            TestDiscrete resultsPhraseLevel2, TestDiscrete resultsByBILOU,
            TestDiscrete resultsSegmentation) {
        NELabel labeler = new NELabel();
        Data dataCloneWithanonymizedLabels = new Data();
        for (int docid = 0; docid < dataSet.documents.size(); docid++) {
            ArrayList<LinkedVector> originalSentences = dataSet.documents.get(docid).sentences;
            ArrayList<LinkedVector> clonedSentences = new ArrayList<>();
            for (LinkedVector originalSentence : originalSentences) {
                LinkedVector sentence = new LinkedVector();
                for (int j = 0; j < originalSentence.size(); j++) {
                    NEWord originalW = (NEWord) originalSentence.get(j);
                    NEWord w = new NEWord(new Word(originalW.form), null, null);
                    w.neLabel = originalW.neLabel;
                    if (w.neLabel.indexOf('-') > -1
                            && dataSet.labelsToIgnoreForEvaluation.contains(w.neLabel.substring(2)))
                        w.neLabel = "O";
                    w.neTypeLevel1 = originalW.neTypeLevel1;
                    if (w.neLabel.indexOf('-') > -1
                            && dataSet.labelsToAnonymizeForEvaluation.contains(w.neLabel
                                    .substring(2))) {
                        w.neLabel = w.neLabel.substring(0, 2) + "ENTITY";
                        // logger.info("replace!!!");
                    }
                    w.neTypeLevel1 = originalW.neTypeLevel1;
                    if (w.neTypeLevel1.indexOf('-') > -1
                            && dataSet.labelsToIgnoreForEvaluation.contains(w.neTypeLevel1
                                    .substring(2)))
                        w.neTypeLevel1 = "O";
                    if (w.neTypeLevel1.indexOf('-') > -1
                            && dataSet.labelsToAnonymizeForEvaluation.contains(w.neTypeLevel1
                                    .substring(2)))
                        w.neTypeLevel1 = w.neTypeLevel1.substring(0, 2) + "ENTITY";
                    w.neTypeLevel2 = originalW.neTypeLevel2;
                    if (w.neTypeLevel2.indexOf('-') > -1
                            && dataSet.labelsToIgnoreForEvaluation.contains(w.neTypeLevel2
                                    .substring(2)))
                        w.neTypeLevel2 = "O";
                    if (w.neTypeLevel2.indexOf('-') > -1
                            && dataSet.labelsToAnonymizeForEvaluation.contains(w.neTypeLevel2
                                    .substring(2)))
                        w.neTypeLevel2 = w.neTypeLevel2.substring(0, 2) + "ENTITY";
                    sentence.add(w);
                }
                clonedSentences.add(sentence);
            }
            NERDocument clonedDoc = new NERDocument(clonedSentences, "fake" + docid);
            dataCloneWithanonymizedLabels.documents.add(clonedDoc);
        }

        for (int docid = 0; docid < dataCloneWithanonymizedLabels.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences =
                    dataCloneWithanonymizedLabels.documents.get(docid).sentences;
            for (LinkedVector vector : sentences) {
                int N = vector.size();
                String[] predictionsLevel1 = new String[N], predictionsLevel2 = new String[N], labels =
                        new String[N];

                for (int i = 0; i < N; ++i) {
                    predictionsLevel1[i] = ((NEWord) vector.get(i)).neTypeLevel1;
                    predictionsLevel2[i] = ((NEWord) vector.get(i)).neTypeLevel2;
                    labels[i] = labeler.discreteValue(vector.get(i));
                    String pLevel1 = predictionsLevel1[i];
                    String pLevel2 = predictionsLevel2[i];
                    if (pLevel1.indexOf('-') > -1)
                        pLevel1 = pLevel1.substring(2);
                    if (pLevel2.indexOf('-') > -1)
                        pLevel2 = pLevel2.substring(2);
                    String l = labels[i];
                    if (l.indexOf('-') > -1)
                        l = l.substring(2);
                    resultsTokenLevel1.reportPrediction(pLevel1, l);
                    resultsTokenLevel2.reportPrediction(pLevel2, l);
                }


                // getting phrase level accuracy level1
                for (int i = 0; i < N; ++i) {
                    String p = "O", l = "O";
                    int pEnd = -1, lEnd = -1;

                    if (predictionsLevel1[i].startsWith("B-")
                            || predictionsLevel1[i].startsWith("I-")
                            && (i == 0 || !predictionsLevel1[i - 1].endsWith(predictionsLevel1[i]
                                    .substring(2)))) {
                        p = predictionsLevel1[i].substring(2);
                        pEnd = i;
                        while (pEnd + 1 < N && predictionsLevel1[pEnd + 1].equals("I-" + p))
                            ++pEnd;
                    }

                    if (labels[i].startsWith("B-")) {
                        l = labels[i].substring(2);
                        lEnd = i;
                        while (lEnd + 1 < N && labels[lEnd + 1].equals("I-" + l))
                            ++lEnd;
                    }

                    if (!p.equals("O") || !l.equals("O")) {
                        if (pEnd == lEnd)
                            resultsPhraseLevel1.reportPrediction(p, l);
                        else {
                            if (!p.equals("O"))
                                resultsPhraseLevel1.reportPrediction(p, "O");
                            if (!l.equals("O"))
                                resultsPhraseLevel1.reportPrediction("O", l);
                        }
                    }
                }

                // getting phrase level accuracy level2
                for (int i = 0; i < N; ++i) {
                    String p = "O", l = "O";
                    int pEnd = -1, lEnd = -1;

                    if (predictionsLevel2[i].startsWith("B-")
                            || predictionsLevel2[i].startsWith("I-")
                            && (i == 0 || !predictionsLevel2[i - 1].endsWith(predictionsLevel2[i]
                                    .substring(2)))) {
                        p = predictionsLevel2[i].substring(2);
                        pEnd = i;
                        while (pEnd + 1 < N && predictionsLevel2[pEnd + 1].equals("I-" + p))
                            ++pEnd;
                    }

                    if (labels[i].startsWith("B-")) {
                        l = labels[i].substring(2);
                        lEnd = i;
                        while (lEnd + 1 < N && labels[lEnd + 1].equals("I-" + l))
                            ++lEnd;
                    }

                    if (!p.equals("O") || !l.equals("O")) {
                        if (pEnd == lEnd)
                            resultsPhraseLevel2.reportPrediction(p, l);
                        else {
                            if (!p.equals("O"))
                                resultsPhraseLevel2.reportPrediction(p, "O");
                            if (!l.equals("O"))
                                resultsPhraseLevel2.reportPrediction("O", l);
                        }
                    }
                }
            }
        }

        TextChunkRepresentationManager.changeChunkRepresentation(
                TextChunkRepresentationManager.EncodingScheme.BIO,
                TextChunkRepresentationManager.EncodingScheme.BILOU, dataCloneWithanonymizedLabels,
                NEWord.LabelToLookAt.GoldLabel);
        TextChunkRepresentationManager.changeChunkRepresentation(
                TextChunkRepresentationManager.EncodingScheme.BIO,
                TextChunkRepresentationManager.EncodingScheme.BILOU, dataCloneWithanonymizedLabels,
                NEWord.LabelToLookAt.PredictionLevel2Tagger);
        for (int docid = 0; docid < dataCloneWithanonymizedLabels.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences =
                    dataCloneWithanonymizedLabels.documents.get(docid).sentences;
            for (LinkedVector sentence : sentences)
                for (int j = 0; j < sentence.size(); j++) {
                    NEWord w = (NEWord) sentence.get(j);
                    String bracketTypePrediction = w.neTypeLevel2;
                    if (bracketTypePrediction.indexOf('-') > 0)
                        bracketTypePrediction = bracketTypePrediction.substring(0, 1);
                    String bracketTypeLabel = w.neLabel;
                    if (bracketTypeLabel.indexOf('-') > 0)
                        bracketTypeLabel = bracketTypeLabel.substring(0, 1);
                    resultsByBILOU.reportPrediction(w.neTypeLevel2, w.neLabel);
                    resultsSegmentation.reportPrediction(bracketTypePrediction, bracketTypeLabel);
                }
        }
    }
}
