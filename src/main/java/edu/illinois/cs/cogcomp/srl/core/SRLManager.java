package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.math.MathUtilities;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.manifest.FeatureManifest;
import edu.illinois.cs.cogcomp.edison.utilities.NomLexReader;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.Constants;
import edu.illinois.cs.cogcomp.srl.SRLProperties;
import edu.illinois.cs.cogcomp.srl.data.FrameData;
import edu.illinois.cs.cogcomp.srl.data.FramesManager;
import edu.illinois.cs.cogcomp.srl.data.LegalArguments;
import edu.illinois.cs.cogcomp.srl.features.FeatureGenerators;
import edu.illinois.cs.cogcomp.srl.features.ProjectedPath;
import edu.illinois.cs.cogcomp.srl.inference.SRLConstraints;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPInference;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Manages the lexicon, models and feature extractors for a verb or nom SRL.
 *
 * @author Vivek Srikumar
 *
 */
public abstract class SRLManager {

	private final static Logger log = LoggerFactory.getLogger(SRLManager.class);

	/**
	 * Indicates the 'null' label
	 */
	public final static String NULL_LABEL = "<null>";

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

	private final HashMap<String, Integer> argToId, senseToId;

	private final Set<String> allArgumentsSet;

	private final LegalArguments knownLegalArguments;
	private final Map<String, Set<String>> legalArgumentsCache;

	private final SRLProperties properties = SRLProperties.getInstance();

	private ArgumentIdentifier identifier;

	private final Set<SRLConstraints> constraints = new HashSet<>();

	private final Map<Models, ModelInfo> modelInfo;

	public final SRLExampleGenerator exampleGenerator;

	protected SRLManager(boolean trainingMode, String defaultParser) throws Exception {
		this.trainingMode = trainingMode;
		this.defaultParser = defaultParser;

		initializeFeatureManifest(defaultParser);

		allArgumentsSet = Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(getArgumentLabels())));

		senseToId = getLabelIdMap(getSenseLabels());
		argToId = getLabelIdMap(getArgumentLabels());
		this.knownLegalArguments = new LegalArguments(getSRLType() + ".legal.arguments");

		this.legalArgumentsCache = new ConcurrentHashMap<>();

		log.info("{} Arguments: " + Sorters.sortSet(argToId.keySet()), argToId.size());
		log.info("{} senses: " + Sorters.sortSet(senseToId.keySet()), senseToId.size());

		modelInfo = new HashMap<>();

		// Load WN properties file from the classpath
		WordNetManager.loadConfigAsClasspathResource(true);
		// For nomSRL define the location of NOMLEX
		NomLexReader.nomLexFile = properties.getNombankHome() + "NOMLEX-plus-clean.1.0";

		initializeModelInfo();

		initializeConstraints();

		exampleGenerator = new SRLExampleGenerator(this);
	}

	/**
	 * Load all the feature extractors
	 *
	 * @throws Exception
	 */
	private void initializeModelInfo() throws Exception {
		for (Models m : Models.values()) {
			ModelInfo info = new ModelInfo(this, m);
			modelInfo.put(m, info);
		}
	}

	/**
	 * Load all the constraints.
	 *
	 * @throws Exception
	 */
	private void initializeConstraints() throws Exception {
		String file = "constraints/" + getSRLType() + ".constraints";
		log.info("Adding all constraints specified in {}", file);
		InputStream in;
		if (IOUtils.exists(file)) {
			in = new FileInputStream(new File(file));
		} else {
			List<URL> ls = IOUtils.lsResources(SRLManager.class, file);
			in = ls.get(0).openStream();
		}
		Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			int semiColon = line.indexOf(';');
			if (semiColon >= 0)
				line = line.substring(0, semiColon);

			line = line.trim();
			if (line.length() == 0)
				continue;
			try {
				SRLConstraints constraint = SRLConstraints.valueOf(line);
				log.info("Including constraint {}", constraint);

				this.addConstraint(constraint);
			} catch (Exception e) {
				log.error("Error with constraint {}", line);
				throw e;
			}
		}

		scanner.close();
		in.close();
	}

	protected HashMap<String, Integer> getLabelIdMap(String[] strings) {

		HashMap<String, Integer> label2Id = new HashMap<>();
		for (int i = 0; i < strings.length; i++) {
			label2Id.put(strings[i], i);
		}

		return label2Id;
	}

	private void initializeFeatureManifest(String defaultParser) {
		Feature.setUseAscii();
		Feature.setKeepString();

		FeatureManifest.setFeatureExtractor("hyphen-argument-feature", FeatureGenerators.hyphenTagFeature);

		// These three are from Surdeanu etal.
		FeatureManifest.setTransformer("parse-left-sibling", FeatureGenerators.getParseLeftSibling(defaultParser));
		FeatureManifest.setTransformer("parse-right-sibling", FeatureGenerators.getParseLeftSibling(defaultParser));
		FeatureManifest.setFeatureExtractor("pp-features", FeatureGenerators.ppFeatures(defaultParser));

		// Introduced in Toutanova etal.
		FeatureManifest.setFeatureExtractor("projected-path", new ProjectedPath(defaultParser));
	}

	public Set<String> getAllArguments() {
		return allArgumentsSet;
	}

	public void addConstraint(SRLConstraints c) {
		this.constraints.add(c);
	}

	public Set<SRLConstraints> getConstraints() {
		return Collections.unmodifiableSet(this.constraints);
	}

	public int getNumLabels(Models type) {
		if (type == Models.Identifier || type == Models.Predicate)
			return 2;
		else if (type == Models.Classifier)
			return getNumArguments();
		else
			return getNumSenses();
	}

	public int getArgumentId(String label) {
		// XXX: Not sure why I'm doing this. Some misguided sense of continuity
		// to the old data format, I think. I'm sure I will pay hell for this
		// sometime in the future with hours of debugging!
		label = label.replace("Support", "SUP");

		if (argToId.containsKey(label))
			return argToId.get(label);
		else {
			log.debug(label + " is not a valid argument. Expecting one of "
					+ this.argToId.keySet() + ", replacing with " + NULL_LABEL);
			return argToId.get(NULL_LABEL);
		}
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

	public abstract SRLType getSRLType();

	protected abstract String[] getArgumentLabels();

	protected abstract String[] getSenseLabels();

	public abstract Set<String> getCoreArguments();

	public abstract Set<String> getModifierArguments();

	protected abstract int getNumArguments();

	protected abstract int getNumSenses();

	public abstract String getArgument(int id);

	public abstract String getSense(int id);

	public abstract ArgumentCandidateGenerator getArgumentCandidateGenerator();

	public abstract FramesManager getFrameManager();

	public abstract AbstractPredicateDetector getHeuristicPredicateDetector();

	public abstract AbstractPredicateDetector getLearnedPredicateDetector();

	public abstract int getPruneSize(Models model);

	public ModelInfo getModelInfo(Models model) {
		return modelInfo.get(model);
	}

	public ArgumentIdentifier getArgumentIdentifier() {
		if (this.identifier == null) {
			synchronized (this) {
				if (this.identifier == null) {
					log.info("Loading argument identifier");
					try {
						InputStream in;
						if (!IOUtils.exists(getIdentifierScaleFile())) {
							log.debug("Looking for {} in the classpath", getIdentifierScaleFile());
							List<URL> urls = IOUtils.lsResources(SRLManager.class, getIdentifierScaleFile());
							if (urls.size() == 0)
								log.error("Argument identifier scale file not found!");
							URL url = urls.get(0);

							in = url.openStream();
						} else {
							log.debug("Looking for {} in the file system", getIdentifierScaleFile());
							in = new FileInputStream(new File(getIdentifierScaleFile()));
						}
						Pair<Double, Double> pair = readIdentifierScale(in);
						this.identifier = new ArgumentIdentifier(pair.getFirst(), pair.getSecond(), this);

						log.info("Finished initializing argument identifier");
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return this.identifier;
	}

	public void writeIdentifierScale(double A, double B) throws IOException {

		String file = getIdentifierScaleFile();

		log.info("Writing identifier scaling info to {}", file);

		BufferedOutputStream stream = new BufferedOutputStream(
				new GZIPOutputStream(new FileOutputStream(file)));

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

		writer.write("IdentifierScale");
		writer.newLine();
		writer.write("" + A);
		writer.newLine();
		writer.write("" + B);

		writer.close();
	}

	private Pair<Double, Double> readIdentifierScale(InputStream in) throws IOException {
		GZIPInputStream zipin = new GZIPInputStream(in);
		BufferedReader reader = new BufferedReader(new InputStreamReader(zipin));

		String line;

		line = reader.readLine().trim();
		if (!line.equals("IdentifierScale")) {
			throw new IOException("Invalid identifier scalefile");
		}

		double A = Double.parseDouble(reader.readLine().trim());
		double B = Double.parseDouble(reader.readLine().trim());

		log.info("Argument identifier scaler (A,B) = ({},{})", A, B);

		zipin.close();

		return new Pair<>(A, B);
	}

	private String getFeatureIdentifier(Models type) {
		return getModelInfo(type).featureManifest.getIncludedFeatures()
				.replaceAll("\\s+", "").hashCode() + "";
	}

	public String getGoldViewName() {
		return getPredictedViewName() + "_GOLD";
	}

	public abstract String getPredictedViewName();

	public String getSRLSystemIdentifier() {
		if (this.getSRLType() == SRLType.Verb)
			return Constants.verbSRLSystemIdentifier;
		else
			return Constants.nomSRLSystemIdentifier;
	}

	/**
	 * The name of the file that contains the identifer scale information
	 */
	public String getIdentifierScaleFile() {
		String identifier = getFeatureIdentifier(Models.Identifier);
		return properties.getModelsDir() + "/" + this.getSRLType() + "." + Models.Identifier.name()
				+ "." + defaultParser + "." + identifier + ".scale";
	}

	/**
	 * The name of the file that contains the lexicon
	 */
	public String getLexiconFileName(Models m) {
		return properties.getModelsDir() + "/" + getSRLType() + "." + m + "." + defaultParser + ".lex";
	}

	/**
	 * The name of the file that contains the model for the given model type
	 */
	public String getModelFileName(Models type) {
		String identifier = getFeatureIdentifier(type);
		return properties.getModelsDir() + "/" + this.getSRLType() + "." + type.name() + "."
				+ defaultParser + "." + identifier + ".lc";
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
	public boolean isValidLabel(SRLMulticlassInstance x, Models type, int label) {
		if (type == Models.Identifier || type == Models.Predicate)
			return label == 0 || label == 1;
		else if (type == Models.Classifier)
			return this.getLegalArguments(x.getPredicateLemma()).contains(this.getArgument(label));
		else
			return this.getLegalSenses(x.getPredicateLemma()).contains(this.getSense(label));
	}

	/**
	 * Checks if the argument id specified is the special NULL_LABEL.
	 */
	public boolean isNullLabel(int argLabelId) {
		return NULL_LABEL.equals(this.getArgument(argLabelId));
	}

	/**
	 * Returns the set of legal arguments for the given lemma. This function
	 * uses the frame files to get the list of valid core arguments. All the
	 * modifiers are treated as legal arguments. In addition, all C-args and
	 * R-args of legal core/modifier arguments are also considered legal. For
	 * unknown predicates, all arguments are legal.
	 */
	public Set<String> getLegalArguments(String lemma) {
		FramesManager frameMan = getFrameManager();

		if (knownLegalArguments.hasLegalArguments(lemma)) {

			Set<String> set = new HashSet<>();

			set.addAll(Arrays.asList("AM-ADV", "AM-DIS", "AM-LOC", "AM-MNR", "AM-MOD", "AM-NEG", "AM-TMP"));

			set.addAll(knownLegalArguments.getLegalArguments(lemma));

			if (lemma.equals("%"))
				lemma = "perc-sign";

			if (lemma.equals("namedrop"))
				lemma = "name-drop";

			if (frameMan.frameData.containsKey(lemma))
				set.addAll(frameMan.getFrame(lemma).getLegalArguments());
			else
				log.warn("Unseen lemma {}", lemma);

			return set;
		}
		else {

			Set<String> knownPredicates = frameMan.getPredicates();
			if (knownPredicates.contains(lemma)) {

				if (legalArgumentsCache.containsKey(lemma))
					return legalArgumentsCache.get(lemma);
				else {

					HashSet<String> set = new HashSet<>(frameMan
                            .getFrame(lemma).getLegalArguments());

					set.addAll(this.getModifierArguments());

					for (String s : new ArrayList<>(set)) {
						set.add("C-" + s);
						set.add("R-" + s);
					}

					set.add(NULL_LABEL);

					legalArgumentsCache.put(lemma, set);

					return set;
				}
			} else
				return getAllArguments();
		}
	}

	/**
	 * Get the set of valid senses for this predicate using the frame files.
	 * For unknown predicates, only the sense 01 is allowed.
	 */
	public Set<String> getLegalSenses(String predicate) {
		FramesManager frameMan = getFrameManager();

		if (frameMan.getPredicates().contains(predicate)) {
			Set<String> senses = frameMan.getFrame(predicate).getSenses();

			// keep only senses that the model knows about
			senses.retainAll(this.senseToId.keySet());

			if (senses.size() > 0)
				return senses;
			else {
				log.error("Unknown predicate {}. Allowing only sense 01", predicate);
			}
		}
		return new HashSet<>(Collections.singletonList("01"));

	}

	/**
	 * Get valid arguments for each sense of a given lemma from the frame files.
	 * For unknown predicates, only the sense 01 is allowed with all arguments
	 */
	public Map<String, Set<String>> getLegalLabelsForSense(String lemma) {
		FramesManager frameMan = getFrameManager();

		Map<String, Set<String>> map = new HashMap<>();

		if (frameMan.getPredicates().contains(lemma)) {
			FrameData frame = frameMan.getFrame(lemma);
			for (String sense : frame.getSenses()) {
				Set<String> argsForSense = new HashSet<>(frame.getArgsForSense(sense));
				argsForSense.add(NULL_LABEL);

				map.put(sense, argsForSense);
			}
		} else {
			map.put("01", getAllArguments());
		}

		return map;
	}

	/**
	 * Scores instance for the different labels allowed for it
	 */
	public double[] getScores(SRLMulticlassInstance x, Models type, boolean rescoreInvalidLabels) {
		int numLabels = this.getNumLabels(type);
		double[] scores = new double[numLabels];

		WeightVector w;
		try {
			w = this.getModelInfo(type).getWeights();
			assert w != null;
		} catch (Exception e) {
			log.error("Unable to load weight vector for {}", type, e);
			throw new RuntimeException(e);
		}

		for (int label = 0; label < numLabels; label++) {

			if (!this.isValidLabel(x, type, label) && rescoreInvalidLabels) {
				scores[label] = -50;
			}
			else {
                scores[label] = w.dotProduct(x.getCachedFeatureVector(type),label * this.getModelInfo(type).getLexicon().size());
			}
		}

		scores = MathUtilities.softmax(scores);

		return scores;

	}

	public SRLILPInference getInference(ILPSolverFactory solver, List<Constituent> predicates) throws Exception {
		return new SRLILPInference(solver, this, predicates);
	}

}
