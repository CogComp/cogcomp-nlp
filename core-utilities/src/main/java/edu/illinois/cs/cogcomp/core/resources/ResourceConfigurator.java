/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
