/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000055C813F028034148FFACD26243442EC6172D1D949D803892F82F218752DE3723CF77B6737AB4EEEEBF64E4E2292D10F06F3B671EE439ED86329194C135AAEA16D5F50760DE11F1C3532B8FE7044D0B0E207D872DF13D58228F34D85C4122A261507E3F9C6E86B0ADC242E57EC54D5B9CED44298AC2D3F083343EB9C267CEF5AD25F3B31A000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class RightToLeftDependencies extends Classifier {
    public RightToLeftDependencies() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "RightToLeftDependencies";
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
                    .println("Classifier 'RightToLeftDependencies(Comma)' defined on line 59 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String[] rtol = c.getLeftToRightDependencies();
        for (int i = 0; i < rtol.length; i++) {
            __id = "" + (rtol[i]);
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
                    .println("Classifier 'RightToLeftDependencies(Comma)' defined on line 59 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "RightToLeftDependencies".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof RightToLeftDependencies;
    }
}
