/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
