/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;


public class SOPrevious extends Classifier {
    private static ResourceManager rm = new ChunkerConfigurator().getDefaultConfig();
    private static String modelFile = rm.getString("modelPath");
    private static String modelLexFile = rm.getString("modelLexPath");
    private static final Chunker __Chunker = new Chunker(modelFile, modelLexFile);
    private static final POSTagger __POSTagger = new POSTagger();

    public SOPrevious() {
        containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
        name = "SOPrevious";
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

        int i;
        Token w = word;
        for (i = 0; i > -2 && w.previous != null; --i) {
            w = (Token) w.previous;
        }
        String[] tags = new String[3];
        String[] labels = new String[2];
        i = 0;
        for (; w != word; w = (Token) w.next) {
            tags[i] = __POSTagger.discreteValue(w);
            if (Chunker.isTraining) {
                labels[i] = w.label;
            } else {
                labels[i] = __Chunker.discreteValue(w);
            }
            i++;
        }
        tags[i] = __POSTagger.discreteValue(w);
        __id = "ll";
        __value = "" + (labels[0] + "_" + labels[1]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name,
                __id, __value, valueIndexOf(__value), (short) 0));
        __id = "lt1";
        __value = "" + (labels[0] + "_" + tags[1]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name,
                __id, __value, valueIndexOf(__value), (short) 0));
        __id = "lt2";
        __value = "" + (labels[1] + "_" + tags[2]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name,
                __id, __value, valueIndexOf(__value), (short) 0));
        return __result;
    }

    public int hashCode() {
        return "SOPrevious".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof SOPrevious;
    }
}
