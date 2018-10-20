/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.utilities;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.verbsense.data.Dataset;

public class VerbSenseConfigurator extends Configurator {

    public static final Property USE_POS = new Property("usePOS", Configurator.TRUE);
    public static final Property USE_CHUNKER = new Property("useChunker", Configurator.TRUE);
    public static final Property USE_LEMMATIZER = new Property("useLemmatizer", Configurator.TRUE);
    public static final Property USE_NER = new Property("useNer", Configurator.FALSE);

    // if true, will read the models from datastore, otherwise from disk or classpath.
    public static final Property LOAD_MODELS_FROM_DATASTORE = new Property("loadFromDatastore",
            Configurator.TRUE);

    // Whether to use the Illinois Curator to get the required annotations for training/testing
    public static final Property USE_CURATOR = new Property("UseCurator", Configurator.FALSE);
    public static final Property CURATOR_HOST = new Property("CuratorHost",
            "trollope.cs.illinois.edu");
    public static final Property CURATOR_PORT = new Property("CuratorPort", "9010");

    // Training corpora directories ###
    // This is the directory of the merged (mrg) WSJ files
    public static final Property PENN_TREEBANK_HOME = new Property("PennTreebankHome",
            "/Users/daniel/ideaProjects/illinois-srl/wsj");
    public static final Property PROPBANK_HOME = new Property("PropbankHome",
            "/Users/daniel/ideaProjects/saul/data2/propbank");
    // The directory of the sentence and pre-extracted features database (~5G of space required)
    // Not used during test/working with pre-trained models
    public static final Property CACHE_DIRECTORY = new Property("CacheDirectory",
            "scratch/verbsense/cache");
    public static final Property MODELS_DIRECTORY = new Property("ModelsDirectory", "models");

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {USE_CHUNKER, USE_LEMMATIZER, USE_NER, USE_POS, USE_CURATOR, CURATOR_HOST,
                        CURATOR_PORT, PENN_TREEBANK_HOME, PROPBANK_HOME, CACHE_DIRECTORY,
                        MODELS_DIRECTORY, LOAD_MODELS_FROM_DATASTORE};
        return new ResourceManager(generateProperties(props));
    }

    public static String getSentenceDBFile(ResourceManager rm) {
        return rm.getString(CACHE_DIRECTORY.key) + "/sentences.db";
    }


    public static String getFeatureCacheFile(String featureSet, Dataset dataset, ResourceManager rm) {
        return rm.getString(CACHE_DIRECTORY.key) + "/features." + featureSet + "." + dataset
                + ".cache";
    }

    public static String getPrunedFeatureCacheFile(String featureSet, ResourceManager rm) {
        return rm.getString(CACHE_DIRECTORY.key) + "/features." + featureSet + ".pruned.cache";
    }


    public static String[] getDevSections() {
        return new String[] {"24"};
    }

    public static String[] getAllTrainSections() {
        return new String[] {"02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "24"};
    }

    public static String getTestSections() {
        return "23";
    }

    public static String[] getAllSections() {
        return new String[] {"02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "24", "23"};

    }

    public static String[] getTrainDevSections() {
        return new String[] {"02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22"};
    }
}
