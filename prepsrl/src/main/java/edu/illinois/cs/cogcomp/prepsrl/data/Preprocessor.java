/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl.data;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.depparse.DepAnnotator;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An annotation preprocessor for {@link PrepSRLDataReader}. The configurations parameters are set
 * in {@link PrepSRLConfigurator}.
 */
public class Preprocessor {
    private static Logger logger = LoggerFactory.getLogger(Preprocessor.class);

    private AnnotatorService annotator;

    public Preprocessor(ResourceManager rm) {
        ResourceManager fullRm =
                Configurator.mergeProperties(new AnnotatorServiceConfigurator().getDefaultConfig(),
                        rm);
        try {
            TextAnnotationBuilder taBldr =
                    new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, false));
            Map<String, Annotator> annotators = new HashMap<>();
            annotators.put(ViewNames.POS, new POSAnnotator());
            annotators.put(ViewNames.LEMMA, new IllinoisLemmatizer());
            annotators.put(ViewNames.SHALLOW_PARSE, new ChunkerAnnotator());
            annotators.put(ViewNames.DEPENDENCY, new DepAnnotator());
            annotator = new BasicAnnotatorService(taBldr, annotators, fullRm);
        } catch (Exception e) {
            logger.error("Unable to create preprocessor. \n{}", e.getMessage());
        }
    }

    /**
     * Add the required views to the {@link TextAnnotation}.
     *
     * @param ta The {@link TextAnnotation} to be annotated
     * @return Whether new views were added
     */
    public boolean annotate(TextAnnotation ta) throws AnnotatorException {
        boolean addedViews = false;
        for (String view : annotator.getAvailableViews()) {
            if (ta.hasView(view))
                continue;
            annotator.addView(ta, view);
            addedViews = true;
        }
        return addedViews;
    }
}
