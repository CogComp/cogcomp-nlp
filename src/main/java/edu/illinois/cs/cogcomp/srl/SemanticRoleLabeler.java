package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorDataStructureInterface;
import edu.illinois.cs.cogcomp.edison.sentences.*;
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
import java.util.Arrays;
import java.util.List;

public class SemanticRoleLabeler {
	private final static Logger log = LoggerFactory.getLogger(SemanticRoleLabeler.class);
	public final SRLManager manager;
	private static SRLProperties properties;

	public static void main(String[] arguments) throws Exception {
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
		if (srlType != null)
			srlLabelers.add(new SemanticRoleLabeler(configFile, srlType));
		else {
			for (SRLType type : SRLType.values()) {
				srlType = type.name();
				srlLabelers.add(new SemanticRoleLabeler(configFile, srlType));
			}
		}

		System.out.print("Enter text (underscore to quit): ");
		input = System.console().readLine().trim();
		if (input.equals("_"))
			return;

		do {
			if (!input.isEmpty()) {
				// XXX Assuming that all SRL types require the same views
				TextAnnotation ta = TextPreProcessor.getInstance().preProcessText(input);

				for (SemanticRoleLabeler srl : srlLabelers) {
					System.out.println(srl.getSRLCuratorName());

					PredicateArgumentView p = srl.getSRL(ta);
					if (p == null) continue;

					System.out.println(p);
					System.out.println();
				}
			}

			System.out.print("Enter text (underscore to quit): ");
			input = System.console().readLine().trim();

		} while (!input.equals("_"));
	}

	public SemanticRoleLabeler(String configFile, String srlType) throws Exception {
		WordNetManager.loadConfigAsClasspathResource(true);

		log.info("Initializing config");
		SRLProperties.initialize(configFile);
		properties = SRLProperties.getInstance();

		log.info("Initializing pre-processor");
		TextPreProcessor.initialize(false);

		log.info("Creating {} manager", srlType);
		manager = Main.getManager(SRLType.valueOf(srlType), false);

		log.info("Loading models");
		loadModels();

		TextAnnotation ta;
		if (manager.getSRLType() == SRLType.Verb)
			ta = initializeDummySentenceVerb();
		else
			ta = initializeDummySentenceNom();

		log.info("Running {} SRL on sentence {}", srlType, ta.getText());
		PredicateArgumentView srl = getSRL(ta);

		log.info("Output: {}", srl.toString());
	}

	public String getSRLCuratorName() {
		return manager.getSRLSystemIdentifier();
	}

	protected TextAnnotation initializeDummySentenceVerb() {
		TextAnnotation ta = new TextAnnotation("", "", Arrays.asList("I do ."));

		TokenLabelView tlv = new TokenLabelView(ViewNames.POS, "Test", ta, 1.0);
		tlv.addTokenLabel(0, "PRP", 1d);
		tlv.addTokenLabel(1, "VBP", 1d);
		tlv.addTokenLabel(2, ".", 1d);
		ta.addView(ViewNames.POS, tlv);

		ta.addView(ViewNames.NER, new SpanLabelView(ViewNames.NER, "test", ta, 1d));

		SpanLabelView chunks = new SpanLabelView(ViewNames.SHALLOW_PARSE, "test", ta, 1d);
		chunks.addSpanLabel(0, 1, "NP", 1d);
		chunks.addSpanLabel(1, 2, "VP", 1d);
		ta.addView(ViewNames.SHALLOW_PARSE, chunks);

		String defaultParser = properties.getDefaultParser();
		String parseView = null;
		if (defaultParser.equals("Charniak")) parseView = ViewNames.PARSE_CHARNIAK;
		if (defaultParser.equals("Stanford")) parseView = ViewNames.PARSE_STANFORD;
		if (defaultParser.equals("Berkeley")) parseView = ViewNames.PARSE_BERKELEY;
		TreeView parse = new TreeView(parseView, defaultParser, ta, 1.0);
		parse.setParseTree(0, TreeParserFactory.getStringTreeParser()
				.parse("(S1 (S (NP (PRP I))       (VP (VPB do))        (. .)))"));
		ta.addView(parse.getViewName(), parse);

		TokenLabelView view = new TokenLabelView(ViewNames.LEMMA, "test", ta, 1d);
		view.addTokenLabel(0, "i", 1d);
		view.addTokenLabel(1, "do", 1d);
		view.addTokenLabel(2, ".", 1d);
		ta.addView(ViewNames.LEMMA, view);
		return ta;
	}

	protected TextAnnotation initializeDummySentenceNom() {
		TextAnnotation ta = new TextAnnotation("", "",
				Arrays.asList("The construction of the library is complete ."));

		TokenLabelView tlv = new TokenLabelView(ViewNames.POS, "Test", ta, 1.0);
		tlv.addTokenLabel(0, "DT", 1d);
		tlv.addTokenLabel(1, "NN", 1d);
		tlv.addTokenLabel(2, "IN", 1d);
		tlv.addTokenLabel(3, "DT", 1d);
		tlv.addTokenLabel(4, "NN", 1d);
		tlv.addTokenLabel(5, "VB", 1d);
		tlv.addTokenLabel(6, "JJ", 1d);
		tlv.addTokenLabel(7, ". ", 1d);

		ta.addView(ViewNames.POS, tlv);
		ta.addView(ViewNames.NER, new SpanLabelView(ViewNames.NER, "test", ta, 1d));
		SpanLabelView chunks = new SpanLabelView(ViewNames.SHALLOW_PARSE, "test", ta, 1d);

		chunks.addSpanLabel(0, 2, "NP", 1d);
		chunks.addSpanLabel(2, 3, "PP", 1d);
		chunks.addSpanLabel(3, 5, "NP", 1d);
		chunks.addSpanLabel(5, 6, "VP", 1d);
		chunks.addSpanLabel(6, 7, "ADJP", 1d);

		ta.addView(ViewNames.SHALLOW_PARSE, chunks);

		String defaultParser = properties.getDefaultParser();
		String parseView = null;
		if (defaultParser.equals("Charniak")) parseView = ViewNames.PARSE_CHARNIAK;
		if (defaultParser.equals("Stanford")) parseView = ViewNames.PARSE_STANFORD;
		if (defaultParser.equals("Berkeley")) parseView = ViewNames.PARSE_BERKELEY;
		TreeView parse = new TreeView(parseView, defaultParser, ta, 1.0);

		String treeString = "(S1 (S (NP (NP (DT The) (NN construction)) (PP (IN of) (NP (DT the) (NN library)))) " +
				"(VP (AUX is) (ADJP (JJ complete))) (. .)))";
		parse.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(treeString));
		ta.addView(parse.getViewName(), parse);

		TokenLabelView view = new TokenLabelView(ViewNames.LEMMA, "test", ta, 1d);
		view.addTokenLabel(0, "the", 1d);
		view.addTokenLabel(1, "construction", 1d);
		view.addTokenLabel(2, "of", 1d);
		view.addTokenLabel(3, "the", 1d);
		view.addTokenLabel(4, "library", 1d);
		view.addTokenLabel(5, "be", 1d);
		view.addTokenLabel(6, "complete", 1d);
		view.addTokenLabel(7, ".", 1d);
		ta.addView(ViewNames.LEMMA, view);
		return ta;

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

		ISRLInference inference = new SRLLagrangeInference(manager, ta, predicates, true, 100);

		return inference.getOutputView();
	}

	public Forest getSRLForest(Record record) throws Exception {
		TextAnnotation ta = CuratorDataStructureInterface.getTextAnnotationViewsFromRecord("", "", record);
		PredicateArgumentView pav = getSRL(ta);
		return CuratorDataStructureInterface.convertPredicateArgumentViewToForest(pav);
	}

}
