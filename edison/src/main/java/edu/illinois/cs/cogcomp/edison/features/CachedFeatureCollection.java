/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

public class CachedFeatureCollection extends FeatureCollection {
    private ThreadLocal<Constituent> currentConstituent = new ThreadLocal<>();
    private ThreadLocal<Set<Feature>> currentFeatures = new ThreadLocal<>();

    public CachedFeatureCollection(String name) {
        super(name);
    }

    public CachedFeatureCollection(String name, FeatureExtractor... feats) {
        super(name, feats);
    }

    public CachedFeatureCollection(String name, FeatureInputTransformer inputTransformer,
            FeatureExtractor... feats) {
        super(name, inputTransformer, feats);
    }

    public CachedFeatureCollection(String name, FeatureInputTransformer inputTransformer) {
        super(name, inputTransformer);
    }

    @Override
    public Set<Feature> getFeatures(Constituent candidate) throws EdisonException {

        if (currentConstituent.get() == candidate)
            return currentFeatures.get();

        Set<Feature> features = super.getFeatures(candidate);

        currentConstituent.set(candidate);
        currentFeatures.set(features);

        return features;
    }

}
