/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.ta.W2VDatalessAnnotator;

/**
 * The Configurator used by {@link W2VDatalessAnnotator}
 *
 * @author shashank
 */
public class W2VDatalessConfigurator extends DatalessConfigurator {

    public static final Property W2V_DIM = new Property("w2vDimension", "100");

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {W2V_DIM, BottomUp_Inference, JSON_Hierarchy_Path, LabelHierarchy_Path,
                        LabelName_Path, LabelDesc_Path, topK, classifierThreshold,
                        classifierLeastK, classifierMaxK};

        return new ResourceManager(generateProperties(props));
    }
}
