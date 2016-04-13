package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.POSConfigurator;

/**
 * After {@link POSTaggerKnown} and {@link POSTaggerUnknown} are trained,
 * this classifier will return the prediction of {@link POSTaggerKnown} if
 * the input word was observed during training or of {@link POSTaggerUnknown}
 * if it wasn't.
 *
 * @author Nick Rizzolo
 **/
public class POSTagger extends Classifier {
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String knownModelFile = rm.getString("knownModelPath");
    private static String knownLexFile = rm.getString("knownLexPath");
    private static String unknownModelFile = rm.getString("unknownModelPath");
    private static String unknownLexFile = rm.getString("unknownLexPath");
    private static String baselineModelFile = rm.getString("baselineModelPath");
    private static String baselineLexFile = rm.getString("baselineLexPath");
    private static String mikheevModelFile = rm.getString("mikheevModelPath");
    private static String mikheevLexFile = rm.getString("mikheevLexPath");

    private static MikheevTable mikheevTable = new MikheevTable(mikheevModelFile, mikheevLexFile);
    private static final BaselineTarget baselineTarget = new BaselineTarget(baselineModelFile, baselineLexFile);
    private static final POSTaggerKnown taggerKnown = new POSTaggerKnown(knownModelFile, knownLexFile, baselineTarget);
    private static final POSTaggerUnknown taggerUnknown = new POSTaggerUnknown(unknownModelFile, unknownLexFile, mikheevTable);

    private static final WordForm __wordForm = new WordForm();

    public POSTagger() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "POSTagger";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete";
    }

    public FeatureVector classify(Object __example)
    {
        return new FeatureVector(featureValue(__example));
    }

    public Feature featureValue(Object __example) {
        String result = discreteValue(__example);
        return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result),
                (short) allowableValues().length);
    }

    public String discreteValue(Object __example) {
        Token w = (Token) __example;

        if (baselineTarget.observed(__wordForm.discreteValue(w))) {
            return taggerKnown.discreteValue(w);
        }
        return taggerUnknown.discreteValue(w);
    }

    public int hashCode() {
        return "POSTagger".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof POSTagger;
    }
}

