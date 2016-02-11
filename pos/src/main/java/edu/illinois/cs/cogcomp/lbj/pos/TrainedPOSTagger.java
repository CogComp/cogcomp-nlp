package edu.illinois.cs.cogcomp.lbj.pos;

import java.util.LinkedList;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;

/**
 * This POS Tagger uses a pre-trained model. The model files will be found by checking two locations in order:
 * <ul>
 *   <li> First, the directory specified in the constant Constants.modelPath
 *   <li> If the files are not found in this directory, the classpath will be checked (this will result in loading the files from the maven repository
 * </ul>
 *
 * This class acts as a wrapper around the POS Tagging classes defined in the LBJava code.
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
        URL knownModelFile = null;
        URL knownLexFile = null;
        URL unknownModelFile = null;
        URL unknownLexFile = null;
        URL baselineModelFile = null;
        URL mikheevModelFile = null;
        try {
          if ((new File(Constants.knownModelPath)).exists()) {
              knownModelFile = (new File(Constants.knownModelPath)).toURL();
          } else {
              knownModelFile = IOUtilities.loadFromClasspath(TrainedPOSTagger.class, Constants.knownModelPath); 
          }
          if ((new File(Constants.knownLexPath)).exists()) {
              knownLexFile = (new File(Constants.knownLexPath)).toURL();
          } else {
              knownLexFile = IOUtilities.loadFromClasspath(TrainedPOSTagger.class, Constants.knownLexPath);
          }
          if ((new File(Constants.unknownModelPath)).exists()) {
              unknownModelFile = (new File(Constants.unknownModelPath)).toURL();
          } else {
              unknownModelFile = IOUtilities.loadFromClasspath(TrainedPOSTagger.class, Constants.unknownModelPath);
          }
          if ((new File(Constants.unknownLexPath)).exists()) {
              unknownLexFile = (new File(Constants.unknownLexPath)).toURL();
          } else {
              unknownLexFile = IOUtilities.loadFromClasspath(TrainedPOSTagger.class, Constants.unknownLexPath);
          }
          if ((new File(Constants.baselineModelPath)).exists()) {
              baselineModelFile = (new File(Constants.baselineModelPath)).toURL();
          } else {
              baselineModelFile = IOUtilities.loadFromClasspath(TrainedPOSTagger.class, Constants.baselineModelPath);
          }
          if ((new File(Constants.mikheevModelPath)).exists()) {
              mikheevModelFile = (new File(Constants.mikheevModelPath)).toURL();
          } else {
              mikheevModelFile = IOUtilities.loadFromClasspath(TrainedPOSTagger.class, Constants.mikheevModelPath);
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
    public String discreteValue(Token w)
    {
      if (baselineTarget.getInstance().observed(wordForm.discreteValue(w)))
      {
        return known.discreteValue(w);
      }
      return unknown.discreteValue(w);
    }  

}
