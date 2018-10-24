/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Given two constituents, indicates whether the first is before or after the other or have an
 * overlap.
 * <p>
 * <b>Important note</b>: To be able to specify the two constituents as input, the feature extractor
 * assumes the following convention: The constituent that is specified as a parameter to the
 * getFeatures function has an incoming relation from the first constituent. Furthermore, this
 * incoming relation should be the only such relation.
 * <p>
 * This convention does not limit the expressivity in any way because the two constituents could be
 * created on the spot before calling the feature extractor.
 *
 * @author Vivek Srikumar
 */
public class LinearPosition implements FeatureExtractor {

    private static final DiscreteFeature CONTAINS = DiscreteFeature.create("C");

    private static final DiscreteFeature AFTER = DiscreteFeature.create("A");

    private static final DiscreteFeature BEFORE = DiscreteFeature.create("B");

    public static LinearPosition instance = new LinearPosition();

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        List<Relation> incomingRelation = c.getIncomingRelations();
        Set<Feature> features = new LinkedHashSet<>();
        if (incomingRelation.size() > 0) {
            Constituent predicate = incomingRelation.get(0).getSource();
            if (predicate.getStartSpan() >= c.getEndSpan())
                features.add(BEFORE);
            else if (c.getStartSpan() >= predicate.getEndSpan())
                features.add(AFTER);
            else
                features.add(CONTAINS);
        }
        return features;
    }

    @Override
    public String getName() {
        return "#lin-pos#";
    }

}
