package edu.illinois.cs.cogcomp.transliteration;


import edu.illinois.cs.cogcomp.core.datastructures.Pair;

/**
 * This is mainly intended to introduce clarity, not add any functionality.
 * Created by mayhew2 on 11/5/15.
 */
public class Production extends Pair<String,String>{

    /**
     * Source segment
     */
    String segS;

    /**
     * Target segment
     */
    String segT;

    public Production(String segS, String segT){
        super(segS,segT);
    }

}
