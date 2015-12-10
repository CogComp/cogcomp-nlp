package edu.illinois.cs.cogcomp.lbj.chunk;

import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
/**
 * Trains chunker models with user specified labeled data in the CoNLL2000 format.
 * Similar to POSTrain.java.
 *
 * @author James Chen
 */
public class ChunkerTrain {
    private static final String NAME = ChunkerTrain.class.getCanonicalName();
    private String modelPath;   // The path to where the trainer will save the trained models
    private int iter;           // Number of iterations to be used when training the chunker
    private Chunker chunker;

    public ChunkerTrain(String modelPath, int iter) {
        this.modelPath = modelPath + "/";
        this.iter = iter;
        this.chunker = new Chunker();
    }


    /**
     * Trains the chunker models with the specified training data
     * @param trainingData The labeled training data
     */
    public void trainModelsWithParser( String trainingData, Parser parser )
    {
        // Run the learner
        for (int i = 0; i < iter; i++) {
            LinkedVector ex;
            while ((ex = (LinkedVector)parser.next()) != null) {
                for (int j = 0; j < ex.size(); j++) {
                    chunker.learn(ex.get(j));
                }
            }
        }

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
     * Saves the ".lc" and ".lex" models to disk in the modelPath specified by the constructor
     * @param chunkerName The name of the chunker models to be saved on disk
     */
    public void writeModelsToDisk(String chunkerName) {
        chunker.write(modelPath + chunkerName + ".lc", modelPath + chunkerName + ".lex");
        System.out.println("Done training, models are in " + modelPath);
    }

    public static void main(String[] args) {
        if ( args.length != 2 )
        {
            System.err.println( "Usage: " + NAME + " trainData modelDir modelName" );
            System.exit( -1 );
        }
        String trainingData = args[ 0 ];
        String modelDir = args[ 1 ];
        String modelName = args[ 2 ];

        ChunkerTrain trainer = new ChunkerTrain(modelDir, 50);
//        trainer.trainModels(Constants.trainingData);
        trainer.trainModels( trainingData);
        trainer.writeModelsToDisk( modelName ); //"illinois-chunker"
    }
}
