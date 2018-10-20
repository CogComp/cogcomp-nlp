/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Default configuration parameters for WNSim Created by mssammon on 12/30/15.
 */
public class WNSimConfigurator extends Configurator {

	public final Property WN_PATH = new Property("wnPath", "wordnet-dict");
	public final Property PARAPHRASE_PATH = new Property("paraphrasePath", "paraphrase.txt");

	// public final Property PARAGRAM_PATH = new Property("paragramPath",
	// "paragram-data");

	@Override
	public ResourceManager getDefaultConfig() {
		Property[] props = { WN_PATH, PARAPHRASE_PATH };

		return new ResourceManager(generateProperties(props));
	}
}
