package edu.illinois.cs.cogcomp.verbsense.experiment;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.HeadFinderDependencyViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.PseudoParse;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPreprocessor;
import edu.illinois.cs.cogcomp.verbsense.Properties;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextPreProcessor {
	private final static Logger log = LoggerFactory.getLogger(TextPreProcessor.class);

	private static TextPreProcessor instance;
	private static final boolean forceUpdate = false;
	private final boolean useCurator;
	private final boolean tokenized;
	private final CuratorClient curator;
	private final IllinoisPreprocessor illinoisPreprocessor;
	private String defaultParser;

	public TextPreProcessor(boolean tokenized) throws Exception {
		Properties config = Properties.getInstance();
		defaultParser = config.getDefaultParser();
		this.useCurator = config.useCurator();
		this.tokenized = tokenized;

		if (useCurator) {
			curator = new CuratorClient(config.getCuratorHost(), config.getCuratorPort(), tokenized);
			illinoisPreprocessor = null;
		}
		else {
			if (!defaultParser.equals("Stanford")) {
				log.error("Illinois Pipeline works only with the Stanford parser.\n" +
						"Please change the 'DefaultParser' parameter in the configuration file.");
				System.exit(-1);
			}
			ResourceManager rm = new ResourceManager(config.getPipelineConfigFile());
			illinoisPreprocessor = new IllinoisPreprocessor(rm);
			curator = null;
		}
	}

	public static void initialize(boolean tokenized) {
		try {
			instance = new TextPreProcessor(tokenized);
		} catch (Exception e) {
			log.error("Unable to initialize the text pre-processor");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static TextPreProcessor getInstance() {
		if (instance == null) {
			// Start a new TextPreProcessor with default values (no Curator, no tokenization)
			try {
				instance = new TextPreProcessor(false);
			} catch (Exception e) {
				log.error("Unable to initialize the text pre-processor");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		return instance;
	}

	public TextAnnotation preProcessText(String text) throws Exception {
		TextAnnotation ta;
		if (useCurator) {
			ta = curator.getTextAnnotation("", "", text, false);
			addViewsFromCurator(ta, curator);
		}
		else {
			ta = illinoisPreprocessor.processTextToTextAnnotation("", "", text, false);
			addAdditionalViewsFromPipeline(ta);
		}
		return ta;
	}

	public void preProcessText(TextAnnotation ta) throws Exception {
		if (useCurator)
			addViewsFromCurator(ta, curator);
		else {
			illinoisPreprocessor.processTextAnnotation(ta, tokenized);
			addAdditionalViewsFromPipeline(ta);
		}
	}

	/**
	 * Adds required views (annotations) from Curator. Used both when training and testing.
	 *
	 * @param ta The {@link TextAnnotation} where the views will be added
	 * @param curator The Curator client providing the views
	 * @throws Exception
	 */
	private void addViewsFromCurator(TextAnnotation ta, CuratorClient curator) throws Exception {
		if (!ta.hasView(ViewNames.NER))
			curator.addNamedEntityView(ta, forceUpdate);
		if (!ta.hasView(ViewNames.SHALLOW_PARSE))
			curator.addChunkView(ta, forceUpdate);
		if (!ta.hasView(ViewNames.LEMMA))
			curator.addLemmaView(ta, forceUpdate);
		if (!ta.hasView(ViewNames.POS))
			curator.addPOSView(ta, forceUpdate);

		if (defaultParser.equals("Charniak") && !ta.hasView(ViewNames.PARSE_CHARNIAK)) {
			curator.addCharniakParse(ta, forceUpdate);
			ta.addView(ClauseViewGenerator.CHARNIAK);
			ta.addView(PseudoParse.CHARNIAK);
			ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_CHARNIAK));
		} else if (defaultParser.equals("Berkeley") && !ta.hasView(ViewNames.PARSE_BERKELEY)) {
			curator.addBerkeleyParse(ta, forceUpdate);
			ta.addView(ClauseViewGenerator.BERKELEY);
			ta.addView(PseudoParse.BERKELEY);
			ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_BERKELEY));
		} else if (defaultParser.equals("Stanford") && !ta.hasView(ViewNames.PARSE_STANFORD)) {
			curator.addStanfordParse(ta, forceUpdate);
			ta.addView(ClauseViewGenerator.STANFORD);
			ta.addView(PseudoParse.STANFORD);
			ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_STANFORD));
		}
	}

	private void addAdditionalViewsFromPipeline(TextAnnotation ta) throws TException, AnnotationFailedException {
		switch (defaultParser) {
			case "Charniak":
				if (!ta.hasView(ViewNames.CLAUSES_CHARNIAK))
					ta.addView(ClauseViewGenerator.CHARNIAK);
				if (!ta.hasView(ViewNames.DEPENDENCY + ":" + ViewNames.PARSE_CHARNIAK))
					ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_CHARNIAK));
				break;
			case "Berkeley":
				if (!ta.hasView(ViewNames.CLAUSES_BERKELEY))
					ta.addView(ClauseViewGenerator.BERKELEY);
				if (!ta.hasView(ViewNames.DEPENDENCY + ":" + ViewNames.PARSE_BERKELEY))
					ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_BERKELEY));
				break;
			case "Stanford":
				if (!ta.hasView(ViewNames.CLAUSES_STANFORD))
					ta.addView(ClauseViewGenerator.STANFORD);
				if (!ta.hasView(ViewNames.DEPENDENCY + ":" + ViewNames.PARSE_STANFORD))
					ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_STANFORD));
				break;
		}
	}
}
