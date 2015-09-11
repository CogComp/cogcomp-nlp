package edu.illinois.cs.cogcomp.nlp.common;

import edu.illinois.cs.cogcomp.core.utilities.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;

import java.util.Properties;

/**
 * Created by mssammon on 9/11/15.
 */
public class PipelineConfigurator extends Configurator
{
//    nerConllConfig   config/ner-conll-config.properties
//    nerOntonotesConfig  config/ner-ontonotes-config.properties
//    lemmaConfig config/lemmatizer-config.properties

    public static final String NER_CONLL_CONFIG = "nerConllConfig";
    public static final String NER_ONTONOTES_CONFIG = "nerOntonotesConfig";
    public static final String LEMMA_CONFIG = "lemmaConfig";

    private static final String DEFAULT_NER_CONLL = "config/ner-conll-config.properties";
    private static final String DEFAULT_NER_ONTONOTES = "config/ner-ontonotes-config.properties";
    private static final String DEFAULT_LEMMA = "config/lemmatizer-config.properties";

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {

        Properties props = new Properties();
        props.setProperty( NER_CONLL_CONFIG, DEFAULT_NER_CONLL );
        props.setProperty( NER_ONTONOTES_CONFIG, DEFAULT_NER_ONTONOTES );
        props.setProperty( LEMMA_CONFIG, DEFAULT_LEMMA );

        return new ResourceManager( props );
    }
}
