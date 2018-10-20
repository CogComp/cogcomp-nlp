/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicting the part of speech of the given word, this classifier is intended to be applied only
 * on words whose forms were <i>not</i> observed during training. {@link WordForm},
 * {@link BaselineTarget}, {@link labelTwoBeforeU}, {@link labelOneBeforeU}, {@link labelOneAfterU},
 * {@link labelTwoAfterU}, {@link L2bL1bU}, {@link L1bL1aU}, {@link L1aL2aU} and
 * {@link suffixFeatures} are all used as features. At test-time, the learning algorithm is
 * restricted to return a prediction from the set of tags returned by the Mikheev rules (see
 * {@link MikheevTable}), which were learned at training-time. The prediction is cached in the
 * <code>Word.partOfSpeech</code> field, and the value of that field will simply be returned
 * whenever it is non-<code>null</code>.
 *
 * @author Nick Rizzolo
 **/
public class POSTaggerUnknown extends SparseNetworkLearner {
    private static Logger logger = LoggerFactory.getLogger(POSTaggerUnknown.class);

    private final MikheevTable __MikheevTable;

    public static boolean isTraining;

    public POSTaggerUnknown(String modelPath, String lexiconPath, MikheevTable mikheevTable) {
        this(new Parameters(), modelPath, lexiconPath, mikheevTable);
    }

    private POSTaggerUnknown(Parameters p, String modelPath, String lexiconPath,
            MikheevTable mikheevTable) {
        super(p);
        try {
            lcFilePath = new java.net.URL("file:" + modelPath);
            lexFilePath = new java.net.URL("file:" + lexiconPath);
        } catch (Exception e) {
            System.err.println("ERROR: Can't create model or lexicon URL: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        if (new java.io.File(modelPath).exists()) {
            readModel(lcFilePath);
            readLexiconOnDemand(lexFilePath);
        } else if (IOUtilities.existsInClasspath(POSTaggerUnknown.class, modelPath)) {
            readModel(IOUtilities.loadFromClasspath(POSTaggerUnknown.class, modelPath));
            readLexiconOnDemand(IOUtilities.loadFromClasspath(POSTaggerUnknown.class, lexiconPath));
        }
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "POSTaggerUnknown";
        setLabeler(new POSLabel());
        setExtractor(new POSTaggerUnknown$$1());
        this.__MikheevTable = mikheevTable;

    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete";
    }

    private Feature cachedFeatureValue(Object __example) {
        Token w = (Token) __example;
        String __cachedValue = w.partOfSpeech;

        if (__cachedValue != null) {
            return new DiscretePrimitiveStringFeature(containingPackage, name, "", __cachedValue,
                    valueIndexOf(__cachedValue), (short) allowableValues().length);
        }

        Feature __result;
        __result = valueOf(w, __MikheevTable.allowableTags(w));
        w.partOfSpeech = __result.getStringValue();
        return __result;
    }

    public FeatureVector classify(Object __example) {
        if (__example instanceof Object[]) {
            Object[] a = (Object[]) __example;
            if (a[0] instanceof int[])
                return super.classify((int[]) a[0], (double[]) a[1]);
        }
        return new FeatureVector(cachedFeatureValue(__example));
    }

    public Feature featureValue(Object __example) {
        if (__example instanceof Object[]) {
            Object[] a = (Object[]) __example;
            if (a[0] instanceof int[])
                return super.featureValue((int[]) a[0], (double[]) a[1]);
        }

        return cachedFeatureValue(__example);
    }

    public String discreteValue(Object __example) {
        if (__example instanceof Object[]) {
            Object[] a = (Object[]) __example;
            if (a[0] instanceof int[])
                return super.discreteValue((int[]) a[0], (double[]) a[1]);
        }

        return cachedFeatureValue(__example).getStringValue();
    }

    public int hashCode() {
        return "POSTaggerUnknown".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof POSTaggerUnknown;
    }

    public static class Parameters extends SparseNetworkLearner.Parameters {
        Parameters() {
            SparseAveragedPerceptron.Parameters p = new SparseAveragedPerceptron.Parameters();
            p.learningRate = .1;
            p.thickness = 4;
            baseLTU = new SparseAveragedPerceptron(p);
        }
    }
}
