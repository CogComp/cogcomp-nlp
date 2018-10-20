/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.constants.CoreConfigNames;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * The properties for the {@link Preprocessor}.
 */
public class PreprocessorConfigurator extends Configurator {
    public static String[] views = {ViewNames.POS};
    public static Property VIEWS_TO_ADD = new Property(CoreConfigNames.VIEWS_TO_ADD,
            getViewsString());

    public static Property DISABLE_CACHE = new Property(
            AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.TRUE);

    // whether to use Curator or Pipeline
    public static String USE_CURATOR = "USE_CURATOR"; // when this is false, the system makes direct
                                                      // calls to the necessary annotators.
    public static Property USE_PIPELINE = new Property(USE_CURATOR, Configurator.FALSE);

    // Curator properties
    public static Property CURATOR_HOST = new Property(CoreConfigNames.CURATOR_HOST,
            "trollope.cs.illinois.edu");
    public static Property CURATOR_PORT = new Property(CoreConfigNames.CURATOR_PORT, "9010");

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = {VIEWS_TO_ADD, DISABLE_CACHE, CURATOR_HOST, CURATOR_PORT, USE_PIPELINE};
        return new ResourceManager(generateProperties(props));
    }

    public static ResourceManager defaults() {
        return new PreprocessorConfigurator().getDefaultConfig();
    }

    private static String getViewsString() {
        String viewsString = "";
        for (String view : views)
            viewsString += "," + view;
        return viewsString.substring(1);
    }
}
