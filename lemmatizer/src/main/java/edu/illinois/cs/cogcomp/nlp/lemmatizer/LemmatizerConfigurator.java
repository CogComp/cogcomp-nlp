/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Lemmatizer constructor parameters Created by mssammon on 1/5/16.
 */
public class LemmatizerConfigurator extends AnnotatorConfigurator {

    public final static Property WN_PATH = new Property("wnPath", "wordnet-dict");
    public final static Property USE_STNFRD_CONVENTIONS = new Property("useStanfordConventions",
            FALSE);
    public final static Property LEMMA_LAZY_INITIALIZE = new Property(
            AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, TRUE);

    public final static Property VERB_LEMMA_FILE = new Property( "verbLemmaFile", "verb-lemDict.txt" );
    public static final Property EXCEPTIONS_FILE = new Property("exceptionsFile", "exceptions.txt");

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = {WN_PATH, USE_STNFRD_CONVENTIONS, LEMMA_LAZY_INITIALIZE,
            VERB_LEMMA_FILE, EXCEPTIONS_FILE};
        return new ResourceManager(generateProperties(props));
    }
}
