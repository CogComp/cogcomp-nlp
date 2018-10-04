/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.lbjava.POSWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Learned classifier that predicts a BIO chunk tag given a word represented as a <code>Token</code>
 * . {@link PreviousTags} from this package and {@link POSWindow} from the <a
 * href="http://l2r.cs.uiuc.edu/~cogcomp/asoftware.php?skey=FLBJPOS">LBJ POS tagger package</a> as
 * well as <code>Forms</code>, <code>Capitalization</code>, <code>WordTypeInformation</code>, and
 * <code>Affixes</code> from the LBJ library are used as features. This classifier caches its
 * prediction in the <code>Token.type</code> field, and it will simply return the value of this
 * field as its prediction if it is non-null.
 *
 * @author Nick Rizzolo
 **/


public class Chunker extends SparseNetworkLearner {
    private static final Logger logger = LoggerFactory.getLogger(Chunker.class);
    public static boolean isTraining;
    private static ResourceManager rm = new ChunkerConfigurator().getDefaultConfig();

    public Chunker()  {
        this(new ChunkerConfigurator().getDefaultConfig());
    }

    public Chunker(ResourceManager config) {
        this(config.getString(ChunkerConfigurator.MODEL_PATH.key), config.getString(ChunkerConfigurator.MODEL_LEX_PATH.key));
    }

    public Chunker(String modelPath, String lexiconPath) {
        this(new Parameters(), modelPath, lexiconPath);
    }

    public Chunker(Parameters p, String modelPath, String lexiconPath) {
        super(p);
        try {
            lcFilePath = new java.net.URL("file:" + modelPath);
            lexFilePath = new java.net.URL("file:" + lexiconPath);
        } catch (Exception e) {
            logger.error("ERROR: Can't create model or lexicon URL: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        if (new java.io.File(modelPath).exists()) {
            readModel(lcFilePath);
            readLexiconOnDemand(lexFilePath);
        } else if (IOUtilities.existsInClasspath(Chunker.class, modelPath)) {
            readModel(IOUtilities.loadFromClasspath(Chunker.class, modelPath));
            readLexiconOnDemand(IOUtilities.loadFromClasspath(Chunker.class, lexiconPath));
        }
        containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
        name = "Chunker";
        setLabeler(new ChunkLabel());
        setExtractor(new Chunker$$1());
    }



    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete";
    }

    private Feature cachedFeatureValue(Object __example) {
        Token word = (Token) __example;
        String __cachedValue = word.type;

        if (__cachedValue != null) {
            return new DiscretePrimitiveStringFeature(containingPackage, name, "", __cachedValue,
                    valueIndexOf(__cachedValue), (short) allowableValues().length);
        }

        Feature __result;
        __result = super.featureValue(__example);
        word.type = __result.getStringValue();
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
        return "Chunker".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof Chunker;
    }

    public static class Parameters extends SparseNetworkLearner.Parameters {
        Parameters() {
            SparseAveragedPerceptron.Parameters p = new SparseAveragedPerceptron.Parameters();
            p.learningRate = .1;
            p.thickness = 2;
            baseLTU = new SparseAveragedPerceptron(p);
        }
    }
}
