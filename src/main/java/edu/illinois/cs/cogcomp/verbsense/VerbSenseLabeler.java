package edu.illinois.cs.cogcomp.verbsense;

import edu.illinois.cs.cogcomp.edison.sentences.*;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.verbsense.inference.ILPInference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class VerbSenseLabeler {
	private final static Logger log = LoggerFactory.getLogger(VerbSenseLabeler.class);
	public final SenseManager manager;

	public static void main(String[] arguments) throws Exception {
		if (arguments.length < 1) {
			System.err.println("Usage: <config-file>");
			System.exit(-1);
		}
		String configFile = arguments[0];

		String input;
		VerbSenseLabeler labeler = new VerbSenseLabeler(configFile);

		System.out.print("Enter text (underscore to quit): ");
		input = System.console().readLine().trim();
		if (input.equals("_"))
			return;

		do {
			if (!input.isEmpty()) {
				TextAnnotation ta = TextPreProcessor.getInstance().preProcessText(input);

				TokenLabelView p = labeler.getPrediction(ta);
				System.out.println(p);
				System.out.println();
			}

			System.out.print("Enter text (underscore to quit): ");
			input = System.console().readLine().trim();

		} while (!input.equals("_"));
	}

	public VerbSenseLabeler(String configFile) throws Exception {
		WordNetManager.loadConfigAsClasspathResource(true);

		log.info("Initializing config");
		Properties.initialize(configFile);

		log.info("Initializing pre-processor");
		TextPreProcessor.initialize(false);

		log.info("Creating manager");
		manager = Main.getManager(false);

		log.info("Loading models");
		loadModel();

		TextAnnotation ta = initializeDummySentenceVerb();

		log.info("Testing on sentence {}", ta.getText());
		TokenLabelView prediction = getPrediction(ta);

		log.info("Output: {}", prediction.toString());
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

		TokenLabelView view = new TokenLabelView(ViewNames.LEMMA, "test", ta, 1d);
		view.addTokenLabel(0, "i", 1d);
		view.addTokenLabel(1, "do", 1d);
		view.addTokenLabel(2, ".", 1d);
		ta.addView(ViewNames.LEMMA, view);
		return ta;
	}

	private void loadModel() throws Exception {
		manager.getModelInfo().loadWeightVector();
		log.info("Finished loading model");
	}

	public TokenLabelView getPrediction(TextAnnotation ta) throws Exception {
		log.debug("Input: {}", ta.getText());
		List<Constituent> predicates = manager.getPredicateDetector().getPredicates(ta);
		if (predicates.isEmpty()) return null;

		ILPSolverFactory solver = new ILPSolverFactory(ILPSolverFactory.SolverType.CuttingPlaneGurobi);
		ILPInference inference = manager.getInference(solver, predicates);
		return inference.getOutputView();
	}
}
