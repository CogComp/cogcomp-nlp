package edu.illinois.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

/**
 * IllinoisLemmatizerInterface is a implementation of LemmatizerInterface for MASCevaluation
 * 
 * @author Zefu Lu
 */

import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;

public class IllinoisLemmatizerInterface implements LemmatizerInterface {
    // Implements init lemmatizer method
    private IllinoisLemmatizer lem;

    public void initLemmatizer() {
        try {
            lem = new IllinoisLemmatizer();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Implements getLemma method
     */
    public String getLemma(String word, String pos) {
        String result = word;
        try {
            result = lem.getLemma(word, pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
