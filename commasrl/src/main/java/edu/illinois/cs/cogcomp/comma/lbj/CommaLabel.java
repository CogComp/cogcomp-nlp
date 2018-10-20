/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945507ECFCDCD44F94C4A4DC1D003351295351C64751AA51082B5A5497A09CA79E9A520197D4B658A500118B557683000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class CommaLabel extends Classifier {
    public CommaLabel() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "CommaLabel";
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
                    .println("Classifier 'CommaLabel(Comma)' defined on line 181 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        return "" + (c.getLabel());
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'CommaLabel(Comma)' defined on line 181 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "CommaLabel".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof CommaLabel;
    }
}
