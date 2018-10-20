/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

import java.util.Map;
import java.util.*;
import edu.stanford.nlp.process.*;

/**
 * StanfordLemmatizerInterface is a implementation of LemmatizerInterface for MASCevaluation
 * 
 * @author Zefu Lu
 */

public class StanfordLemmatizerInterface implements LemmatizerInterface {

    Morphology morph;
    private Map<String, String> MASChack;

    /**
     * Implements initLemmatizer method
     */
    public void initLemmatizer() {
        morph = new Morphology();
        MASChack = new HashMap<String, String>();
        MASChack.put("’d", "have");
        MASChack.put("’ll", "will");
        MASChack.put("’s", "be");
        MASChack.put("’re", "be");
        MASChack.put("’m", "be");
        MASChack.put("’ve", "have");
        MASChack.put("'d", "have");
        MASChack.put("'ll", "will");
        MASChack.put("'s", "be");
        MASChack.put("'re", "be");
        MASChack.put("'m", "be");
        MASChack.put("'ve", "have");
        MASChack.put("her", "her");
        MASChack.put("him", "him");
        MASChack.put("his", "his");
        MASChack.put("their", "their");
        MASChack.put("them", "them");
        MASChack.put("your", "your");
        MASChack.put("us", "us");
        MASChack.put("me", "me");
        MASChack.put("an", "an");
        MASChack.put("n't", "n't");
        MASChack.put("our", "our");
    }

    /**
     * Implements getLemma method
     */
    public String getLemma(String word, String pos) {
        if (this.MASChack.containsKey(word)) {
            if (word.equals("'s") || word.equals("’s"))
                if (pos.equals("POS"))
                    return word;
            return this.MASChack.get(word);
        }
        try {
            return morph.lemma(word, pos);
        } catch (Exception e) {
            return word;
        }
    }
}
