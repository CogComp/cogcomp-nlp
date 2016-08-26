/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.chunker.main;


import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Trains chunker models with user specified labeled data in the CoNLL2000 format. Similar to
 * POSTrain.java.
 *
 * @author James Chen
 */
public class ChunkerTrain {
    private int iter; // Number of iterations to be used when training the chunker
    private Chunker chunker;
    private ResourceManager rm;

    public ChunkerTrain() {
        this(50);
    }

    public ChunkerTrain(int iter) {
        this.iter = iter;
        this.init();
    }

    public void init() {
        rm = new ChunkerConfigurator().getDefaultConfig();
        String modelFile = rm.getString("modelPath");
        String modelLexFile = rm.getString("modelLexPath");
        chunker = new Chunker(modelFile, modelLexFile);
    }

    /**
     * Trains the taggers with the default training data found in ChunkerConfigurator.java
     */
    public void trainModels() {
        System.out.println("Using default training data: " + rm.getString("trainingData"));
        trainModels(rm.getString("trainingData"));
    }

    /**
     * Trains the chunker models with the specified training data which must be in CoNLL2000 format
     *
     * @param trainingData The labeled training data
     */
    public void trainModels(String trainingData) {
        Parser parser = new CoNLL2000Parser(trainingData);
        trainModelsWithParser(parser);
    }

    /**
     * Trains the chunker models with the specified training data
     * 
     * @param parser Parser for the training data. Initialized in trainModels(String trainingData)
     */
    public void trainModelsWithParser(Parser parser) {
        Chunker.isTraining = true;

        // Run the learner
        for (int i = 1; i <= iter; i++) {
            LinkedVector ex;
            while ((ex = (LinkedVector) parser.next()) != null) {
                for (int j = 0; j < ex.size(); j++) {
                    chunker.learn(ex.get(j));
                }
            }
            parser.reset();
            chunker.doneWithRound();
            System.out.println("Iteration number : " + i);
        }
        chunker.doneLearning();
    }

    /**
     * Saves the ".lc" and ".lex" models to disk in the modelPath specified by the constructor The
     * modelName ("illinois-chunker") is fixed
     */
    public void writeModelsToDisk() {
        chunker.save();
        System.out.println("Done training, models are in " + rm.getString("modelDirPath"));
    }

    public static void main(String[] args) {
        ChunkerTrain trainer = new ChunkerTrain();
        trainer.trainModels();
        trainer.writeModelsToDisk();
    }
}
