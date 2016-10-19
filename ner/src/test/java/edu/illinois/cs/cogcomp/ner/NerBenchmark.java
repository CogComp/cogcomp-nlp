/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This runs a standard benchmark test across several datasets for the NER package. Training can be
 * enabled by passing in the "-training" flag. The configurations and data are expected in the
 * "benchmark" directory. Within, there will be a directory for each dataset you want to run
 * against. Within those individual dataset directories, there will be a "config" directory
 * containing one or more configuration files, a "test" directory (which can be a link to the
 * directory with the test data), and a "train" directory where the training data files are found.
 * If only a "train" directory is located in the directory, that will be used for both test and
 * train directories. Each configuration file will result in a run to evaluate the results, and
 * potentially one run to train up the model. The results are presented on standard out.
 * <p>
 *
 * {@code
 * Directory format:
 * - "benchmark"
 *   - <dataset name> there can be as many of these directories as you like, Reuters, Ontonotes, MUC7
 *    and Web are examples of datasets one might run.
 *     - "config" : this must contain one or more configuration files, there will be at least one run per config file.
 *     - "test" : the test directory. If training, and not test directory, the "train" directory will be used for both.
 *     - "train" : the directory with the training data, only needed if "-training" passed.
 *     - "dev" : the hold out set for training.
 * 
 * Command Line Options:
 * -verbose : provide detailed report on all scoring methods with separate evaluation for phrase level tokenization, 
 * word level tokenization and so on. Alternatively, only the overall F1 scores are reported.
 * -training : this option will cause a training run, if training, evaluation will not be performed, that requires another run.
 * -features : for debugging, reports the feature vector for each token in the dataset. Output produced in a "features.out" file.
 * }
 */
public class NerBenchmark {
    private static Logger logger = LoggerFactory.getLogger(NerBenchmark.class);

    /** default directory containing benchmark runs. */
    static String directory = "benchmark";

    /** if true, skip all training. */
    static boolean skiptraining = true;

    /**
     * this will cause a report of the labels for each word, this is useful for comparing
     * differences between models.
     */
    static boolean reportLabels = false;

    /** Report the input features for each level */
    static boolean reportFeatures = false;

    /** Report the input features for each level */
    static boolean verbose = false;

    /** the output file name. */
    static String output = null;

    /**
     * parse the arguments, only the directory.
     * 
     * @param args the arguments.
     */
    private static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-d":
                    i++;
                    if (args.length <= i) {
                        throw new IllegalArgumentException(
                                "The \"-d\" command line argument must be followed by a directory name "
                                        + "containing the benchmark configuration and data.");
                    }
                    directory = args[i];
                    break;
                case "-verbose":
                    verbose = true;
                    break;
                case "-training":
                    skiptraining = false;
                    break;
                case "-report":
                    reportLabels = true;
                    break;
                case "-features":
                    reportFeatures = true;
                    if (args.length <= i + 1) {
                        output = "features.out";
                    } else {
                        i++;
                        output = args[i];
                    }
                    break;
            }
        }
    }

    /**
     * Run a benchmark test against each subdirectory within the benchmark directory.
     * 
     * @param args may specify the directory containing the benchmarks.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        parseArguments(args);

        // Loop over every directory within the benchmark directory. Each subdirectory will contain
        // a configuration file, and a directory with the test data at the very least. If there is
        // also
        // a train directory, a new model will be trained.
        String[] configs = new File(directory).list();

        // for each directory, run the benchmark test once for each config file within the config
        // directory.
        for (String benchmarkdir : configs) {
            String dir = directory + "/" + benchmarkdir;
            File configsDir = new File(dir + "/config/");
            
            // training data.
            String trainDirName = dir + "/train/";
            File trainDir = new File(trainDirName);
            
            // hold out set for training
            String devDirName = dir + "/dev/";
            File devDir = new File(devDirName);

            // final test set.
            String testDirName = dir + "/test/";
            if (!new File(testDirName).exists())
                testDirName = dir + "/train/";
            
            if (configsDir.exists() && configsDir.isDirectory()) {
                String[] configfiles = configsDir.list();
                for (String confFile : configfiles) {
                    confFile = dir + "/config/" + confFile;
                    if (!skiptraining) {
                        Parameters.readConfigAndLoadExternalData(confFile, !skiptraining);
                        if (!trainDir.exists()) {
                            System.out.print("Expected a training directory named " + trainDirName
                                    + ", but it is not there.");
                            System.exit(0);
                        }
                        logger.info("----- Training up the model ------");

                        // there is a training directory, with training enabled, so train. We use the same dataset
                        // for both training and evaluating.
                        LearningCurveMultiDataset.getLearningCurve(-1, trainDirName, devDirName);
                        logger.info("completed training against configuration : " + confFile);
                        logger.info("\n\n\n\n----- Final Results, testing against test set ------");
                        NETesterMultiDataset.test(testDirName, true,
                                ParametersForLbjCode.currentParameters.labelsToIgnoreInEvaluation,
                                ParametersForLbjCode.currentParameters.labelsToAnonymizeInEvaluation);
                        logger.info("\n\n----- Final Results, F1 only ------");
                        NETesterMultiDataset.test(testDirName, false,
                                ParametersForLbjCode.currentParameters.labelsToIgnoreInEvaluation,
                                ParametersForLbjCode.currentParameters.labelsToAnonymizeInEvaluation);
                        
                    } else {
                        Parameters.readConfigAndLoadExternalData(confFile, !skiptraining);
                        logger.info("Benchmark against configuration : " + confFile);
                        if (reportLabels)
                            NEDisplayPredictions.test(testDirName, "-c", verbose);
                        else if (reportFeatures)
                            NETesterMultiDataset.dumpFeaturesLabeledData(testDirName, output);
                        else
                            NETesterMultiDataset.test(testDirName, verbose,
                                            ParametersForLbjCode.currentParameters.labelsToIgnoreInEvaluation,
                                            ParametersForLbjCode.currentParameters.labelsToAnonymizeInEvaluation);
                    }
                }
            }
        }
    }
}
