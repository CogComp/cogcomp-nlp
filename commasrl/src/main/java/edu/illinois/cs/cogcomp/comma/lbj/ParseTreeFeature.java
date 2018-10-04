/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005A39D4E62C030158FA23875E8CA2285B9EAA257511020A700BC9121B41CE6DE90C20177FA3E424B80A253DE27E7C3FEB736BB0314B742485B2F1077E11F515153E19FB8B3C1418EC061F40768D29736B40A8D00FC0C89403690A0318495DD6A66278E97FACBA0821A071FC5714CABE96984B474A8C83BC354BE45BD9BC17F4BAD74004B4C78199562705BE768841BCBA8A0121E207AAC4D80C72D53D41F3A15570EC69F697ECAD91D349E6DD4E874CAB35D0DBB8B57032193DA8E688F19CC51FFEA836DBC738D9F808DD892BAF53DED7E4C7DD8E82AD47B722EA993B746E36E856CEFB6AE7E753D9CFF19D3E7986CBDEF1CD5E3128E5C58E52300000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class ParseTreeFeature extends Classifier {
    public ParseTreeFeature() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "ParseTreeFeature";
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
                    .println("Classifier 'ParseTreeFeature(Comma)' defined on line 75 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        String tree = "";
        int distance = 0;
        String phrase;
        do {
            phrase = c.getNotation(c.getPhraseToLeftOfParent(distance));
            tree = phrase + tree;
            distance++;
        } while (!phrase.equals("NULL"));

        String commaLevelPhrases = "";
        distance = 0;
        do {
            phrase = c.getNotation(c.getPhraseToLeftOfComma(distance));
            commaLevelPhrases = phrase + commaLevelPhrases;
            distance++;
        } while (!phrase.equals("NULL"));

        distance = 1;
        do {
            phrase = c.getNotation(c.getPhraseToRightOfComma(distance));
            commaLevelPhrases = commaLevelPhrases + phrase;
            distance++;
        } while (!phrase.equals("NULL"));

        tree += "(" + commaLevelPhrases + ")";
        distance = 1;
        do {
            phrase = c.getNotation(c.getPhraseToRightOfParent(distance));
            tree = tree + phrase;
            distance++;
        } while (!phrase.equals("NULL"));

        return "" + (tree);
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'ParseTreeFeature(Comma)' defined on line 75 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "ParseTreeFeature".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof ParseTreeFeature;
    }
}
