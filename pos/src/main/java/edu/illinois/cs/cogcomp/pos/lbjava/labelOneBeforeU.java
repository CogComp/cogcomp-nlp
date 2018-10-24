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
 * Produces the POS tag label of the word one before the target word. During the training of
 * {@link POSTaggerUnknown}, these labels are present in the data, so the value of {@link POSLabel}
 * is simply returned. Otherwise, the prediction made by {@link POSTaggerUnknown} is returned.
 *
 * @author Nick Rizzolo
 **/
public class labelOneBeforeU extends Classifier {
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String unknownModelFile = rm.getString("unknownModelPath");
    private static String unknownLexFile = rm.getString("unknownLexPath");
    private static String mikheevModelFile = rm.getString("mikheevModelPath");
    private static String mikheevLexFile = rm.getString("mikheevLexPath");
    private static MikheevTable mikheevTable = new MikheevTable(mikheevModelFile, mikheevLexFile);
    private static final POSTaggerUnknown __POSTaggerUnknown = new POSTaggerUnknown(
            unknownModelFile, unknownLexFile, mikheevTable);
    private static final POSLabel __POSLabel = new POSLabel();

    private static ThreadLocal __cache = new ThreadLocal() {};
    private static ThreadLocal __exampleCache = new ThreadLocal() {};

    public labelOneBeforeU() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "labelOneBeforeU";
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

        if (w.previous != null) {
            if (__POSTaggerUnknown.isTraining) {
                return "" + (__POSLabel.discreteValue(w.previous));
            }
            return "" + (__POSTaggerUnknown.discreteValue(w.previous));
        }
        return "";
    }

    public int hashCode() {
        return "labelOneBeforeU".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof labelOneBeforeU;
    }
}
