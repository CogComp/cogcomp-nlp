package edu.illinois.cs.cogcomp.lbj.pos;

import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.io.File;

/**
 * Simple class to build and train models from existing training data, as opposed to
 * using the prepackaged jar.
 *
 * @author James Chen
 * @author Christos Christodoulopoulos
 */
public class POSTrain {
    private static final String NAME = POSTrain.class.getCanonicalName();
    private String modelPath;   // Path to the directory where the models are stored.
    private int iter;           // Number of training iterations
    private POSTaggerKnown taggerKnown;
    private POSTaggerUnknown taggerUnknown;
    private MikheevTable mikheevTable;
    private POSBaselineLearner baselineTarget;

    public POSTrain(String modelPath) {
        this.modelPath = modelPath;
        this.iter = 50;
        this.init();
    }

    public POSTrain(String modelPath, int iter) {
        this.modelPath = modelPath;
        this.iter = iter;
        this.init();
    }

    /**
     * Known and unknown taggers to be trained later.
     */
    private void init() {
        taggerKnown = new POSTaggerKnown();
        taggerUnknown = new POSTaggerUnknown();
        mikheevTable = new MikheevTable();
        baselineTarget = new baselineTarget();
    }

    /**
     * Trains the taggers with the default training data found in Constants.java
     */
    public void trainModels() {
        System.out.println("Using default training data: " + Constants.trainingAndDevData);
        trainModels(Constants.trainingAndDevData);
    }

    /**
     * Trains the taggers with specified, labeled training data.
     * @param trainingData The labeled training data
     */
    public void trainModels(String trainingData) {
        // Set up the data
        Parser trainingParser = new POSBracketToToken(trainingData);
        Parser trainingParserUnknown = new POSLabeledUnknownWordParser(trainingData);

        MikheevTable.isTraining = true;
        edu.illinois.cs.cogcomp.lbj.pos.baselineTarget.isTraining = true;

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
            System.out.println("\tFinished training " + Constants.knownName);
            while ((ex = trainingParserUnknown.next()) != null) {
                taggerUnknown.learn(ex);
            }
            System.out.println("\tFinished training " + Constants.unknownName);
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
    public void writeModelsToDisk() {
        // Make sure necessary directories exist
        (new File(modelPath)).mkdirs();

        // There isn't a lexicon for baselineTarget/mikheevTable
        baselineTarget.writeModel(Constants.baselineModelPath);
        mikheevTable.writeModel(modelPath + Constants.mikheevName + ".lc");
        taggerKnown.write(Constants.knownModelPath, Constants.knownLexPath);
        taggerUnknown.write(Constants.unknownModelPath, Constants.unknownLexPath);
        System.out.println("Done training, models are in " + modelPath);
    }

    public static void main(String[] args) {
        if ( args.length != 2 && args.length != 1) {
            System.err.println( "Usage: " + NAME + " modelPath [trainingFile]" );
            System.err.println( "'trainingDataFile' must contain training data in specified format (" +
                "see doc/README); 'modelPath' specifies directory to which the learned models will be written." );
            System.exit( -1 );
        }
        String modelPath = args[ 0 ];
	String trainingFile = null;
        if (args.length == 2) {
          trainingFile = args[ 1 ];
        }

        File writeDir = new File( modelPath );
        if ( !writeDir.exists() ) {
            System.err.println( NAME + ".writeModelsToDisk(): creating dir '" + writeDir.getName() + "'..." );
            writeDir.mkdir();
        }

        POSTrain trainer = new POSTrain( modelPath );
        if (trainingFile == null) {
          trainer.trainModels();
        } else {
          trainer.trainModels( trainingFile );
        }

        trainer.writeModelsToDisk();
    }
}
