/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.PathLSTMAnnotator2;

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
     * defaults in {@link PipelineConfigurator}
     *
     * @param rm non-default config options
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline(ResourceManager rm) throws IOException,
            AnnotatorException {
        // Merges default configuration with the user-specified overrides.
        ResourceManager fullRm = (new PipelineConfigurator()).getConfig(rm);
        Boolean splitOnDash = fullRm.getBoolean(PipelineConfigurator.SPLIT_ON_DASH);
        boolean isSentencePipeline = fullRm.getBoolean(PipelineConfigurator.USE_SENTENCE_PIPELINE.key);

        TextAnnotationBuilder taBldr = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnDash));

        Map<String, Annotator> annotators = buildAnnotators();
        return isSentencePipeline ? new BasicAnnotatorService(taBldr, annotators, fullRm) :
                new SentencePipeline(taBldr, annotators, fullRm);
    }

    /**
     * instantiate a set of annotators for use in an AnnotatorService object by default, will use
     * lazy initialization where possible -- change this behavior with the
     * {@link PipelineConfigurator#USE_LAZY_INITIALIZATION} property.
     *
     * @return a Map from annotator view name to annotator
     */
    private static Map<String, Annotator> buildAnnotators()
            throws IOException {
        Map<String, Annotator> viewGenerators = new HashMap<>();
        PathLSTMAnnotator2 pathSRL = new PathLSTMAnnotator2(ViewNames.SRL_VERB, new String[]{});
        viewGenerators.put(pathSRL.getViewName(), pathSRL);

        return viewGenerators;
    }

    public static void main(String[] args) throws AnnotatorException, IOException {
        System.out.println("Starting to run the dummy . . . ");
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        System.out.println(ta.getAvailableViews());
        ta.addView(ViewNames.SRL_VERB, ta.getView(ViewNames.PARSE_GOLD));
        System.out.println(ta.getAvailableViews());
        System.out.println("Building ExternalAnnotatorServiceFactory . . . ");
        AnnotatorService service = ExternalAnnotatorServiceFactory.buildPipeline();
        System.out.println("Done building ExternalAnnotatorServiceFactory . . . ");
        service.addView(ta, ViewNames.SRL_VERB);
        System.out.println("After ExternalAnnotatorServiceFactory  addView . . . ");
        System.out.println(ta.getAvailableViews());
    }

}
