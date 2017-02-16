package edu.illinois.cs.cogcomp.depparse;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class DepConfigurator extends Configurator {
    public static final Property MODEL_NAME = new Property("modelName", "struct-perceptron-auto-20iter.model");

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = {MODEL_NAME};
        return new ResourceManager(generateProperties(props));
    }
}
