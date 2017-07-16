/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.pipeline.handlers.clausie.ClausIEAnnotator;
import edu.illinois.cs.cogcomp.pipeline.main.ExternalAnnotatorServiceFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClausIE_AnnotatorService {
    public static AnnotatorService service = null;
    public static void initialize() throws IOException, AnnotatorException {
        Map<String, Annotator> viewGenerators = new HashMap<>();
        ClausIEAnnotator annotator = new ClausIEAnnotator();
        viewGenerators.put(annotator.getViewName(), annotator);
        service = ExternalAnnotatorServiceFactory.buildPipeline(viewGenerators);
    }
}
