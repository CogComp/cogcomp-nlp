/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.Affixes;
import edu.illinois.cs.cogcomp.lbjava.nlp.Capitalization;
import edu.illinois.cs.cogcomp.lbjava.nlp.Forms;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordTypeInformation;
import edu.illinois.cs.cogcomp.pos.lbjava.POSWindow;

import java.util.LinkedList;


public class Chunker$$1 extends Classifier {
    private static final Forms __Forms = new Forms();
    private static final Capitalization __Capitalization = new Capitalization();
    private static final WordTypeInformation __WordTypeInformation = new WordTypeInformation();
    private static final Affixes __Affixes = new Affixes();
    private static final PreviousTags __PreviousTags = new PreviousTags();
    private static final POSWindow __POSWindow = new POSWindow();
    private static final Mixed __Mixed = new Mixed();
    private static final POSWindowpp __POSWindowpp = new POSWindowpp();
    private static final Formpp __Formpp = new Formpp();
    private static final SOPrevious __SOPrevious = new SOPrevious();

    public Chunker$$1() {
        containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
        name = "Chunker$$1";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        FeatureVector __result;
        __result = new FeatureVector();
        __result.addFeatures(__Forms.classify(__example));
        __result.addFeatures(__Capitalization.classify(__example));
        __result.addFeatures(__WordTypeInformation.classify(__example));
        __result.addFeatures(__Affixes.classify(__example));
        __result.addFeatures(__PreviousTags.classify(__example));
        __result.addFeatures(__POSWindow.classify(__example));
        __result.addFeatures(__Mixed.classify(__example));
        __result.addFeatures(__POSWindowpp.classify(__example));
        __result.addFeatures(__Formpp.classify(__example));
        __result.addFeatures(__SOPrevious.classify(__example));
        return __result;
    }

    public int hashCode() {
        return "Chunker$$1".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof Chunker$$1;
    }

    public LinkedList getCompositeChildren() {
        LinkedList<Classifier> result = new LinkedList<>();
        result.add(__Forms);
        result.add(__Capitalization);
        result.add(__WordTypeInformation);
        result.add(__Affixes);
        result.add(__PreviousTags);
        result.add(__POSWindow);
        result.add(__Mixed);
        result.add(__POSWindowpp);
        result.add(__Formpp);
        result.add(__SOPrevious);
        return result;
    }
}
