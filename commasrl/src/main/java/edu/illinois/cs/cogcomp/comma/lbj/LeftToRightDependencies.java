/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000057C81BA02C038148F55E611A1A85C9D4D94747257B2D142DB6AF34B9494E7729EBBB1ABB3D1CDD7FDD029D52A2F08B374D75C788F94F6C591606072CC5D53E2BCB1EC0AD32EB87A6290EBBE13B6C48B0C53E9AFF45361361AA290A0920F9C6986775B9991CBE45A8AB635E733346EEB472D3EC0D4F1A5C68DE708766A7061A000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class LeftToRightDependencies extends Classifier {
    public LeftToRightDependencies() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "LeftToRightDependencies";
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
                    .println("Classifier 'LeftToRightDependencies(Comma)' defined on line 54 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String[] ltor = c.getLeftToRightDependencies();
        for (int i = 0; i < ltor.length; i++) {
            __id = "" + (ltor[i]);
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
                    .println("Classifier 'LeftToRightDependencies(Comma)' defined on line 54 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "LeftToRightDependencies".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof LeftToRightDependencies;
    }
}
