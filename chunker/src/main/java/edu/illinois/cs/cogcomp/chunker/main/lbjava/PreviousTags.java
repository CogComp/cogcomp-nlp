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


/**
 * Feature generator that senses the chunk tags of the previous two words. During training, labels
 * are present, so the previous two chunk tags are simply read from the data. Otherwise, the
 * prediction of the {@link Chunker} is used.
 *
 * @author Nick Rizzolo
 **/
public class PreviousTags extends Classifier {
    private static ResourceManager rm = new ChunkerConfigurator().getDefaultConfig();
    private static String modelFile = rm.getString("modelPath");
    private static String modelLexFile = rm.getString("modelLexPath");
    private static final Chunker __Chunker = new Chunker(modelFile, modelLexFile);

    public PreviousTags() {
        containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
        name = "PreviousTags";
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
        for (; w != word; w = (Token) w.next) {
            if (Chunker.isTraining) {
                __id = "" + (i++);
                __value = w.label;
                __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                        this.name, __id, __value, valueIndexOf(__value), (short) 0));
            } else {
                __id = "" + (i++);
                __value = __Chunker.discreteValue(w);
                __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                        this.name, __id, __value, valueIndexOf(__value), (short) 0));
            }
        }
        return __result;
    }

    public int hashCode() {
        return "PreviousTags".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof PreviousTags;
    }
}
