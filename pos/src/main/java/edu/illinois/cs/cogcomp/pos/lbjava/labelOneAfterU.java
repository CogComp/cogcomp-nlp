/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D294558C94C4A4DC1FFCB457C4B294D2A05D809CFCE4DC35827D4584E4C4EC84D4150B1D558A658CC4350D827DBCB4DA8215054B558CB2DC9C1D40A8205B79615E14D08227A4C2E4DC9CCCB4D058AA4D4B658A58921525202700655D9CAA27000000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.*;


/**
 * Returns the value of {@link labelOrBaselineU} on the next word.
 *
 * @author Nick Rizzolo
 **/
public class labelOneAfterU extends Classifier {
    private static final labelOrBaselineU __labelOrBaselineU = new labelOrBaselineU();

    private static ThreadLocal __cache = new ThreadLocal() {};
    private static ThreadLocal __exampleCache = new ThreadLocal() {};

    public static void clearCache() {
        __exampleCache = new ThreadLocal() {};
    }

    public labelOneAfterU() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "labelOneAfterU";
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
        if (!(__example instanceof Token)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'labelOneAfterU(Token)' defined on line 83 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return new FeatureVector(cachedFeatureValue(__example));
    }

    public Feature featureValue(Object __example) {
        if (!(__example instanceof Token)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'labelOneAfterU(Token)' defined on line 83 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return cachedFeatureValue(__example);
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof Token)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'labelOneAfterU(Token)' defined on line 83 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return cachedFeatureValue(__example).getStringValue();
    }

    private String _discreteValue(Object __example) {
        Token w = (Token) __example;

        if (w.next != null) {
            return "" + (__labelOrBaselineU.discreteValue(w.next));
        }
        return "";
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Token[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'labelOneAfterU(Token)' defined on line 83 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "labelOneAfterU".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof labelOneAfterU;
    }
}
