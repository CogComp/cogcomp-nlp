/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.common;

import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Default configuration
 */
public class Stanford331Configurator extends AnnotatorConfigurator {
    public static final Property STFRD_TIME_PER_SENTENCE = new Property(
            "stanfordMaxTimePerSentence", "100000");
    public static final Property STFRD_MAX_SENTENCE_LENGTH = new Property(
            "stanfordParseMaxSentenceLength", "80");
    public static final Property THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK = new Property(
            "throwExceptionOnFailedLengthCheck", TRUE);

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     * default SRL_TYPE is Verb.
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = {STFRD_TIME_PER_SENTENCE, STFRD_MAX_SENTENCE_LENGTH, THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK};
        return (new AnnotatorServiceConfigurator().getConfig(new ResourceManager(
                generateProperties(properties))));
    }

    /**
     * Get a {@link ResourceManager} with non-default properties. Overloaded to merge the properties
     * of {@link AnnotatorServiceConfigurator}.
     *
     * @param nonDefaultRm The non-default properties
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getConfig(ResourceManager nonDefaultRm) {
        ResourceManager pipelineRm = super.getConfig(nonDefaultRm);
        return new AnnotatorServiceConfigurator().getConfig(pipelineRm);
    }
}
