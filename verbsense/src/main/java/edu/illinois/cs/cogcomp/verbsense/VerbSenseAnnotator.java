/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.verbsense.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.verbsense.utilities.VerbSenseConfigurator;

/**
 * @author Daniel Khashabi
 */
public class VerbSenseAnnotator extends Annotator {

    VerbSenseLabeler labeler;
    TextPreProcessor preProcessor;

    public VerbSenseAnnotator() {
        this(true);
    }

    public VerbSenseAnnotator(boolean lazilyInitialize) {
        this(lazilyInitialize, new VerbSenseConfigurator().getDefaultConfig());
    }

    public VerbSenseAnnotator(boolean lazilyInitialize, ResourceManager rm) {
        super(ViewNames.VERB_SENSE, new String[] {ViewNames.POS, ViewNames.LEMMA,
                ViewNames.SHALLOW_PARSE, ViewNames.NER_CONLL}, lazilyInitialize,
                new VerbSenseConfigurator().getConfig(rm));
    }

    @Override
    public void initialize(ResourceManager resourceManager) {
        try {
            labeler = new VerbSenseLabeler();
        } catch (Exception e) {
            e.printStackTrace();
        }
        preProcessor = TextPreProcessor.getInstance();
    }

    @Override
    protected void addView(TextAnnotation textAnnotation) throws AnnotatorException {
        try {
            textAnnotation.addView(viewName, labeler.getPrediction(textAnnotation));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
