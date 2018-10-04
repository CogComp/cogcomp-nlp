/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D2C813B02C034148FFACD224241B83B9A351C1D5C1B47801F9960DCB2CBCBC42DFFE682D9E0EBBBFEE59A40125A316C5A2F7EE4E5BA05133EA93B740B81EC8F1E9A2983E43383A8FC507348E329EEAC36746CA3CB751894CA84D60717D268348EFB4C157968ABEC6BFB24C58E8E6A433EA059A4E0B16BF301867C51F19000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class ChunkFeatures extends Classifier {
    public ChunkFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "ChunkFeatures";
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
                    .println("Classifier 'ChunkFeatures(Comma)' defined on line 33 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String[] ngrams = c.getChunkNgrams();
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
                    .println("Classifier 'ChunkFeatures(Comma)' defined on line 33 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "ChunkFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof ChunkFeatures;
    }
}
