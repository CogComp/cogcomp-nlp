package edu.illinois.cs.cogcomp.core.resources;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Default configuration for resources
 */
public class ResourceConfigurator extends Configurator {
    public static final Property ENDPOINT = new Property("datastoreEndpoint", "http://smaug.cs.illinois.edu:8080");

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = { ENDPOINT };
        return new ResourceManager(generateProperties(properties));
    }
}
