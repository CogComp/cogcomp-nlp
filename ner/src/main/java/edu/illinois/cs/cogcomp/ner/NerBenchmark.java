/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

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

    /** if true, skip all training. */
    private boolean skiptraining = true;

    /**
     * this will cause a report of the labels for each word, this is useful for comparing
     * differences between models.
     */
    private boolean reportLabels = false;

    /** Report the input features for each level */
    private boolean reportFeatures = false;

    /** build the final release model, using test and train to train on, dev as a hold out
     * for auto convergence. */
    private boolean release = false;

    /** Report the input features for each level */
    private boolean verbose = false;

    /** the output file name. */
    private String output = null;

    /** -1 to converge automatically, positive number to do a fixed number of iterations. */
    private int iterations = -1;

    /** If this is set, we will start with the existing weights (and averages) for the 
     * model and continue training from there. */
    private boolean incremental = false;

    /**
     * all default.
     */
    private NerBenchmark() {
    }

    /**
     * Create an instance with more arguments to provide more control over the training process.
     * @param benchmarks the name of the directory containing all the training directories.
     * @param incremental true if existing model is to be update.
     * @param verbose true to provide more output.
     * @param train true to training a new model.
     */
    private NerBenchmark(String benchmarks, boolean incremental, boolean verbose, boolean train) {
        this.incremental = incremental;
        this.verbose = verbose;
        this.skiptraining = !train;
    }

    /**
     * @return if true, skip training.
     */
    public boolean isSkiptraining() {
        return skiptraining;
    }

    /**
     * @param skiptraining if true, skip training.
     */
    public NerBenchmark setSkiptraining(boolean skiptraining) {
        this.skiptraining = skiptraining;
        return this;
    }

    /**
     * @return if true report labels names.
     */
    public boolean isReportLabels() {
        return reportLabels;
    }

    /**
     * @param reportLabels  if true report labels names.
     */
    public NerBenchmark setReportLabels(boolean reportLabels) {
        this.reportLabels = reportLabels;
        return this;
    }

    /**
     * @return the reportFeatures
     */
    public boolean isReportFeatures() {
        return reportFeatures;
    }

    /**
     * @param reportFeatures the reportFeatures to set
     */
    public NerBenchmark setReportFeatures(boolean reportFeatures) {
        this.reportFeatures = reportFeatures;
        return this;
    }

    /**
     * @return the release
     */
    public boolean isRelease() {
        return release;
    }

    /**
     * @param release the release to set
     */
    public NerBenchmark setRelease(boolean release) {
        this.release = release;
        return this;
    }

    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public NerBenchmark setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public NerBenchmark setOutput(String output) {
        this.output = output;
        return this;
    }

    /**
     * @return the iterations
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * @param iterations the iterations to set
     */
    public NerBenchmark setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    /**
     * @return the incremental
     */
    public boolean isIncremental() {
        return incremental;
    }

    /**
     * @param incremental the incremental to set
     */
    public NerBenchmark setIncremental(boolean incremental) {
        this.incremental = incremental;
        return this;
    }

    /**
     * for the builder design pattern, less the factory, which I consider just a waste of
     * time.
     * @return a default instance of an NerBenchmark.
     */
    public static NerBenchmark build() {
        return new NerBenchmark();
    }

    /**
     * Run a benchmark test against each subdirectory within the benchmark directory.
     * 
     * @param args may specify the directory containing the benchmarks.
     * @throws Exception if anything goes wrong.
     */
    public static void main(String[] args) throws Exception {
        String directory = "benchmark";
        boolean skiptraining = true;
        boolean reportLabels = false;
        boolean reportFeatures = false;
        boolean release = false;
        boolean verbose = false;
        String output = null;
        int iterations = -1;
        boolean incremental = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-d":
                    i++;
                    if (args.length <= i) {
                        throw new IllegalArgumentException("The \"-d\" command line argument must be followed by a directory name " + "containing the benchmark configuration and data.");
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
                        throw new IllegalArgumentException("The \"-iterations\" command line argument must be followed by an integer " + "indicating the number of training iterations.");
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
        NerBenchmark bm = NerBenchmark.build().setIncremental(incremental).setIterations(iterations).setOutput(output).setRelease(release).setReportFeatures(reportFeatures).setSkiptraining(skiptraining).setReportLabels(reportLabels).setVerbose(verbose);

        // Loop over every directory within the benchmark directory. Each subdirectory will contain
        // a configuration file, and a directory with the test data at the very least. If there is
        // also a train directory, a new model will be trained.
        String[] configs = new File(directory).list();
        if (configs == null || configs.length == 0) {
            throw new RuntimeException("There were no directories within \"" + directory + "\". " + "Expected directories for each dataset to evaluate.");
        }

        // for each directory, run the benchmark test once for each config file within the config
        // directory.
        for (String benchmarkdir : configs) {
            String dir = directory + "/" + benchmarkdir;
            if (!new File(dir).isDirectory())
                continue;
            File configsDir = new File(dir + "/config/");
            if (!configsDir.exists()) {
                System.err.println("There was no config file in " + configsDir);
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
                    bm.execute(confFile, trainDirName, trainDir, devDirName, devDir, testDirName, testDir);
                }
            }
        }
    }

    /**
     * This method does all the work of actually training the model. It requires the config file name, the name of the training
     * directory, and a file representation of that, and the same for the dev and test directories.
     * @param confFile the name of the config file.
     * @param trainDirName the name of the train directory.
     * @param trainDir the file object for the train directory.
     * @param devDirName the name of the dev directory.
     * @param devDir the file object for the dev directory.
     * @param testDirName the test directory name.
     * @param testDir the test directory file object.
     * @throws Exception 
     */
    public Vector<TestDiscrete[]> execute(String confFile, String trainDirName, File trainDir, String devDirName, File devDir, String testDirName, File testDir) throws Exception {
        if (!skiptraining) {
            if (trainDir.exists() && testDir.exists() && devDir.exists()) {
                return trainModel(confFile, trainDirName, trainDir, devDirName, devDir, testDirName, testDir);
            } else {
                System.err.println("Training requires a \"train\", \"test\" and \"dev\" subdirectory!");
            }
        } else if (!release) {

            // if not training, and not build a release model, we are just reporting the accuracy of the existing
            // model against a training set.
            reportResults(confFile, testDirName);
        }
        if (release) {

            // we are building a release model. This differs from the standard training in that we will build the 
            // model using a fixed number of iterations, and we will build it against ALL the data, test, train and 
            // dev data using the number of iterations to determine a stopping point rather than using the dev set
            // for that.
            if (trainDir.exists() && testDir.exists() && devDir.exists()) {
                ParametersForLbjCode prms = Parameters.readConfigAndLoadExternalData(confFile, true);
                ResourceManager rm = new ResourceManager(confFile);
                ModelLoader.load(rm, rm.getString("modelName"), true, prms);
                System.out.println("\n\n----- Building a final model for " + confFile + " ------");

                // there is a training directory, with training enabled, so train. We use the same dataset
                // for both training and evaluating.
                LearningCurveMultiDataset.buildFinalModel(iterations, trainDirName, testDirName, devDirName, incremental, prms);
                System.out.println("\n\n----- Release results for " + confFile + ", verbose ------");
                NETesterMultiDataset.test(devDirName, true, prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
                System.out.println("\n\n----- Release results for " + confFile + ", F1 only ------");
                return NETesterMultiDataset.test(devDirName, false, prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
            } else {
                System.out.println("Building a final model requires a \"train\", \"test\" and \"dev\" subdirectory.");
            }
        }
        return null;
    }

    /**
     * Report the result for the currently existing model. The config file specifies the location of the models 
     * on the file system, use the labeled data in the test directory to produce the models accuracy results.
     * @param confFile the configuration file.
     * @param testDirName the directory with the test data in it.
     * @throws Exception if anything goes wrong.
     */
    private void reportResults(String confFile, String testDirName) throws Exception {
        System.out.println("\n\n----- Reporting results from existing models for " + confFile + " ------");
        ParametersForLbjCode prms = Parameters.readConfigAndLoadExternalData(confFile, !skiptraining);
        ResourceManager rm = new ResourceManager(confFile);
        ModelLoader.load(rm, rm.getString("modelName"), !skiptraining, prms);
        System.out.println("Benchmark against configuration : " + confFile);
        if (reportLabels)
            NEDisplayPredictions.test(testDirName, "-c", verbose, prms);
        else if (reportFeatures)
            NETesterMultiDataset.dumpFeaturesLabeledData(testDirName, output, prms);
        else
            NETesterMultiDataset.test(testDirName, verbose, prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
    }

    /**
     * This method does all the work of actually training the model. It requires the config file name, the name of the training
     * directory, and a file representation of that, and the same for the dev and test directories. The model is trained only
     * against the training data, dev data is used to test for convergence, rather than using the number of iterations, and the 
     * test data is held out to compute the final accuracy results which are returned.
     * @param confFile the name of the config file.
     * @param trainDirName the name of the train directory.
     * @param trainDir the file object for the train directory.
     * @param devDirName the name of the dev directory.
     * @param devDir the file object for the dev directory.
     * @param testDirName the test directory name.
     * @param testDir the test directory file object.
     * @return 
     * @throws Exception 
     */
    private Vector<TestDiscrete[]> trainModel(String confFile, String trainDirName, File trainDir, String devDirName, File devDir, String testDirName, File testDir) throws Exception {
        System.out.println("\n\n----- Training models for evaluation for " + confFile + " ------");
        ParametersForLbjCode prms = Parameters.readConfigAndLoadExternalData(confFile, true);
        ResourceManager rm = new ResourceManager(confFile);
        ModelLoader.load(rm, rm.getString("modelName"), true, prms);
        NETaggerLevel1 taggerLevel1 = (NETaggerLevel1) prms.taggerLevel1;
        NETaggerLevel2 taggerLevel2 = (NETaggerLevel2) prms.taggerLevel2;
        SparseAveragedPerceptron sap1 = (SparseAveragedPerceptron) taggerLevel1.getBaseLTU();
        sap1.setLearningRate(prms.learningRatePredictionsLevel1);
        sap1.setThickness(prms.thicknessPredictionsLevel1);
        System.out.println("L1 learning rate = " + sap1.getLearningRate() + ", thickness = " + sap1.getPositiveThickness());
        if (prms.featuresToUse.containsKey("PredictionsLevel1")) {
            SparseAveragedPerceptron sap2 = (SparseAveragedPerceptron) taggerLevel2.getBaseLTU();
            sap2.setLearningRate(prms.learningRatePredictionsLevel2);
            sap2.setThickness(prms.thicknessPredictionsLevel2);
            System.out.println("L2 learning rate = " + sap2.getLearningRate() + ", thickness = " + sap2.getPositiveThickness());
        }

        // there is a training directory, with training enabled, so train. We use the same dataset
        // for both training and evaluating.
        LearningCurveMultiDataset.getLearningCurve(iterations, trainDirName, devDirName, incremental, prms);
        System.out.println("\n\n----- Final results for " + confFile + ", verbose ------");
        NETesterMultiDataset.test(testDirName, true, prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
        System.out.println("\n\n----- Final results for " + confFile + ", F1 only ------");
        return NETesterMultiDataset.test(testDirName, false, prms.labelsToIgnoreInEvaluation, prms.labelsToAnonymizeInEvaluation, prms);
    }
}
