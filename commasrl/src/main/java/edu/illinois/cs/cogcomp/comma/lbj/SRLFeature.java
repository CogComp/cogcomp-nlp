/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000052C81BA03C03C044F75E6928D4868EC57A350A356A6EB0C8BA60D01B1C256949CFB75E662D1AB77FEDC29A292D503FB6A7254DDA29B1BCAB6442F81EA8F26261D6DA0E184DF24A369CA19337E5AD7D9F08F49A07C951C6CCD28D91EFB8E5877A60077D9735DCA5D6586627766F7276657C6C905612BEFE0DAB150C183E70E03D8F852A000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.*;

import java.util.List;


public class SRLFeature extends Classifier {
    public SRLFeature() {
        containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
        name = "SRLFeature";
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
                    .println("Classifier 'SRLFeature(Comma)' defined on line 66 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Comma c = (Comma) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        List SRLs = c.getContainingSRLs();
        for (int i = 0; i < SRLs.size(); i++) {
            String s = (String) SRLs.get(i);
            __id = "" + (s);
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
                    .println("Classifier 'SRLFeature(Comma)' defined on line 66 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "SRLFeature".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof SRLFeature;
    }
}
