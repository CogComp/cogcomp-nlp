/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.ExternalToolsConfigurator;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * builds an AnnotatorService with a set of NLP components.
 */
public class ExternalAnnotatorServiceFactory {

    /**
     * create an AnnotatorService with default configuration.
     *
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline(Map<String, Annotator> annotators, String cacheDirectory) throws IOException, AnnotatorException {
        Properties p = new Properties();
        p.setProperty(AnnotatorServiceConfigurator.CACHE_DIR.key, cacheDirectory);
        ResourceManager emptyConfig = new ResourceManager(p);
        return buildPipeline(emptyConfig, annotators);
    }

    /**
     * create an AnnotatorService with components specified by the ResourceManager (to override
     * defaults in {@link ExternalToolsConfigurator}
     *
     * @param rm non-default config options
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline(ResourceManager rm, Map<String, Annotator> annotators) throws IOException,
            AnnotatorException {
        // Merges default configuration with the user-specified overrides.
        ResourceManager fullRm = (new ExternalToolsConfigurator()).getConfig(rm);
        Boolean splitOnDash = fullRm.getBoolean(ExternalToolsConfigurator.SPLIT_ON_DASH);
        boolean isSentencePipeline =
                fullRm.getBoolean(ExternalToolsConfigurator.USE_SENTENCE_PIPELINE.key);

        TextAnnotationBuilder taBldr =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnDash, false));

        return new BasicAnnotatorService(taBldr, annotators, fullRm);
    }
}
