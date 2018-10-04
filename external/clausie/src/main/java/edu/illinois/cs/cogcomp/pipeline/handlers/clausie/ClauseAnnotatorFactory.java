/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers.clausie;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ClauseAnnotatorFactory {

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
     *
     * @param rm non-default config options
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline(ResourceManager rm) throws IOException,
            AnnotatorException {
        TextAnnotationBuilder taBldr =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer(false, false));

        Map<String, Annotator> annotators = buildAnnotators();
        return new BasicAnnotatorService(taBldr, annotators, rm);
    }

    private static Map<String, Annotator> buildAnnotators() throws IOException {
        Map<String, Annotator> viewGenerators = new HashMap<>();
        ClausIEAnnotator annotator = new ClausIEAnnotator();
        viewGenerators.put(annotator.getViewName(), annotator);
        return viewGenerators;
    }
}
