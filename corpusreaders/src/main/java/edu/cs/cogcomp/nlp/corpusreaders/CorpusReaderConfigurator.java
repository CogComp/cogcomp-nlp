/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.nlp.corpusreaders;

import edu.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.cs.cogcomp.core.utilities.configuration.Property;
import edu.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.Properties;

/**
 * Created by mssammon on 6/21/16.
 */
public class CorpusReaderConfigurator extends Configurator {
    public static final Property CORPUS_NAME = new Property("corpusName", "dummyCorpusName");
    public static final Property CORPUS_DIRECTORY = new Property("corpusDirectory",
            "dummyCorpusDirectory");


    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = new Property[] {CORPUS_NAME, CORPUS_DIRECTORY};
        return new ResourceManager(generateProperties(props));
    }

    public static ResourceManager buildResourceManager(String corpus) {
        Properties props = new Properties();
        props.setProperty(CorpusReaderConfigurator.CORPUS_NAME.key, corpus);
        return new ResourceManager(props);
    }

    public static ResourceManager buildResourceManager(String corpusName, String corpusDirectory) {
        Properties props = new Properties();
        props.setProperty(CorpusReaderConfigurator.CORPUS_NAME.key, corpusName);
        props.setProperty(CorpusReaderConfigurator.CORPUS_DIRECTORY.key, corpusDirectory);
        return new ResourceManager(props);
    }

}
