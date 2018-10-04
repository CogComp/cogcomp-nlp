/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class PrepSRLConfigurator extends Configurator {
    public static Property MODELS_DIR = new Property("ModelsDir", "models");
    public static Property PREP_DATA_DIR = new Property("Prepositions", "data/srl-prep");
    public static Property DISABLE_CACHE = new Property(
            AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.TRUE);

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = {MODELS_DIR, PREP_DATA_DIR, DISABLE_CACHE};
        return new ResourceManager(generateProperties(props));
    }

    public static ResourceManager defaults() {
        return new PrepSRLConfigurator().getDefaultConfig();
    }
}
