/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.pos.MikheevLearner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Learned with {@link MikheevLearner}, this classifier records statistics on the POS tags that
 * suffixes of lengths 3 and 4 are associated with. Its {@link #classify(Object)} method does not
 * return any features; all interaction is performed through the {@link #allowableTags(Word)}
 * method. Only {@link WordForm} is used as a feature.
 *
 * @author Nick Rizzolo
 **/
public class MikheevTable extends MikheevLearner {
    private static Logger logger = LoggerFactory.getLogger(MikheevTable.class);

    public static boolean isTraining;

    public MikheevTable(String modelPath, String lexiconPath) {
        this(new Parameters(), modelPath, lexiconPath);
    }

    private MikheevTable(Parameters p, String modelPath, String lexiconPath) {
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
        } else if (IOUtilities.existsInClasspath(MikheevTable.class, modelPath)) {
            readModel(IOUtilities.loadFromClasspath(MikheevTable.class, modelPath));
            readLexiconOnDemand(IOUtilities.loadFromClasspath(MikheevTable.class, lexiconPath));
        }
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "MikheevTable";
        setLabeler(new POSLabel());
        setExtractor(new WordForm());
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete";
    }

    public FeatureVector classify(Object __example) {
        return new FeatureVector(featureValue(__example));
    }

    public String discreteValue(Object __example) {
        return featureValue(__example).getStringValue();
    }

    public int hashCode() {
        return "MikheevTable".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof MikheevTable;
    }
}
