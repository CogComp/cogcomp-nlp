/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945580C2D4CCB29CC29CC4D26F94C4A4DC1D07ECFCB2602FB4353FA441AC3FB8254351C64751AA5108A6B4B82F0C22A79E9A52015BA96DA05B0061C8AAEE94000000

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


public class QuantitiesLabel extends Classifier {
    public QuantitiesLabel() {
        containingPackage = "edu.illinois.cs.cogcomp.quant.lbj";
        name = "QuantitiesLabel";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent";
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
        if (!(__example instanceof Constituent)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'QuantitiesLabel(Constituent)' defined on line 8 of chunk.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Constituent word = (Constituent) __example;

        return "" + (word.getLabel());
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Constituent[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'QuantitiesLabel(Constituent)' defined on line 8 of chunk.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "QuantitiesLabel".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof QuantitiesLabel;
    }
}
