/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SpanLengthFeature implements FeatureExtractor {

    public static SpanLengthFeature instance = new SpanLengthFeature();

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return new LinkedHashSet<Feature>(Collections.singletonList(RealFeature.create("l",
                c.size())));
    }

    @Override
    public String getName() {
        return "#ntoks";
    }

}
