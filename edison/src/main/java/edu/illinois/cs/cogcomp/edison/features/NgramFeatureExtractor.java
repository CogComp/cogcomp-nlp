/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class NgramFeatureExtractor extends WordConstituentFeatureExtractor {

    private final WordConstituentFeatureExtractor base;
    private final int n;

    public NgramFeatureExtractor(WordConstituentFeatureExtractor base, int n) {
        this.base = base;
        this.n = n;

    }

    public static NgramFeatureExtractor bigrams(WordConstituentFeatureExtractor base) {
        return new NgramFeatureExtractor(base, 2);
    }

    public static NgramFeatureExtractor trigrams(WordConstituentFeatureExtractor base) {
        return new NgramFeatureExtractor(base, 3);
    }

    @Override
    public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {

        Set<Feature> features = new LinkedHashSet<>();
        for (int i = wordPosition - n + 1; i <= wordPosition; i++) {

            // TODO: Add a caching mechanism for baseFeatures(ta, i)
            Set<Feature> f = base.getWordFeatures(ta, i);
            if (features.isEmpty())
                features = f;
            else
                features = FeatureUtilities.conjoin(features, f);
        }

        return features;
    }

    @Override
    public String getName() {
        return "ngram(" + n + "):" + base.getName();
    }
}
