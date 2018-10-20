/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Created by nitishgupta on 3/25/16.
 */
public class ChunkerConfigurator extends Configurator {

    public static final Property TRAINING_DATA = new Property("trainingData",
            "/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/train.txt");

    public static final Property TEST_GOLDPOS_DATA = new Property("testGoldPOSData",
            "/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/test.txt");

    public static final Property TEST_NOPOS_DATA = new Property("testNoPOSData",
                    "/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/test.noPOS.txt");

    /*Change the following two properties if a retrained model is desired*/
    public static final Property MODEL_NAME = new Property("modelName", "Chunker");
    public static final Property MODEL_DIR_PATH = new Property("modelDirPath", "ChunkerModel/");

    public static final Property MODEL_PATH = new Property("modelPath", MODEL_DIR_PATH.value
            + MODEL_NAME.value + ".lc");

    public static final Property MODEL_LEX_PATH = new Property("modelLexPath", MODEL_DIR_PATH.value
            + MODEL_NAME.value + ".lex");


    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {TRAINING_DATA, TEST_GOLDPOS_DATA, TEST_NOPOS_DATA, MODEL_NAME, MODEL_DIR_PATH,
                        MODEL_PATH, MODEL_LEX_PATH};
        return new ResourceManager(generateProperties(props));
    }

}
