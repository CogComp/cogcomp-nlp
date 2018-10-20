/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.depparse.DepConfigurator;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.nlp.DataReader;
import edu.illinois.cs.cogcomp.prepsrl.data.PrepSRLDataReader;
import edu.illinois.cs.cogcomp.prepsrl.inference.ConstrainedPrepSRLClassifier;

import java.util.*;

/**
 * An {@link Annotator} that adds a {@link TokenLabelView} for Prepositional SRL. This consists of
 * role information for each preposition (e.g. Location, Instrument, etc) and (syntactic) argument
 * information (governor, object) to the syntactic heads of the governing and object phrases of the
 * preposition respectively.
 *
 * @author Christos Christodoulopoulos
 */
public class PrepSRLAnnotator extends Annotator {

    Classifier classifier;

    /**
     * default: don't use lazy initialization
     */
    public PrepSRLAnnotator() {
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
                ViewNames.LEMMA, ViewNames.DEPENDENCY}, lazilyInitialize, new DepConfigurator()
                .getConfig(rm));
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

        PredicateArgumentView prepositionLabelView = new PredicateArgumentView(viewName, viewName + "-annotator", ta, 1.0);


        IQueryable<Constituent> chunkCons = ta.select(ViewNames.SHALLOW_PARSE);
        for (Constituent c : candidates) {
            String role = classifier.discreteValue(c);
            if (!role.equals(DataReader.CANDIDATE)) {
                Constituent p = new Constituent(role, 1.0, viewName, ta, c.getStartSpan(), c.getEndSpan());

                List<String> relations = new ArrayList<>();
                List<Constituent> arguments = new ArrayList<>();

                // adding source and target constituents from chunk view
                IQueryable<Constituent> before = chunkCons.where(Queries.before(c));
                before.orderBy((o1, o2) -> {
                    // Descending order to find the nearest before
                    return o2.getEndSpan() - o1.getEndSpan();
                });
                Iterator<Constituent> beforeIt = before.iterator();
                if (beforeIt.hasNext()) {
                    Constituent nearestBefore = beforeIt.next();
                    arguments.add(nearestBefore);
                    relations.add("Governer");
                }

                IQueryable<Constituent> after = chunkCons.where(Queries.after(c));
                after.orderBy(Comparator.comparingInt(Constituent::getEndSpan));
                Iterator<Constituent> afterIt = after.iterator();
                if (afterIt.hasNext()) {
                    Constituent nearestAfter = afterIt.next();
                    arguments.add(nearestAfter);
                    relations.add("Object");
                }
                double[] scores = new double[arguments.size()];
                Arrays.fill(scores, 1.0);
                prepositionLabelView.addPredicateArguments(p, arguments, relations.toArray(new String[relations.size()]), scores);
            }
        }

        ta.addView(viewName, prepositionLabelView);
    }
}
