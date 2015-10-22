package edu.illinois.cs.cogcomp.nlp.collocations;

/**
 * @author vivek
 */
public interface CollocationComputer {
    double getCount(String str) throws Exception;

    double getCollocationScore(String left, String right) throws Exception;
}
