/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class DepConfigurator extends Configurator {
    public static final Property MODEL_NAME = new Property("modelName",
            "struct-perceptron-auto-20iter.model");

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = {MODEL_NAME};
        return new ResourceManager(generateProperties(props));
    }
}
