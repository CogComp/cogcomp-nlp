package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mssammon on 2/22/17.
 */
public class SentencePipeline extends BasicAnnotatorService {
    /**
     * constructor with ResourceManager properties for caching behavior
     *
     * @param textAnnotationBuilder tokenizes and sentence splits input text.
     * @param viewProviders         Annotators that populate a View with the same name as the corresponding
     *                              Annotator key.
     * @param rm                    A {@link ResourceManager} containing cache configuration options.
     * @throws AnnotatorException
     */
    public SentencePipeline(TextAnnotationBuilder textAnnotationBuilder, Map<String, Annotator> viewProviders, ResourceManager rm) throws AnnotatorException {
        super(textAnnotationBuilder, viewProviders, rm);
    }

    /**
     * DOES NOT CACHE THE ADDED VIEW!!!
     *
     * @param textAnnotation textAnnotation to be modified
     * @param viewName       name of view to be added
     * @return 'true' if textAnnotation was modified
     * @throws AnnotatorException
     */
    @Override
    public boolean addView(TextAnnotation textAnnotation, String viewName) throws AnnotatorException {
        boolean isUpdated = false;

        if (ViewNames.SENTENCE.equals(viewName) || ViewNames.TOKENS.equals(viewName))
            return false;

        if (!textAnnotation.hasView(viewName) || super.forceUpdate) {
            isUpdated = true;

            if (!viewProviders.containsKey(viewName))
                throw new AnnotatorException("View '" + viewName + "' cannot be provided by this AnnotatorService.");

            Annotator annotator = viewProviders.get(viewName);

            for (String prereqView : annotator.getRequiredViews()) {
                addView(textAnnotation, prereqView);
            }


            View v = null;

            if (!annotator.isSentenceLevel())
                v = annotator.getView(textAnnotation);
            else
                v = processBySentence(annotator, textAnnotation);


            textAnnotation.addView(annotator.getViewName(), v);
        }

        if (isUpdated && throwExceptionIfNotCached)
            throwNotCachedException(textAnnotation.getCorpusId(), textAnnotation.getId(), textAnnotation.getText());
        return isUpdated;
    }

    /**
     * Process each sentence individually. This potentially allows for failure at an individual sentence level,
     *      without failing for the whole text.
     * THIS REQUIRES THAT ALL RELATIONS ARE INTRA-SENTENCE.
     * @param annotator Annotator to apply
     * @param textAnnotation  TextAnnotation to augment
     * @return
     */
    private View processBySentence(Annotator annotator, TextAnnotation textAnnotation) {
        View sentences = textAnnotation.getView(ViewNames.SENTENCE);
        for (Constituent sentence : sentences) {
            int startTokOffset = sentence.getStartSpan();
            int endTokOffset = sentence.getEndSpan();

            for (String viewName : textAnnotation.getAvailableViews()) {
                View v = textAnnotation.getView(viewName);
                Set<Constituent> constituents = new HashSet<>();
                for (int i = startTokOffset; i < endTokOffset; ++i) {
                    constituents.addAll(v.getConstituentsCoveringToken(i));
                }
                Set<Relation> relations = new HashSet<>();
                for (Constituent c : constituents) {
                    relations.addAll(c.getOutgoingRelations());
                }

            }
        }
        return null;
    }

}

