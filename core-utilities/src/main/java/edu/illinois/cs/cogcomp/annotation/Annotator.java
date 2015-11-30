package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

/**
 * An interface for creating views of a specified name from a
 * {@link TextAnnotation}
 *
 * @author Vivek Srikumar, Mark Sammons, Christos Christodoulopoulos
 */
public abstract class Annotator {


    protected String viewName;
    protected String[] requiredViews;


    /**
     * set the name of the View this Annotator creates, and the list of prerequisite Views that this Annotator
     *    requires as input
     *
     * @param viewName
     * @param requiredViews
     */
    public Annotator( String viewName, String[] requiredViews )
    {
        this.viewName = viewName;
        this.requiredViews = requiredViews;
    }


    /**
     * create and add the view named by getViewName() to the TextAnnotation argument.
     *
     * @param ta
     */
    public abstract void addView( TextAnnotation ta ) throws AnnotatorException;


    /**
     * return the name of the View created by this Annotator
     * @return
     */
    public String getViewName() {
        return viewName;
    }


    /**
     * add the view named by getViewName() to the TextAnnotation argument, and return the View
     *
     * @param ta
     * @return
     * @throws AnnotatorException
     */
    public View getView(TextAnnotation ta) throws AnnotatorException
    {
        addView( ta );
        return ta.getView( viewName );
    }

    /**
     * Can be used internally by {@link BasicAnnotatorService} to check for pre-requisites before calling
     * any single (external) {@link Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by this ViewGenerator
     */
    public String[] getRequiredViews()
    {
        return requiredViews;
    }

}
