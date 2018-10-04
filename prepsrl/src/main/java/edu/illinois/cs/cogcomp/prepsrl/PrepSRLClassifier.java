/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.prepsrl.features.POSBigrams;
import edu.illinois.cs.cogcomp.prepsrl.features.POSContextBigrams;
import edu.illinois.cs.cogcomp.prepsrl.features.WordBigrams;
import edu.illinois.cs.cogcomp.prepsrl.features.WordContextBigrams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static edu.illinois.cs.cogcomp.prepsrl.features.PrepSRLFeatures.*;

public class PrepSRLClassifier extends SparseNetworkLearner {
    private static final Logger logger = LoggerFactory.getLogger(PrepSRLClassifier.class);
    private static final String PACKAGE_NAME = "edu.illinois.cs.cogcomp.esrl.prepsrl";
    public static final String CLASS_NAME = "PrepSRLClassifier";

    public static boolean isTraining;

    public PrepSRLClassifier(String modelPath, String lexiconPath) {
        super(PACKAGE_NAME + "." + CLASS_NAME, new Parameters());
        try {
            lcFilePath = new URL("file:" + modelPath);
            lexFilePath = new URL("file:" + lexiconPath);
        } catch (MalformedURLException e) {
            logger.error("Cannot create model/lexicon URLs (check path definition)");
            System.exit(-1);
        }
        if (new File(modelPath).exists()) {
            logger.info("Reading model from " + modelPath);
            readModel(modelPath);
            logger.info("Reading lexicon from " + lexiconPath);
            readLexiconOnDemand(lexiconPath);
        } else if (IOUtilities.existsInClasspath(PrepSRLClassifier.class, modelPath)) {
            logger.info("Model file " + modelPath + " located in a jar file");
            readModel(IOUtilities.loadFromClasspath(PrepSRLClassifier.class, modelPath));
            readLexiconOnDemand(IOUtilities.loadFromClasspath(PrepSRLClassifier.class, lexiconPath));
        } else {
            logger.info("Creating new model/lexicon");
            containingPackage = PACKAGE_NAME;
            name = CLASS_NAME;
            setLabeler(new Label());
            setExtractor(new FeatureExtractor());
        }
    }

    protected static class Parameters extends SparseNetworkLearner.Parameters {
        public Parameters() {
            SparseAveragedPerceptron.Parameters p = new SparseAveragedPerceptron.Parameters();
            p.learningRate = .1;
            p.thickness = 2;
            baseLTU = new SparseAveragedPerceptron(p);
        }
    }

    public static class FeatureExtractor extends Classifier {
        protected Classifier[] featureSet;

        public FeatureExtractor() {
            containingPackage = PACKAGE_NAME;
            name = CLASS_NAME + "$FeatureExtractor";
            featureSet =
                    new Classifier[] {new WordContextBigrams(), new POSContextBigrams(),
                            new WordBigrams(), new POSBigrams(), govFeatures, objFeatures,
                            prevWordFeatures, prevVerbFeatures};
        }

        public FeatureVector classify(Object example) {
            FeatureVector result = new FeatureVector();
            for (Classifier featExtractor : featureSet)
                result.addFeatures(featExtractor.classify(example));
            return result;
        }
    }

    public static class Label extends Classifier {
        public Label() {
            containingPackage = PACKAGE_NAME;
            name = CLASS_NAME + "$Label";
        }

        public String discreteValue(Object example) {
            return ((Constituent) example).getLabel();
        }

        public FeatureVector classify(Object example) {
            return new FeatureVector(featureValue(example));
        }

        public Feature featureValue(Object example) {
            String result = discreteValue(example);
            return new DiscretePrimitiveStringFeature(containingPackage, name, "", result,
                    valueIndexOf(result), (short) allowableValues().length);
        }
    }
}
