/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.datalessclassification.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * @author shashank
 */
public class DatalessConfigurator extends Configurator {
    public static final Property BottomUp_Inference = new Property("inferenceBottomUp", "True");
    
    public static final Property LabelHierarchy_Path = new Property("labelHierarchyPath", "hierarchies/20newsgroups/parentChildIdMap.txt");
    public static final Property LabelName_Path = new Property("labelNamePath", "hierarchies/20newsgroups/idToLabelNameMap.txt");
    public static final Property LabelDesc_Path = new Property("labelDescPath", "hierarchies/20newsgroups/labelDesc_Kws_simple.txt");
//  public static final Property LabelDesc_Path = new Property("labelDescPath", "hierarchies/20newsgroups/labelDesc_Kws_embellished.txt");
    
    public static final Property classifierThreshold = new Property("classifierThreshold", "0.99");
	public static final Property classifierLeastK = new Property("classifierLeastK", "1");
	public static final Property classifierMaxK = new Property("classifierMaxK", "3");

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig () {
        Property[] props = {
        					BottomUp_Inference,
        					LabelHierarchy_Path,
        					LabelName_Path,
        					LabelDesc_Path,
        					classifierThreshold,
        					classifierLeastK,
        					classifierMaxK
        					};

        return new ResourceManager(generateProperties(props));
    }
}
