/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D2C81CA02C030144F756E224241B87E6AE940F8658E1B4F012EA13066B0B9DE94AFFE682D3D0CB7333FA4528092D90FC16CB397D558A89BD293B740B8EFC8F164594C17A91C15C7E28B224B194BE0E1B3036D1EDB80C42654AAAF2EA64F74DBDF2174DF454D436BEB512E2478B92DC8E0A2B293C68DEF0EDEF78DFD8000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class POSFeatures extends Classifier {
    public POSFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "POSFeatures";
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
                    .println("Classifier 'POSFeatures(Comma)' defined on line 26 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String[] ngrams = c.getPOSNgrams();
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
                    .println("Classifier 'POSFeatures(Comma)' defined on line 26 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "POSFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof POSFeatures;
    }
}
