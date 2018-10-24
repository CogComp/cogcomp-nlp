/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense;

import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandIgnore;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.sl.core.StructuredProblem;
import edu.illinois.cs.cogcomp.sl.inference.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.verbsense.caches.FeatureVectorCacheFile;
import edu.illinois.cs.cogcomp.verbsense.caches.SentenceDBHandler;
import edu.illinois.cs.cogcomp.verbsense.core.ModelInfo;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.data.Dataset;
import edu.illinois.cs.cogcomp.verbsense.data.VerbSensePropbankReader;
import edu.illinois.cs.cogcomp.verbsense.experiment.PreExtractor;
import edu.illinois.cs.cogcomp.verbsense.experiment.PruningPreExtractor;
import edu.illinois.cs.cogcomp.verbsense.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.verbsense.inference.ILPInference;
import edu.illinois.cs.cogcomp.verbsense.inference.MulticlassInference;
import edu.illinois.cs.cogcomp.verbsense.learn.JLISLearner;
import edu.illinois.cs.cogcomp.verbsense.learn.LearnerParameters;
import edu.illinois.cs.cogcomp.verbsense.utilities.VerbSenseConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VerbSenseClassifierMain {
    private final static Logger log = LoggerFactory.getLogger(VerbSenseClassifierMain.class);

    private static ResourceManager rm;

    @CommandIgnore
    public static void main(String[] arguments) throws Exception {
        InteractiveShell<VerbSenseClassifierMain> shell =
                new InteractiveShell<>(VerbSenseClassifierMain.class);

        if (arguments.length == 0) {
            System.err.println("Usage: <config-file> command");
            System.err.println("Required parameter config-file missing.");
            shell.showDocumentation();
        } else if (arguments.length == 1) {
            System.err.println("Usage: <config-file> command");
            shell.showDocumentation();
        } else {
            long start_time = System.currentTimeMillis();
            try {
                rm = new VerbSenseConfigurator().getDefaultConfig();

                String[] args = new String[arguments.length - 1];
                System.arraycopy(arguments, 1, args, 0, args.length);
                shell.runCommand(args);

                long runTime = (System.currentTimeMillis() - start_time) / 1000;
                System.out.println("This experiment took " + runTime + " secs");

            } catch (AssertionError e) {
                e.printStackTrace();
                System.exit(-1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @CommandDescription(description = "Performs the full training & testing sequence",
            usage = "expt cacheDatasets=[true | false]")
    public static void expt(String cacheDatasets) throws Exception {

        // Step 1: Cache all the datasets we're going to use
        if (Boolean.parseBoolean(cacheDatasets))
            cacheDatasets();

        // Step 2: Pre-extract and train the model
        preExtract();
        train();

        // Step 3: Evaluate
        evaluate();
    }

    @CommandDescription(description = "Reads and caches all the datasets", usage = "cacheDatasets")
    public static void cacheDatasets() throws Exception {
        log.info("Initializing datasets");
        SentenceDBHandler.instance.initializeDatasets(VerbSenseConfigurator.getSentenceDBFile(rm));

        // Add Propbank data
        log.info("Caching PropBank data");
        cachePropbank();

        log.info("Cached PropBank");

        log.info("Adding required views in PTB");
        addRequiredViews(SentenceDBHandler.instance.getDataset(Dataset.PTBAll));
    }

    private static void cachePropbank() throws Exception {
        String treebankHome = rm.getString(VerbSenseConfigurator.PENN_TREEBANK_HOME);
        String[] allSectionsArray = VerbSenseConfigurator.getAllSections();
        List<String> trainSections = Arrays.asList(VerbSenseConfigurator.getAllTrainSections());
        List<String> testSections = Arrays.asList(VerbSenseConfigurator.getTestSections());
        List<String> trainDevSections = Arrays.asList(VerbSenseConfigurator.getTrainDevSections());
        List<String> devSections = Arrays.asList(VerbSenseConfigurator.getDevSections());
        List<String> ptb0204Sections = Arrays.asList("02", "03", "04");

        String dataHome = rm.getString(VerbSenseConfigurator.PROPBANK_HOME.key);

        String goldView = SenseManager.getGoldViewName();
        Iterator<TextAnnotation> data =
                new VerbSensePropbankReader(treebankHome, dataHome, allSectionsArray);

        int count = 0;
        while (data.hasNext()) {
            TextAnnotation ta = data.next();
            if (ta.hasView(goldView)) {
                String id = ta.getId();
                String section = id.substring(id.indexOf('/') + 1, id.lastIndexOf('/'));
                SentenceDBHandler.instance.addTextAnnotation(Dataset.PTBAll, ta);
                if (trainSections.contains(section))
                    SentenceDBHandler.instance.addTextAnnotation(Dataset.PTBTrain, ta);
                if (devSections.contains(section))
                    SentenceDBHandler.instance.addTextAnnotation(Dataset.PTBDev, ta);
                if (trainDevSections.contains(section))
                    SentenceDBHandler.instance.addTextAnnotation(Dataset.PTBTrainDev, ta);
                if (testSections.contains(section))
                    SentenceDBHandler.instance.addTextAnnotation(Dataset.PTBTest, ta);
                if (ptb0204Sections.contains(section))
                    SentenceDBHandler.instance.addTextAnnotation(Dataset.PTB0204, ta);
            }

            count++;
            if (count % 10000 == 0)
                System.out.println(count + " sentences done");
        }
    }

    private static void addRequiredViews(IResetableIterator<TextAnnotation> dataset) {
        Counter<String> addedViews = new Counter<>();

        log.info("Initializing pre-processor");
        TextPreProcessor.initialize();

        int count = 0;
        while (dataset.hasNext()) {
            TextAnnotation ta = dataset.next();
            Set<String> views = new HashSet<>(ta.getAvailableViews());

            try {
                TextPreProcessor.getInstance().preProcessText(ta);
            } catch (Exception e) {
                // Remove from dataset
                log.error("Annotation failed, removing sentence from dataset");
                SentenceDBHandler.instance.removeTextAnnotation(ta);
                continue;
            }

            Set<String> newViews = new HashSet<>(ta.getAvailableViews());
            newViews.removeAll(views);

            if (newViews.size() > 0) {
                SentenceDBHandler.instance.updateTextAnnotation(ta);
                for (String s : newViews)
                    addedViews.incrementCount(s);
            }
            count++;
            if (count % 1000 == 0)
                System.out.println(count + " sentences done");
        }
        System.out.println("New views: ");
        for (String s : addedViews.items())
            System.out.println(s + "\t" + addedViews.getCount(s));
    }

    @CommandIgnore
    public static SenseManager getManager(boolean trainingMode) throws Exception {
        return new SenseManager(trainingMode);
    }

    @CommandDescription(
            description = "Pre-extracts the features for the verb-sense model. Run this before training.",
            usage = "preExtract")
    public static void preExtract() throws Exception {
        SenseManager manager = getManager(true);

        ResourceManager conf = new VerbSenseConfigurator().getDefaultConfig();

        // If models directory doesn't exist create it
        if (!IOUtils.isDirectory(conf.getString(conf
                .getString(VerbSenseConfigurator.MODELS_DIRECTORY))))
            IOUtils.mkdir(conf.getString(conf.getString(VerbSenseConfigurator.MODELS_DIRECTORY)));

        int numConsumers = Runtime.getRuntime().availableProcessors();

        Dataset dataset = Dataset.PTBTrainDev;

        log.info("Pre-extracting features");
        ModelInfo modelInfo = manager.getModelInfo();

        String featureSet = "" + modelInfo.featureManifest.getIncludedFeatures().hashCode();

        String allDataCacheFile =
                VerbSenseConfigurator.getFeatureCacheFile(featureSet, dataset, rm);
        FeatureVectorCacheFile featureCache =
                preExtract(numConsumers, manager, dataset, allDataCacheFile);

        pruneFeatures(numConsumers, manager, featureCache,
                VerbSenseConfigurator.getPrunedFeatureCacheFile(featureSet, rm));

        Lexicon lexicon = modelInfo.getLexicon().getPrunedLexicon(manager.getPruneSize());

        log.info("Saving lexicon  with {} features to {}", lexicon.size(),
                manager.getLexiconFileName());
        log.info(lexicon.size() + " features in the lexicon");

        lexicon.save(manager.getLexiconFileName());
    }

    private static void pruneFeatures(int numConsumers, SenseManager manager,
            FeatureVectorCacheFile featureCache, String cacheFile2) throws Exception {
        if (IOUtils.exists(cacheFile2)) {
            log.warn("Old pruned cache file found. Deleting...");
            IOUtils.rm(cacheFile2);
            log.info("Done");
        }

        log.info("Pruning features. Saving pruned features to {}", cacheFile2);
        FeatureVectorCacheFile prunedfeatureCache = new FeatureVectorCacheFile(cacheFile2, manager);
        PruningPreExtractor p1 =
                new PruningPreExtractor(manager, featureCache, prunedfeatureCache, numConsumers);
        p1.run();
        p1.finalize();
    }

    private static FeatureVectorCacheFile preExtract(int numConsumers, SenseManager manager,
            Dataset dataset, String cacheFile) throws Exception {
        if (IOUtils.exists(cacheFile)) {
            log.warn("Old cache file found. Deleting...");
            IOUtils.rm(cacheFile);
            log.info("Done");
        }

        FeatureVectorCacheFile featureCache = new FeatureVectorCacheFile(cacheFile, manager);
        Iterator<TextAnnotation> data = SentenceDBHandler.instance.getDataset(dataset);
        PreExtractor p = new PreExtractor(manager, data, numConsumers, featureCache);

        p.run();

        p.finalize();
        return featureCache;
    }

    @CommandDescription(description = "Trains the verb-sense model.", usage = "train")
    public static void train() throws Exception {
        SenseManager manager = getManager(true);
        int numThreads = Runtime.getRuntime().availableProcessors();

        ModelInfo modelInfo = manager.getModelInfo();

        String featureSet = "" + modelInfo.featureManifest.getIncludedFeatures().hashCode();
        String cacheFile = VerbSenseConfigurator.getPrunedFeatureCacheFile(featureSet, rm);
        AbstractInferenceSolver[] inference = new AbstractInferenceSolver[numThreads];

        // TODO Can I replace this with ILPInference?
        for (int i = 0; i < inference.length; i++)
            inference[i] = new MulticlassInference(manager);

        double c;
        FeatureVectorCacheFile cache;

        cache = new FeatureVectorCacheFile(cacheFile, manager);
        StructuredProblem cvProblem = cache.getStructuredProblem(20000);
        cache.close();
        LearnerParameters params = JLISLearner.crossvalStructSVMSense(cvProblem, inference, 4);
        c = params.getcStruct();
        log.info("c = {} after cv", c);


        cache = new FeatureVectorCacheFile(cacheFile, manager);

        StructuredProblem problem = cache.getStructuredProblem();
        cache.close();

        WeightVector w = JLISLearner.trainStructSVM(inference, problem, c);
        JLISLearner.saveWeightVector(w, manager.getModelFileName());
    }

    @CommandDescription(description = "Performs evaluation.", usage = "evaluate")
    public static void evaluate() throws Exception {
        SenseManager manager = getManager(false);
        Dataset testSet = Dataset.PTBTest;

        ILPSolverFactory solver =
                new ILPSolverFactory(ILPSolverFactory.SolverType.JLISCuttingPlaneGurobi);

        ClassificationTester senseTester = new ClassificationTester();
        long start = System.currentTimeMillis();
        int count = 0;

        manager.getModelInfo().loadWeightVector();

        IResetableIterator<TextAnnotation> dataset = SentenceDBHandler.instance.getDataset(testSet);
        while (dataset.hasNext()) {
            TextAnnotation ta = dataset.next();
            if (!ta.hasView(SenseManager.getGoldViewName()))
                continue;

            TokenLabelView gold = (TokenLabelView) ta.getView(SenseManager.getGoldViewName());

            ILPInference inference = manager.getInference(solver, gold.getConstituents());

            assert inference != null;
            TokenLabelView prediction = inference.getOutputView();

            evaluateSense(gold, prediction, senseTester);

            count++;
            if (count % 1000 == 0) {
                long end = System.currentTimeMillis();
                log.info(count + " sentences done. Took " + (end - start)
                        + "ms, Micro-F1 so far = " + senseTester.getMicroF1());
            }
        }

        long end = System.currentTimeMillis();
        System.out.println(count + " sentences done. Took " + (end - start) + "ms");
        System.out.println("\n\n* Sense");
        System.out.println(senseTester.getPerformanceTable(false).toOrgTable());
    }

    private static void evaluateSense(TokenLabelView gold, TokenLabelView prediction,
            ClassificationTester tester) {
        Map<Constituent, Constituent> goldToPredictionPredicateMapping = new HashMap<>();

        // Align the gold constituents to the predicted ones
        // XXX not really needed since we're evaluating with gold predicates
        for (Constituent gp : gold.getConstituents()) {
            for (Constituent pp : prediction.getConstituents()) {
                if (gp.getSpan().equals(pp.getSpan())) {
                    goldToPredictionPredicateMapping.put(gp, pp);
                    break;
                }
            }
        }

        for (Constituent gp : gold.getConstituents()) {
            if (goldToPredictionPredicateMapping.containsKey(gp)) {
                Constituent pp = goldToPredictionPredicateMapping.get(gp);
                String goldSense = gp.getLabel();

                // XXX: As in training, all predicates that are labeled as XX are marked as 01
                if (goldSense.equals("XX"))
                    goldSense = "01";

                String predSense = pp.getLabel();
                assert predSense != null;
                tester.record(goldSense, predSense);
            }
        }
    }
}
