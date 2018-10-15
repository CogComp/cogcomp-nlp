/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;

import java.io.File;
import java.io.FilenameFilter;

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
 *     - "config" : this must contain one or more configuration files, there will be a run per config file, only files
 *     ending with ".config" are processed
 *     - "test" : the test directory. If training, and not test directory, the "train" directory will be used for both.
 *     - "train" : the directory with the training data, only needed if "-training" passed.
 *     - "dev" : the hold out set for training.
 * 
 * Command Line Options:
 * -verbose : provide detailed report on all scoring methods with separate evaluation for phrase level tokenization, 
 * word level tokenization and so on. Alternatively, only the overall F1 scores are reported.
 * -training : this option will cause a training run, if training, evaluation will not be performed, that requires another run.
 * -features : for debugging, reports the feature vector for each token in the dataset. Output produced in a "features.out" file.
 * -iterations : specify a fixed number of iterations, or -1 (the default) means auto converge requiring a "dev" directory.
 * -release : build a final model for release, it will build on test and train, and unless "-iterations" specified, it will autoconvert
 * -incremental : rather than discarding existing weights, start with those that already exist and continue training.
 * using "dev" for a holdout set.
 * }
 */
public class NerBenchmark {
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

    /** build the final release model, using test and train to train on, dev as a hold out
     * for auto convergence. */
    static boolean release = false;

    /** Report the input features for each level */
    static boolean verbose = false;

    /** the output file name. */
    static String output = null;

    /** -1 to converge automatically, positive number to do a fixed number of iterations. */
    static int iterations = -1;
    
    /** If this is set, we will start with the existing weights (and averages) for the 
     * model and continue training from there. */
    static boolean incremental = false;

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
                case "-incremental":
                    System.out.println("Configured for incremental training.");
                    incremental = true;
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
                case "-release":
                    release = true;
                    break;
                case "-iterations":
                    i++;
                    if (args.length <= i) {
                        throw new IllegalArgumentException(
                                "The \"-iterations\" command line argument must be followed by an integer "
                                + "indicating the number of training iterations.");
                    }
                    iterations = Integer.parseInt(args[i]);
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
     * @throws Exception if anything goes wrong.
     */
    public static void main(String[] args) throws Exception {
        parseArguments(args);

        // Loop over every directory within the benchmark directory. Each subdirectory will contain
        // a configuration file, and a directory with the test data at the very least. If there is
        // also a train directory, a new model will be trained.
        String[] configs = new File(directory).list();
        if (configs == null || configs.length == 0) {
            throw new RuntimeException("There were no directories within \""+directory+"\". "
                + "Expected directories for each dataset to evaluate.");
        }

        // for each directory, run the benchmark test once for each config file within the config
        // directory.
        for (String benchmarkdir : configs) {
            String dir = directory + "/" + benchmarkdir;
            if (!new File(dir).isDirectory())
                continue;
            File configsDir = new File(dir + "/config/");
            if (!configsDir.exists()) {
                System.err.println("There was no config file in "+configsDir);
                continue;
            }

            // training data.
            String trainDirName = dir + "/train/";
            File trainDir = new File(trainDirName);
            
            // hold out set for training
            String devDirName = dir + "/dev/";
            File devDir = new File(devDirName);

            // final test set.
            String testDirName = dir + "/test/";
            File testDir = new File(testDirName);
            
            if (configsDir.exists() && configsDir.isDirectory()) {
                String[] configfiles = configsDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".config");
                    }
                });
                for (String confFile : configfiles) {
                    confFile = dir + "/config/" + confFile;
                    ParametersForLbjCode prms = null;
                    if (!skiptraining) {
                        if (trainDir.exists() && testDir.exists() && devDir.exists()) {
                            System.out.println("\n\n----- Training models for evaluation for "+confFile+" ------");
                            prms = Parameters.readConfigAndLoadExternalData(confFile, true);
                            ResourceManager rm = new ResourceManager(confFile);
                            ModelLoader.load(rm, rm.getString("modelName"), true, prms);
                            NETaggerLevel1 taggerLevel1 = (NETaggerLevel1) prms.taggerLevel1;
                            NETaggerLevel2 taggerLevel2 = (NETaggerLevel2) prms.taggerLevel2;
                            SparseAveragedPerceptron sap1 = (SparseAveragedPerceptron)taggerLevel1.getBaseLTU();
                            sap1.setLearningRate(prms.learningRatePredictionsLevel1);
                            sap1.setThickness(prms.thicknessPredictionsLevel1);
                            System.out.println("L1 learning rate = "+sap1.getLearningRate()+", thickness = "+sap1.getPositiveThickness());
                            if (prms.featuresToUse.containsKey("PredictionsLevel1")) {
                                SparseAveragedPerceptron sap2 = (SparseAveragedPerceptron)taggerLevel2.getBaseLTU();
                                sap2.setLearningRate(prms.learningRatePredictionsLevel2);
                                sap2.setThickness(prms.thicknessPredictionsLevel2);
                                System.out.println("L2 learning rate = "+sap2.getLearningRate()+", thickness = "+sap2.getPositiveThickness());
                            }
                            
                            // there is a training directory, with training enabled, so train. We use the same dataset
                            // for both training and evaluating.
                            LearningCurveMultiDataset.getLearningCurve(iterations, trainDirName, devDirName, incremental, prms);
                            System.out.println("\n\n----- Final results for "+confFile+", verbose ------");
                            NETesterMultiDataset.test(testDirName, true,
                                prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
                            System.out.println("\n\n----- Final results for "+confFile+", F1 only ------");
                            NETesterMultiDataset.test(testDirName, false,
                                prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
                        } else {
                            System.out.println("Training requires a \"train\", \"test\" and \"dev\" subdirectory, "
                                + "not so within "+dir+", skipping that directory.");
                        }
                    } else if (!release) {
                        System.out.println("\n\n----- Reporting results from existing models for "+confFile+" ------");
                        prms = Parameters.readConfigAndLoadExternalData(confFile, !skiptraining);
                        ResourceManager rm = new ResourceManager(confFile);
                        ModelLoader.load(rm, rm.getString("modelName"), !skiptraining, prms);
                        System.out.println("Benchmark against configuration : " + confFile);
                        if (reportLabels)
                            NEDisplayPredictions.test(testDirName, "-c", verbose, prms);
                        else if (reportFeatures)
                            NETesterMultiDataset.dumpFeaturesLabeledData(testDirName, output, prms);
                        else
                            NETesterMultiDataset.test(testDirName, verbose,
                                prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
                    }

                    if (release) {
                        if (trainDir.exists() && testDir.exists() && devDir.exists()) {
                            prms = Parameters.readConfigAndLoadExternalData(confFile, true);
                            ResourceManager rm = new ResourceManager(confFile);
                            ModelLoader.load(rm, rm.getString("modelName"), true, prms);
                            System.out.println("\n\n----- Building a final model for "+confFile+" ------");

                            // there is a training directory, with training enabled, so train. We use the same dataset
                            // for both training and evaluating.
                            LearningCurveMultiDataset.buildFinalModel(iterations, trainDirName, testDirName, devDirName, incremental, prms);
                            System.out.println("\n\n----- Release results for "+confFile+", verbose ------");
                            NETesterMultiDataset.test(devDirName, true,
                                prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
                            System.out.println("\n\n----- Release results for "+confFile+", F1 only ------");
                            NETesterMultiDataset.test(devDirName, false,
                                    prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
                        } else {
                            System.out.println("Building a final model requires a \"train\", \"test\" and \"dev\" subdirectory, "
                                + "not so within "+dir+", skipping that directory.");
                        }
                    }
                }
            }
        }
    }
}
