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
    public static String[] views = {ViewNames.LEMMA, ViewNames.POS, ViewNames.SHALLOW_PARSE,
            ViewNames.DEPENDENCY_STANFORD, ViewNames.PARSE_STANFORD};
    public static Property VIEWS_TO_ADD = new Property(CoreConfigNames.VIEWS_TO_ADD, getViewsString());

    public static Property DISABLE_CACHE = new Property(AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.TRUE);

	// Curator properties
	public static Property CURATOR_HOST = new Property(CoreConfigNames.CURATOR_HOST, "trollope.cs.illinois.edu");
	public static Property CURATOR_PORT = new Property(CoreConfigNames.CURATOR_PORT, "9010");

	@Override
	public ResourceManager getDefaultConfig() {
		Property[] props = {VIEWS_TO_ADD, DISABLE_CACHE, CURATOR_HOST, CURATOR_PORT};
		return new ResourceManager(generateProperties(props));
	}

	public static ResourceManager defaults() {
		return new PreprocessorConfigurator().getDefaultConfig();
	}

    private static String getViewsString() {
        String viewsString = "";
        for (String view : views) viewsString += "," + view;
        return viewsString.substring(1);
    }
}