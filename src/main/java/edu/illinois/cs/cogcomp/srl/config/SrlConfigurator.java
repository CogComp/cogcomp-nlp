package edu.illinois.cs.cogcomp.srl.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by mssammon on 12/21/15.
 */
public class SrlConfigurator extends Configurator {


    public static final Property USE_CURATOR = new Property("UseCurator", FALSE);
    public static final Property DEFAULT_PARSER = new Property("DefaultParser", "Stanford" );

    /**
     * Num of threads for feat. ext.
     */
    public static final Property NUM_FEX_THREADS = new Property("NumFeatExtThreads", "10" );

    /**
     *     Training corpora directories
     */

    //This is the directory of the merged (mrg) WSJ files
    public static final Property PENNTB_HOME = new Property("PennTreebankHome", "/shared/corpora/corporaWeb/treebanks/eng/pennTreebank/treebank-3/parsed/mrg/wsj/" );
    public static final Property PROPBANK_PARSER = new Property( "PropbankHome", "/shared/corpora/corporaWeb/treebanks/eng/propbank_1/data" );
    public static final Property NOMBANK_HOME = new Property( "NombankHome", "/shared/corpora/corporaWeb/treebanks/eng/nombank/" );


    /**
     * The directory of the sentence and pre-extracted features database (~5G of space required)
     * Not used during test/working with pre-trained models
     */

    public static final Property CACHE_DIR = new Property( "CacheDirectory", "cache");

    public static final Property MODEL_DIR = new Property( "ModelsDirectory", "models");


/**
 *    Directory to output gold and predicted files for manual comparison
 */
    public static final Property OUTPUT_DIR = new Property( "OutputDirectory", "srl-out");

//    public SrlConfigurator(URL url) throws IOException {
//        getConfig( new ResourceManager( url.getFile() ) );
//    }


    @Override
    public ResourceManager getDefaultConfig() {

        Property[] properties = {USE_CURATOR, DEFAULT_PARSER, NUM_FEX_THREADS, PENNTB_HOME, PROPBANK_PARSER,
                NOMBANK_HOME, CACHE_DIR, MODEL_DIR };
        return (new LearnerConfigurator().getConfig(new ResourceManager(generateProperties(properties))) );
    }
}
