/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.common;


import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;

/**
 * Default configuration for pipeline class
 *
 * @author Mark Sammons
 * @author Christos Christodoulopoulos
 */
public class PipelineConfigurator extends AnnotatorServiceConfigurator {

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
    public static final Property USE_SRL_PREP = new Property("usePrepSRL", FALSE);
    public static final Property USE_SRL_COMMA = new Property("useCommaSRL", FALSE);
    public static final Property USE_QUANTIFIER = new Property("useQuantifier", FALSE);
    public static final Property USE_VERB_SENSE = new Property("useVerbSense", FALSE);
    public static final Property USE_JSON = new Property("useJson", FALSE);
    public static final Property USE_TRANSLITERATION = new Property("useTransliteration", FALSE);
    public static final Property USE_MENTION = new Property("useMention", FALSE);
    public static final Property USE_RELATION = new Property("useRelation", FALSE);
    public static final Property USE_LAZY_INITIALIZATION = new Property(
            AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, TRUE);
    public static final Property USE_SRL_INTERNAL_PREPROCESSOR = new Property(
            SrlConfigurator.INSTANTIATE_PREPROCESSOR.key, FALSE);
    public static final Property USE_TIMEX3 = new Property("useTimex3", FALSE);
    public static final Property USE_DATALESS_ESA = new Property("useDatalessESA", FALSE);
    public static final Property USE_DATALESS_W2V = new Property("useDatalessW2V", FALSE);
    public static final Property USE_QUESTION_TYPER = new Property("useQuestionTyper", FALSE);

    /**
     * if 'true', the PipelineFactory will return a sentence-level pipeline that will use all viable
     * annotators in a "per-sentence" way: it will split the text into sentences, process each
     * individually, then splice the annotations together. This allows for partial annotation of
     * documents in cases where document text causes local problems for individual annotators.
     */
    public static final Property USE_SENTENCE_PIPELINE = new Property("useSentencePipeline", FALSE);

    /**
     * if 'true', set tokenizer to split on hyphen. Default is 'false' until CCG NLP annotator
     * modules are updated to account for hyphen-split training data.
     */
    public static final Property SPLIT_ON_DASH = new Property(TextAnnotationBuilder.SPLIT_ON_DASH,
            FALSE);



    /**
     * get a ResourceManager object with the default key/value pairs for this configurator default
     * SRL_TYPE is Verb.
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties =
                {USE_POS, USE_LEMMA, USE_SHALLOW_PARSE, USE_DEP, USE_NER_CONLL, USE_NER_ONTONOTES,
                        USE_STANFORD_PARSE, USE_STANFORD_DEP, USE_SRL_VERB, USE_SRL_NOM, USE_SRL_PREP, USE_SRL_COMMA,
                        USE_QUANTIFIER, USE_VERB_SENSE, USE_JSON, USE_RELATION,
                        USE_LAZY_INITIALIZATION, USE_SRL_INTERNAL_PREPROCESSOR, SPLIT_ON_DASH,
						USE_SENTENCE_PIPELINE, USE_TIMEX3, USE_MENTION, USE_TRANSLITERATION,
                        USE_DATALESS_ESA, USE_DATALESS_W2V, USE_QUESTION_TYPER};
        
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
