/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

/**
 * Returns the <i>form</i> of the word, i.e, the raw text that represents it. The only exceptions
 * are the brackets <code>'('</code>, <code>'['</code>, and <code>'{'</code> which are translated to
 * <code>'-LRB-'</code> and <code>')'</code>, <code>']'</code>, <code>'}'</code> which are
 * translated to <code>'-RRB-'</code>.
 *
 * @author Nick Rizzolo
 **/
public class WordForm extends Classifier {
    public WordForm() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "WordForm";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
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
        Token w = (Token) __example;

        if (w.form.length() == 1) {
            if ("([{".indexOf(w.form.charAt(0)) != -1) {
                return "-LRB-";
            }
            if (")]}".indexOf(w.form.charAt(0)) != -1) {
                return "-RRB-";
            }
        }
        return w.form;
    }

    public int hashCode() {
        return "WordForm".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof WordForm;
    }
}
