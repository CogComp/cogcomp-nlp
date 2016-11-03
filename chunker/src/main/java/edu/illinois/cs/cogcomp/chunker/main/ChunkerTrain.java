/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.chunker.main;


import edu.illinois.cs.cogcomp.chunker.main.lbjava.ChunkLabel;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.BIOTester;
import edu.illinois.cs.cogcomp.lbjava.parse.ChildrenFromVectors;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import java.io.File;
import java.lang.Math;
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

    public void trainModels(String trainingData, String modeldir, String modelname, double dev_ratio) {
        Parser parser = new CoNLL2000Parser(trainingData);
        trainModelsWithParser(parser, modeldir, modelname, dev_ratio);
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

    public void trainModelsWithParser(Parser parser, String modeldir, String modelname, double dev_ratio) {
        Chunker.isTraining = true;
        double tmpF1 = 0;
        double bestF1 = 0;
        int bestIter = 0;
        double[] F1array = new double[iter];
        String lcpath = modeldir+File.separator+modelname+".lc";
        String lexpath = modeldir+File.separator+modelname+".lex";

        // Get the total number of training set
        int cnt = 0;
        LinkedVector ex;
        while ((ex = (LinkedVector) parser.next()) != null) {
            cnt++;
        }
        parser.reset();
        // Get the boundary between train and dev
        long idx = Math.round(cnt*(1-dev_ratio));
        if( idx < 0 )
            idx = 0;
        if( idx > cnt )
            idx = cnt;

        // Run the learner and save F1 for each iteration
        for (int i = 1; i <= iter; i++) {
            cnt = 0;
            while ((ex = (LinkedVector) parser.next()) != null) {
                for (int j = 0; j < ex.size(); j++) {
                    chunker.learn(ex.get(j));
                }
                if(cnt>=idx)
                    break;
                else
                    cnt++;
            }
            chunker.doneWithRound();
            writeModelsToDisk(modeldir,modelname);
            // Test on dev set
            BIOTester tester =
                    new BIOTester(new Chunker(lcpath,lexpath), new ChunkLabel(), new ChildrenFromVectors(parser));
            double[] result = tester.test().getOverallStats();
            tmpF1 = result[2];

            F1array[i-1] = tmpF1;
            System.out.println("Iteration number : " + i + ". F1 score on devset: "+tmpF1);
            parser.reset();
        }

        // Get the best F1 score and corresponding iter
        for(int i = 0; i < iter; i++){
            if(F1array[i]>bestF1){
                bestF1 = F1array[i];
                bestIter = i+1;
            }
        }
        System.out.println("Best #Iter = "+bestIter+" (F1="+bestF1+")");
        System.out.println("Rerun the learner using best #Iter...");
        // Rerun the learner
        for (int i = 1; i <= bestIter; i++) {
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
     * modelName ("Chunker", as specified in ChunkerConfigurator) is fixed
     */
    public void writeModelsToDisk() {
        IOUtils.mkdir(rm.getString("modelDirPath"));
        chunker.save();
        System.out.println("Done training, models are in " + rm.getString("modelDirPath"));
    }
    public void writeModelsToDisk(String dir, String modelName){
        IOUtils.mkdir(dir);
        chunker.write(dir + File.separator + modelName + ".lc", dir + File.separator + modelName + ".lex");
        System.out.println("Done training, models are in " + dir+File.separator+modelName+".lc (.lex)");
    }
    public static void main(String[] args) {
        if(args.length!=4&&args.length!=5){
            System.out.println("Usage1: ...ChunkerTrain traindata modeldir modelname round");
            System.out.println("Usage2: ...ChunkerTrain traindata modeldir modelname max_round dev_ratio");
            System.exit(-1);
        }
        ChunkerTrain trainer = new ChunkerTrain(Integer.parseInt(args[3]));
        if(args.length==4) {
            trainer.trainModels(args[0]);
        }
        else if(args.length==5){
            trainer.trainModels(args[0], args[1],args[2],Double.parseDouble(args[4]));
        }
        trainer.writeModelsToDisk(args[1],args[2]);
    }
}
