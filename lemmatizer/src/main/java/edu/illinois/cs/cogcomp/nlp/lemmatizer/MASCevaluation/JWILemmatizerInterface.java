/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

import edu.mit.jwi.morph.WordnetStemmer;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.POS;

import java.io.File;
import java.util.List;

public class JWILemmatizerInterface implements LemmatizerInterface {
    WordnetStemmer stemmer;

    /**
     * Convert pos tag to JWI POS instance
     */
    public static POS convertPOS(String pos) {
        if (pos.startsWith("NN"))
            return POS.NOUN;
        else if (pos.startsWith(("VB")))
            return POS.VERB;
        else if (pos.startsWith(("JJ")))
            return POS.ADJECTIVE;
        else if (pos.startsWith(("RB")))
            return POS.ADVERB;
        return null;
    }

    /**
     * Implements initLemmatizer method
     */
    public void initLemmatizer() {
        try {
            Dictionary dict = new Dictionary(new File("wordnet-dict"));
            dict.open();
            stemmer = new WordnetStemmer(dict);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements getLemma method
     */
    public String getLemma(String word, String pos) {
        List<String> lis = stemmer.findStems(word, convertPOS(pos));
        if (lis.size() == 0) {
            return word;
        } else {
            return lis.get(0);
        }
    }
}
