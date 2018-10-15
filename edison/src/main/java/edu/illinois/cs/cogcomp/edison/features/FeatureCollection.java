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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A feature set. This feature extractor can chain several other feature extractors.
 *
 * @author Vivek Srikumar
 */
public class FeatureCollection implements FeatureExtractor {
    private static final DiscreteFeature NULL_INPUT = DiscreteFeature.create("NULL");

    protected final List<FeatureExtractor> generators;
    private final String name;

    private final FeatureInputTransformer inputTransformer;

    /**
     * Create a new feature extractor. By itself, it does not generate any features. Use
     * {@code FeatureExtractor#addFeatureGenerator(IFeatureExtractor))} to add feature generators.
     */
    public FeatureCollection(String name) {
        this(name, FeatureInputTransformer.identity);
    }

    public FeatureCollection(String name, FeatureExtractor... feats) {
        this(name, FeatureInputTransformer.identity, feats);
    }

    public FeatureCollection(String name, FeatureInputTransformer inputTransformer,
            FeatureExtractor... feats) {
        this.name = name;
        this.generators = new ArrayList<>();

        for (FeatureExtractor f : feats) {
            this.generators.add(f);
        }
        this.inputTransformer = inputTransformer;
    }

    public FeatureCollection(String name, FeatureInputTransformer inputTransformer) {
        this.name = name;
        this.generators = new ArrayList<>();
        this.inputTransformer = inputTransformer;
    }

    public void addFeatureExtractor(FeatureExtractor f) {
        this.generators.add(f);
    }

    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        boolean hasName = this.getName().length() > 0;
        Set<Feature> features = new LinkedHashSet<>();

        if (this.inputTransformer == FeatureInputTransformer.identity) {
            getFeatures(hasName, features, c);
        } else {
            List<Constituent> input = inputTransformer.transform(c);

            if (input == null || input.size() == 0) {
                features.add(NULL_INPUT);
            } else {
                for (Constituent in : input) {
                    getFeatures(hasName, features, in);
                }
            }
            features = FeatureUtilities.prefix(this.inputTransformer.name(), features);
        }
        return features;
    }

    private void getFeatures(boolean hasName, Set<Feature> features, Constituent in)
            throws EdisonException {
        for (FeatureExtractor f : generators) {
            String name = f.getName();

            boolean fHasName = name.length() > 0;

            Set<Feature> feats = f.getFeatures(in);

            for (Feature feat : feats)

                if (hasName && fHasName)
                    features.add(feat.prefixWith(this.getName() + ":" + name));
                else if (!hasName && fHasName)
                    features.add(feat.prefixWith(name));
                else if (hasName && !fHasName)
                    features.add(feat.prefixWith(getName()));
                else
                    features.add(feat);
        }
    }

    public String getName() {
        return this.name;
    }
}
