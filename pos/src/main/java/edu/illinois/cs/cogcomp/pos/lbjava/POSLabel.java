package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

/**
 * Simply returns the value of {@link Token#label}.
 *
 * @author Nick Rizzolo
 **/
public class POSLabel extends Classifier {
    public POSLabel() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "POSLabel";
    }

    public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
    public String getOutputType() { return "discrete"; }

    public FeatureVector classify(Object __example)
    {
        return new FeatureVector(featureValue(__example));
    }

    public Feature featureValue(Object __example) {
        String result = discreteValue(__example);
        return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result), (short) allowableValues().length);
    }

    public String discreteValue(Object __example) {
        return ((Token) __example).label;
    }

    public int hashCode() { return "POSLabel".hashCode(); }
    public boolean equals(Object o) { return o instanceof POSLabel; }
}

