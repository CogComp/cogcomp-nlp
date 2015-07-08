package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.edison.data.curator.CuratorDataStructureInterface;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.srl.inference.ISRLInference;
import edu.illinois.cs.cogcomp.srl.inference.SRLLagrangeInference;
import edu.illinois.cs.cogcomp.thrift.base.Forest;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SemanticRoleLabeler {
	private final static Logger log = LoggerFactory.getLogger(SemanticRoleLabeler.class);
	public final SRLManager manager;
	private static SRLProperties properties;

	public static void main(String[] arguments) {
		if (arguments.length < 1) {
			System.err.println("Usage: <config-file> [Verb | Nom]");
			System.exit(-1);
		}
		String configFile = arguments[0];
		String srlType;
		// If no second argument is provided it means we need all the SRL types
		srlType = arguments.length == 1 ? null : arguments[1];

		String input;
		List<SemanticRoleLabeler> srlLabelers = new ArrayList<SemanticRoleLabeler>();
		try {
			if (srlType != null)
				srlLabelers.add(new SemanticRoleLabeler(configFile, srlType));
			else {
				for (SRLType type : SRLType.values()) {
					srlType = type.name();
					srlLabelers.add(new SemanticRoleLabeler(configFile, srlType));
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
			if (input.equals("_")) return;

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

	public SemanticRoleLabeler(String configFile, String srlType) throws Exception {
		WordNetManager.loadConfigAsClasspathResource(true);

		log.info("Initializing config");
		SRLProperties.initialize(configFile);
		properties = SRLProperties.getInstance();

		log.info("Initializing pre-processor");
		TextPreProcessor.initialize(configFile, false);

		log.info("Creating {} manager", srlType);
		manager = Main.getManager(SRLType.valueOf(srlType), false);

		log.info("Loading models");
		loadModels();
	}

	public String getSRLCuratorName() {
		return manager.getSRLSystemIdentifier();
	}

	public String getVersion() {
		return properties.getSRLVersion();
	}

	public String getCuratorName() {
		return "illinoisSRL";
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

		if (predicates.isEmpty()) return null;

		ISRLInference inference = new SRLLagrangeInference(manager, ta, predicates, true, properties.getMaxInferenceRounds());

		return inference.getOutputView();
	}

	public Forest getSRLForest(Record record) throws Exception {
		TextAnnotation ta = CuratorDataStructureInterface.getTextAnnotationViewsFromRecord("", "", record);
		PredicateArgumentView pav = getSRL(ta);
		return CuratorDataStructureInterface.convertPredicateArgumentViewToForest(pav);
	}

}
