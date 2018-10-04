/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.infer.InferenceManager;
import org.cogcomp.re.LbjGen.constrained_RE;
import org.cogcomp.re.LbjGen.relation_classifier;

/*
 * This Classifier is a wrapper for testing purposes used by org.cogcomp.re.ACERelationTester
 * It accepts a trained classifier so that the tester does not rely on lbjava:compile
 */
public class ACERelationConstrainedClassifier  extends Classifier {
    private static relation_classifier __relation_classifier = new relation_classifier();

    public ACERelationConstrainedClassifier(relation_classifier input)
    {
        containingPackage = "org.cogcomp.re";
        name = "ACERelationConstrainedClassifier";
        __relation_classifier = input;
    }

    public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
    public String getOutputType() { return "discrete"; }


    public FeatureVector classify(Object __example)
    {
        return new FeatureVector(featureValue(__example));
    }

    public Feature featureValue(Object __example)
    {
        String result = discreteValue(__example);
        return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result), (short) allowableValues().length);
    }

    public String discreteValue(Object __example)
    {
        if (!(__example instanceof Relation))
        {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err.println("Classifier 'ACERelationConstrainedClassifier(Relation)' received '" + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Relation head = constrained_RE.findHead((Relation) __example);
        constrained_RE inference = (constrained_RE) InferenceManager.get("org.cogcomp.re.constrained_RE", head);

        if (inference == null)
        {
            inference = new constrained_RE(head);
            InferenceManager.put(inference);
        }

        String result = null;

        try { result = inference.valueOf(__relation_classifier, __example); }
        catch (Exception e)
        {
            System.err.println("LBJava ERROR: Fatal error while evaluating classifier ACERelationConstrainedClassifier: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        return result;
    }

    public FeatureVector[] classify(Object[] examples)
    {
        if (!(examples instanceof Relation[]))
        {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err.println("Classifier 'ACERelationConstrainedClassifier(Relation)' received '" + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() { return "ACERelationConstrainedClassifier".hashCode(); }
    public boolean equals(Object o) { return o instanceof ACERelationConstrainedClassifier; }

}
