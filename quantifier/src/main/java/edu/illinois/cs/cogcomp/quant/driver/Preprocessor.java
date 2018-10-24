/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.driver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorClient;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;

/**
 * An annotation preprocessor used by all the modules. Can use either the {@link CuratorClient} or
 * other annotators directly. The configurations parameters are set in
 * {@link PreprocessorConfigurator} and should be merged with {@link AnnotatorConfigurator}.
 */
class Preprocessor {

    private final ResourceManager rm;
    private AnnotatorService annotator;

    private static Map<String, Annotator> buildAnnotators() throws IOException {
        Map<String, Annotator> viewGenerators = new HashMap<>();
        POSAnnotator pos = new POSAnnotator();
        viewGenerators.put(pos.getViewName(), pos);
        return viewGenerators;
    }

    Preprocessor(ResourceManager rm) {
        Map<String, String> nonDefaultValues = new HashMap<>();
        nonDefaultValues.put(CuratorConfigurator.RESPECT_TOKENIZATION.key, Configurator.TRUE);
        nonDefaultValues.put("cacheDirectory", "annotation-cache-quantifier");
        this.rm =
                Configurator.mergeProperties(rm,
                        new AnnotatorServiceConfigurator().getConfig(nonDefaultValues));
        if (!rm.getBoolean(PreprocessorConfigurator.USE_CURATOR)) {
            try {
                annotator =
                        new BasicAnnotatorService(new TokenizerTextAnnotationBuilder(
                                new StatefulTokenizer()), buildAnnotators(), this.rm);
            } catch (AnnotatorException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                annotator = CuratorFactory.buildCuratorClient(this.rm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add the required views to the {@link TextAnnotation}. The views are specified in
     * {@link PreprocessorConfigurator#VIEWS_TO_ADD}.
     *
     * @param ta The {@link TextAnnotation} to be annotated
     * @return Whether new views were added
     */
    boolean annotate(TextAnnotation ta) throws AnnotatorException {
        boolean addedViews = false;
        for (String view : rm.getCommaSeparatedValues(PreprocessorConfigurator.VIEWS_TO_ADD)) {
            if (ta.hasView(view))
                continue;
            annotator.addView(ta, view);
            addedViews = true;
        }
        return addedViews;
    }
}
