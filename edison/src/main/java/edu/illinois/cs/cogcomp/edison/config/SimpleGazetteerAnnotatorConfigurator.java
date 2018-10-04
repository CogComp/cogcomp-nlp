/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.config;

import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 *
 * Created by mssammon on 8/10/16.
 */
public class SimpleGazetteerAnnotatorConfigurator extends AnnotatorConfigurator {


    public static final Property PATH_TO_DICTIONARIES = new Property("pathToDictionaries",
            "somepath");
    public static final Property PHRASE_LENGTH = new Property("phraseLength", "4");

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                new Property[] {PATH_TO_DICTIONARIES, PHRASE_LENGTH,
                        new Property(IS_LAZILY_INITIALIZED.key, TRUE)};
        return new ResourceManager(generateProperties(props));
    }
}
