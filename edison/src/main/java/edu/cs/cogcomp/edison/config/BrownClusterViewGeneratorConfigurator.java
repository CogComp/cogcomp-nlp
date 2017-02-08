/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.edison.config;

import edu.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.cs.cogcomp.core.utilities.configuration.Property;
import edu.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 *
 * Created by ctsai12 on 1/17/17.
 */
public class BrownClusterViewGeneratorConfigurator extends AnnotatorConfigurator {


    public static final Property NORMALIZE_TOKEN = new Property("normalizeToken",
            TRUE);

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                new Property[] {NORMALIZE_TOKEN};
        return new ResourceManager(generateProperties(props));
    }
}
