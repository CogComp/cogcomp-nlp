package edu.illinois.cs.cogcomp.pos;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pos.lbjava.*;
import org.junit.Before;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * This POS Tagger uses a pre-trained model. The model files will be found by checking two locations
 * in order:
 * <ul>
 * <li>First, the directory specified in the constant Constants.modelPath
 * <li>If the files are not found in this directory, the classpath will be checked (this will result
 * in loading the files from the maven repository
 * </ul>
 *
 * This class acts as a wrapper around the POS Tagging classes defined in the LBJava code.
 * 
 * @author Colin Graber
 */
public class TrainedPOSTagger {

    private POSTaggerKnown known;
    private POSTaggerUnknown unknown;
    private wordForm wordForm;

    /**
     * Initializes a tagger from either a pre-specified directory or the classpath
     */
    public TrainedPOSTagger() {
        ResourceManager rm = new POSConfigurator().getDefaultConfig();
        URL knownModelFile = null;
        URL knownLexFile = null;
        URL unknownModelFile = null;
        URL unknownLexFile = null;
        URL baselineModelFile = null;
        URL mikheevModelFile = null;
        try {
            if ((new File(rm.getString("knownModelPath"))).exists()) {
                knownModelFile = (new File(rm.getString("knownModelPath"))).toURL();
            } else {
                knownModelFile =
                        IOUtilities.loadFromClasspath(TrainedPOSTagger.class,
                                rm.getString("knownModelPath"));
            }
            if ((new File(rm.getString("knownLexPath"))).exists()) {
                knownLexFile = (new File(rm.getString("knownLexPath"))).toURL();
            } else {
                knownLexFile =
                        IOUtilities.loadFromClasspath(TrainedPOSTagger.class,
                                rm.getString("knownLexPath"));
            }
            if ((new File(rm.getString("unknownModelPath"))).exists()) {
                unknownModelFile = (new File(rm.getString("unknownModelPath"))).toURL();
            } else {
                unknownModelFile =
                        IOUtilities.loadFromClasspath(TrainedPOSTagger.class,
                                rm.getString("unknownModelPath"));
            }
            if ((new File(rm.getString("unknownLexPath"))).exists()) {
                unknownLexFile = (new File(rm.getString("unknownLexPath"))).toURL();
            } else {
                unknownLexFile =
                        IOUtilities.loadFromClasspath(TrainedPOSTagger.class,
                                rm.getString("unknownLexPath"));
            }
            if ((new File(rm.getString("baselineModelPath"))).exists()) {
                baselineModelFile = (new File(rm.getString("baselineModelPath"))).toURL();
            } else {
                baselineModelFile =
                        IOUtilities.loadFromClasspath(TrainedPOSTagger.class,
                                rm.getString("baselineModelPath"));
            }
            if ((new File(rm.getString("mikheevModelPath"))).exists()) {
                mikheevModelFile = (new File(rm.getString("mikheevModelPath"))).toURL();
            } else {
                mikheevModelFile =
                        IOUtilities.loadFromClasspath(TrainedPOSTagger.class,
                                rm.getString("mikheevModelPath"));
            }
        } catch (MalformedURLException e) {
            System.out.println("ERROR: MALRFORMED URL (THIS SHOULD NEVER HAPPEN)");
            System.exit(1);
        }
        baselineTarget.getInstance().readModel(baselineModelFile);
        MikheevTable.getInstance().readModel(mikheevModelFile);
        known = POSTaggerKnown.getInstance();
        known.readModel(knownModelFile);
        known.readLexicon(knownLexFile);
        unknown = POSTaggerUnknown.getInstance();
        unknown.readModel(unknownModelFile);
        unknown.readLexicon(unknownLexFile);

        wordForm = new wordForm();
    }

    /**
     * Finds the correct POS tag for the provided token
     *
     * @param w The Token whose POS tag is being sought
     * @return A string representing the POS tag for the token
     */
    public String discreteValue(Token w) {
        if (baselineTarget.getInstance().observed(wordForm.discreteValue(w))) {
            return known.discreteValue(w);
        }
        return unknown.discreteValue(w);
    }
}
