/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D29455550F9CF4E4CC17ECFCDCD447EC94C2E2ECC4BCC4D22515134D00B09242B6A28D8EA2408F70BB5A626949615A61BE82404261517A228BE4985954989D529854E398949A930517D1507EC82DCBC648AA0FCF2A4140F0008F278464E7000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class LocalCommaClassifier$$1 extends Classifier {
    private static final POSFeatures __POSFeatures = new POSFeatures();
    private static final ParseFeatures __ParseFeatures = new ParseFeatures();
    private static final BayraktarLabelFeature __BayraktarLabelFeature =
            new BayraktarLabelFeature();
    private static final ChunkFeatures __ChunkFeatures = new ChunkFeatures();
    private static final WordFeatures __WordFeatures = new WordFeatures();

    public LocalCommaClassifier$$1() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "LocalCommaClassifier$$1";
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
                    .println("Classifier 'LocalCommaClassifier$$1(Comma)' defined on line 187 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        FeatureVector __result;
        __result = new FeatureVector();
        __result.addFeatures(__POSFeatures.classify(__example));
        __result.addFeatures(__ParseFeatures.classify(__example));
        __result.addFeature(__BayraktarLabelFeature.featureValue(__example));
        __result.addFeatures(__ChunkFeatures.classify(__example));
        __result.addFeatures(__WordFeatures.classify(__example));
        return __result;
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'LocalCommaClassifier$$1(Comma)' defined on line 187 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "LocalCommaClassifier$$1".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof LocalCommaClassifier$$1;
    }

    public java.util.LinkedList getCompositeChildren() {
        java.util.LinkedList result = new java.util.LinkedList();
        result.add(__POSFeatures);
        result.add(__ParseFeatures);
        result.add(__BayraktarLabelFeature);
        result.add(__ChunkFeatures);
        result.add(__WordFeatures);
        return result;
    }
}
