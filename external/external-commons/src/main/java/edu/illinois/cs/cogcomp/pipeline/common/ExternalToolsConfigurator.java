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

/**
 * Default configuration for external annotators
 */
public class ExternalToolsConfigurator extends AnnotatorConfigurator {
    public static final Property USE_JSON = new Property("useJson", FALSE);
    public static final Property USE_LAZY_INITIALIZATION = new Property(
            AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, TRUE);
    public static final Property USE_SRL_INTERNAL_PREPROCESSOR = new Property("instantiatePreprocessorkey", FALSE);

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
                {USE_JSON, USE_LAZY_INITIALIZATION, USE_SRL_INTERNAL_PREPROCESSOR, SPLIT_ON_DASH,
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
