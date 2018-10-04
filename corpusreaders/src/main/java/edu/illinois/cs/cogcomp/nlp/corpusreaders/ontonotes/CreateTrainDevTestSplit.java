/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.google.common.io.Files;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils.CorpusSplitConfigurator;

/**
 * In an attempt to build a generic method to split data into test train and dev, this 
 * class will traverse a directory distributing what data it finds into a test, train and
 * dev set sequentially. The resulting data will be split across a test train and dev directory
 * within an output directory provided on the command line. The split configuration is determined
 * by the {@link edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils.CorpusSplitConfigurator}.
 * @author redman
 */
public class CreateTrainDevTestSplit {

    /** the text set. */
    static private ArrayList<File> allfiles = new ArrayList<File>();
    
    /**
     * read from the cache.
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("Usage: CreateTrainDevTestSplit inputdirectory outputdirectory");
            System.exit(-1);
        }
        
        // check input directory
        File input = new File(args[0]);
        if (!input.exists()) {
            System.err.println("The input directory did not exist : "+args[0]);
            System.exit(-1);
        }
        
        // check output directory/
        File output = new File(args[1]);
        if (output.exists()) {
            if (!output.isDirectory()) {
                System.err.println("The output directory is not actually a directory : "+args[1]);
                System.exit(-1);
            }
        } else 
            output.mkdirs();
        
        // check training directory.
        File traindir = new File (output, "train");
        if (traindir.exists())
            if (traindir.isDirectory())
                System.out.println("The training directory already existed, any existing files by the same name overwritten.");
            else {
                System.err.println("The training directory is not a directory : "+traindir.toString());
                System.exit(-1);
            }
        else
            traindir.mkdir();
        
        // check test directory.
        File testdir = new File (output, "test");
        if (testdir.exists())
            if (testdir.isDirectory())
                System.out.println("The test directory already existed, any existing files by the same name overwritten.");
            else {
                System.err.println("The test directory is not a directory : "+testdir.toString());
                System.exit(-1);
            }
        else
            testdir.mkdir();

        // check dev directory.
        File devdir = new File (output, "dev");
        if (devdir.exists())
            if (devdir.isDirectory())
                System.out.println("The dev directory already existed, any existing files by the same name overwritten.");
            else {
                System.err.println("The dev directory is not a directory : "+devdir.toString());
                System.exit(-1);
            }
        else
            devdir.mkdir();
        
        ResourceManager fullRm = new CorpusSplitConfigurator().getDefaultConfig();
        double trainFrac = fullRm.getDouble(CorpusSplitConfigurator.TRAIN_FRACTION.key);
        double testFrac = fullRm.getDouble(CorpusSplitConfigurator.TEST_FRACTION.key);

        // compile a list of all the files in these directories.
        traverse(input);
        
        // we have all the files, just shuffle them.
        Collections.shuffle(allfiles);
        
        // compute the number of files that go into each set.
        final int trainend = (int)(trainFrac * allfiles.size());
        final int testend = trainend + (int) (testFrac * allfiles.size());
        
        // populate the train directory
        int i = 0;
        for (; i < trainend; i++) {
            File orig = allfiles.get(i);
            Files.copy(orig, new File(traindir, orig.getName()));
        }
        
        // populate the test directory
        for (; i < testend; i++) {
            File orig = allfiles.get(i);
            Files.copy(orig, new File(testdir, orig.getName()));
        }
        // populate the test directory
        for (; i < allfiles.size(); i++) {
            File orig = allfiles.get(i);
            Files.copy(orig, new File(devdir, orig.getName()));
        }
    }

    /**
     * traverse the directory structure.
     * @param input the directory to look into.
     */
    private static void traverse(File input) {
        File[] contents = input.listFiles();
        for (File file : contents) {
            if (file.isDirectory())
                traverse(file);
            else {
                allfiles.add(file);
            }
        }
    }
}
