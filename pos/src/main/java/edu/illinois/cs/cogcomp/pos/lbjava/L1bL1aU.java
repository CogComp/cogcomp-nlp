/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// discrete L1bL1aU(Token w) <- labelOneBeforeU && labelOneAfterU

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
 * The classifier conjunction of {@link labelOneBeforeU} and {@link labelOneAfterU}.
 *
 * @author Nick Rizzolo
 **/
public class L1bL1aU extends Classifier {
    private static final labelOneBeforeU left = new labelOneBeforeU();
    private static final labelOneAfterU right = new labelOneAfterU();

    public L1bL1aU() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "L1bL1aU";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete";
    }

    public Feature featureValue(Object __example) {
        if (!(__example instanceof Token)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'L1bL1aU(Token)' defined on line 114 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Feature __result;
        __result = left.featureValue(__example).conjunction(right.featureValue(__example), this);
        return __result;
    }

    public FeatureVector classify(Object __example) {
        return new FeatureVector(featureValue(__example));
    }

    public String discreteValue(Object __example) {
        return featureValue(__example).getStringValue();
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Token[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'L1bL1aU(Token)' defined on line 114 of POSUnknown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "L1bL1aU".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof L1bL1aU;
    }
}
