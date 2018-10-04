/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;

/**
 * The basic Configurator used by various variants of the Dataless Annotator
 *
 * @author shashank
 */
public abstract class DatalessConfigurator extends Configurator {
    public static final Property BottomUp_Inference = new Property("inferenceBottomUp", "True");

    public static final Property JSON_Hierarchy_Path = new Property("jsonHierarchyPath", "");

    public static final Property LabelHierarchy_Path = new Property("labelHierarchyPath",
            "hierarchies/20newsgroups/parentChildIdMap.txt");
    public static final Property LabelName_Path = new Property("labelNamePath",
            "hierarchies/20newsgroups/idToLabelNameMap.txt");
    public static final Property LabelDesc_Path = new Property("labelDescPath",
            "hierarchies/20newsgroups/labelDesc_Kws_embellished.txt");

    public static final Property topK = new Property("topK", "1");
    public static final Property classifierThreshold = new Property("classifierThreshold", "0.99");
    public static final Property classifierLeastK = new Property("classifierLeastK", "1");
    public static final Property classifierMaxK = new Property("classifierMaxK", "3");
}
