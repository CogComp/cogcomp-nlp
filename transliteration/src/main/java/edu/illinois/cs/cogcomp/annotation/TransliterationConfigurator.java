package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class TransliterationConfigurator extends Configurator {

    public static final Property LANGUAGE = new Property("usePos", FALSE);

    public static final Property MODEL_PATH = new Property("transliterationModelPath", ""); // todo: fix this

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = { MODEL_PATH, LANGUAGE };
        return (new TransliterationConfigurator().getConfig(new ResourceManager(generateProperties(properties))));
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
