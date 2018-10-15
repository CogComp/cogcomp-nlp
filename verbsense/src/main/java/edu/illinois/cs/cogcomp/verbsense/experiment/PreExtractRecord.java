/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.experiment;

import edu.illinois.cs.cogcomp.sl.util.FeatureVector;

class PreExtractRecord {
    String lemma;
    int label;
    FeatureVector features;

    public PreExtractRecord(String lemma, int label, FeatureVector features) {
        this.lemma = lemma;
        this.label = label;
        this.features = features;
    }

}
