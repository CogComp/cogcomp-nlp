/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059191CE028030144F7565E452443A7EAE9C4C311532AF105BC20D40A5D671D381EFDDA0A4C40E0A7CEB96766ABB9A272D2212CAD4559844D9B45A377B14145B546D240912C27A0F0848CA7114EB06C873C27F5851E0D1CA02808F74EB4B09382D409A27424B44F736EC1253ED4A39D3093BC196B6840923A95B7AECFE8626CC86795B1ECE361168C7439FDE71D054ED7C74117860EE58A2118D4AB7133CB6D2A47C28DE92E83879BF759D5CF156F0A2F2E7E6B34615F30BFFADAF5A5D65F8DFAB9728B0CE9CD4D100000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;


public class CommaSiblingsFeature extends Classifier {
    public CommaSiblingsFeature() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "CommaSiblingsFeature";
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
                    .println("Classifier 'CommaSiblingsFeature(Comma)' defined on line 149 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        String commaLevelPhrases = "";
        String phrase;
        int distance = 0;
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

        return "" + (commaLevelPhrases);
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Comma[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'CommaSiblingsFeature(Comma)' defined on line 149 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "CommaSiblingsFeature".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof CommaSiblingsFeature;
    }
}
