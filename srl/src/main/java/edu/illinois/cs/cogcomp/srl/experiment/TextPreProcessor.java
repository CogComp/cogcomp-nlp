/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.experiment;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.HeadFinderDependencyViewGenerator;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;
import edu.illinois.cs.cogcomp.srl.SRLProperties;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextPreProcessor {
	private final static Logger log = LoggerFactory.getLogger(TextPreProcessor.class);
    private static final String NAME = TextPreProcessor.class.getCanonicalName();

	private static TextPreProcessor instance;
    private final AnnotatorService annotator;
	public final static String[] requiredViews = { ViewNames.POS,
			ViewNames.NER_CONLL,ViewNames.LEMMA, ViewNames.SHALLOW_PARSE,
			ViewNames.PARSE_STANFORD };

	/**
	* requires SRLProperties to have been instantiated already
	 */
    public TextPreProcessor( SRLProperties config ) throws Exception {
        String defaultParser = config.getDefaultParser();
        boolean useCurator = config.useCurator();

        if (useCurator) {
			log.info("Using curator");
			annotator = CuratorFactory.buildCuratorClient();
		} else {
			log.info("Using pipeline");
			if (!defaultParser.equals("Stanford")) {
				log.error("Illinois Pipeline works only with the Stanford parser.\n"
						+ "Please change the 'DefaultParser' parameter in the configuration file.");
				System.exit(-1);
			}
			annotator = IllinoisPipelineFactory.buildPipeline(new ResourceManager(config.getPipelineConfig()));
		}
	}

    public static void initialize()
    {
        SRLProperties props = SRLProperties.getInstance();
        initialize(props);
    }

	public static void initialize(ResourceManager rm) {
        SRLProperties props = SRLProperties.getInstance(rm);

        initialize(props);
    }

    public static void initialize( SRLProperties props )
    {
        log.info("## initializing {}.", NAME );
		try {
			instance = new TextPreProcessor( props );
		} catch (Exception e) {
			log.error("Unable to initialize the text pre-processor");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static TextPreProcessor getInstance() {
		if (instance == null) {
			// Start a new TextPreProcessor with default values (no Curator, no
			// tokenization) and default config
			initialize( new SrlConfigurator().getDefaultConfig() );
		}
		return instance;
	}

	public TextAnnotation preProcessText(String text) throws Exception {
		TextAnnotation ta;
		ta = annotator.createBasicTextAnnotation("", "", text);
		addViews(ta);
		return ta;
	}

	public void preProcessText(TextAnnotation ta) throws Exception {
		addViews(ta);
	}

	private void addViews(TextAnnotation ta) throws AnnotatorException {
		for (String view : requiredViews) {
			if (!ta.hasView(view))
				annotator.addView(ta, view);
		}
		if (!ta.hasView(ViewNames.CLAUSES_STANFORD))
			ta.addView(ClauseViewGenerator.STANFORD);
		if (!ta.hasView(ViewNames.DEPENDENCY + ":" + ViewNames.PARSE_STANFORD))
			ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_STANFORD));

	}
}
