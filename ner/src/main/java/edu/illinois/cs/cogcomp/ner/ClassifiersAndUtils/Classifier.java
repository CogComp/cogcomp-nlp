/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;


public abstract class Classifier {
    public String methodName = null;
    public int classesN;

    public abstract double[] getPredictionConfidence(Document doc);

    public abstract int classify(Document doc, double thres);

    public abstract String getExtendedFeatures(Document d);
}
