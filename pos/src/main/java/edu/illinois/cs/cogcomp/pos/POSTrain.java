/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pos.lbjava.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Simple class to build and train models from existing training data, as opposed to using the
 * prepackaged jar.
 *
 * @author James Chen
 * @author Christos Christodoulopoulos
 */
public class POSTrain {
    private static Logger logger = LoggerFactory.getLogger(POSTrain.class);

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
        rm = new POSConfigurator().getDefaultConfig();
        this.init();
    }

    public POSTrain(int iter, String configFile) throws IOException {
        this.iter = iter;
        rm = new POSConfigurator().getConfig(new ResourceManager(configFile));
        this.init();
    }

    /**
     * Known and unknown taggers to be trained later.
     */
    private void init() {
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
        logger.info("Using default training data: " + rm.getString("trainingAndDevData"));
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
        logger.info("Done training, wrote models to disk.");
    }

    public static void main(String[] args) throws Exception{
        POSTrain trainer;
        if(args.length > 0) {
            System.out.printf("Use config file : %s\n", args[0]);
            trainer = new POSTrain(50, args[0]);
        }
        else
            trainer = new POSTrain(50);
        trainer.trainModels();
        trainer.writeModelsToDisk();
    }
}
