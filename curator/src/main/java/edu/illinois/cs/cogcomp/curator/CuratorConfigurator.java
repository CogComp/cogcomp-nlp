/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class CuratorConfigurator extends Configurator {
    public static final Property CURATOR_HOST = new Property("curatorHost",
            "trollope.cs.illinois.edu");
    public static final Property CURATOR_PORT = new Property("curatorPort", "9010");
    public static final Property CURATOR_FORCE_UPDATE = new Property("curatorForceUpdate",
            Configurator.FALSE);

    // CuratorClient flags
    /** If set to {@code true}, the input text will be assumed to be pre-tokenized */
    public static final Property RESPECT_TOKENIZATION = new Property("respectTokenization",
            Configurator.FALSE);
    /**
     * A comma-separated list of views to add (see
     * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} for a complete list of views.
     */
    public static final Property VIEWS_TO_ADD = new Property("viewsToAdd", ViewNames.POS + ","
            + ViewNames.NER_CONLL);

    // Cache related flags
    // TODO Until a caching mechanism is available in illinois-core-utilities, this AnnotatorService
    // will not support caching
    public static final Property DISABLE_CACHE = new Property("disableCache", Configurator.TRUE);
    public static final Property CACHE_FORCE_UPDATE = new Property("cacheForceUpdate",
            Configurator.FALSE);

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {CURATOR_HOST, CURATOR_PORT, CURATOR_FORCE_UPDATE, RESPECT_TOKENIZATION,
                        VIEWS_TO_ADD, DISABLE_CACHE, CACHE_FORCE_UPDATE};
        return new ResourceManager(generateProperties(props));
    }
}
