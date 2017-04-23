/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Created by ZhiliFeng on 10/30/16.
 */
public class TemporalChunkerConfigurator extends Configurator {


    public static final Property MODEL_NAME = new Property("modelName", "TBAQ_full_1label_corr50");

    public static final Property MODEL_DIR_PATH = new Property("modelDirPath",
            "lbjava/");

    public static final Property MODEL_PATH = new Property("modelPath", MODEL_DIR_PATH.value
            + MODEL_NAME.value + ".lc");

    public static final Property MODEL_LEX_PATH = new Property("modelLexPath", MODEL_DIR_PATH.value
            + MODEL_NAME.value + ".lex");

    public static final Property DOCUMENT_TYPE = new Property("documentType", "NEWS");

    public static final Property OUTPUT_TYPE = new Property("outputType", "TIMEML");

    public static final Property HEIDELTIME_CONFIG =
            new Property("heideltimeConfig", "edu/illinois/cs/cogcomp/temporal/normalizer/main/conf/heideltime_config.props");

    public static final Property POSTAGGER_TYPE = new Property("posTagger", "NO");

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {MODEL_NAME, MODEL_DIR_PATH, MODEL_PATH, MODEL_LEX_PATH,
                        DOCUMENT_TYPE, OUTPUT_TYPE, HEIDELTIME_CONFIG, POSTAGGER_TYPE};
        return new ResourceManager(generateProperties(props));
    }

}