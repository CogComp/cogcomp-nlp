/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945580DCBCC4F2A4CCD0ACC4FC8217B4D4C292D2A45D07ECFCDCD445846D450B1D558A6500A2B2D2AC35846DB4F4D290FCF2A4909C70B26D034D4B658A5000A43FB0B184000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class UnigramRightFeature extends Classifier {
    public UnigramRightFeature() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "UnigramRightFeature";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.Comma";
    }

    public String getOutputType() {
        return "discrete";
    }


    public FeatureVector classify(Object __example) {
        return new FeatureVector(featureValue(__example));
    }

    public Feature featureValue(Object __example) {
        String result = discreteValue(__example);
        return new DiscretePrimitiveStringFeature(containingPackage, name, "", result,
                valueIndexOf(result), (short) allowableValues().length);
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof Comma)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'UnigramRightFeature(Comma)' defined on line 14 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        return "" + (c.getWordToRight(1));
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'UnigramRightFeature(Comma)' defined on line 14 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "UnigramRightFeature".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof UnigramRightFeature;
    }
}
