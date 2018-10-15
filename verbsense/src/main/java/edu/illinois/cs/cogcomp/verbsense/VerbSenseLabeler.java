/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.verbsense.inference.ILPInference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        VerbSenseLabeler labeler = new VerbSenseLabeler();

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

    public VerbSenseLabeler() throws Exception {
        WordNetManager.loadConfigAsClasspathResource(true);

        log.info("Initializing pre-processor");
        TextPreProcessor.initialize();

        log.info("Creating manager");
        manager = VerbSenseClassifierMain.getManager(false);

        log.info("Loading models");
        loadModel();

        TextAnnotation ta = initializeDummySentenceVerb();

        log.info("Testing on sentence {}", ta.getText());
        TokenLabelView prediction = getPrediction(ta);

        log.info("Output: {}", prediction.toString());
    }

    protected TextAnnotation initializeDummySentenceVerb() {
        List<String[]> listOfTokens = new ArrayList<>();
        listOfTokens.add(new String[] {"I", "do", "."});
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens("", "", listOfTokens);

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
        // If there are no verb identified, return an empty TokenLabelView
        if (predicates.isEmpty())
            return new TokenLabelView(SenseManager.getPredictedViewName(),
                    VerbSenseConstants.systemIdentifier, ta, 1.0);

        ILPSolverFactory solver =
                new ILPSolverFactory(ILPSolverFactory.SolverType.JLISCuttingPlaneGurobi);
        ILPInference inference = manager.getInference(solver, predicates);
        return inference.getOutputView();
    }
}
