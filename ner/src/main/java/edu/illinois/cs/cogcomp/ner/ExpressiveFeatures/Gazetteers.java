/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
