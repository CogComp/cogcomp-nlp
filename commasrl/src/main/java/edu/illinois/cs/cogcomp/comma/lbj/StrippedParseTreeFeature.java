/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DA3914E63C020154FA23165858A15BB6AD555AEACA43A6393002C31B19C10716C9C22ACDD381BD963A4A2536579C0F99FFEF005890AD3212C72973D4385C259F08B2F88F68A8A5F8CF5DD67BA04760FCF0078423B5205410CB003621C85282C402565775A7493A6A9ACBA0821A071FC5FB88BFA7E522D866B074A8C83BC35579943B2793E68ED731140D21F1B57699C1D578E522DA5E9DC58090748D756A640E3BE5437CFA655D183B5CA3FC957D36083DD5AAC177857FEA168437994622A7A19D509FD2BB37C8BADCB733DF08E7A900DF16ACAEFC4D7D531FD73D77157ABD4117CEC9DDA124CA1BC8DF758BFDF535F8131334CFA3DA7B3C34C3E900A5C155F7D4300000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class StrippedParseTreeFeature extends Classifier {
    public StrippedParseTreeFeature() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "StrippedParseTreeFeature";
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
                    .println("Classifier 'StrippedParseTreeFeature(Comma)' defined on line 112 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        String tree = "";
        int distance = 0;
        String phrase;
        do {
            phrase = c.getStrippedNotation(c.getPhraseToLeftOfParent(distance));
            tree = phrase + tree;
            distance++;
        } while (!phrase.equals("NULL"));

        String commaLevelPhrases = "";
        distance = 0;
        do {
            phrase = c.getStrippedNotation(c.getPhraseToLeftOfComma(distance));
            commaLevelPhrases = phrase + commaLevelPhrases;
            distance++;
        } while (!phrase.equals("NULL"));

        distance = 1;
        do {
            phrase = c.getStrippedNotation(c.getPhraseToRightOfComma(distance));
            commaLevelPhrases = commaLevelPhrases + phrase;
            distance++;
        } while (!phrase.equals("NULL"));

        tree += "(" + commaLevelPhrases + ")";
        distance = 1;
        do {
            phrase = c.getStrippedNotation(c.getPhraseToRightOfParent(distance));
            tree = tree + phrase;
            distance++;
        } while (!phrase.equals("NULL"));

        return "" + (tree);
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'StrippedParseTreeFeature(Comma)' defined on line 112 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "StrippedParseTreeFeature".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof StrippedParseTreeFeature;
    }
}
