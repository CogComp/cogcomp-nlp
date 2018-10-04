/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D2C813B02C034148FFACD224241B83B9A390E8E2D1C1A47809E3360CCB0CBCBE42DFFE649E470FD7777B4AA14849E0874195E64E5751AA6EA5276F80613C11F1C8A2983E43383A8FC517148E329EF617FF3136D1E9540621B2253F727D268DBFDFB983AEBA1AEA3BDEEA2175ADDD4966C91A2B293C68DEB0554606A5F8000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class WordFeatures extends Classifier {
    public WordFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "WordFeatures";
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
                    .println("Classifier 'WordFeatures(Comma)' defined on line 19 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String[] ngrams = c.getWordNgrams();
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
                    .println("Classifier 'WordFeatures(Comma)' defined on line 19 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "WordFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof WordFeatures;
    }
}
