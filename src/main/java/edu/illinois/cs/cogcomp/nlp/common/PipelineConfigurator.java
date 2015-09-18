package edu.illinois.cs.cogcomp.nlp.common;

import edu.illinois.cs.cogcomp.core.utilities.AnnotatorServiceConfigurator;
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
    public static final java.lang.String STFRD_TIME_PER_SENTENCE = "stanfordMaxTimePerSentence";
    public static final java.lang.String STFRD_MAX_SENTENCE_LENGTH = "stanfordParseMaxSentenceLength";

    private static final String DEFAULT_NER_CONLL = "config/ner-conll-config.properties";
    private static final String DEFAULT_NER_ONTONOTES = "config/ner-ontonotes-config.properties";
    private static final String DEFAULT_LEMMA = "config/lemmatizer-config.properties";
    private static final String DEFAULT_TIME_PER_SENTENCE = "1000"; // presumably, in ms and per sentence, not document
    private static final String DEFAULT_MAX_PARSE_SENTENCE_LENGTH = "60";
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
        props.setProperty( STFRD_TIME_PER_SENTENCE, DEFAULT_TIME_PER_SENTENCE );
        props.setProperty( STFRD_MAX_SENTENCE_LENGTH, DEFAULT_MAX_PARSE_SENTENCE_LENGTH );

//        return ( new AnnotatorServiceConfigurator() ).getConfig(new ResourceManager( props ));
        return new ResourceManager(props);
    }
}
