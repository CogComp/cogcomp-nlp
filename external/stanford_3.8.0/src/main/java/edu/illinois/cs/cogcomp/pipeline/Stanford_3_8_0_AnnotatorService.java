/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordCorefHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordOpenIEHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordRelationsHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordTrueCaseHandler;
import edu.illinois.cs.cogcomp.pipeline.main.ExternalAnnotatorServiceFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Stanford_3_8_0_AnnotatorService {
    public static AnnotatorService service = null;
    public static void initialize() throws IOException, AnnotatorException {
        Map<String, Annotator> viewGenerators = new HashMap<>();

        StanfordCorefHandler corefNLPCoref = new StanfordCorefHandler();
        viewGenerators.put(corefNLPCoref.getViewName(), corefNLPCoref);

        StanfordRelationsHandler mentionHandler = new StanfordRelationsHandler();
        viewGenerators.put(mentionHandler.getViewName(), mentionHandler);

        StanfordOpenIEHandler openIEHandler = new StanfordOpenIEHandler();
        viewGenerators.put(openIEHandler.getViewName(), openIEHandler);

        StanfordTrueCaseHandler trueCaseHandler = new StanfordTrueCaseHandler();
        viewGenerators.put(trueCaseHandler.getViewName(), trueCaseHandler);

        service = ExternalAnnotatorServiceFactory.buildPipeline(viewGenerators,
                Stanford_3_8_0_AnnotatorService.class.getSimpleName());
    }
}
