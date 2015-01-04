package edu.illinois.cs.cogcomp.verbsense.experiment;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPreprocessor;
import edu.illinois.cs.cogcomp.verbsense.Properties;
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

	public TextPreProcessor(boolean tokenized) throws Exception {
		Properties config = Properties.getInstance();
		this.useCurator = config.useCurator();
		this.tokenized = tokenized;

		if (useCurator) {
			curator = new CuratorClient(config.getCuratorHost(), config.getCuratorPort(), tokenized);
			illinoisPreprocessor = null;
		}
		else {
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
		}
		return ta;
	}

	public void preProcessText(TextAnnotation ta) throws Exception {
		if (useCurator)
			addViewsFromCurator(ta, curator);
		else {
			illinoisPreprocessor.processTextAnnotation(ta, tokenized);
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
	}
}
