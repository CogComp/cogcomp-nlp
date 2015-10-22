package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;

/**
 * An interface for creating views of a specified name from a
 * {@link TextAnnotation}
 *
 * @author Vivek Srikumar, Mark Sammons, Christos Christodoulopoulos
 */
public interface Annotator {


    public String getViewName();

    public View getView(TextAnnotation ta) throws AnnotatorException;

    /**
     * Can be used internally by {@link edu.illinois.cs.cogcomp.nlp.utilities.BasicAnnotatorService} to check for pre-requisites before calling
     * any single (external) {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by this ViewGenerator
     */
    public String[] getRequiredViews();
}
