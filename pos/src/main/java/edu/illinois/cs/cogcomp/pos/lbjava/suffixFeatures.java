/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.*;

/**
 * When {@link BaselineTarget} has not observed the given word during training, this classifier
 * extracts suffixes of the word of various lengths.
 *
 * @author Nick Rizzolo
 **/
public class suffixFeatures extends Classifier {
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String baselineModelFile = rm.getString("baselineModelPath");
    private static String baselineLexFile = rm.getString("baselineLexPath");
    private static final BaselineTarget __baselineTarget = new BaselineTarget(baselineModelFile,
            baselineLexFile);

    public suffixFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "suffixFeatures";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        Token w = (Token) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        int length = w.form.length();
        boolean unknown =
                POSTaggerUnknown.isTraining
                        && __baselineTarget.observedCount(w.form) <= POSLabeledUnknownWordParser.threshold
                        || !POSTaggerUnknown.isTraining
                        && __baselineTarget.discreteValue(w).equals("UNKNOWN");
        if (unknown && length > 3 && Character.isLetter(w.form.charAt(length - 1))) {
            __id = "" + (w.form.substring(length - 1).toLowerCase());
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
            if (Character.isLetter(w.form.charAt(length - 2))) {
                __id = "" + (w.form.substring(length - 2).toLowerCase());
                __value = "true";
                __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                        this.name, __id, __value, valueIndexOf(__value), (short) 0));
                if (length > 4 && Character.isLetter(w.form.charAt(length - 3))) {
                    __id = "" + (w.form.substring(length - 3).toLowerCase());
                    __value = "true";
                    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                            this.name, __id, __value, valueIndexOf(__value), (short) 0));
                }
            }
        }
        return __result;
    }

    public int hashCode() {
        return "suffixFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof suffixFeatures;
    }
}
