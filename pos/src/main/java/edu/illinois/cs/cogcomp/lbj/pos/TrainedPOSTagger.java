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

    known = new POSTaggerKnown();
    known.read(knownModelFile, knownLexFile);
    unknown = new POSTaggerUnknown();
    unknown.read(unknownModelFile, unknownLexFile);
    mikheev = new MikheevTable();
    baseline = new baselineTarget();
    wordForm = new wordForm();

    //known.unclone();
    //unknown.unclone();
    baseline.readModel(baselineModelFile);
    mikheev.readModel(mikheevModelFile);
    mikheev.unclone();
    baseline.unclone();
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
      System.out.println("Observed!");
System.out.println(baseline.allowableTags(wordForm.discreteValue(w)));
      return known.valueOf(w, baseline.allowableTags(wordForm.discreteValue(w))).getStringValue();
    }
System.out.println("Unobserved!");
System.out.println(mikheev.allowableTags(w));
    return unknown.valueOf(w, mikheev.allowableTags(w)).getStringValue();
  }  

}
