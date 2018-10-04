/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Where possible, run {@link Annotator} members over one sentence at a time and splice their
 * outputs together. Ignore failure of Annotators on individual sentences. Current implementation
 * repeats the split/join process for each layer of annotation; a more efficient route would process
 * each sentence with all annotations, but requires some care as not all annotators will support
 * this mode (e.g. co-reference; and Wikification and NER benefit from more context. For components
 * like NER, expect a performance drop if you use this mode due to the reduced context for
 * decisions.
 * 
 * @author mssammon
 */
public class SentencePipeline extends BasicAnnotatorService {

    static private Logger logger = LoggerFactory.getLogger(SentencePipeline.class);

    /**
     * constructor with ResourceManager properties for caching behavior
     *
     * @param textAnnotationBuilder tokenizes and sentence splits input text.
     * @param viewProviders Annotators that populate a View with the same name as the corresponding
     *        Annotator key.
     * @param rm A {@link ResourceManager} containing cache configuration options.
     * @throws AnnotatorException
     */
    public SentencePipeline(TextAnnotationBuilder textAnnotationBuilder,
            Map<String, Annotator> viewProviders, ResourceManager rm) throws AnnotatorException {
        super(textAnnotationBuilder, viewProviders, rm);
    }

    /**
     * DOES NOT CACHE THE ADDED VIEW!!!
     *
     * @param textAnnotation textAnnotation to be modified
     * @param viewName name of view to be added
     * @return 'true' if textAnnotation was modified
     * @throws AnnotatorException
     */
    @Override
    public boolean addView(TextAnnotation textAnnotation, String viewName)
            throws AnnotatorException {
        boolean isUpdated = false;
        logger.debug("in addView()...");

        if (ViewNames.SENTENCE.equals(viewName) || ViewNames.TOKENS.equals(viewName))
            return false;

        if (!textAnnotation.hasView(viewName) || super.forceUpdate) {
            isUpdated = true;

            if (!viewProviders.containsKey(viewName))
                throw new AnnotatorException("View '" + viewName
                        + "' cannot be provided by this AnnotatorService.");

            Annotator annotator = viewProviders.get(viewName);

            for (String prereqView : annotator.getRequiredViews()) {
                addView(textAnnotation, prereqView);
            }


            View v = null;

            if (!annotator.isSentenceLevel()) {
                v = annotator.getView(textAnnotation);
                textAnnotation.addView(annotator.getViewName(), v);
            } else
                processBySentence(annotator, textAnnotation);
        }

        if (isUpdated && throwExceptionIfNotCached)
            throwNotCachedException(textAnnotation.getCorpusId(), textAnnotation.getId(),
                    textAnnotation.getText());
        return isUpdated;
    }

    /**
     * Process each sentence individually. This potentially allows for failure at an individual
     * sentence level, without failing for the whole text. THIS REQUIRES THAT ALL RELATIONS ARE
     * INTRA-SENTENCE. Any that are *not* will be omitted for the sentence-level processing.
     *
     * @param annotator Annotator to apply
     * @param textAnnotation TextAnnotation to augment
     * @return
     */
    public void processBySentence(Annotator annotator, TextAnnotation textAnnotation) {
        logger.debug("in processBySentence()...");
        for (int sentenceId = 0; sentenceId < textAnnotation.sentences().size(); ++sentenceId) {
            TextAnnotation sentTa =
                    TextAnnotationUtilities.getSubTextAnnotation(textAnnotation, sentenceId);
            try {
                annotator.getView(sentTa);
                int start = textAnnotation.getSentence(sentenceId).getStartSpan();
                int end = textAnnotation.getSentence(sentenceId).getEndSpan();
                TextAnnotationUtilities.copyViewFromTo(annotator.getViewName(), sentTa,
                        textAnnotation, start, end, start);
            } catch (AnnotatorException e) {
                e.printStackTrace();
            }
        }
        return;
    }

}
