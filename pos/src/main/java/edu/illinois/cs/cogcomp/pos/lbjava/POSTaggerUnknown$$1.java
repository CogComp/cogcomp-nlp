/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.pos.POSConfigurator;

import java.util.LinkedList;

public class POSTaggerUnknown$$1 extends Classifier {
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String baselineModelFile = rm.getString("baselineModelPath");
    private static String baselineLexFile = rm.getString("baselineLexPath");
    private static final BaselineTarget __baselineTarget = new BaselineTarget(baselineModelFile,
            baselineLexFile);
    private static final WordForm __wordForm = new WordForm();
    private static final labelTwoBeforeU __labelTwoBeforeU = new labelTwoBeforeU();
    private static final labelOneBeforeU __labelOneBeforeU = new labelOneBeforeU();
    private static final labelOneAfterU __labelOneAfterU = new labelOneAfterU();
    private static final labelTwoAfterU __labelTwoAfterU = new labelTwoAfterU();
    private static final L2bL1bU __L2bL1bU = new L2bL1bU();
    private static final L1bL1aU __L1bL1aU = new L1bL1aU();
    private static final L1aL2aU __L1aL2aU = new L1aL2aU();
    private static final suffixFeatures __suffixFeatures = new suffixFeatures();

    public POSTaggerUnknown$$1() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "POSTaggerUnknown$$1";
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
        __result.addFeature(__wordForm.featureValue(__example));
        __result.addFeature(__baselineTarget.featureValue(__example));
        __result.addFeature(__labelTwoBeforeU.featureValue(__example));
        __result.addFeature(__labelOneBeforeU.featureValue(__example));
        __result.addFeature(__labelOneAfterU.featureValue(__example));
        __result.addFeature(__labelTwoAfterU.featureValue(__example));
        __result.addFeature(__L2bL1bU.featureValue(__example));
        __result.addFeature(__L1bL1aU.featureValue(__example));
        __result.addFeature(__L1aL2aU.featureValue(__example));
        __result.addFeatures(__suffixFeatures.classify(__example));
        return __result;
    }

    public int hashCode() {
        return "POSTaggerUnknown$$1".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof POSTaggerUnknown$$1;
    }

    public LinkedList getCompositeChildren() {
        LinkedList result = new LinkedList();
        result.add(__wordForm);
        result.add(__baselineTarget);
        result.add(__labelTwoBeforeU);
        result.add(__labelOneBeforeU);
        result.add(__labelOneAfterU);
        result.add(__labelTwoAfterU);
        result.add(__L2bL1bU);
        result.add(__L1bL1aU);
        result.add(__L1aL2aU);
        result.add(__suffixFeatures);
        return result;
    }
}
