package edu.illinois.cs.cogcomp.chunker.main;

import java.util.LinkedList;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.*;

/**
 * This Chunker Tagger uses a pre-trained model. The model files will be found by checking two locations
 * in order:
 * <ul>
 * <li>First, the directory specified in the constant Constants.modelPath
 * <li>If the files are not found in this directory, the classpath will be checked (this will result
 * in loading the files from the maven repository
 * </ul>
 *
 * This class acts as a wrapper around the Chunker Tagging classes defined in the LBJava code.
 * 
 * @author nitishgupta
 */
public class TrainedChunker {

    private Chunker chunker;
    //private wordForm wordForm;

    /**
     * Initializes a tagger from either a pre-specified directory or the classpath
     */
    public TrainedChunker() {
        ResourceManager rm = new ChunkerConfigurator().getDefaultConfig();
        URL modelFile = null;
        URL modelLexFile = null;
        try {
            if ((new File(rm.getString("modelPath"))).exists()) {
                modelFile = (new File(rm.getString("modelPath"))).toURL();
            } else {
                modelFile =
                        IOUtilities.loadFromClasspath(TrainedChunker.class,
                                rm.getString("modelPath"));
            }
            if ((new File(rm.getString("modelLexPath"))).exists()) {
                modelLexFile = (new File(rm.getString("modelLexPath"))).toURL();
            } else {
                modelLexFile =
                        IOUtilities.loadFromClasspath(TrainedChunker.class,
                                rm.getString("modelLexPath"));
            }
        } catch (MalformedURLException e) {
            System.out.println("ERROR: MALRFORMED URL (THIS SHOULD NEVER HAPPEN)");
            System.exit(1);
        }
        known = Chunker.getInstance();
        known.readModel(modelFile);
	known.readLexicon(modelLexFile);

        //wordForm = new wordForm();
    }

    
	/** - Find equivalent function in Chunker to tag. And then define equivalent function to tag using the model loaded.
		
     * Finds the correct POS tag for the provided token
     *
     * @param w The Token whose POS tag is being sought
     * @return A string representing the POS tag for the token
     
    public String discreteValue(Token w) {
        if (baselineTarget.getInstance().observed(wordForm.discreteValue(w))) {
            return known.discreteValue(w);
        }
        return unknown.discreteValue(w);
    }

	*/

}
