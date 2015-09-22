package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import java.io.IOException;

/**
 * Wraps illinois-ner as an illinois-core-utilities Annotator, for use as a pipeline component.
 *
 * Created by mssammon on 8/24/15.
 */
public class IllinoisNerHandler extends PipelineAnnotator
{
    private static final String SHORT_NAME_PREFIX = "NER";
    private static final String FULL_NAME_PREFIX = "Illinois Named Entity Tagger";
    private static final String VERSION = "2.8.5-SNAPSHOT";
    public NERAnnotator nerAnnotator;

    public IllinoisNerHandler(String config, String modelType) throws IOException {
        super( FULL_NAME_PREFIX + " " + modelType, VERSION, SHORT_NAME_PREFIX + " " + modelType );
        this.nerAnnotator =  NerAnnotatorManager.buildNerAnnotator( config ,modelType);
    }

    @Override
    public String getViewName() {
        return ViewNames.NER_CONLL;
    }

    @Override
    public View getView(TextAnnotation textAnnotation) throws AnnotatorException {
        View nerView = nerAnnotator.getView( textAnnotation );
        textAnnotation.addView( nerAnnotator.getViewName(), nerView );
        return nerView;
    }

    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }
}
