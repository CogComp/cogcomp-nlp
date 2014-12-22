package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandIgnore;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.edison.annotators.HeadFinderDependencyViewGenerator;
import edu.illinois.cs.cogcomp.edison.data.ColumnFormatWriter;
import edu.illinois.cs.cogcomp.edison.data.IResetableIterator;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.sl.core.StructuredProblem;
import edu.illinois.cs.cogcomp.sl.inference.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.caches.FeatureVectorCacheFile;
import edu.illinois.cs.cogcomp.srl.caches.SentenceDBHandler;
import edu.illinois.cs.cogcomp.srl.core.ModelInfo;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.data.Dataset;
import edu.illinois.cs.cogcomp.srl.data.NombankReader;
import edu.illinois.cs.cogcomp.srl.data.PropbankReader;
import edu.illinois.cs.cogcomp.srl.experiment.PreExtractor;
import edu.illinois.cs.cogcomp.srl.experiment.PruningPreExtractor;
import edu.illinois.cs.cogcomp.srl.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPInference;
import edu.illinois.cs.cogcomp.srl.inference.SRLMulticlassInference;
import edu.illinois.cs.cogcomp.srl.learn.IdentifierThresholdTuner;
import edu.illinois.cs.cogcomp.srl.learn.JLISLearner;
import edu.illinois.cs.cogcomp.srl.learn.LearnerParameters;
import edu.illinois.cs.cogcomp.srl.nom.NomSRLManager;
import edu.illinois.cs.cogcomp.srl.utilities.PredicateArgumentEvaluator;
import edu.illinois.cs.cogcomp.srl.verb.VerbSRLManager;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class Main {
	private final static Logger log = LoggerFactory.getLogger(Main.class);

	private static String defaultParser;
	private static SRLProperties properties;


	@CommandIgnore
	public static void main(String[] arguments) throws ConfigurationException {

		InteractiveShell<Main> shell = new InteractiveShell<Main>(Main.class);

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
				SRLProperties.initialize(arguments[0]);
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

		log.info("Cahced all datasets");

		log.info("Adding required views in PTB");
		addRequiredViews(SentenceDBHandler.instance.getDataset(Dataset.PTBAll));
	}

	private static void cacheVerbNom(SRLType srlType) throws Exception {
		String treebankHome = properties.getPennTreebankHome();
		String[] allSectionsArray = properties.getAllSections();
		List<String> trainSections = Arrays.asList(properties.getAllTrainSections());
		List<String> testSections = Arrays.asList(properties.getTestSections());
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

	private static void addRequiredViews(IResetableIterator<TextAnnotation> dataset) {
		Counter<String> addedViews = new Counter<String>();

		log.info("Initializing pre-processor");
		TextPreProcessor.initialize(true);

		int count = 0;
		while (dataset.hasNext()) {
			TextAnnotation ta = dataset.next();
			Set<String> views = new HashSet<String>(ta.getAvailableViews());

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

			Set<String> newViews = new HashSet<String>(ta.getAvailableViews());
			newViews.removeAll(views);

			if (newViews.size() > 0) {
				SentenceDBHandler.instance.updateTextAnnotation(ta);
				for (String s : newViews) addedViews.incrementCount(s);
			}
			count++;
			if (count % 1000 == 0) System.out.println(count + " sentences done");
		}
		System.out.println("New views: ");
		for (String s : addedViews.items()) System.out.println(s + "\t" + addedViews.getCount(s));
	}

	@CommandIgnore
	public static SRLManager getManager(SRLType srlType, boolean trainingMode) throws Exception {
		String viewName;
		if (defaultParser == null) defaultParser = SRLProperties.getInstance().getDefaultParser();
		if (defaultParser.equals("Charniak")) viewName = ViewNames.PARSE_CHARNIAK;
		else if (defaultParser.equals("Berkeley")) viewName = ViewNames.PARSE_BERKELEY;
		else if (defaultParser.equals("Stanford")) viewName = ViewNames.PARSE_STANFORD;
		else viewName = defaultParser;

		if (srlType == SRLType.Verb)
			return new VerbSRLManager(trainingMode, viewName);
		else if (srlType == SRLType.Nom)
			return new NomSRLManager(trainingMode, viewName);
		else return null;
	}

	@CommandDescription(description = "Pre-extracts the features for a specific model and SRL type. " +
			"Run this before training",
			usage = "preExtract [Verb | Nom | Prep] [Predicate | Sense | Identifier | Classifier]")
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
			log.warn("Old pruned cache file found. Deleting...");
			IOUtils.rm(cacheFile2);
			log.info("Done");
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
			log.warn("Old cache file found. Deleting...");
			IOUtils.rm(cacheFile);
			log.info("Done");
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
		int numThreads = Runtime.getRuntime().availableProcessors();

		Models model = Models.valueOf(model_);
		ModelInfo modelInfo = manager.getModelInfo(model);

		String featureSet = "" + modelInfo.featureManifest.getIncludedFeatures().hashCode();
		String cacheFile = properties.getPrunedFeatureCacheFile(srlType, model, featureSet, defaultParser);
		AbstractInferenceSolver[] inference = new AbstractInferenceSolver[numThreads];

		for (int i = 0; i < inference.length; i++)
			inference[i] = new SRLMulticlassInference(manager, model);

		double c;
		FeatureVectorCacheFile cache;

		if (model == Models.Classifier) {
			c = 0.00390625;
			log.info("Skipping cross-validation for Classifier. c = {}", c);
		}
		else {
			cache = new FeatureVectorCacheFile(cacheFile, model, manager);
			StructuredProblem cvProblem = cache.getStructuredProblem(20000);
			cache.close();
			LearnerParameters params = JLISLearner.cvStructSVMSRL(cvProblem, inference, 5);
			c = params.getcStruct();
			log.info("c = {} for {} after cv", c, srlType + " " + model);
		}

		cache = new FeatureVectorCacheFile(cacheFile, model, manager);

		StructuredProblem problem = cache.getStructuredProblem();
		cache.close();

		WeightVector w = JLISLearner.trainStructSVM(inference, problem, c);
		JLISLearner.saveWeightVector(w, manager.getModelFileName(model));
	}

	private static void tuneIdentifier(String srlType_) throws Exception {
		SRLType srlType = SRLType.valueOf(srlType_);
		SRLManager manager = getManager(srlType, true);

		int nF = 2;
		if (srlType == SRLType.Nom)
			nF = 3;

		ModelInfo modelInfo = manager.getModelInfo(Models.Identifier);
		modelInfo.loadWeightVector();

		String featureSet = "" + modelInfo.featureManifest.getIncludedFeatures().hashCode();

		Dataset dataset = Dataset.PTBDev;
		String cacheFile = properties.getFeatureCacheFile(srlType,
				Models.Identifier, featureSet, defaultParser, dataset);

		FeatureVectorCacheFile cache = new FeatureVectorCacheFile(cacheFile, Models.Identifier, manager);

		StructuredProblem problem = cache.getStructuredProblem();
		cache.close();

		IdentifierThresholdTuner tuner = new IdentifierThresholdTuner(manager, nF, problem);

		List<Double> A = new ArrayList<Double>();
		List<Double> B = new ArrayList<Double>();

		for (double x = 0.01; x < 10; x += 0.01) {
			A.add(x);
			B.add(x);
		}

		Pair<Double, Double> pair = tuner.tuneIdentifierScale(A, B);
		manager.writeIdentifierScale(pair.getFirst(), pair.getSecond());
	}

	@CommandDescription(description = "Performs evaluation.", usage = "evaluate [Verb | Prep | Nom]")
	public static void evaluate(String srlType_) throws Exception {
		SRLType srlType = SRLType.valueOf(srlType_);
		SRLManager manager = getManager(srlType, false);

		Dataset testSet = Dataset.PTBTest;

		ILPSolverFactory solver = new ILPSolverFactory(ILPSolverFactory.SolverType.CuttingPlaneGurobi);

		String outDir = properties.getOutputDir();
		PrintWriter goldWriter = null, predWriter = null;
		ColumnFormatWriter writer = null;
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

		while (dataset.hasNext()) {
			TextAnnotation ta = dataset.next();

			if (!ta.hasView(manager.getGoldViewName())) continue;

			//ta.addView(new HeadFinderDependencyViewGenerator(manager.defaultParser));
			PredicateArgumentView gold = (PredicateArgumentView) ta.getView(manager.getGoldViewName());

			SRLILPInference inference = manager.getInference(solver, gold.getPredicates());

			assert inference != null;
			PredicateArgumentView prediction = inference.getOutputView();

			PredicateArgumentEvaluator.evaluate(gold, prediction, tester);
			PredicateArgumentEvaluator.evaluateSense(gold, prediction, senseTester);

			if (outDir != null) {
				writer.printPredicateArgumentView(gold, goldWriter);
				writer.printPredicateArgumentView(prediction, predWriter);
			}

			count++;
			if (count % 1000 == 0) {
				long end = System.currentTimeMillis();
				log.info(count + " sentences done. Took "
						+ (end - start) + "ms, F1 so far = "
						+ tester.getAverageF1());
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
