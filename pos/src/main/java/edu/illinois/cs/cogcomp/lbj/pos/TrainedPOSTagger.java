package edu.illinois.cs.cogcomp.lbj.pos;

import java.util.LinkedList;

public class TrainedPOSTagger extends POSTagger {
  
  public TrainedPOSTagger(String modelDirectory) {
    super();
    
    String knownModelFile = modelDirectory + "POSTaggerKnown.lc";
    String knownLexFile = modelDirectory + "POSTaggerKnown.lex";
    String unknownModelFile = modelDirectory  + "POSTaggerUnknown.lc";
    String unknownLexFile = modelDirectory  + "POSTaggerUnknown.lex";
    String baselineModelFile = modelDirectory + "baselineTarget.lc";
    String mikheevModelFile = modelDirectory + "MikheevTable.lc";
    //__POSTaggerKnown.read(knownModelFile, knownLexFile);
    //__POSTaggerUnknown.read(unknownModelFile, unknownLexFile); 

    //LinkedList knownChildren = __POSTaggerKnown.getCompositeChildren();
    //LinkedList unknownChildren = __POSTaggerUnknown.getCompositeChildren();
    //System.out.println(getLabeler());

  } 

}
