package edu.illinois.cs.cogcomp.lbj.pos;

import java.util.LinkedList;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;

public class TrainedPOSTagger {

  private POSTaggerKnown known;
  private POSTaggerUnknown unknown;
  private wordForm wordForm;

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

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POSTagger(Token)' defined on line 17 of POS.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token w = (Token) __example;
    if (baselineTarget.getInstance().observed(wordForm.discreteValue(w)))
    {
      return known.discreteValue(w);
    }
    return unknown.discreteValue(w);
  }  

}
