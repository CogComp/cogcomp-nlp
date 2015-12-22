package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory.SolverType;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.srl.inference.ISRLInference;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPInference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SemanticRoleLabeler extends Annotator {
	private final static Logger log = LoggerFactory.getLogger(SemanticRoleLabeler.class);
	public final SRLManager manager;
	private static SRLProperties properties;

	public static void main(String[] arguments) {
		if (arguments.length < 1) {
			System.err.println("Usage: <config-file> [Verb | Nom]");
			System.exit(-1);
		}
		String configFile = arguments[0];
        ResourceManager rm = null;
        try {
            rm = new SrlConfigurator().getConfig( new ResourceManager( configFile ) );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit( -1 );
        }
        String srlType;
		// If no second argument is provided it means we need all the SRL types
		srlType = arguments.length == 1 ? null : arguments[1];

		String input;
		List<SemanticRoleLabeler> srlLabelers = new ArrayList<>();
		try {
			if (srlType != null)
				srlLabelers.add(new SemanticRoleLabeler(rm, srlType, true));
			else {
				for (SRLType type : SRLType.values()) {
					srlType = type.name();
					srlLabelers
							.add(new SemanticRoleLabeler(rm, srlType, true));
				}
			}
		} catch (Exception e) {
			log.error("Unable to initialize SemanticRoleLabeler:");
			e.printStackTrace();
			System.exit(-1);
		}

		do {
			System.out.print("Enter text (underscore to quit): ");
			input = System.console().readLine().trim();
			if (input.equals("_"))
				return;

			if (!input.isEmpty()) {
				// XXX Assuming that all SRL types require the same views
				TextAnnotation ta;
				try {
					ta = TextPreProcessor.getInstance().preProcessText(input);
				} catch (Exception e) {
					log.error("Unable to pre-process the text:");
					e.printStackTrace();
					continue;
				}

				for (SemanticRoleLabeler srl : srlLabelers) {
					System.out.println(srl.getSRLCuratorName());

					PredicateArgumentView p;
					try {
						p = srl.getSRL(ta);
					} catch (Exception e) {
						log.error("Unable to produce SRL annotation:");
						e.printStackTrace();
						continue;
					}

					System.out.println(p);
					System.out.println();
				}
			}
		} while (!input.equals("_"));
	}

	public SemanticRoleLabeler(String srlType) throws Exception {
		this( new SrlConfigurator().getDefaultConfig(), srlType );
	}

	public SemanticRoleLabeler(ResourceManager rm, String srlType) throws Exception {

		this(rm, srlType, false);
	}

	public SemanticRoleLabeler(ResourceManager rm, String srlType, boolean initialize) throws Exception {
		super( getViewNameForType(srlType), TextPreProcessor.requiredViews );

		WordNetManager.loadConfigAsClasspathResource(true);

		log.info("Initializing config");
		SRLProperties.initialize(rm);
		properties = SRLProperties.getInstance();

		if(initialize) {
			log.info("Initializing pre-processor");
			TextPreProcessor.initialize(properties);
		}
		log.info("Creating {} manager", srlType);

		manager = Main.getManager(SRLType.valueOf(srlType), false);

		log.info("Loading models");
		loadModels();
	}

	private static String getViewNameForType(String srlType) {
		if ( srlType.equals( SRLType.Verb.name() ) )
			return ViewNames.SRL_VERB;
		else if ( srlType.equals( SRLType.Nom.name() ) )
			return ViewNames.SRL_NOM;
		else
			throw new IllegalArgumentException( "ERROR: type '" + srlType + "' not recognized." );
	}

	public String getSRLCuratorName() {
		return manager.getSRLSystemIdentifier();
	}

	public String getVersion() {
		return properties.getSRLVersion();
	}

    private void loadModels() throws Exception {
		for (Models m : Models.values()) {
			if (manager.getSRLType() == SRLType.Verb && m == Models.Predicate)
				continue;

			log.info("Loading model {}", m);
			manager.getModelInfo(m).loadWeightVector();
		}

		log.info("Finished loading all models");
	}

	public PredicateArgumentView getSRL(TextAnnotation ta) throws Exception {
		log.debug("Input: {}", ta.getText());

		List<Constituent> predicates;
		if (manager.getSRLType() == SRLType.Verb)
			predicates = manager.getHeuristicPredicateDetector().getPredicates(ta);
		else
			predicates = manager.getLearnedPredicateDetector().getPredicates(ta);

		if (predicates.isEmpty())
			return null;
		ILPSolverFactory s = new ILPSolverFactory(SolverType.Gurobi);
		ISRLInference inference = new SRLILPInference(s, manager, predicates);

		return inference.getOutputView();
	}


	@Override
	public void addView(TextAnnotation ta) throws AnnotatorException {
		try {
            View srlView = getSRL(ta);
            ta.addView( getViewName(), srlView);
			return ;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotatorException(e.getMessage());
		}
	}

	@Override
	public String getViewName() {
		if (manager.getSRLType() == SRLType.Verb) {
			return ViewNames.SRL_VERB;
		} else if (manager.getSRLType() == SRLType.Nom)
			return ViewNames.SRL_NOM;
		return null;
	}

}
