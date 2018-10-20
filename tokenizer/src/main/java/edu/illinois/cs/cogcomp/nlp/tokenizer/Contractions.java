/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.util.HashSet;

/**
 * List of known contractions compiled into a hash map for fast lookup.
 * 
 * @author redman
 */
public class Contractions {
    /**
     * Given the character key, get the list of all acronyms starting with that character.
     * 
     * @param key the key is the first char of the acronym
     * @return the ist of strings that are the acronyms.
     */
    static boolean contains(String word) {
        return contractions.contains(word);
    }

    /**
     * This map keys a set of acronyms to the first letter of that acronym, for faster lookup. This
     * map is constructed from the list of strings below, so if you want to add a new acronym,
     * simply add it to the list below.
     */
    final static private HashSet<String> contractions = new HashSet<String>();

    /** this is where we add acroyms. */
    final static private String[] known_contractions = {"bout", "nuff", "n", "Nuff", "cause", "em",
            "er", "ns"};

    // init the abbr data structure.
    static {
        for (String t : known_contractions) {
            contractions.add(t);
        }
    }

}
