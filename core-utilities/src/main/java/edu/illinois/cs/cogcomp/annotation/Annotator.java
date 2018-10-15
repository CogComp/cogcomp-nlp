/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * An interface for creating views of a specified name from a {@link TextAnnotation}. Supports lazy initialization.
 * IMPORTANT FOR DERIVED CLASSES: if you initialize fields at declaration rather than in the constructor,
 *     those assignments take place AFTER the super.constructor has run -- so e.g. you cannot
 *     use a Logger you declare in this way to log messages in the initialize() method that
 *     is called by the Annotator class constructor.
 *
 * @author Vivek Srikumar, Mark Sammons, Christos Christodoulopoulos
 */
public abstract class Annotator {
    private static Logger logger = LoggerFactory.getLogger(Annotator.class);

    protected String viewName;
    protected String[] requiredViews;
    protected boolean isInitialized;

    /**
     * stores configuration for lazy initialization.
     */
    protected ResourceManager config;
    private boolean isSentenceLevel;  // default is TRUE


    /**
     * set the name of the View this Annotator creates, and the list of prerequisite Views that this
     * Annotator requires as input
     *
     * @param viewName The name of the View this annotator will populate. This will be used to
     *        access the created view from the TextAnnotation holding it.
     * @param requiredViews The views that must be populated for this new view to be created.
     */
    public Annotator(String viewName, String[] requiredViews) {
        this(viewName, requiredViews, false);
    }

    /**
     * explicitly declare whether lazy initialization should be used
     * 
     * @param viewName The name of the View this annotator will populate. This will be used to
     *        access the created view from the TextAnnotation holding it.
     * @param requiredViews The views that must be populated for this new view to be created.
     * @param isLazilyInitialized if 'true', defers the initialization of the derived class until
     *        getView() is called.
     */
    public Annotator(String viewName, String[] requiredViews, boolean isLazilyInitialized) {
        this(viewName, requiredViews, isLazilyInitialized, new ResourceManager(new Properties()));
    }


    /**
     * If lazy initialization is desired, set the property
     * {@link AnnotatorConfigurator#IS_LAZILY_INITIALIZED} in the ResourceManager argument
     * 
     * @param viewName The name of the View this annotator will populate. This will be used to
     *        access the created view from the TextAnnotation holding it.
     * @param requiredViews The views that must be populated for this new view to be created.
     * @param rm configuration parameters. lazy initialization is set to 'false' by default.
     */
    public Annotator(String viewName, String[] requiredViews, ResourceManager rm) {
        this(viewName, requiredViews, rm.getBoolean(
                AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, Configurator.FALSE),
                new AnnotatorConfigurator().getConfig(rm));
    }


    /**
     * some annotators have complex initialization, so will have to pass a ResourceManager to be on
     * hand for their initialization if non-lazy initialization is desired.
     * 
     * @param viewName The name of the View this annotator will populate. This will be used to
     *        access the created view from the TextAnnotation holding it.
     * @param requiredViews The views that must be populated for this new view to be created.
     * @param isLazilyInitialized if 'true', defers the initialization of the derived class until
     *        getView() is called.
     * @param config these properties are stored for use by derived class, esp. in
     *        initialize()
     */
    public Annotator(String viewName, String[] requiredViews, boolean isLazilyInitialized,
            ResourceManager config) {
        this.viewName = viewName;
        this.requiredViews = requiredViews;
        this.config = config;
        isSentenceLevel = true;
        isInitialized = false;
        if (!isLazilyInitialized)
            doInitialize();
    }


    /**
     * Derived classes use this to load memory- or time-consuming resources. <b>Don't try to log
     * from this method unless your Logger is static.</b> Generated code puts non-static Logger
     * initialization in the constructor, so if lazyInitialize is 'false' you'll get a null pointer
     * exception trying to write to Logger in initialize().
     * 
     * @param rm configuration parameters
     */
    public abstract void initialize(ResourceManager rm);


    /**
     * If you want lazy initialization, this method must load the component models/resources,
     * Default implementation just sets the relevant field to 'true'.
     */
    final public void doInitialize() {
        initialize(config);
        isInitialized = true;
    }


    /**
     * Indicates whether or not all models/resources have been loaded. Purpose is to support lazy
     * initialization.
     * 
     * @return 'true' if model is initialized, 'false' otherwise.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * create and add the view named by getViewName() to the TextAnnotation argument.
     *
     * @param ta the TextAnnotation to modify.
     */
    protected abstract void addView(TextAnnotation ta) throws AnnotatorException;


    /**
     * return the name of the View created by this Annotator
     *
     * @return the name generated by this view.
     */
    public String getViewName() {
        return viewName;
    }


    /**
     * Add the view named by getViewName() to the TextAnnotation argument, and return the View
     * 
     * @param ta
     * @return the newly created View.
     * @throws AnnotatorException
     */
    public final View getView(TextAnnotation ta) throws AnnotatorException {
        lazyAddView(ta);
        return ta.getView(viewName);
    }

    /**
     * Add the view named by getViewName() to the TextAnnotation argument, and return the View
     *
     * @param ta
     * @return the newly created View.
     * @throws AnnotatorException
     */
    public final View getView(TextAnnotation ta, ResourceManager runtimeAttributes) throws AnnotatorException {
        lazyAddView(ta, runtimeAttributes);
        return ta.getView(viewName);
    }

    /**
     Add the content of the annotators to a given TextAnnotation object.
     @param runtimeAttributes the parameters that might change the behavior of the annotator while after initialization and while running.
     */
    protected void addView(TextAnnotation ta, ResourceManager runtimeAttributes) throws AnnotatorException {
        logger.warn("This annotator does not accept run-time attributes. You have to run `addView() function without the attributes parameter. `");
        addView(ta);
    }

    /**
     * First, checks whether model is initialized, and calls initialize() if not. Then, calls
     * addView(). IMPORTANT: clients should always call getView().
     * 
     * @param ta
     */
    private void lazyAddView(TextAnnotation ta) throws AnnotatorException {

        if (!isInitialized()) {
            doInitialize();
        }
        addView(ta);
    }

    /**
     * Same lazy view function, but with resource parameters
     * First, checks whether model is initialized, and calls initialize() if not. Then, calls
     * addView(). IMPORTANT: clients should always call getView().
     */
    private void lazyAddView(TextAnnotation ta, ResourceManager runtimeAttributes) throws AnnotatorException {
        if (!isInitialized()) {
            doInitialize();
        }
        addView(ta, runtimeAttributes);
    }

    /**
     * Can be used internally by {@link BasicAnnotatorService} to check for pre-requisites before
     * calling any single (external) {@link Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by
     *         this ViewGenerator
     */
    public String[] getRequiredViews() {
        return requiredViews;
    }

    /**
     * Return possible tag values that the annotator can produce.
     *
     * @return the set of string representing the tag values
     */
    public Set<String> getTagValues() {
        logger.error("Not yet implemented.");
        return null;
    }


    public boolean isSentenceLevel() {
        return isSentenceLevel;
    }
}
