/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.jlis;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.FeatureVector;
import edu.illinois.cs.cogcomp.verbsense.core.ModelInfo;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SenseInstance implements IInstance {

    private final static Logger log = LoggerFactory.getLogger(SenseInstance.class);

    private final Constituent predicate;

    private FeatureVector features;
    private String predicateLemma;

    private SenseManager manager;

    public SenseInstance(Constituent predicate, SenseManager manager) {
        this.predicate = predicate;
        this.manager = manager;
        predicateLemma = predicate.getAttribute(PredicateArgumentView.LemmaIdentifier);
    }

    public SenseInstance(String lemma, String features) {
        predicate = null;
        this.predicateLemma = lemma;

        this.cacheFeatureVector(getFeatureVector(features));
    }

    @Override
    public double size() {
        return 1;
    }

    public String getPredicateLemma() {
        return predicateLemma;
    }

    @Override
    public String toString() {
        return "SenseInstance [predicate=" + predicateLemma + "]";
    }

    public void cacheFeatureVector(FeatureVector f) {
        features = f;
    }

    public FeatureVector getCachedFeatureVector() {
        return features;
    }

    private FeatureVector getFeatureVector(String features) {
        String[] parts = features.split(" ");
        int[] idx = new int[parts.length];
        float[] vals = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            String[] f = parts[i].split(":");

            idx[i] = Integer.parseInt(f[0]);
            vals[i] = Float.parseFloat(f[1]);
        }

        return new FeatureVector(idx, vals);
    }

    public Constituent getConstituent() {
        return predicate;
    }

    public void cacheFeatureVector(Set<Feature> features) {
        Map<String, Float> featureMap = new HashMap<>();
        for (Feature f : features) {
            featureMap.put(f.getName(), f.getValue());
        }

        ModelInfo modelInfo = manager.getModelInfo();
        Pair<int[], float[]> feats = modelInfo.getLexicon().getFeatureVector(featureMap);

        this.cacheFeatureVector(new FeatureVector(feats.getFirst(), feats.getSecond()));
    }

    public void cacheAllFeatureVectors() {
        ModelInfo modelInfo = manager.getModelInfo();
        try {
            Set<Feature> feats = modelInfo.fex.getFeatures(getConstituent());
            cacheFeatureVector(feats);
        } catch (Exception e) {
            log.error("Unable to extract features for {}", this, e);
            throw new RuntimeException(e);
        }
    }
}
