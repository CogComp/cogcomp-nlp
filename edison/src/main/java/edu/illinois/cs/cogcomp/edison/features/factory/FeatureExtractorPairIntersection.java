/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.HashSet;
import java.util.Set;

/**
 * Given two feature extractors, for two
 * this is good when you want to find overlap between two feature representations
 *
 * @author daniel
 */
public class FeatureExtractorPairIntersection<T1, T2> extends PairExtractor<T1, T2> {
    FeatureExtractor<T1> fe1 = null;
    FeatureExtractor<T2> fe2 = null;

    public FeatureExtractorPairIntersection(FeatureExtractor<T1> fe1, FeatureExtractor<T2> fe2) {
        this.fe1 = fe1;
        this.fe2 = fe2;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        throw new EdisonException("this function shouldn't be used");
    }

    /**
     * @throws EdisonException
     */
    protected Set<Feature> getCombinedFeaturesImplementation(T1 c1, T2 c2) throws EdisonException {
        Set<Feature> extractedFeatures1 = fe1.getFeatures(c1);
        Set<Feature> extractedFeatures2 = fe2.getFeatures(c2);

        Set<String> f2Names = new HashSet<>();
        for (Feature f : extractedFeatures1)
            f2Names.add(f.getName());

        Set<Feature> intersection = new HashSet<>();
        for (Feature f : extractedFeatures2) {
            if (f2Names.contains(f.getName())) {
                intersection.add(f.prefixWith(this.getName()));
            }
        }
        return intersection;
    }

    @Override
    public String getName() {
        return "#intrscn#";
    }
}