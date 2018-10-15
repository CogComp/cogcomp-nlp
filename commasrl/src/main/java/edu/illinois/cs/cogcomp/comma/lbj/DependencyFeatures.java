/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D29455550794D284DCB494DCB4EA47B4D4C292D2A4D26D07ECFCDCD445846D450B1D5580ACC4FC82909C7F94D4B218BACCC4D26D150098484E385A155200F3D8F105B5000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class DependencyFeatures extends Classifier {
    private static final RightToLeftDependencies __RightToLeftDependencies =
            new RightToLeftDependencies();
    private static final LeftToRightDependencies __LeftToRightDependencies =
            new LeftToRightDependencies();

    public DependencyFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "DependencyFeatures";
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
                    .println("Classifier 'DependencyFeatures(Comma)' defined on line 176 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        FeatureVector __result;
        __result = new FeatureVector();
        __result.addFeatures(__RightToLeftDependencies.classify(__example));
        __result.addFeatures(__LeftToRightDependencies.classify(__example));
        return __result;
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'DependencyFeatures(Comma)' defined on line 176 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "DependencyFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof DependencyFeatures;
    }

    public java.util.LinkedList getCompositeChildren() {
        java.util.LinkedList result = new java.util.LinkedList();
        result.add(__RightToLeftDependencies);
        result.add(__LeftToRightDependencies);
        return result;
    }
}
