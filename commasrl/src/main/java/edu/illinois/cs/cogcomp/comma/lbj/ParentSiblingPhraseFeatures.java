/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6C813B038034148FFACD258409A837367A247C2283A834A9EB6C08E31E5E9351FFB73417CE47077FDD7FA4938092D90D97126DE3D37E4C1BB94C76AB397DD482B9BDABC2E11C2AD33E38E55A023C80E82E79C8B224D194FF80E1F30C85787FA20398519A0ED8B21DE17FA762E8A359AAAAC61B762EC47C634A11718AC64E0B36FF2EEC1BBC7DA000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class ParentSiblingPhraseFeatures extends Classifier {
    public ParentSiblingPhraseFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "ParentSiblingPhraseFeatures";
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
                    .println("Classifier 'ParentSiblingPhraseFeatures(Comma)' defined on line 47 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String[] ngrams = c.getParentSiblingPhraseNgrams();
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
                    .println("Classifier 'ParentSiblingPhraseFeatures(Comma)' defined on line 47 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "ParentSiblingPhraseFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof ParentSiblingPhraseFeatures;
    }
}
