/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Annotator configuration options.
 */
public class AnnotatorConfigurator extends Configurator {

    /**
     * if 'true', the annotator will only be initialized on the first call to its getView() method.
     */
    public static Property IS_LAZILY_INITIALIZED = new Property("isLazilyInitialized", FALSE);

    /**
     * if 'true', this Annotator has no inter-sentence dependencies during processing, so each
     *    sentence can be processed individually.
     */
    public static Property IS_SENTENCE_LEVEL = new Property("isSentenceLevel", FALSE);

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = new Property[] {IS_LAZILY_INITIALIZED, IS_SENTENCE_LEVEL};
        return new ResourceManager(generateProperties(props));
    }
}
