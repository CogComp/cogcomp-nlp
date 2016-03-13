package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Lemmatizer constructor parameters Created by mssammon on 1/5/16.
 */
public class LemmatizerConfigurator extends Configurator {

    public final static Property WN_PATH = new Property("wnPath", "wordnet-dict");
    public final static Property USE_STNFRD_CONVENTIONS = new Property("useStanfordConventions",
            FALSE);

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = {WN_PATH, USE_STNFRD_CONVENTIONS};
        return new ResourceManager(generateProperties(props));
    }
}
