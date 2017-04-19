/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.ExternalToolsConfigurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.PathLSTMHandler;

import java.io.IOException;
import java.util.HashMap;
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
    public static BasicAnnotatorService buildPipeline() throws IOException, AnnotatorException {
        ResourceManager emptyConfig = new ResourceManager(new Properties());
        return buildPipeline(emptyConfig);
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
    public static BasicAnnotatorService buildPipeline(ResourceManager rm) throws IOException,
            AnnotatorException {
        // Merges default configuration with the user-specified overrides.
        ResourceManager fullRm = (new ExternalToolsConfigurator()).getConfig(rm);
        Boolean splitOnDash = fullRm.getBoolean(ExternalToolsConfigurator.SPLIT_ON_DASH);
        boolean isSentencePipeline =
                fullRm.getBoolean(ExternalToolsConfigurator.USE_SENTENCE_PIPELINE.key);

        TextAnnotationBuilder taBldr =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnDash));

        Map<String, Annotator> annotators = buildAnnotators();
        return isSentencePipeline ? new BasicAnnotatorService(taBldr, annotators, fullRm)
                : new SentencePipeline(taBldr, annotators, fullRm);
    }

    /**
     * instantiate a set of annotators for use in an AnnotatorService object by default, will use
     * lazy initialization where possible -- change this behavior with the
     * {@link ExternalToolsConfigurator#USE_LAZY_INITIALIZATION} property.
     *
     * @return a Map from annotator view name to annotator
     */
    private static Map<String, Annotator> buildAnnotators() throws IOException {
        Map<String, Annotator> viewGenerators = new HashMap<>();
        PathLSTMHandler pathSRL = new PathLSTMHandler(true);
        viewGenerators.put(pathSRL.getViewName(), pathSRL);

        return viewGenerators;
    }
}
