/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000055C813B02C034148FFACD224241B83B9A390E82247C2D126C76A1862587FE5729EF77384717A38BBFEBB75290C4A4704F9E937A21F131B71AB197D5994CC579C9D32854774C70DB27546811522BFC28B024B194FF4DBFF6236D1EDB03C4A2A8451C39BA1DDE26B33598A35DAA96C6DF51A224BF634A117682FA4E0B16BF2D56A9FED1A000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class SiblingPhraseFeatures extends Classifier {
    public SiblingPhraseFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "SiblingPhraseFeatures";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.Comma";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        if (!(__example instanceof Comma)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'SiblingPhraseFeatures(Comma)' defined on line 40 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String[] ngrams = c.getSiblingPhraseNgrams();
        for (int i = 0; i < ngrams.length; i++) {
            __id = "" + (ngrams[i]);
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        return __result;
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'SiblingPhraseFeatures(Comma)' defined on line 40 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "SiblingPhraseFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof SiblingPhraseFeatures;
    }
}
