package edu.illinois.cs.cogcomp.llm.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.common.LlmConstants;

/**
 * provides default configuration for LLM.
 * Created by mssammon on 9/12/15.
 */
public class LlmConfigurator extends Configurator
{

    public static final Property USE_SIMPLE_SCORE = new Property( "useSimpleScore", Configurator.FALSE);
    public static final Property STOPWORD_FILE = new Property( "stopwordFile", "llmStopwords.txt");
    public static final Property WORD_METRIC = new Property( "wordMetric", "word2vec" ) ;
    public static final Property WORD_ENTAILMENT_THRESHOLD = new Property( "wordEntailmentThreshold", "0.001" );
    public static final Property LLM_ENTAILMENT_THRESHOLD = new Property( "llmThreshold", "0.5" );
    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = { USE_SIMPLE_SCORE, STOPWORD_FILE, WORD_METRIC, WORD_ENTAILMENT_THRESHOLD, LLM_ENTAILMENT_THRESHOLD };

        return new ResourceManager( generateProperties( props ) );
    }
}
