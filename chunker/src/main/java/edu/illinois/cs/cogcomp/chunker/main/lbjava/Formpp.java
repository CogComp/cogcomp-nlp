/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;


public class Formpp extends Classifier {
    public Formpp() {
        containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
        name = "Formpp";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        Token word = (Token) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        int before = 2;
        int after = 2;
        int k = 2;
        int i;
        Token w = word, last = word;
        for (i = 0; i <= after && last != null; ++i) {
            last = (Token) last.next;
        }
        for (i = 0; i > -before && w.previous != null; --i) {
            w = (Token) w.previous;
        }
        String[] forms = new String[before + after + 1];
        i = 0;
        for (; w != last; w = (Token) w.next) {
            forms[i++] = word.form;
        }
        for (int j = 0; j < k; j++) {
            for (i = 0; i < forms.length; i++) {
                StringBuilder f = new StringBuilder();
                for (int context = 0; context <= j && i + context < forms.length; context++) {
                    if (context != 0) {
                        f.append("_");
                    }
                    f.append(forms[i + context]);
                }
                __id = "" + (i + "_" + j);
                __value = "" + (f.toString());
                __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                        this.name, __id, __value, valueIndexOf(__value), (short) 0));
            }
        }
        return __result;
    }

    public int hashCode() {
        return "Formpp".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof Formpp;
    }
}
