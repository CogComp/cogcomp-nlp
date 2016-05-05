package edu.illinois.cs.cogcomp.nlp.common;


import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Default configuration for pipeline class
 *
 * @author Mark Sammons
 * @author Christos Christodoulopoulos
 */
public class PipelineConfigurator extends Configurator
{
//    public static final Property NER_CONLL_CONFIG = new Property("nerConllConfig",
//            "ner-conll-config-default.properties");
//    public static final Property NER_ONTONOTES_CONFIG = new Property("nerOntonotesConfig",
//            "ner-ontonotes-config-default.properties");
//    public static final Property LEMMA_CONFIG = new Property("lemmaConfig", "lemmatizer-config-default.properties");
    // presumably, in ms and per sentence, not document
    public static final Property STFRD_TIME_PER_SENTENCE = new Property("stanfordMaxTimePerSentence", "10000");
    public static final Property STFRD_MAX_SENTENCE_LENGTH = new Property("stanfordParseMaxSentenceLength", "80");
//    public static final Property ACTIVE_VIEWS = "activeViews";
    public static final Property SIMPLE_CACHE_DIR = new Property("simpleCacheDir", "simple-annotation-cache");

    // flags for individual components; default is TRUE for everything
    public static final Property USE_POS = new Property("usePos", TRUE);
    public static final Property USE_LEMMA = new Property("useLemma", TRUE);
    public static final Property USE_SHALLOW_PARSE = new Property("useShallowParse", TRUE);
    public static final Property USE_NER_CONLL = new Property("useNerConll", TRUE);
    public static final Property USE_NER_ONTONOTES = new Property("useNerOntonotes", TRUE);
    public static final Property USE_STANFORD_PARSE = new Property("useStanfordParse", TRUE);
    public static final Property USE_STANFORD_DEP = new Property("useStanfordDep", TRUE);
    public static final Property USE_SRL_VERB = new Property("useSrlVerb", TRUE);
    public static final Property USE_SRL_NOM = new Property("useSrlNom", TRUE);
    public static final Property THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK = new Property("throwExceptionOnFailedLengthCheck", TRUE );


    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = {STFRD_TIME_PER_SENTENCE,
                STFRD_MAX_SENTENCE_LENGTH, SIMPLE_CACHE_DIR, USE_POS, USE_LEMMA, USE_SHALLOW_PARSE,
                USE_NER_CONLL, USE_NER_ONTONOTES, USE_STANFORD_PARSE, USE_STANFORD_DEP, USE_SRL_VERB, USE_SRL_NOM,
                THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK};
        return (new AnnotatorServiceConfigurator().getConfig(new ResourceManager(generateProperties(properties))));
    }
}
