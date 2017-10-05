package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

/**
 * Created by stephen on 11/7/15.
 */
public class TransliterationAnnotator implements Annotator {
    @Override
    public String getViewName() {
        return "TRANSLITERATION";
    }

    @Override
    public View getView(TextAnnotation textAnnotation) throws AnnotatorException {
        // FIXME: annotate the textannotation here.

        return null;
    }

    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }
}
