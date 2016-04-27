/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.TwoLayerPredictionAggregationFeatures;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.PredictionsAndEntitiesConfidenceScores;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Vector;

public class LearningCurveMultiDataset {

    private static final String NAME = LearningCurveMultiDataset.class.getCanonicalName();
    private static final Logger logger = LoggerFactory.getLogger(LearningCurveMultiDataset.class);

    /**
     * train a model with the specified inputs, evaluate with the specified test data
     * <p>
     * use fixedNumIterations=-1 if you want to use the automatic convergence criterion
     */
    public static void getLearningCurve(int fixedNumIterations, String trainDataPath,
            String testDataPath) throws Exception {
        logger.debug("getLearningCurve(): fni = " + fixedNumIterations + " trainDataPath = '"
                + trainDataPath + "' testDataPath = '" + testDataPath + "'....");
        Data trainData =
                new Data(trainDataPath, trainDataPath, "-c", new String[] {}, new String[] {});
        ExpressiveFeaturesAnnotator.annotate(trainData);
        Data testData =
                new Data(testDataPath, testDataPath, "-c", new String[] {}, new String[] {});
        ExpressiveFeaturesAnnotator.annotate(testData);
        Vector<Data> train = new Vector<>();
        train.addElement(trainData);
        Vector<Data> test = new Vector<>();
        test.addElement(testData);
        getLearningCurve(train, test, fixedNumIterations);
    }

    /**
     * use fixedNumIterations=-1 if you want to use the automatic convergence criterion
     * <p>
     * NB: assuming column format
     */
    public static void getLearningCurve(Vector<Data> trainDataSet, Vector<Data> testDataSet,
            int fixedNumIterations) throws Exception {
        double bestF1Level1 = -1;
        int bestRoundLevel1 = 0;
        // Get the directory name (<configname>.model is appended in LbjTagger/Parameters.java:139)
        String modelPath = ParametersForLbjCode.currentParameters.pathToModelFile;
        String modelPathDir = modelPath.substring(0, modelPath.lastIndexOf("/"));
        if (IOUtils.exists(modelPathDir)) {
            if (!IOUtils.isDirectory(modelPathDir)) {
                String msg =
                        "ERROR: " + NAME + ".getLearningCurve(): model directory '" + modelPathDir
                                + "' already exists as a (non-directory) file.";
                logger.error(msg);
                throw new IOException(msg);
            } else
                logger.warn(NAME + ".getLearningCurve(): writing to existing model path '"
                        + modelPathDir + "'...");
        } else {
            IOUtils.mkdir(modelPathDir);
        }

        NETaggerLevel1 tagger1 =
                new NETaggerLevel1(modelPath + ".level1", modelPath + ".level1.lex");
        tagger1.forget();

        for (int dataId = 0; dataId < trainDataSet.size(); dataId++) {
            Data trainData = trainDataSet.elementAt(dataId);
            if (ParametersForLbjCode.currentParameters.featuresToUse
                    .containsKey("PredictionsLevel1")) {
                PredictionsAndEntitiesConfidenceScores.getAndMarkEntities(trainData,
                        NEWord.LabelToLookAt.GoldLabel);
                TwoLayerPredictionAggregationFeatures.setLevel1AggregationFeatures(trainData, true);
            }
        }


        logger.info("Pre-extracting the training data for Level 1 classifier");
        // TODO: 2/22/16 This is not OS-neutral, should be removed
        Runtime rt = Runtime.getRuntime();
        Process pr =
                rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1.prefetchedTrainData*");
        pr.waitFor();
        pr =
                rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1.prefetchedTestData*");
        pr.waitFor();
        String arg =
                ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1.prefetchedTrainData";
        logger.info("Pre-extracting the training data for Level 1 classifier");
        BatchTrainer bt1train = prefetchAndGetBatchTrainer(tagger1, trainDataSet, arg);
        arg = ParametersForLbjCode.currentParameters.pathToModelFile + ".level1.prefetchedTestData";

        logger.info("Pre-extracting the testing data for Level 1 classifier, argument '" + arg
                + "...");
        BatchTrainer bt1test = prefetchAndGetBatchTrainer(tagger1, testDataSet, arg);
        Parser testParser1 = bt1test.getParser();

        for (int i = 0; (fixedNumIterations == -1 && i < 200 && i - bestRoundLevel1 < 10)
                || (fixedNumIterations > 0 && i <= fixedNumIterations); ++i) {
            logger.info("Learning first level classifier; round " + i);
            bt1train.train(1);

            logger.info("Testing level 1 classifier;  on prefetched data, round: " + i);
            testParser1.reset();
            TestDiscrete simpleTest = new TestDiscrete();
            simpleTest.addNull("O");
            TestDiscrete.testDiscrete(simpleTest, tagger1, null, testParser1, true, 0);

            double f1Level1 = simpleTest.getOverallStats()[2];
            if (f1Level1 > bestF1Level1) {
                bestF1Level1 = f1Level1;
                bestRoundLevel1 = i;
                tagger1.save();
            }

            if (i % 5 == 0)
                logger.info(i + " rounds.  Best so far for Level1 : (" + bestRoundLevel1 + ")="
                        + bestF1Level1);
        }
        // TODO: 2/22/16 This is not OS-neutral, should be removed
        pr =
                rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1.prefetchedTrainData*");
        pr.waitFor();
        pr =
                rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1.prefetchedTestData*");
        pr.waitFor();

        logger.info("Testing level 1 classifier, final performance: ");
        TestDiscrete[] results =
                NETesterMultiDataset.printAllTestResultsAsOneDataset(testDataSet, tagger1, null,
                        false);
        double f1Level1 = results[0].getOverallStats()[2];
        logger.info("Level 1; round " + bestRoundLevel1 + "\t" + f1Level1);


        NETaggerLevel2 tagger2 =
                new NETaggerLevel2(ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level2", ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level2.lex");
        tagger2.forget();
        // Previously checked if PatternFeatures was in featuresToUse.
        if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PredictionsLevel1")) {
            double bestF1Level2 = -1;
            int bestRoundLevel2 = 0;
            // TODO: 2/22/16 This is not OS-neutral, should be removed
            pr =
                    rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2.prefetchedTrainData*");
            pr.waitFor();
            pr =
                    rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2.prefetchedTestData*");
            pr.waitFor();
            logger.info("Pre-extracting the training data for Level 2 classifier");
            BatchTrainer bt2train =
                    prefetchAndGetBatchTrainer(tagger2, trainDataSet,
                            ParametersForLbjCode.currentParameters.pathToModelFile
                                    + ".level2.prefetchedTrainData");
            logger.info("Pre-extracting the testing data for Level 2 classifier");
            BatchTrainer bt2test =
                    prefetchAndGetBatchTrainer(tagger2, testDataSet,
                            ParametersForLbjCode.currentParameters.pathToModelFile
                                    + ".level2.prefetchedTestData");
            Parser testParser2 = bt2test.getParser();

            for (int i = 0; (fixedNumIterations == -1 && i < 200 && i - bestRoundLevel2 < 10)
                    || (fixedNumIterations > 0 && i <= fixedNumIterations); ++i) {
                logger.info("Learning Level2 ; round " + i);
                bt2train.train(1);

                logger.info("Testing level 2 classifier;  on prefetched data, round: " + i);
                testParser2.reset();
                TestDiscrete simpleTest = new TestDiscrete();
                simpleTest.addNull("O");
                TestDiscrete.testDiscrete(simpleTest, tagger2, null, testParser2, true, 0);


                double f1Level2 = simpleTest.getOverallStats()[2];
                if (f1Level2 > bestF1Level2) {
                    bestF1Level2 = f1Level2;
                    bestRoundLevel2 = i;
                    tagger2.save();
                }

                if (i % 5 == 0)
                    logger.info(i + " rounds.  Best so far for Level2 : (" + bestRoundLevel2 + ") "
                            + bestF1Level2);
            }
            // TODO: 2/22/16 This is not OS-neutral, should be removed
            pr =
                    rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2.prefetchedTrainData*");
            pr.waitFor();
            pr =
                    rt.exec("rm ./ " + ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2.prefetchedTestData*");
            pr.waitFor();

            logger.info("Testing both levels ...");
            results =
                    NETesterMultiDataset.printAllTestResultsAsOneDataset(testDataSet, tagger1,
                            tagger2, false);
            f1Level1 = results[0].getOverallStats()[2];
            double f1Level2 = results[1].getOverallStats()[2];
            logger.info("Level1: bestround=" + bestRoundLevel1 + "\t F1=" + f1Level1
                    + "\t Level2: bestround=" + bestRoundLevel2 + "\t F1=" + f1Level2);
        }


        /*
         * This will override the models forcing to save the iteration we're interested in- the
         * fixedNumIterations iteration, the last one. But note - both layers will be saved for this
         * iteration. If the best performance for one of the layers came before the final iteration,
         * we're in a small trouble- the performance will decrease
         */
        if (fixedNumIterations > -1) {
            tagger1.save();
            tagger2.save();
        }
    }

    /*
     * Parts is the number of parts to which we split the data. in training - if you have a lot of
     * samples- use 100 partitions otherwise, the zip doesn't work on training files larger than 4G
     */
    private static BatchTrainer prefetchAndGetBatchTrainer(SparseNetworkLearner classifier,
            Vector<Data> dataSets, String exampleStorePath) {
        logger.info("Pre-extracting the training data for Level 1 classifier");
        for (int dataId = 0; dataId < dataSets.size(); dataId++) {
            Data data = dataSets.elementAt(dataId);
            TextChunkRepresentationManager.changeChunkRepresentation(
                    TextChunkRepresentationManager.EncodingScheme.BIO,
                    ParametersForLbjCode.currentParameters.taggingEncodingScheme, data,
                    NEWord.LabelToLookAt.GoldLabel);
        }
        BatchTrainer bt = new BatchTrainer(classifier, new SampleReader(dataSets), 1000);
        logger.debug("setting lexicon from batchtrainer, exampleStorePath is '" + exampleStorePath
                + "'...");

        classifier.setLexicon(bt.preExtract(exampleStorePath));
        for (int dataId = 0; dataId < dataSets.size(); dataId++) {
            Data trainData = dataSets.elementAt(dataId);
            TextChunkRepresentationManager.changeChunkRepresentation(
                    ParametersForLbjCode.currentParameters.taggingEncodingScheme,
                    TextChunkRepresentationManager.EncodingScheme.BIO, trainData,
                    NEWord.LabelToLookAt.GoldLabel);
        }
        return bt;
    }


    public static class SampleReader implements Parser {
        public Vector<Data> dataset = null;
        int datasetId = 0;
        int docid = 0;
        int sentenceId = 0;
        int tokenId = 0;
        int generatedSamples = 0;

        public SampleReader(Vector<Data> dataset) {
            this.dataset = dataset;
        }

        public void close() {
            // do nothing
        }

        public Object next() {
            if (datasetId >= dataset.size())
                return null;
            // logger.debug("token = "+tokenId+"; sentence = "+sentenceId+"; dataset = "+datasetId+" ---  datasets="+dataset.size()+" now sentences= "+dataset.elementAt(datasetId).sentences.size()+"; now tokens = "+dataset.elementAt(datasetId).sentences.elementAt(sentenceId).size());
            Object res =
                    dataset.elementAt(datasetId).documents.get(docid).sentences.get(sentenceId)
                            .get(tokenId);
            if (tokenId < dataset.elementAt(datasetId).documents.get(docid).sentences.get(
                    sentenceId).size() - 1)
                tokenId++;
            else {
                tokenId = 0;
                if (sentenceId < dataset.elementAt(datasetId).documents.get(docid).sentences.size() - 1) {
                    sentenceId++;
                } else {
                    sentenceId = 0;
                    if (docid < dataset.elementAt(datasetId).documents.size() - 1) {
                        docid++;
                    } else {
                        docid = 0;
                        datasetId++;
                    }
                }
            }
            // logger.debug("token = "+tokenId+"; sentence = "+sentenceId+"; dataset = "+datasetId+" ---  datasets="+dataset.size()+" now sentences= "+dataset.elementAt(datasetId).sentences.size()+"; now tokens = "+dataset.elementAt(datasetId).sentences.elementAt(sentenceId).size());
            generatedSamples++;
            // logger.debug(generatedSamples+" samples generated by SampleReader");
            return res;
        }

        public void reset() {
            datasetId = 0;
            sentenceId = 0;
            tokenId = 0;
            generatedSamples = 0;
        }
    }
}
