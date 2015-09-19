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

    /**
     *
     * @param config
     * @param viewName  the canonical name for this view.  Should usually be either ViewNames.NER_CONLL
     *                  or ViewNames.NER_ONTONOTES, but if you create a different NER model, you must
     *                  also create a new canonical name, extend the ViewNames class, and add the new
     *                  name as a constant there.
     * @throws IOException
     */
    public IllinoisNerHandler(String config, String viewName) throws IOException {
        super( FULL_NAME_PREFIX + " " + viewName, VERSION, SHORT_NAME_PREFIX + " " + viewName );
        this.nerAnnotator =  NerAnnotatorManager.buildNerAnnotator( config, viewName );
    }

    @Override
    public String getViewName() {
        return this.nerAnnotator.getViewName();
    }

    @Override
    public View getView(TextAnnotation textAnnotation) throws AnnotatorException {
        return nerAnnotator.getView( textAnnotation );
    }

    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }
}
