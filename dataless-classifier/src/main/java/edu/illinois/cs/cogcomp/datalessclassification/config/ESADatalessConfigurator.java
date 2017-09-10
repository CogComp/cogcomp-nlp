package edu.illinois.cs.cogcomp.datalessclassification.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * @author shashank
 */
public class ESADatalessConfigurator extends DatalessConfigurator {

    public static final Property ESA_PATH = new Property("esaPath", "esaEmbedding/esa_vectors.txt");
    public static final Property ESA_Map_PATH = new Property("esaMapPath", "esaEmbedding/idToConceptMap.txt");
    public static final Property ESA_DIM = new Property("esaDimension", "100");

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig () {
        Property[] props = { 
        					ESA_PATH,
        					ESA_Map_PATH,
        					ESA_DIM,
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
