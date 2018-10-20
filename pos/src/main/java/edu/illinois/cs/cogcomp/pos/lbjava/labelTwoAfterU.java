/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D294558C94C4A4DC9092FC77C4B294D2A05D809CFCE4DC35827D4584E4C4EC84D4150B1D558A658CC4350D827DBCB4DA8215054B558CB2DC9C1505353508880A80B6205130D4D2D2AC3889BEF54E4985C9A9399979A1AA184AC53DA51A616AE494908C1000639FE248E8000000

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
 * Returns the value of {@link labelOrBaselineU} on the word after the next word.
 *
 * @author Nick Rizzolo
 **/
public class labelTwoAfterU extends Classifier {
    private static final labelOrBaselineU __labelOrBaselineU = new labelOrBaselineU();

    private static ThreadLocal __cache = new ThreadLocal() {};
    private static ThreadLocal __exampleCache = new ThreadLocal() {};

    public static void clearCache() {
        __exampleCache = new ThreadLocal() {};
    }

    public labelTwoAfterU() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "labelTwoAfterU";
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
                    .println("Classifier 'labelTwoAfterU(Token)' defined on line 95 of POSUnknown.lbj received '"
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
                    .println("Classifier 'labelTwoAfterU(Token)' defined on line 95 of POSUnknown.lbj received '"
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
                    .println("Classifier 'labelTwoAfterU(Token)' defined on line 95 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return cachedFeatureValue(__example).getStringValue();
    }

    private String _discreteValue(Object __example) {
        Token w = (Token) __example;

        if (w.next != null && w.next.next != null) {
            return "" + (__labelOrBaselineU.discreteValue(w.next.next));
        }
        return "";
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Token[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'labelTwoAfterU(Token)' defined on line 95 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "labelTwoAfterU".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof labelTwoAfterU;
    }
}
