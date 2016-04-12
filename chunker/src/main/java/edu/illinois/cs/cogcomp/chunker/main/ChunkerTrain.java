package edu.illinois.cs.cogcomp.chunker.main;


import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.chunker.utils.Constants;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.pos.POSConfigurator;

import java.io.File;

/**
 * Trains chunker models with user specified labeled data in the CoNLL2000 format.
 * Similar to POSTrain.java.
 *
 * @author James Chen
 */
public class ChunkerTrain {
    private static final String NAME = ChunkerTrain.class.getCanonicalName();
    private String modelDirPath;   // Path to where the trainer will save the trained models
    private int iter;           // Number of iterations to be used when training the chunker
    private ResourceManager rm;

    private Chunker chunker;

    public ChunkerTrain(int iter) {
        this.init();
        this.modelDirPath = rm.getString("modelDirPath");
        this.iter = iter;

    }

    public ChunkerTrain(String modelDirPath, int iter) {
        if(!modelDirPath.endsWith("/")) {
            modelDirPath += "/";
        }
        this.modelDirPath = modelDirPath;
        this.iter = iter;
        this.init();
    }


    public ChunkerTrain(String modelDirPath) {
        if(!modelDirPath.endsWith("/")) {
            modelDirPath += "/";
        }
        this.modelDirPath = modelDirPath;
        this.iter = 50;
        this.init();
    }

    private void init(){
        rm = new ChunkerConfigurator().getDefaultConfig();
        this.chunker = new Chunker();
    }

    /**
     * Trains the taggers with the default training data found in ChunkerConfigurator.java
     */
    public void trainModels() {
        System.out.println("Using default training data: " + rm.getString("trainingData"));
        trainModels(rm.getString("trainingData"));
    }

    /**
     * Trains the chunker models with the specified training data
     *   which must be in CoNLL2000 format
     *
     * @param trainingData The labeled training data
     */
    public void trainModels(String trainingData) {
        Parser parser = new CoNLL2000Parser(trainingData);
        trainModelsWithParser( trainingData, parser );
    }

    /**
     * Trains the chunker models with the specified training data
     * @param trainingData The labeled training data
     */
    public void trainModelsWithParser(String trainingData, Parser parser )
    {
        // Run the learner
        for (int i = 1; i <= iter; i++) {
            LinkedVector ex;
            while ((ex = (LinkedVector)parser.next()) != null) {
                for (int j = 0; j < ex.size(); j++) {
                    chunker.learn(ex.get(j));
                }
            }
            parser.reset();
            System.out.println("Iteration number : " + i);
        }
    }

    /**
     * Saves the ".lc" and ".lex" models to disk in the modelPath specified by the constructor
     * The modelName ("illinois-chunker") is fixed
     */
    public void writeModelsToDisk() {
        // Make sure necessary directories exist
        System.out.println("Writing models to disk");
        (new File(modelDirPath)).mkdirs();
        chunker.write(modelDirPath + rm.getString("modelName") + ".lc", modelDirPath + rm.getString("modelName") + ".lex");
        System.out.println("Done training, models are in " + modelDirPath);
    }

    public static void main(String[] args) {
        String modelDir = null;
        String trainingFile = null;

        if(args.length >= 1)
            modelDir = args[0];

        if (args.length == 2) {
            trainingFile = args[1];
        }

        ChunkerTrain trainer = null;
        if(modelDir!=null)
            trainer = new ChunkerTrain(modelDir, 50);
        else
            trainer = new ChunkerTrain(50); //Using default model output path from configurator


        System.out.println("Starting training ...");

//        trainer.trainModels(Constants.trainingData);
        if(trainingFile == null){
            trainer.trainModels();
        }
        else{
            trainer.trainModels(trainingFile);
        }
        trainer.writeModelsToDisk(); //"Chunker"
    }
}
