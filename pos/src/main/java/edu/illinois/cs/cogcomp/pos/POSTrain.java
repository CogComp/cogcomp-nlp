/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pos.lbjava.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.File;

/**
 * Simple class to build and train models from existing training data, as opposed to using the
 * prepackaged jar.
 *
 * @author James Chen
 * @author Christos Christodoulopoulos
 */
public class POSTrain {
    private static final String NAME = POSTrain.class.getCanonicalName();
    private int iter; // Number of training iterations
    private POSTaggerKnown taggerKnown;
    private POSTaggerUnknown taggerUnknown;
    private MikheevTable mikheevTable;
    private BaselineTarget baselineTarget;
    private ResourceManager rm;

    public POSTrain() {
        this(50);
    }

    public POSTrain(int iter) {
        this.iter = iter;
        this.init();
    }

    /**
     * Known and unknown taggers to be trained later.
     */
    private void init() {
        rm = new POSConfigurator().getDefaultConfig();
        String knownModelFile = rm.getString("knownModelPath");
        String knownLexFile = rm.getString("knownLexPath");
        String unknownModelFile = rm.getString("unknownModelPath");
        String unknownLexFile = rm.getString("unknownLexPath");
        String baselineModelFile = rm.getString("baselineModelPath");
        String baselineLexFile = rm.getString("baselineLexPath");
        String mikheevModelFile = rm.getString("mikheevModelPath");
        String mikheevLexFile = rm.getString("mikheevLexPath");
        baselineTarget = new BaselineTarget(baselineModelFile, baselineLexFile);
        mikheevTable = new MikheevTable(mikheevModelFile, mikheevLexFile);
        taggerKnown = new POSTaggerKnown(knownModelFile, knownLexFile, baselineTarget);
        taggerUnknown = new POSTaggerUnknown(unknownModelFile, unknownLexFile, mikheevTable);
    }

    /**
     * Trains the taggers with the default training data found in POSConfigurator.java
     */
    public void trainModels() {
        System.out.println("Using default training data: " + rm.getString("trainingAndDevData"));
        trainModels(rm.getString("trainingAndDevData"));
    }

    /**
     * Trains the taggers with specified, labeled training data.
     * 
     * @param trainingData The labeled training data
     */
    public void trainModels(String trainingData) {
        // Set up the data
        Parser trainingParser = new POSBracketToToken(trainingData);
        Parser trainingParserUnknown = new POSLabeledUnknownWordParser(trainingData);

        MikheevTable.isTraining = true;
        BaselineTarget.isTraining = true;

        Object ex;
        // baseline and mikheev just count, they don't learn -- so one iteration should be enough
        while ((ex = trainingParser.next()) != null) {
            baselineTarget.learn(ex);
            mikheevTable.learn(ex);
        }

        baselineTarget.doneLearning();
        mikheevTable.doneLearning();
        trainingParser.reset();

        POSTaggerUnknown.isTraining = true;
        POSTaggerKnown.isTraining = true;

        // Run the learner
        for (int i = 0; i < iter; i++) {
            System.out.println("Training round " + i);
            while ((ex = trainingParser.next()) != null) {
                taggerKnown.learn(ex);
            }
            System.out.println("\tFinished training " + rm.getString("knownName"));
            while ((ex = trainingParserUnknown.next()) != null) {
                taggerUnknown.learn(ex);
            }
            System.out.println("\tFinished training " + rm.getString("unknownName"));
            trainingParser.reset();
            trainingParserUnknown.reset();
            taggerKnown.doneWithRound();
            taggerUnknown.doneWithRound();
        }
        taggerUnknown.doneLearning();
        taggerKnown.doneLearning();
    }

    /**
     * Saves the ".lc" and ".lex" models to disk in the modelPath specified by the constructor
     */
    private void writeModelsToDisk() {
        baselineTarget.save();
        mikheevTable.save();
        taggerKnown.save();
        taggerUnknown.save();
        System.out.println("Done training, wrote models to disk.");
    }

    public static void main(String[] args) {
        POSTrain trainer = new POSTrain();
        trainer.trainModels();
        trainer.writeModelsToDisk();
    }
}
