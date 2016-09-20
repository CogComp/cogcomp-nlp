package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandIgnore;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.NombankReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PropbankReader;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.caches.FeatureVectorCacheFile;
import edu.illinois.cs.cogcomp.srl.caches.SentenceDBHandler;
import edu.illinois.cs.cogcomp.srl.core.ModelInfo;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.data.Dataset;
import edu.illinois.cs.cogcomp.srl.experiment.PreExtractor;
import edu.illinois.cs.cogcomp.srl.experiment.PruningPreExtractor;
import edu.illinois.cs.cogcomp.srl.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPInference;
import edu.illinois.cs.cogcomp.srl.inference.SRLMulticlassInference;
import edu.illinois.cs.cogcomp.srl.learn.SRLFeatureExtractor;
import edu.illinois.cs.cogcomp.srl.learn.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLMulticlassLabel;
import edu.illinois.cs.cogcomp.srl.learn.IdentifierThresholdTuner;
import edu.illinois.cs.cogcomp.srl.nom.NomSRLManager;
import edu.illinois.cs.cogcomp.srl.utilities.WeightVectorUtils;
import edu.illinois.cs.cogcomp.srl.verb.VerbSRLManager;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.PredicateArgumentEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
	private final static Logger log = LoggerFactory.getLogger(Main.class);

	private static String defaultParser;
	private static SRLProperties properties;
	private static String configFile;


	@CommandIgnore
	public static void main(String[] arguments) {

		InteractiveShell<Main> shell = new InteractiveShell<>(Main.class);

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
				configFile = arguments[0];
				SRLProperties.initialize(configFile);
				properties = SRLProperties.getInstance();

				defaultParser = SRLProperties.getInstance().getDefaultParser();

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

	@CommandDescription(description = "Performs the full training & testing sequence for all SRL types",
			usage = "expt [Verb | Nom] cacheDatasets=[true | false]")
	public static void expt(String srlType, String cacheDatasets) throws Exception {
		// Step 1: Cache all the datasets we're going to use
		if (Boolean.parseBoolean(cacheDatasets)) cacheDatasets();

		// Step 2: Iterate between pre-extracting all the features needed for training and training

        // We don't need to train a predicate classifier for Verb
        if (SRLType.valueOf(srlType) == SRLType.Nom) {
            preExtract(srlType, "Predicate");
            train(srlType, "Predicate");
        }

        preExtract(srlType, "Sense");
        train(srlType, "Sense");

        preExtract(srlType, "Identifier");
        train(srlType, "Identifier");
        tuneIdentifier(srlType);

        preExtract(srlType, "Classifier");
        train(srlType, "Classifier");

		// Step 3: Evaluate
		evaluate(srlType);
	}

	@CommandDescription(description = "Reads and caches all the datasets", usage = "cacheDatasets")
	public static void cacheDatasets() throws Exception {
		log.info("Initializing datasets");
		SentenceDBHandler.instance.initializeDatasets(properties.getSentenceDBFile());

		// Add Propbank data
		log.info("Caching PropBank data");
		cacheVerbNom(SRLType.Verb);

		log.info("Caching NomBank data");
		cacheVerbNom(SRLType.Nom);

		log.info("Cached all datasets");

		log.info("Adding required views in PTB");
		addRequiredViews(SentenceDBHandler.instance.getDataset(Dataset.PTBAll));
	}

	private static void cacheVerbNom(SRLType srlType) throws Exception {
		String treebankHome = properties.getPennTreebankHome();
		String[] allSectionsArray = properties.getAllSections();
		List<String> trainSections = Arrays.asList(properties.getAllTrainSections());
		List<String> testSections = Collections.singletonList(properties.getTestSections());
		List<String> trainDevSections = Arrays.asList(properties.getTrainDevSections());
		List<String> devSections = Arrays.asList(properties.getDevSections());
		List<String> ptb0204Sections = Arrays.asList("02", "03", "04");

		String dataHome;
		if (srlType == SRLType.Verb)
			dataHome = properties.getPropbankHome();
		else dataHome = properties.getNombankHome();

		String goldView = (srlType == SRLType.Verb)? ViewNames.SRL_VERB : ViewNames.SRL_NOM;
		goldView +=  "_GOLD";
		Iterator<TextAnnotation> data;
		if (srlType == SRLType.Verb)
			data = new PropbankReader(treebankHome, dataHome, allSectionsArray, goldView, true);
		else
			data = new NombankReader(treebankHome, dataHome, allSectionsArray, goldView, true);

		int count = 0;
		while (data.hasNext()) {
			TextAnnotation ta = data.next();
			if (ta.hasView(goldView)) {
				String id = ta.getId();
				String section = id.substring(id.indexOf('/')+1, id.lastIndexOf('/'));
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
			if (count % 10000 == 0) System.out.println(count + " sentences done");
		}
	}

	private static void addRequiredViews(IResetableIterator<TextAnnotation> dataset) throws IOException {
		Counter<String> addedViews = new Counter<>();

		log.info("Initializing pre-processor");
		TextPreProcessor.initialize( new ResourceManager( configFile ));

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
			String parserView = ViewNames.DEPENDENCY + ":";
			String parser = properties.getDefaultParser();
			if (parser.equals("Charniak")) parserView += ViewNames.PARSE_CHARNIAK;
			if (parser.equals("Berkeley")) parserView += ViewNames.PARSE_BERKELEY;
			if (parser.equals("Stanford")) parserView += ViewNames.PARSE_STANFORD;
			if (ta.getView(parserView).getNumberOfConstituents() != ta.getSentence(0).size()) {
				log.error("Head-dependency mismatch, removing sentence from dataset");
				SentenceDBHandler.instance.removeTextAnnotation(ta);
				continue;
			}

			Set<String> newViews = new HashSet<>(ta.getAvailableViews());
			newViews.removeAll(views);

			if (newViews.size() > 0) {
				SentenceDBHandler.instance.updateTextAnnotation(ta);
				for (String s : newViews) addedViews.incrementCount(s);
			}
			count++;
			if (count % 1000 == 0) log.info(count + " sentences done");
		}
		log.info("New views: ");
		for (String s : addedViews.items()) log.info(s + "\t" + addedViews.getCount(s));
	}

	@CommandIgnore
	public static SRLManager getManager(SRLType srlType, boolean trainingMode) throws Exception {
		String viewName;
		if (defaultParser == null) defaultParser = SRLProperties.getInstance().getDefaultParser();
        switch (defaultParser) {
            case "Charniak":
                viewName = ViewNames.PARSE_CHARNIAK;
                break;
            case "Berkeley":
                viewName = ViewNames.PARSE_BERKELEY;
                break;
            case "Stanford":
                viewName = ViewNames.PARSE_STANFORD;
                break;
            default:
                viewName = defaultParser;
                break;
        }

		if (srlType == SRLType.Verb)
			return new VerbSRLManager(trainingMode, viewName);
		else if (srlType == SRLType.Nom)
			return new NomSRLManager(trainingMode, viewName);
		else return null;
	}

	@CommandDescription(description = "Pre-extracts the features for a specific model and SRL type. " +
			"Run this before training",
			usage = "preExtract [Verb | Nom] [Predicate | Sense | Identifier | Classifier]")
	public static void preExtract(String srlType_, String model) throws Exception {
		SRLType srlType = SRLType.valueOf(srlType_);
		SRLManager manager = getManager(srlType, true);

		String gapStr = "";
		for (int i  =0; i < ((29-model.length())/2); i++) gapStr += " ";
		log.info("\n\n\n\n" +
				"**************************************************\n" +
				"** " + gapStr + "PRE-EXTRACTING " + model.toUpperCase() + gapStr + " **\n" +
				"**************************************************\n");
		// If models directory doesn't exist create it
		if (!IOUtils.isDirectory(SRLProperties.getInstance().getModelsDir()))
			IOUtils.mkdir(SRLProperties.getInstance().getModelsDir());

		int numConsumers = Runtime.getRuntime().availableProcessors();

		Models modelToExtract = Models.valueOf(model);

		Dataset dataset = Dataset.PTBTrainDev;
		if (modelToExtract == Models.Identifier)
			dataset = Dataset.PTBTrain;


		log.info("Pre-extracting {} features", modelToExtract);
        assert manager != null;
        ModelInfo modelInfo = manager.getModelInfo(modelToExtract);

		String featureSet = "" + modelInfo.featureManifest.getIncludedFeatures().hashCode();

		String allDataCacheFile = properties.getFeatureCacheFile(srlType,
				modelToExtract, featureSet, defaultParser, dataset);
		FeatureVectorCacheFile featureCache = preExtract(numConsumers, manager,
				modelToExtract, dataset, allDataCacheFile, false);

		pruneFeatures(numConsumers, manager, modelToExtract, featureCache,
				properties.getPrunedFeatureCacheFile(srlType, modelToExtract, featureSet, defaultParser));

		Lexicon lexicon = modelInfo.getLexicon().getPrunedLexicon(
				manager.getPruneSize(modelToExtract));

		log.info("Saving lexicon  with {} features to {}", lexicon.size(),
				manager.getLexiconFileName(modelToExtract));
		log.info(lexicon.size() + " features in the lexicon");

		lexicon.save(manager.getLexiconFileName(modelToExtract));

		// We don't have a separate dev set to tune Identifier for prepSRL
		if (modelToExtract == Models.Identifier) {
			Dataset datasetIdentifier = Dataset.PTBDev;
			String devCacheFile = properties.getFeatureCacheFile(srlType,
					modelToExtract, featureSet, defaultParser, datasetIdentifier);
			preExtract(numConsumers, manager, modelToExtract, datasetIdentifier, devCacheFile, false);
		}
	}

	private static void pruneFeatures(
			int numConsumers, SRLManager manager,
			Models modelToExtract, FeatureVectorCacheFile featureCache,
			String cacheFile2) throws Exception {
		if (IOUtils.exists(cacheFile2)) {
			log.warn("Old pruned cache file found. Not doing anything...");
			return;
		}

		log.info("Pruning features. Saving pruned features to {}", cacheFile2);

		FeatureVectorCacheFile prunedfeatureCache = new FeatureVectorCacheFile(
				cacheFile2, modelToExtract, manager);

		PruningPreExtractor p1 = new PruningPreExtractor(manager,
				modelToExtract, featureCache, prunedfeatureCache, numConsumers);
		p1.run();
		p1.finalize();
	}

	private static FeatureVectorCacheFile preExtract(
			int numConsumers, SRLManager manager, Models modelToExtract, Dataset dataset,
			String cacheFile, boolean lockLexicon) throws Exception {
		if (IOUtils.exists(cacheFile)) {
			log.warn("Old cache file found. Returning it...");
			FeatureVectorCacheFile vectorCacheFile = new FeatureVectorCacheFile(cacheFile, modelToExtract, manager);
			vectorCacheFile.openReader();
			while (vectorCacheFile.hasNext()) {
				Pair<SRLMulticlassInstance, SRLMulticlassLabel> pair=vectorCacheFile.next();
				IFeatureVector cachedFeatureVector = pair.getFirst().getCachedFeatureVector(modelToExtract);
				int length=cachedFeatureVector.getNumActiveFeatures();
                for(int i=0;i<length;i++) {
                    manager.getModelInfo(modelToExtract).getLexicon().countFeature(cachedFeatureVector.getIdx(i));
                }
			}
			vectorCacheFile.close();
			vectorCacheFile.openReader();
			return vectorCacheFile;
		}

		FeatureVectorCacheFile featureCache = new FeatureVectorCacheFile(cacheFile, modelToExtract, manager);
		Iterator<TextAnnotation> data = SentenceDBHandler.instance.getDataset(dataset);
		PreExtractor p = new PreExtractor(manager, data, numConsumers, modelToExtract, featureCache);

		if (lockLexicon)
			p.lockLexicon();

		p.run();

		p.finalize();
		return featureCache;
	}

	@CommandDescription(description = "Trains a specific model and SRL type",
			usage = "train [Verb | Nom] [Predicate | Sense | Identifier | Classifier]")
	public static void train(String srlType_, String model_) throws Exception {
		SRLType srlType = SRLType.valueOf(srlType_);
		SRLManager manager = getManager(srlType, true);

		String gapStr = "";
		for (int i  =0; i < ((36-model_.length())/2); i++) gapStr += " ";
		log.info("\n\n\n\n" +
				"**************************************************\n" +
				"** " + gapStr + "TRAINING " + model_.toUpperCase() + gapStr + " **\n" +
				"**************************************************\n");

		Models model = Models.valueOf(model_);
        assert manager != null;
        ModelInfo modelInfo = manager.getModelInfo(model);

		String featureSet = "" + modelInfo.featureManifest.getIncludedFeatures().hashCode();
		String cacheFile = properties.getPrunedFeatureCacheFile(srlType, model, featureSet, defaultParser);
		System.out.println("In train feat cahce is "+cacheFile);

        // NB: Tuning code for the C value has been deleted
		double c = 0.01;
		FeatureVectorCacheFile cache;

		if (model == Models.Classifier) c = 0.00390625;

		cache = new FeatureVectorCacheFile(cacheFile, model, manager);
		SLProblem problem;
		problem = cache.getStructuredProblem();

		cache.close();

		log.info("Setting up solver, learning may take time if you have too many instances in SLProblem ....");

		SLParameters params = new SLParameters();
		params.loadConfigFile(properties.getLearnerConfig());
		params.C_FOR_STRUCTURE = (float) c;

		SRLMulticlassInference infSolver = new SRLMulticlassInference(manager, model);
		Learner learner = LearnerFactory.getLearner(infSolver, new SRLFeatureExtractor(), params);
		WeightVector w = learner.train(problem);
		WeightVectorUtils.save(manager.getModelFileName(model), w);
	}

	private static void tuneIdentifier(String srlType_) throws Exception {
		SRLType srlType = SRLType.valueOf(srlType_);
		SRLManager manager = getManager(srlType, true);

		int nF = 2;
		if (srlType == SRLType.Nom)
			nF = 3;

        assert manager != null;
        ModelInfo modelInfo = manager.getModelInfo(Models.Identifier);
		modelInfo.loadWeightVector();

		String featureSet = "" + modelInfo.featureManifest.getIncludedFeatures().hashCode();

		Dataset dataset = Dataset.PTBDev;
		String cacheFile = properties.getFeatureCacheFile(srlType,
				Models.Identifier, featureSet, defaultParser, dataset);

		FeatureVectorCacheFile cache = new FeatureVectorCacheFile(cacheFile, Models.Identifier, manager);

		SLProblem problem = cache.getStructuredProblem();
		cache.close();

		IdentifierThresholdTuner tuner = new IdentifierThresholdTuner(manager, nF, problem);

		List<Double> A = new ArrayList<>();
		List<Double> B = new ArrayList<>();

		for (double x = 0.01; x < 10; x += 0.01) {
			A.add(x);
			B.add(x);
		}

		Pair<Double, Double> pair = tuner.tuneIdentifierScale(A, B);
		manager.writeIdentifierScale(pair.getFirst(), pair.getSecond());
	}

	@CommandDescription(description = "Performs evaluation.", usage = "evaluate [Verb | Nom]")
	public static void evaluate(String srlType_) throws Exception {
		SRLType srlType = SRLType.valueOf(srlType_);
		SRLManager manager = getManager(srlType, false);

		Dataset testSet = Dataset.PTBTest;

		ILPSolverFactory solver = new ILPSolverFactory(properties.getILPSolverType(true));

		String outDir = properties.getOutputDir();
		PrintWriter goldWriter = null, predWriter = null;
		ColumnFormatWriter writer = null;
        assert manager != null;
        String goldOutFile = null, predOutFile = null;
        if (outDir != null) {
            // If output directory doesn't exist, create it
            if (!IOUtils.isDirectory(outDir)) IOUtils.mkdir(outDir);

            String outputFilePrefix = outDir+"/" + srlType + "."
					+ manager.defaultParser + "." + new Random().nextInt();

			goldOutFile = outputFilePrefix + ".gold";
			goldWriter = new PrintWriter(new File(goldOutFile));
			predOutFile = outputFilePrefix + ".predicted";
			predWriter = new PrintWriter(new File(predOutFile));
			writer = new ColumnFormatWriter();
		}

		ClassificationTester tester = new ClassificationTester();
		tester.ignoreLabelFromSummary("V");


		ClassificationTester senseTester = new ClassificationTester();

		long start = System.currentTimeMillis();
		int count = 0;

        manager.getModelInfo(Models.Identifier).loadWeightVector();
		manager.getModelInfo(Models.Classifier).loadWeightVector();

		manager.getModelInfo(Models.Sense).loadWeightVector();
		IResetableIterator<TextAnnotation> dataset = SentenceDBHandler.instance.getDataset(testSet);
		log.info("All models weights loaded now!");
        PredicateArgumentEvaluator evaluator = new PredicateArgumentEvaluator();

		while (dataset.hasNext()) {
			TextAnnotation ta = dataset.next();

			if (!ta.hasView(manager.getGoldViewName())) continue;

			PredicateArgumentView gold = (PredicateArgumentView) ta.getView(manager.getGoldViewName());

			SRLILPInference inference = manager.getInference(solver, gold.getPredicates());

			assert inference != null;
			PredicateArgumentView prediction = inference.getOutputView();

            evaluator.evaluate(tester, gold, prediction);
            evaluator.evaluateSense(senseTester, gold, prediction);

			if (outDir != null) {
				writer.printPredicateArgumentView(gold, goldWriter);
			}

			count++;
			if (count % 1000 == 0) {
				long end = System.currentTimeMillis();
				log.info(count + " sentences done. Took " + (end - start) + "ms, " +
                        "F1 so far = " + tester.getMacroF1());
			}
		}
		long end = System.currentTimeMillis();
		System.out.println(count + " sentences done. Took " + (end - start) + "ms");

		System.out.println("* Arguments");
		System.out.println(tester.getPerformanceTable(false).toOrgTable());

		System.out.println("\n\n* Sense");
		System.out.println(senseTester.getPerformanceTable(false).toOrgTable());

		if (outDir != null) {
			goldWriter.close();
			predWriter.close();

			System.out.println("To use standard CoNLL evaluation, compare the following files:\n"
					+ goldOutFile + " " + predOutFile);
		}
	}
}
