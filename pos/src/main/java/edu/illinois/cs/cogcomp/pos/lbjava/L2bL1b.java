/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// discrete L2bL1b(Token w) <- labelTwoBefore && labelOneBefore

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
 * The classifier conjunction of {@link labelTwoBefore} and {@link labelOneBefore}.
 *
 * @author Nick Rizzolo
 **/
public class L2bL1b extends Classifier {
    private static final labelTwoBefore left = new labelTwoBefore();
    private static final labelOneBefore right = new labelOneBefore();

    public L2bL1b() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "L2bL1b";
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
                    .println("Classifier 'L2bL1b(Token)' defined on line 131 of POSKnown.lbj received '"
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
                    .println("Classifier 'L2bL1b(Token)' defined on line 131 of POSKnown.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "L2bL1b".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof L2bL1b;
    }
}
