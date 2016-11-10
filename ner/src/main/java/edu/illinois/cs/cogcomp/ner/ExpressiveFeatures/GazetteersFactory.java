/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This singleton class contains all the gazetteer data and dictionaries. Can only be accessed via
 * the get() method all constructors are private.
 * 
 * @author redman
 */
public class GazetteersFactory {

    /** the logger. */
    static private Logger logger = LoggerFactory.getLogger(GazetteersFactory.class);

    /** the sole instance of this class. */
    private static Gazetteers gazetteers = null;

    /** this is a token whose only use it to ensure thread safety. */
    private static String GAZ_INIT_LOCK = "GAZ_INIT_LOCK";
    /**
     * Initialize the gazetteers. This method requires some exception handling, so the
     * initialization is separated from the fetching.
     * 
     * @param path path to the gaz files.
     * @throws IOException
     */
    static public void init(int maxPhraseLength, String path, boolean flatgazetteers)
            throws IOException {
        synchronized (GAZ_INIT_LOCK) {
            if (gazetteers == null) {
                if (flatgazetteers) {
                    gazetteers = new FlatGazetteers(path);
                } else {
                    gazetteers = new TreeGazetteers(maxPhraseLength, path);
                }
            } else {
                if (flatgazetteers) {
                    if (gazetteers instanceof TreeGazetteers ) {
                        logger.warn("We had previously loaded a TreeGazetteers, but reloading a FlatGazetteers");
                        // we want a flat gazetteer, but we have a tree gazetteer
                        gazetteers = null;
                        gazetteers = new FlatGazetteers(path);
                    }
                } else {
                    if (gazetteers instanceof FlatGazetteers ) {
                        logger.warn("We had previously loaded a FlatGazetteers, but reloading a TreeGazetteers");
                        gazetteers = null;
                        gazetteers = new TreeGazetteers(maxPhraseLength, path);
                    }
                }
            }
        }
    }

    /**
     * This method should never be called before init, or the gazetteer will not be initialized.
     * 
     * @return the singleton instance of the Gazetteers class.
     */
    static public Gazetteers get() {
        return gazetteers;
    }
}
