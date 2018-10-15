/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.constants.Language;

/**
 * This singleton class contains all the gazetteer data and dictionaries. Can only be accessed via
 * the get() method all constructors are private.
 * 
 * @author redman
 */
public class GazetteersFactory {

    /** this is a token whose only use it to ensure thread safety. */
    private static String GAZ_INIT_LOCK = "GAZ_INIT_LOCK";

    /** keep the initialized gazetteers, this is added due to multilingual NER */
    private static Map<String, Gazetteers> gazetteers_map = new HashMap<>();

    /**
     * Initialize the gazetteers. This method requires some exception handling, so the
     * initialization is separated from the fetching.
     * @param maxPhraseLength the max number of tokens to keep for phrases.
     * @param path path to the gaz files.
     * @param flatgazetteers  if true, create a flat gaz, less memory, but slower.
     * @param language the language.
     * @return the gazetteer, newly created if we don't already have it.
     * @throws IOException
     */
    static public Gazetteers get(int maxPhraseLength, String path, boolean flatgazetteers, Language language)
            throws IOException {

        synchronized (GAZ_INIT_LOCK) {
            if (flatgazetteers) {
                if (!gazetteers_map.containsKey(path) || gazetteers_map.get(path) instanceof TreeGazetteers) {
                    gazetteers_map.put(path, new FlatGazetteers(path));
                }
            } else {
                if (!gazetteers_map.containsKey(path) || gazetteers_map.get(path) instanceof FlatGazetteers) {
                    gazetteers_map.put(path, new TreeGazetteers(maxPhraseLength, path, language));
                }
            }
            return gazetteers_map.get(path);
        }
    }
}
