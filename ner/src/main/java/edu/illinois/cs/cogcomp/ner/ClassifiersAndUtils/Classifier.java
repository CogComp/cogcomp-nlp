package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;


public abstract class Classifier {
    public String methodName = null;
    public int classesN;

    public abstract double[] getPredictionConfidence(Document doc);

    public abstract int classify(Document doc, double thres);

    public abstract String getExtendedFeatures(Document d);
}
