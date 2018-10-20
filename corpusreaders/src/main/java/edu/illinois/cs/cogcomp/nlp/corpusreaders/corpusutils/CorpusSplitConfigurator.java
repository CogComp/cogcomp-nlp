/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Parameters for ERE corpus splitter.
 *
 * @author mssammon
 */
public class CorpusSplitConfigurator extends Configurator {

    public static final Property TRAIN_FRACTION = new Property("trainFraction", "0.7");
    public static final Property DEV_FRACTION = new Property("devFraction", "0.1");
    public static final Property TEST_FRACTION = new Property("testFraction", "0.2");
    public static final Property VIEWS_TO_CONSIDER = new Property("viewsToConsider",
            StringUtils.join(",", new String[]{ViewNames.EVENT_ERE,ViewNames.MENTION_ERE}));


    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = { TRAIN_FRACTION, DEV_FRACTION, TEST_FRACTION, VIEWS_TO_CONSIDER };
        return new ResourceManager(generateProperties(props));
    }
}
