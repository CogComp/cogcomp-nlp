/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.depparse.DepConfigurator;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.nlp.DataReader;
import edu.illinois.cs.cogcomp.prepsrl.data.PrepSRLDataReader;
import edu.illinois.cs.cogcomp.prepsrl.inference.ConstrainedPrepSRLClassifier;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Annotator} that adds a {@link TokenLabelView} for Prepositional SRL.
 * This consists of role information for each preposition (e.g. Location, Instrument, etc)
 * and (syntactic) argument information (governor, object) to the syntactic heads of the
 * governing and object phrases of the preposition respectively.
 *
 * @author Christos Christodoulopoulos
 */
public class PrepSRLAnnotator extends Annotator {

    Classifier classifier;

    /**
     * default: don't use lazy initialization
     */
    public PrepSRLAnnotator(){
        this(true);
    }

    /**
     * Constructor parameter allows user to specify whether or not to lazily initialize.
     *
     * @param lazilyInitialize If set to 'true', models will not be loaded until first call
     *        requiring Chunker annotation.
     */
    public PrepSRLAnnotator(boolean lazilyInitialize) {
        this(lazilyInitialize, PrepSRLConfigurator.defaults());
    }

    public PrepSRLAnnotator(boolean lazilyInitialize, ResourceManager rm) {
        super(ViewNames.SRL_PREP, new String[] {ViewNames.POS, ViewNames.SHALLOW_PARSE,
                ViewNames.LEMMA, ViewNames.DEPENDENCY}, lazilyInitialize,
                new DepConfigurator().getConfig(rm));
    }

    @Override
    public void initialize(ResourceManager rm) {
        classifier = new ConstrainedPrepSRLClassifier(rm);
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        List<Constituent> candidates = new ArrayList<>();
        for (Constituent c : ta.getView(ViewNames.TOKENS).getConstituents()) {
            int tokenId = c.getStartSpan();
            if (PrepSRLDataReader.isPrep(ta, tokenId))
                candidates.add(c
                        .cloneForNewViewWithDestinationLabel(viewName, DataReader.CANDIDATE));
            // Now check bigrams & trigrams
            Constituent multiWordPrep = PrepSRLDataReader.isBigramPrep(ta, tokenId, viewName);
            if (multiWordPrep != null)
                candidates.add(multiWordPrep);
            multiWordPrep = PrepSRLDataReader.isTrigramPrep(ta, tokenId, viewName);
            if (multiWordPrep != null)
                candidates.add(multiWordPrep);
        }

        TokenLabelView prepositionLabelView = new TokenLabelView(viewName, ta);
        for (Constituent c : candidates) {
            String role = classifier.discreteValue(c);
            if (!role.equals(DataReader.CANDIDATE))
                prepositionLabelView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), role, 1.0);
        }
        ta.addView(viewName, prepositionLabelView);
    }
}
