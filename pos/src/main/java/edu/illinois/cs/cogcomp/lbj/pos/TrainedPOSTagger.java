package edu.illinois.cs.cogcomp.lbj.pos;

import java.util.LinkedList;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

public class TrainedPOSTagger {

  private POSTaggerKnown known;
  private POSTaggerUnknown unknown;
  private MikheevTable mikheev;
  private baselineTarget baseline;
  private wordForm wordForm;

  public TrainedPOSTagger(String modelDirectory) {
    String knownModelFile = modelDirectory + "POSTaggerKnown.lc";
    String knownLexFile = modelDirectory + "POSTaggerKnown.lex";
    String unknownModelFile = modelDirectory  + "POSTaggerUnknown.lc";
    String unknownLexFile = modelDirectory  + "POSTaggerUnknown.lex";
    String baselineModelFile = modelDirectory + "baselineTarget.lc";
    String mikheevModelFile = modelDirectory + "MikheevTable.lc";

    known = new POSTaggerKnown();
    unknown = new POSTaggerUnknown();
    mikheev = new MikheevTable();
    baseline = new baselineTarget();
    wordForm = new wordForm();

    known.read(knownModelFile, knownLexFile);
    unknown.read(unknownModelFile, unknownLexFile);
    baseline.readModel(baselineModelFile);
    mikheev.readModel(mikheevModelFile);
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

    if (baseline.observed(wordForm.discreteValue(w)))
    {
      return known.valueOf(w, baseline.allowableTags(wordForm.discreteValue(w))).getStringValue();
    }
    return unknown.valueOf(w, mikheev.allowableTags(w)).getStringValue();
  }  

}
