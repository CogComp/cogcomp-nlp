package edu.illinois.cs.cogcomp.verbsense.core;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.math.MathUtilities;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.manifest.FeatureManifest;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.verbsense.Constants;
import edu.illinois.cs.cogcomp.verbsense.Properties;
import edu.illinois.cs.cogcomp.verbsense.features.FeatureGenerators;
import edu.illinois.cs.cogcomp.verbsense.features.ProjectedPath;
import edu.illinois.cs.cogcomp.verbsense.inference.ILPInference;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Manages the lexicon, model and feature extractors for the sense classifier
 *
 * @author Vivek Srikumar
 *
 */
public class SenseManager {
	private final static Logger log = LoggerFactory.getLogger(SenseManager.class);

	private static final String[] allSenses = { "01", "02", "03", "04", "05",
			"06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16",
			"17", "18", "19", "20", "21" };

	/**
	 * A flag that indicates that the manager is in training mode, which prompts
	 * extensive caching.
	 */
	public final boolean trainingMode;

	/**
	 * This class works with a parse trees from this parser, which controls both
	 * candidate extraction and feature generation.
	 */
	public final String defaultParser;

	private final HashMap<String, Integer> senseToId;

	private final Map<String, Set<String>> legalSenses;

	private final Properties properties = Properties.getInstance();

	private ModelInfo modelInfo;

	public final SenseExampleGenerator exampleGenerator;
	private final PredicateDetector heuristicPredicateDetector;

	public SenseManager(boolean trainingMode, String defaultParser) throws Exception {
		this.trainingMode = trainingMode;
		this.defaultParser = defaultParser;

		initializeFeatureManifest(defaultParser);

		senseToId = getLabelIdMap(getSenseLabels());
		legalSenses = getLegalSensesMap();

		log.info("{} senses: " + Sorters.sortSet(senseToId.keySet()), senseToId.size());

		// Load WN properties file from the classpath
		WordNetManager.loadConfigAsClasspathResource(true);

		initializeModelInfo();
		//XXX Copy this method from illinois-srl if constraints are added
		//initializeConstraints();

		exampleGenerator = new SenseExampleGenerator(this);

		heuristicPredicateDetector = new PredicateDetector(this);
	}

	/**
	 * Load all the feature extractors
	 *
	 * @throws Exception
	 */
	private void initializeModelInfo() throws Exception {
		modelInfo = new ModelInfo(this);
	}

	protected HashMap<String, Integer> getLabelIdMap(String[] strings) {
		HashMap<String, Integer> label2Id = new HashMap<>();
		for (int i = 0; i < strings.length; i++) {
			label2Id.put(strings[i], i);
		}
		return label2Id;
	}

	private Map<String, Set<String>> getLegalSensesMap() {
		Map<String, Set<String>> map = new HashMap<>();
		try {
			for (String line : LineIO.read("data/sense-list.txt")) {
                String predicate = line.split("\t")[0];
				String[] senseArray = line.split("\t")[1].split(",");
				Set<String> senseSet = new HashSet<>(Arrays.asList(senseArray));
				map.put(predicate, senseSet);
            }
		} catch (FileNotFoundException e) {
			log.error("Unable to load list of legal senses: ", e);
			System.exit(-1);
		}
		return map;
	}

	private void initializeFeatureManifest(String defaultParser) {
		Feature.setUseAscii();
		Feature.setKeepString();

		FeatureManifest.setJWNLConfigFile(properties.getWordNetFile());

		FeatureManifest.setFeatureExtractor("hyphen-argument-feature", FeatureGenerators.hyphenTagFeature);

		// These three are from Surdeanu etal.
		FeatureManifest.setTransformer("parse-left-sibling", FeatureGenerators.getParseLeftSibling(defaultParser));
		FeatureManifest.setTransformer("parse-right-sibling", FeatureGenerators.getParseLeftSibling(defaultParser));
		FeatureManifest.setFeatureExtractor("pp-features", FeatureGenerators.ppFeatures(defaultParser));

		// Introduced in Toutanova etal.
		FeatureManifest.setFeatureExtractor("projected-path", new ProjectedPath(defaultParser));
	}

	public int getNumLabels() {
		return getNumSenses();
	}

	public int getSenseId(String label) {
		// XXX: special case for Propbank. Not clear what this label means, but
		// it occurs occasionally in the data and we don't really want to
		// predict this
		if (label.equals("XX"))
			return senseToId.get("01");
		else if (senseToId.containsKey(label))
			return senseToId.get(label);
		else
			throw new NullPointerException(label + " not a valid sense");
	}

	protected String[] getSenseLabels() {
		return allSenses;
	}

	public int getNumSenses() {
		return allSenses.length;
	}

	public String getSense(int id) {
		return allSenses[id];
	}

	public PredicateDetector getPredicateDetector() {
		return heuristicPredicateDetector;
	}

	public int getPruneSize() {
		return 4;
	}

	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	private String getFeatureIdentifier() {
		return getModelInfo().featureManifest.getIncludedFeatures().replaceAll("\\s+", "").hashCode() + "";
	}

	public static String getGoldViewName() {
		return getPredictedViewName() + "_GOLD";
	}

	public static String getPredictedViewName() {
		return Constants.viewName;
	}

	/**
	 * The name of the file that contains the lexicon
	 */
	public String getLexiconFileName() {
		return properties.getModelsDir() + "/" + defaultParser + ".lex";
	}

	/**
	 * The name of the file that contains the model for the given model type
	 */
	public String getModelFileName() {
		String identifier = getFeatureIdentifier();
		return properties.getModelsDir() + "/" + defaultParser + "." + identifier + ".lc";
	}

	/**
	 * Checks if the given sense id is a valid predicate sense for the given
	 * predicate. The sense id should be valid according to the function getSenseId.
	 */
	public boolean isValidSense(String predicate, int senseId) {
		assert 0 <= senseId && senseId < getNumSenses();
		return this.getLegalSenses(predicate).contains(getSense(senseId));
	}

	/**
	 * Checks if the input label is a valid label Id of the specified model type
	 * for the input x.
	 */
	public boolean isValidLabel(SenseInstance x, int label) {
		return this.getLegalSenses(x.getPredicateLemma()).contains(this.getSense(label));
	}

	/**
	 * Get the set of valid senses for this predicate using the frame files.
	 * For unknown predicates, only the sense 01 is allowed.
	 */
	public Set<String> getLegalSenses(String predicate) {
		if (legalSenses.containsKey(predicate)) {
			Set<String> senses = legalSenses.get(predicate);

			// keep only senses that the model knows about
			senses.retainAll(this.senseToId.keySet());

			if (senses.size() > 0)
				return senses;
			else {
				log.error("Unknown predicate {}. Allowing only sense 01", predicate);
			}
		}
		return new HashSet<>(Arrays.asList("01"));
	}

	/**
	 * Scores instance for the different labels allowed for it
	 */
	public double[] getScores(SenseInstance x, boolean rescoreInvalidLabels) {
		int numLabels = this.getNumLabels();
		double[] scores = new double[numLabels];

		WeightVector w;
		try {
			w = this.getModelInfo().getWeights();
			assert w != null;
		} catch (Exception e) {
			log.error("Unable to load weight vector, exception:\n{}", e);
			throw new RuntimeException(e);
		}

		for (int label = 0; label < numLabels; label++) {

			if (!this.isValidLabel(x, label) && rescoreInvalidLabels) {
				scores[label] = -50;
			}
			else {
				SenseStructure y = new SenseStructure(x, label, this);
				scores[label] = w.dotProduct(y.getFeatureVector());
			}
		}

		scores = MathUtilities.softmax(scores);

		return scores;

	}

	public ILPInference getInference(ILPSolverFactory solver, List<Constituent> predicates) throws Exception {
		return new ILPInference(solver, this, predicates);
	}

}
