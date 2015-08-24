package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

/**
 * Created by mssammon on 8/24/15.
 */
public class NerHandler extends PipelineAnnotator
{
    public NerHandler(String fullName, String version, String shortName) {
        super(fullName, version, shortName);
    }

    @Override
    public String getViewName() {
        return null;
    }

    @Override
    public View getView(TextAnnotation textAnnotation) throws AnnotatorException {
        return null;
    }

    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }
}
