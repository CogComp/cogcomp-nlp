/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.jlis;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVector;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;

public class SenseStructure implements IStructure {

    private SenseInstance instance;
    private int label;
    private SenseManager manager;

    public SenseStructure(SenseInstance instance, int label, SenseManager manager) {
        this.instance = instance;
        this.label = label;
        this.manager = manager;
    }


    public FeatureVector getFeatureVector() {
        FeatureVector feats = instance.getCachedFeatureVector();
        return feats.copyWithShift(label * manager.getModelInfo().getLexicon().size());
    }

    public int getLabel() {
        return label;
    }

    public SenseInstance getInstance() {
        return instance;
    }
}
