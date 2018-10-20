/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class FeatureNGramUtility {

    private static final ITransformer<Constituent, String> labelTransformer =
            new ITransformer<Constituent, String>() {

                @Override
                public String transform(Constituent input) {
                    return input.getLabel();
                }
            };

    public static Set<Feature> getLabelNgramsOrdered(List<Constituent> list, int ngramLength) {
        return getNgramsOrdered(list, ngramLength, labelTransformer);
    }

    public static <T> Set<Feature> getNgramsOrdered(List<T> list, int ngramLength,
            ITransformer<T, String> f) {
        Set<Feature> features = new LinkedHashSet<>();

        for (int i = 0; i < list.size() - ngramLength + 1; i++) {

            List<String> strings = new ArrayList<>();

            for (int j = 0; j < ngramLength; j++) {

                if (i + j < list.size()) {
                    T cc = list.get(i + j);
                    strings.add(f.transform(cc));
                }

            }
            features.add(DiscreteFeature.create(i + ":" + StringUtils.join("-", strings)));
        }
        return features;
    }

    public static Set<Feature> getLabelNgramsUnordered(List<Constituent> list, int ngramLength) {
        return getNgramsUnordered(list, ngramLength, labelTransformer);
    }

    public static <T> Set<Feature> getNgramsUnordered(List<T> list, int ngramLength,
            ITransformer<T, String> f) {
        Set<Feature> features = new LinkedHashSet<>();

        for (int i = 0; i < list.size() - ngramLength + 1; i++) {

            List<String> strings = new ArrayList<>();

            for (int j = 0; j < ngramLength; j++) {

                if (i + j < list.size()) {
                    T cc = list.get(i + j);
                    strings.add(f.transform(cc));
                }

            }
            features.add(DiscreteFeature.create(StringUtils.join("-", strings)));
        }
        return features;
    }

}
