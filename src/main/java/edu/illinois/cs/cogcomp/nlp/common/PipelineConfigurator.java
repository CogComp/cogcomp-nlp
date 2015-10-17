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
//    nerConllConfig   config/ner-conll-config-default.properties
//    nerOntonotesConfig  config/ner-ontonotes-config-default.properties
//    lemmaConfig config/lemmatizer-config-default.properties

    public static final String NER_CONLL_CONFIG = "nerConllConfig";
    public static final String NER_ONTONOTES_CONFIG = "nerOntonotesConfig";
    public static final String LEMMA_CONFIG = "lemmaConfig";
    public static final java.lang.String STFRD_TIME_PER_SENTENCE = "stanfordMaxTimePerSentence";
    public static final java.lang.String STFRD_MAX_SENTENCE_LENGTH = "stanfordParseMaxSentenceLength";
//    public static final String ACTIVE_VIEWS = "activeViews";
    public static final java.lang.String SIMPLE_CACHE_DIR = "simpleCacheDir";

    // flags for individual components; default is TRUE for everything
    public static final String USE_POS = "usePos";
    public static final String USE_LEMMA = "useLemma";
    public static final String USE_SHALLOW_PARSE = "useShallowParse";
    public static final String USE_NER_CONLL = "useNerConll";
    public static final String USE_NER_ONTONOTES = "useNerOntonotes";
    public static final String USE_STANFORD_PARSE = "useStanfordParse";
    public static final String USE_STANFORD_DEP = "useStanfordDep";



    private static final String DEFAULT_NER_CONLL = "ner-conll-config-default.properties";
    private static final String DEFAULT_NER_ONTONOTES = "ner-ontonotes-config-default.properties";
    private static final String DEFAULT_LEMMA = "lemmatizer-config-default.properties";
    private static final String DEFAULT_TIME_PER_SENTENCE = "1000"; // presumably, in ms and per sentence, not document
    private static final String DEFAULT_MAX_PARSE_SENTENCE_LENGTH = "60";
    private static final String DEFAULT_SIMPLE_CACHE_DIR = "simple-annotation-cache";
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
        props.setProperty( SIMPLE_CACHE_DIR, DEFAULT_SIMPLE_CACHE_DIR );
        props.setProperty( USE_POS, TRUE );
        props.setProperty( USE_LEMMA, TRUE );
        props.setProperty( USE_SHALLOW_PARSE, TRUE );
        props.setProperty( USE_NER_CONLL, TRUE );
        props.setProperty( USE_NER_ONTONOTES, TRUE );
        props.setProperty( USE_STANFORD_DEP, TRUE );
        props.setProperty( USE_STANFORD_PARSE, TRUE );

        return ( new AnnotatorServiceConfigurator().getConfig( new ResourceManager( props ) ) );
    }
}
