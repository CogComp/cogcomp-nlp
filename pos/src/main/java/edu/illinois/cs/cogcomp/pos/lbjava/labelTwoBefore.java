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
 * Produces the POS tag label of the word two before the target word. During the training of
 * {@link POSTaggerKnown}, these labels are present in the data, so the value of {@link POSLabel} is
 * simply returned. Otherwise, the prediction made by {@link POSTaggerKnown} is returned.
 *
 * @author Nick Rizzolo
 **/
public class labelTwoBefore extends Classifier {
    private static final POSLabel __POSLabel = new POSLabel();
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String knownModelFile = rm.getString("knownModelPath");
    private static String knownLexFile = rm.getString("knownLexPath");
    private static String baselineModelFile = rm.getString("baselineModelPath");
    private static String baselineLexFile = rm.getString("baselineLexPath");
    private static BaselineTarget baselineTarget = new BaselineTarget(baselineModelFile,
            baselineLexFile);
    private static final POSTaggerKnown __POSTaggerKnown = new POSTaggerKnown(knownModelFile,
            knownLexFile, baselineTarget);
    private static ThreadLocal __cache = new ThreadLocal() {};
    private static ThreadLocal __exampleCache = new ThreadLocal() {};

    public labelTwoBefore() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "labelTwoBefore";
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

        if (w.previous != null && w.previous.previous != null) {
            if (__POSTaggerKnown.isTraining) {
                return "" + (__POSLabel.discreteValue(w.previous.previous));
            }
            return "" + (__POSTaggerKnown.discreteValue(w.previous.previous));
        }
        return "";
    }

    public int hashCode() {
        return "labelTwoBefore".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof labelTwoBefore;
    }
}
