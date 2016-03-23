package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import java.io.IOException;

/**
 * This singleton class contains all the gazetteer data and dictionaries. Can only be accessed via
 * the get() method all constructors are private.
 * 
 * @author redman
 */
public class GazetteersFactory {

    /** the sole instance of this class. */
    private static Gazetteers gazetteers = null;

    /**
     * Initialize the gazetteers. This method requires some exception handling, so the
     * initialization is separated from the fetching.
     * 
     * @param path path to the gaz files.
     * @throws IOException
     */
    static public void init(int maxPhraseLength, String path, boolean flatgazetteers)
            throws IOException {
        if (flatgazetteers) {
            gazetteers = new FlatGazetteers(path);
        } else {
            gazetteers = new TreeGazetteers(maxPhraseLength, path);
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
