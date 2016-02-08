package edu.illinois.cs.cogcomp.lbj.pos;

import java.util.LinkedList;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

public class TrainedPOSTagger {

  private POSTaggerKnown known;
  private POSTaggerUnknown unknown;
  private MikheevTable mikheev;
  private baselineTarget baseline;
  private wordForm wordForm;

  public TrainedPOSTagger() {
    //TODO: Initialize via default path
  }

  public TrainedPOSTagger(String modelDirectory) {
    String knownModelFile = modelDirectory + "POSTaggerKnown.lc";
    String knownLexFile = modelDirectory + "POSTaggerKnown.lex";
    String unknownModelFile = modelDirectory  + "POSTaggerUnknown.lc";
    String unknownLexFile = modelDirectory  + "POSTaggerUnknown.lex";
    String baselineModelFile = modelDirectory + "baselineTarget.lc";
    String mikheevModelFile = modelDirectory + "MikheevTable.lc";


    baselineTarget.getInstance().readModel(baselineModelFile);
    MikheevTable.getInstance().readModel(mikheevModelFile);

    known = new POSTaggerKnown(knownModelFile, knownLexFile);
    unknown = new POSTaggerUnknown(unknownModelFile, unknownLexFile);
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
