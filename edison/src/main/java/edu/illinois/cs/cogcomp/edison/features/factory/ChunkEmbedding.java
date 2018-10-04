/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.QueryableList;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Adds the following features using chunks from the specified view:
 * <ul>
 * <li>The number of chunks contained in the constituent</li>
 * <li>A thresholded version of the number of chunks. This is a discrete feature with four possible
 * values: 0, 1, 2, many</li>
 * <li>The embedding of the chunks (one of embedded-in, embeds, is, exclusively-overlaps,
 * has-overlap, has-no-overlap, same-start-span, same-end-span). These conditions are checked using
 * the corresponding query in {@link Queries}. This feature also includes the label of the chunk</li>
 * <li>The number of chunks of each type embedded (as a real feature)</li>
 * </ul>
 *
 * @author Vivek Srikumar
 */
public class ChunkEmbedding implements FeatureExtractor {

    public final static ChunkEmbedding NER = new ChunkEmbedding(ViewNames.NER_CONLL);
    public final static ChunkEmbedding SHALLOW_PARSE = new ChunkEmbedding(ViewNames.SHALLOW_PARSE);

    private final String viewName;

    public ChunkEmbedding(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        View constituents = c.getTextAnnotation().getView(viewName);
        IQueryable<Constituent> shallowParseContained =
                constituents.where(Queries.containedInConstituent(c));

        int numChunks = ((QueryableList<Constituent>) shallowParseContained).size();
        Set<Feature> features = new LinkedHashSet<>();

        if (numChunks > 0)
            features.add(RealFeature.create("nchnks", numChunks));

        if (numChunks == 1) {
            features.add(DiscreteFeature.create("nchnks-th:1"));
        } else if (numChunks == 2) {
            features.add(DiscreteFeature.create("nchnks-th:2"));
        } else if (numChunks >= 3) {
            features.add(DiscreteFeature.create("nchnks-th:many"));
        }

        Counter<String> counter = new Counter<>();

        Predicate<Constituent> condition = Queries.containedInConstituentExclusive(c);

        addFeatures(features, constituents, condition, counter, "cont-in");

        condition = Queries.containsConstituent(c);

        addFeatures(features, constituents, condition, counter, "contains");

        condition = Queries.sameSpanAsConstituent(c);

        addFeatures(features, constituents, condition, counter, "=span");

        condition = Queries.exclusivelyOverlaps(c);

        addFeatures(features, constituents, condition, counter, "ex-ovlp");

        condition = Queries.hasOverlap(c);
        addFeatures(features, constituents, condition, counter, "has-ovlp");

        // condition = Queries.hasNoOverlap(c);
        // addFeatures(features, constituents, condition, counter,
        // "has-no-overlap");

        condition = Queries.sameStartSpanAs(c);
        addFeatures(features, constituents, condition, counter, "=start");

        condition = Queries.sameEndSpanAs(c);
        addFeatures(features, constituents, condition, counter, "=end");

        features.addAll(FeatureUtilities.getFeatures(counter));

        return features;
    }

    private void addFeatures(Set<Feature> features, View view, Predicate<Constituent> condition,
            Counter<String> counter, String name) {
        for (Constituent chunk : view.where(condition)) {
            String label = name + "-" + chunk.getLabel();
            features.add(DiscreteFeature.create("e:" + label));
            counter.incrementCount("n:" + label);
        }
    }

    @Override
    public String getName() {
        return "#emb#" + viewName;
    }

}
