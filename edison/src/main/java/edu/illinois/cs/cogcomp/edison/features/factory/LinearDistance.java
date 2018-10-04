/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Given two constituents, indicates distance between the two. The distance is quantized into one of
 * {0,1,2,3, many} and negatives indicate before. For multi-word constituents, the distance is
 * between the end of the first one and the start of the second.
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
public class LinearDistance implements FeatureExtractor {

    private static final DiscreteFeature ZERO = DiscreteFeature.create("0");

    private static final DiscreteFeature ONE = DiscreteFeature.create("1");
    private static final DiscreteFeature TWO = DiscreteFeature.create("2");
    private static final DiscreteFeature THREE = DiscreteFeature.create("3");
    private static final DiscreteFeature MANY = DiscreteFeature.create("+MANY");

    private static final DiscreteFeature MINUS_ONE = DiscreteFeature.create("-1");
    private static final DiscreteFeature MINUS_TWO = DiscreteFeature.create("-2");
    private static final DiscreteFeature MINUS_THREE = DiscreteFeature.create("-3");
    private static final DiscreteFeature MINUS_MANY = DiscreteFeature.create("-MANY");

    public static LinearDistance instance = new LinearDistance();

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        Constituent predicate = c.getIncomingRelations().get(0).getSource();

        Set<Feature> features = new LinkedHashSet<>();
        if (Queries.before(predicate).transform(c)) {
            int first = c.getEndSpan() - 1;
            int second = predicate.getStartSpan();

            int diff = second - first;
            assert diff > 0;

            switch (diff) {
                case 1:
                    features.add(MINUS_ONE);
                    break;
                case 2:
                    features.add(MINUS_TWO);
                    break;
                case 3:
                    features.add(MINUS_THREE);
                    break;
                default:
                    features.add(MINUS_MANY);
            }

        } else if (Queries.after(predicate).transform(c)) {
            int first = predicate.getEndSpan() - 1;
            int second = c.getStartSpan();

            int diff = second - first;
            assert diff > 0;

            switch (diff) {
                case 1:
                    features.add(ONE);
                    break;
                case 2:
                    features.add(TWO);
                    break;
                case 3:
                    features.add(THREE);
                    break;
                default:
                    features.add(MANY);
            }
        } else
            features.add(ZERO);
        return features;
    }

    @Override
    public String getName() {
        return "#lin-dis#";
    }

}
