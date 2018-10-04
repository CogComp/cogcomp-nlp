/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.infer.InferenceManager;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLClassifier;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLConfigurator;
import edu.illinois.cs.cogcomp.prepsrl.inference.constraints.LegalRoles;
import edu.illinois.cs.cogcomp.prepsrl.inference.constraints.PrepSRLInferenceConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An LBJava {@link Classifier} that creates an ILP inference using the constraints defined in
 * {@link PrepSRLInferenceConstraints} (right now only the {@link LegalRoles} constraint).
 */
public class ConstrainedPrepSRLClassifier extends Classifier {
    private final Logger logger = LoggerFactory.getLogger(ConstrainedPrepSRLClassifier.class);
    private static PrepSRLClassifier prepSRLClassifier;

    public ConstrainedPrepSRLClassifier() {
        this(PrepSRLConfigurator.defaults());
    }

    public ConstrainedPrepSRLClassifier(ResourceManager rm) {
        containingPackage = "edu.illinois.cs.cogcomp.esrl.prepsrl";
        name = "ConstrainedPrepSRLClassifier";
        String modelsDir = rm.getString(PrepSRLConfigurator.MODELS_DIR);
        String modelName = modelsDir + "/" + PrepSRLClassifier.CLASS_NAME;
        prepSRLClassifier = new PrepSRLClassifier(modelName + ".lc", modelName + ".lex");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent";
    }

    public String getOutputType() {
        return "discrete";
    }


    public FeatureVector classify(Object example) {
        return new FeatureVector(featureValue(example));
    }

    public Feature featureValue(Object example) {
        String result = discreteValue(example);
        return new DiscretePrimitiveStringFeature(containingPackage, name, "", result,
                valueIndexOf(result), (short) allowableValues().length);
    }

    public String discreteValue(Object example) {
        Constituent head = PrepSRLInference.findHead((Constituent) example);
        PrepSRLInference inference =
                (PrepSRLInference) InferenceManager.get(
                        "edu.illinois.cs.cogcomp.esrl.prepsrl.inference.PrepSRLInference", head);

        if (inference == null) {
            inference = new PrepSRLInference(head);
            InferenceManager.put(inference);
        }

        String result;

        try {
            result = inference.valueOf(prepSRLClassifier, example);
        } catch (AssertionError | Exception e) {
            logger.error
                    ("LBJava ERROR: Fatal error while evaluating classifier ConstrainedPrepSRLClassifier: "
                            + e);
            logger.error("Returning local classifier prediction.");
            result = prepSRLClassifier.discreteValue(example);
        }

        return result;
    }

    public int hashCode() {
        return "ConstrainedPrepSRLClassifier".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof ConstrainedPrepSRLClassifier;
    }
}
