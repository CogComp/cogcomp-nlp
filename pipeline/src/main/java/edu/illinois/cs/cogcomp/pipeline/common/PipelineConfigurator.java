/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.common;


import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;

/**
 * Default configuration for pipeline class
 *
 * @author Mark Sammons
 * @author Christos Christodoulopoulos
 */
public class PipelineConfigurator extends AnnotatorConfigurator {
    public static final Property STFRD_TIME_PER_SENTENCE = new Property(
            "stanfordMaxTimePerSentence", "100000");
    public static final Property STFRD_MAX_SENTENCE_LENGTH = new Property(
            "stanfordParseMaxSentenceLength", "80");

    // flags for individual components; default is TRUE for everything
    public static final Property USE_POS = new Property("usePos", FALSE);
    // The default dependency parse is Stanford -- switch with USE_STANFORD_DEP
    public static final Property USE_DEP = new Property("useDep", FALSE);
    public static final Property USE_LEMMA = new Property("useLemma", FALSE);
    public static final Property USE_SHALLOW_PARSE = new Property("useShallowParse", FALSE);
    public static final Property USE_NER_CONLL = new Property("useNerConll", FALSE);
    public static final Property USE_NER_ONTONOTES = new Property("useNerOntonotes", FALSE);
    public static final Property USE_STANFORD_PARSE = new Property("useStanfordParse", FALSE);
    public static final Property USE_STANFORD_DEP = new Property("useStanfordDep", FALSE);
    public static final Property USE_SRL_VERB = new Property("useSrlVerb", FALSE);
    public static final Property USE_SRL_NOM = new Property("useSrlNom", FALSE);
    public static final Property USE_QUANTIFIER = new Property("useQuantifier", FALSE);
    public static final Property THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK = new Property(
            "throwExceptionOnFailedLengthCheck", TRUE);
    public static final Property USE_JSON = new Property("useJson", FALSE);
    public static final Property USE_LAZY_INITIALIZATION = new Property(
            AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, TRUE);
    public static final Property USE_SRL_INTERNAL_PREPROCESSOR = new Property(
            SrlConfigurator.INSTANTIATE_PREPROCESSOR.key, FALSE);
    public static final Property SPLIT_ON_DASH = new Property("splitOnDash", TRUE);

    /**
     * if 'true', the PipelineFactory will return a sentence-level pipeline that will use all viable annotators in
     *     a "per-sentence" way: it will split the text into sentences, process each individually, then splice the
     *     annotations together. This allows for partial annotation of documents in cases where document text
     *     causes local problems for individual annotators.
     */
    public static final Property USE_SENTENCE_PIPELINE = new Property("useSentencePipeline", FALSE);

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     * default SRL_TYPE is Verb.
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties =
                {STFRD_TIME_PER_SENTENCE, STFRD_MAX_SENTENCE_LENGTH, USE_POS, USE_LEMMA,
                        USE_SHALLOW_PARSE, USE_DEP, USE_NER_CONLL, USE_NER_ONTONOTES,
                        USE_STANFORD_PARSE, USE_STANFORD_DEP, USE_SRL_VERB, USE_SRL_NOM,
                        USE_QUANTIFIER, THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK, USE_JSON,
                        USE_LAZY_INITIALIZATION, USE_SRL_INTERNAL_PREPROCESSOR, SPLIT_ON_DASH,
                        USE_SENTENCE_PIPELINE};
        return (new AnnotatorServiceConfigurator().getConfig(new ResourceManager(
                generateProperties(properties))));
    }

    /**
     * Get a {@link ResourceManager} with non-default properties. Overloaded to merge the properties
     * of {@link AnnotatorServiceConfigurator}.
     *
     * @param nonDefaultRm The non-default properties
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getConfig(ResourceManager nonDefaultRm) {
        ResourceManager pipelineRm = super.getConfig(nonDefaultRm);
        return new AnnotatorServiceConfigurator().getConfig(pipelineRm);
    }
}
