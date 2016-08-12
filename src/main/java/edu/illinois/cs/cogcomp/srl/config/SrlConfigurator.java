package edu.illinois.cs.cogcomp.srl.config;

import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;

/**
 * A configuration helper to allow centralization of config options in dependencies with
 *    clear default settings (only values that override defaults need to be specified).
 *
 * NOTE: The parameters for Penn Treebank, Nombank, and Propbank are set to directories
 *    on CCG servers, so must be overridden on other machines for uses of SRL that
 *    require these corpora (presumably, for training only).
 *
 * Created by mssammon on 12/21/15.
 */
public class SrlConfigurator extends AnnotatorConfigurator {


    public static final Property USE_CURATOR = new Property("UseCurator", FALSE);
    public static final Property DEFAULT_PARSER = new Property("DefaultParser", "Stanford" );

    /**
     * Num of threads for feat. ext.
     */
    public static final Property NUM_FEX_THREADS = new Property("NumFeatExtThreads", "10" );

    /**
     * The ILP solver to use for the joint inference
     * Options are: Gurobi, OJAlgo
     */
    public static final Property ILP_SOLVER = new Property("ILPSolver", "OJAlgo");
    /**
     * Training corpora directories
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


    /**
     * whether to instantiate a preprocessing pipeline to provide needed inputs to standalone SRL
     */
    public static final Property INSTANTIATE_PREPROCESSOR = new Property( "instantiatePreprocessor", FALSE );

    /**
     * whether to use lazy initialization to defer loading of models etc until actually needed
     */
    public static final Property LAZILY_INITIALIZE =
            new Property( AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, FALSE );


    /**
     * SRL models to load
     */
    public static final Property SRL_TYPE = new Property( "srlType", SRLType.Verb.name() );


    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = {USE_CURATOR, DEFAULT_PARSER, NUM_FEX_THREADS, PENNTB_HOME, PROPBANK_PARSER,
                NOMBANK_HOME, CACHE_DIR, MODEL_DIR, OUTPUT_DIR, ILP_SOLVER, INSTANTIATE_PREPROCESSOR,
                LAZILY_INITIALIZE, SRL_TYPE};
        return (new LearnerConfigurator().getConfig(new ResourceManager(generateProperties(properties))) );
    }
}
