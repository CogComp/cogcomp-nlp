package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;

/**
 * This interface defines the gazetteers public API. There is only one method, to annotate the
 * provided word object or TextAnnotation object..
 * 
 * @author redman
 */
public interface Gazetteers {


    /**
     * This method, given a word for any gazetteer manager will annotate that word..
     * 
     * @param startword the word to match around.
     */
    void annotate(final NEWord startword);
}
