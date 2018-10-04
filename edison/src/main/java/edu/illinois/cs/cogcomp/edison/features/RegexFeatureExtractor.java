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

import java.util.LinkedHashSet;
import java.util.Set;

public class RegexFeatureExtractor implements FeatureExtractor {

    private final static DiscreteFeature matches = DiscreteFeature.create("Y");
    private final String regex;

    public RegexFeatureExtractor(String regex) {
        this.regex = regex;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        Set<Feature> feature = new LinkedHashSet<>();

        if (c.getTokenizedSurfaceForm().matches(regex))
            feature.add(matches);

        return feature;
    }

    @Override
    public String getName() {
        return "regex:" + regex;
    }

}
