/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.POSConfigurator;

/**
 * During the training of {@link POSTaggerUnknown}, return the value of {@link POSLabel}; otherwise,
 * return the value of {@link BaselineTarget}.
 *
 * @author Nick Rizzolo
 **/
public class labelOrBaselineU extends Classifier {
    private static final POSLabel __POSLabel = new POSLabel();
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String baselineModelFile = rm.getString("baselineModelPath");
    private static String baselineLexFile = rm.getString("baselineLexPath");
    private static final BaselineTarget __baselineTarget = new BaselineTarget(baselineModelFile,
            baselineLexFile);

    private static ThreadLocal __cache = new ThreadLocal() {};
    private static ThreadLocal __exampleCache = new ThreadLocal() {};

    public labelOrBaselineU() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "labelOrBaselineU";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete";
    }

    private Feature cachedFeatureValue(Object __example) {
        if (__example == __exampleCache.get())
            return (Feature) __cache.get();
        __exampleCache.set(__example);
        String __cachedValue = _discreteValue(__example);
        Feature __result =
                new DiscretePrimitiveStringFeature(containingPackage, name, "", __cachedValue,
                        valueIndexOf(__cachedValue), (short) allowableValues().length);
        __cache.set(__result);
        return __result;
    }

    public FeatureVector classify(Object __example) {
        return new FeatureVector(cachedFeatureValue(__example));
    }

    public Feature featureValue(Object __example) {
        return cachedFeatureValue(__example);
    }

    public String discreteValue(Object __example) {
        return cachedFeatureValue(__example).getStringValue();
    }

    private String _discreteValue(Object __example) {
        Token w = (Token) __example;

        if (POSTaggerUnknown.isTraining) {
            return "" + (__POSLabel.discreteValue(w));
        }
        return "" + (__baselineTarget.discreteValue(w));
    }

    public int hashCode() {
        return "labelOrBaselineU".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof labelOrBaselineU;
    }
}
