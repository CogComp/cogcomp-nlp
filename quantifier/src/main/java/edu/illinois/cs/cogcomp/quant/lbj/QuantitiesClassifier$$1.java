/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D294555580C2D4CCB29CC29CC4D267EC94C2E2ECC4BCC4D22515134D07ECFCB260A8796A6E5982497E715A86A28D8EA2483091E4999E54989B5CA306E0055594A65490C5C20CF38134821B4A425B82FCD2531B4A4B82518200004A02C61AC7000000

package edu.illinois.cs.cogcomp.quant.lbj;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.quant.features.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QuantitiesClassifier$$1 extends Classifier {
    private static final WordBigrams __WordBigrams = new WordBigrams();
    private static final WordContextBigrams __WordContextBigrams = new WordContextBigrams();
    private static final POSContextBigrams __POSContextBigrams = new POSContextBigrams();
    private static final PatternFeatures __PatternFeatures = new PatternFeatures();

    public QuantitiesClassifier$$1() {
        containingPackage = "edu.illinois.cs.cogcomp.quant.lbj";
        name = "QuantitiesClassifier$$1";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        if (!(__example instanceof Constituent)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'QuantitiesClassifier$$1(Constituent)' defined on line 91 of chunk.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        FeatureVector __result;
        __result = new FeatureVector();
        __result.addFeatures(__WordBigrams.classify(__example));
        __result.addFeatures(__WordContextBigrams.classify(__example));
        __result.addFeatures(__POSContextBigrams.classify(__example));
        __result.addFeatures(__PatternFeatures.classify(__example));
        return __result;
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Constituent[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'QuantitiesClassifier$$1(Constituent)' defined on line 91 of chunk.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "QuantitiesClassifier$$1".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof QuantitiesClassifier$$1;
    }

    public java.util.LinkedList getCompositeChildren() {
        java.util.LinkedList result = new java.util.LinkedList();
        result.add(__WordBigrams);
        result.add(__WordContextBigrams);
        result.add(__POSContextBigrams);
        result.add(__PatternFeatures);
        return result;
    }
}
