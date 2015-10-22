package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.annotators.GazetteerViewGenerator;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.WordNetFeatureClass;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordLists;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Vivek Srikumar
 */
public class WordFeatureExtractorFactory {

	public static final WordFeatureExtractor gerundMarker = new WordFeatureExtractor() {

		private final Feature gerundMarker = DiscreteFeature.create("GerundMarker");

		public String getName() {
			return "#gerund";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			String word = getSafeToken(ta, wordPosition);

			boolean condition = word.endsWith("ing");
			return getConditionalFeature(condition, gerundMarker);
		}

	};
	public static final WordFeatureExtractor dateMarker = new WordFeatureExtractor() {

		private final Feature dateMarker = DiscreteFeature.create("Y");

		private boolean initialized = false;

		private List<DateFormat> sdfs;

		private synchronized void initialize() {
			if (initialized) return;

			Locale[] locales = {Locale.US, Locale.UK};

			sdfs = new ArrayList<>();
			for (Locale l : locales) {

				for (int format : new int[]{DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL}) {
					sdfs.add(SimpleDateFormat.getDateInstance(format, l));
				}

			}
			initialized = true;

		}

		public String getName() {
			return "#dt";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			if (!initialized) initialize();

			boolean condition = false;

			String word = getSafeToken(ta, wordPosition);

			// This need not be the best way of doing this, but for now...
			// Try the US and UK locales, look for short, medium and long
			// formats

			for (DateFormat sdf : sdfs) {
				try {
					sdf.parse(word);
					condition = true;
					break;
				} catch (Exception ex) {
					// Don't do anything
				}

			}

			return getConditionalFeature(condition, dateMarker);

		}
	};
	private static final Map<String, WordFeatureExtractor> gazetteerFeatureExtractors = new HashMap<>();
	/**
	 * Adds the following two features: One with the word in its actual case,
	 * and the second, an indicator for whether the word is captitalized
	 */
	public static WordFeatureExtractor capitalization = new WordFeatureExtractor() {

		public String getName() {
			return "#cap";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			Set<Feature> feats = new LinkedHashSet<>();

			String token = getSafeToken(ta, wordPosition);

			feats.add(DiscreteFeature.create("wordCap:" + token.trim()));

			feats.add(DiscreteFeature.create("capitalized:" + WordHelpers.isCapitalized(ta, wordPosition)));

			return feats;
		}

	};
	/**
	 * The coarse POS tag (one of Noun, Verb, Adjective, Adverb, Punctuation,
	 * Pronoun and Other)
	 */
	public static WordFeatureExtractor conflatedPOS = new WordFeatureExtractor() {

		public String getName() {
			return "#confl-p";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {

			String pos = getSafePOS(ta, wordPosition);

			Set<Feature> feats = new LinkedHashSet<>();
			feats.add(DiscreteFeature.create(POSUtils.getCoarsePOS(pos)));
			return feats;
		}

	};
	public static SuffixFeatureExtractor deAdjectivalAbstractNounsSuffixes =
			new SuffixFeatureExtractor(WordLists.DE_ADJ_SUFFIXES, "de-adj", true);
	public static SuffixFeatureExtractor deNominalNounProducingSuffixes =
			new SuffixFeatureExtractor(WordLists.DENOM_SUFFIXES, "de-nom", true);
	public static SuffixFeatureExtractor deVerbalSuffix =
			new SuffixFeatureExtractor(WordLists.DE_VERB_SUFFIXES, "de-verb", true);
	public static PrefixFeatureExtractor knownPrefixes =
			new PrefixFeatureExtractor(WordLists.PREFIXES, "prefixes", true);
	public static WordFeatureExtractor lemma = new WordFeatureExtractor() {

		public String getName() {
			return "#lmm";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			Set<Feature> feats = new LinkedHashSet<>();

			String lemma = getSafeLemma(ta, wordPosition);

			feats.add(DiscreteFeature.create(lemma));

			return feats;
		}
	};
	public static WordFeatureExtractor nominalizationMarker = new WordFeatureExtractor() {

		private final Feature isNom = DiscreteFeature.create("Y");

		public String getName() {
			return "#nom?";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {

			Set<Feature> features = new LinkedHashSet<>();

			List<String> nomFrames;
			try {
				URL file = IOUtils.lsResources(WordFeatureExtractorFactory.class, "nombank.list.gz").get(0);
				nomFrames = LineIO.readGZip(file.getFile());

				String lemma = WordHelpers.getLemma(ta, wordPosition);

				if (nomFrames.contains(lemma)) {
					features.add(isNom);
				}
			} catch (Exception e) {
				System.err.println("Could not read nombank.list.gz file from classpath");
				e.printStackTrace();
			}
			return features;
		}
	};
	public static WordFeatureExtractor numberNormalizer = new WordFeatureExtractor() {

		private final DiscreteFeature NUMBER_IDENTIFIER = DiscreteFeature.create("Y");

		private final String numberPattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";

		public String getName() {
			return "#@";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			String pos = getSafePOS(ta, wordPosition);

			Set<Feature> features = new LinkedHashSet<>();
			if (POSUtils.isPOSNumber(pos)) features.add(NUMBER_IDENTIFIER);
			else if (WordLists.NUMBERS.contains(ta.getToken(wordPosition).toLowerCase()))
				features.add(NUMBER_IDENTIFIER);
			else if (ta.getToken(wordPosition).matches(numberPattern)) features.add(NUMBER_IDENTIFIER);

			return features;
		}
	};
	public static WordFeatureExtractor pos = new WordFeatureExtractor() {

		public String getName() {
			return "#pos";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			Set<Feature> feats = new LinkedHashSet<>();

			String pos = getSafePOS(ta, wordPosition);
			feats.add(DiscreteFeature.create(pos));

			return feats;
		}
	};
	public static WordFeatureExtractor prefixSuffixes = new WordFeatureExtractor() {

		public String getName() {
			return "#pr-sf";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {

			Set<Feature> feats = new LinkedHashSet<>();

			if (wordPosition >= 0 && wordPosition < ta.size()) {
				String word = getSafeToken(ta, wordPosition).toLowerCase();
				feats.add(DiscreteFeature.create("first2:" + getSafeSubstring(word, 0, 2)));
				feats.add(DiscreteFeature.create("first3:" + getSafeSubstring(word, 0, 3)));
				feats.add(DiscreteFeature.create("last2:" + getSafeSubstring(word, word.length() - 2, word.length())));
				feats.add(DiscreteFeature.create("last3:" + getSafeSubstring(word, word.length() - 3, word.length())));
			}

			return feats;
		}
	};
	public static WordFeatureExtractor wordCase = new WordFeatureExtractor() {

		public String getName() {
			return "#wordC";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			Set<Feature> feats = new LinkedHashSet<>();

			String token = getSafeToken(ta, wordPosition);

			feats.add(DiscreteFeature.create(token.trim()));

			return feats;
		}
	};
	public static WordFeatureExtractor word = new WordFeatureExtractor() {

		public String getName() {
			return "#wd";
		}

		@Override
		public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
			Set<Feature> feats = new LinkedHashSet<>();

			String token = getSafeToken(ta, wordPosition);

			feats.add(DiscreteFeature.create(token.toLowerCase().trim()));

			return feats;
		}
	};
	private static BrownClusterFeatureExtractor brownFeatureGenerator;
	private static WordNetFeatureExtractor wnFeatureGenerator;

	public static WordFeatureExtractor getBrownFeatureGenerator(String name, String brownClustersFileName, int[] brownClusterLengths) throws
			EdisonException {

		if (brownFeatureGenerator == null) {
			synchronized (WordFeatureExtractorFactory.class) {
				if (brownFeatureGenerator == null) {
					try {
						brownFeatureGenerator =
								new BrownClusterFeatureExtractor(name, brownClustersFileName, brownClusterLengths);
					} catch (EdisonException e) {
						throw new EdisonException("Error creating brown features", e);
					}
				}
			}
		}

		return brownFeatureGenerator;
	}

	private static Set<Feature> getConditionalFeature(boolean condition, Feature... featureName) {
		if (condition) return new LinkedHashSet<>(Arrays.asList(featureName));
		else return new LinkedHashSet<>();
	}

	public static WordFeatureExtractor getGazetteerFeatureExtractor(final String name, final GazetteerViewGenerator gazetteerViewGenerator) {

		if (!gazetteerFeatureExtractors.containsKey(name)) {

			synchronized (gazetteerFeatureExtractors) {
				if (!gazetteerFeatureExtractors.containsKey(name)) {
					WordFeatureExtractor feats = new WordFeatureExtractor() {

						@Override
						public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws
								EdisonException {
							String viewName = gazetteerViewGenerator.getViewName();
							if (!ta.hasView(viewName)) try {
								ta.addView(gazetteerViewGenerator);
							} catch (AnnotatorException e) {
								throw new EdisonException(e);
							}

							Set<Feature> feats = new LinkedHashSet<>();
							for (String label : ta.getView(viewName).getLabelsCoveringToken(wordPosition))
								feats.add(DiscreteFeature.create(label));
							return feats;
						}

						@Override
						public String getName() {
							return "gz";
						}
					};
					gazetteerFeatureExtractors.put(name, feats);
				}
			}
		}

		return gazetteerFeatureExtractors.get(name);
	}

	private static String getSafeLemma(TextAnnotation ta, int wordPosition) {
		String lemma = WordHelpers.getLemma(ta, wordPosition);

		if (lemma.length() == 0) lemma = "*";
		return lemma;
	}

	private static String getSafePOS(TextAnnotation ta, int wordPosition) {
		String pos = WordHelpers.getPOS(ta, wordPosition);

		if (pos.length() == 0) pos = "*";
		return pos;
	}

	private static String getSafeSubstring(String word, int start, int end) {

		StringBuffer sb = new StringBuffer();
		for (int i = start; i < 0; i++) {
			sb.append("*");
		}

		String begin = sb.toString();

		sb = new StringBuffer();
		for (int i = word.length(); i < end; i++)
			sb.append("*");

		String last = sb.toString();

		String s = word.substring(Math.max(0, start), Math.min(word.length(), end));

		return begin + s + last;

	}

	private static String getSafeToken(TextAnnotation ta, int wordPosition) {
		String token = "*";
		if (wordPosition >= 0 && wordPosition < ta.size()) token = ta.getToken(wordPosition);
		return token;
	}

	public static WordNetFeatureExtractor getWordNetFeatureExtractor(String jwnlConfigFile, WordNetFeatureClass... wordNetFeatureClasses) throws
			EdisonException {
		if (wnFeatureGenerator == null) {
			synchronized (WordFeatureExtractorFactory.class) {
				if (wnFeatureGenerator == null) {
					try {
						wnFeatureGenerator = new WordNetFeatureExtractor(jwnlConfigFile);

						for (WordNetFeatureClass c : wordNetFeatureClasses)
							wnFeatureGenerator.addFeatureType(c);

					} catch (Exception e) {
						throw new EdisonException(
								"Error creating word feature extractor , properties = " + jwnlConfigFile, e);
					}
				}
			}
		}
		return wnFeatureGenerator;
	}
}
