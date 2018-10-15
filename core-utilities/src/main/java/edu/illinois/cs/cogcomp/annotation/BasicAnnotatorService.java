/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.caches.TextAnnotationCache;
import edu.illinois.cs.cogcomp.core.io.caches.TextAnnotationMapDBHandler;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class defines the behavior of an annotation service that can be fulfilled by an arbitrary
 * backend, and which will cache the annotations so fulfilled. Sentence and Token views are
 * populated by the TextAnnotationBuilder object passed to the constructor. By default, only
 * Annotators provided as 'viewProviders' will be applied to input text. Their dependencies will be
 * (minimally) checked to make sure a requested view has its required inputs provided. By default,
 * all annotation views are cached. The constructor provides default values for caching behavior
 * (see the default values), but these can be overridden by providing a ResourceManager (and
 * configuration file) that specifies these values.
 * <p>
 * Many Annotators are likely to be singletons to avoid memory overload. AnnotatorService can be
 * subclassed to provide homespun views without creating Annotator classes, though probably the
 * latter is the better way to go. There is at least one use case that motivates this design --
 * backward compatibility with Curator to replace CuratorClient/CachingCuratorClient.
 * <p>
 *
 * @author Mark Sammons
 * @author Christos Christodoulopoulos
 * @author Narender Gupta
 *
 * Created by mssammon on 4/13/15.
 */
public class BasicAnnotatorService implements AnnotatorService {

    private static Logger logger = LoggerFactory.getLogger(BasicAnnotatorService.class);

    protected TextAnnotationCache annotationCache = null;
    protected boolean disableCache = false;

    /**
     * used when processing a corpus and then using it in experiments -- if 'true', you will know in
     * the experimental run that a data element is missing.
     */
    protected boolean throwExceptionIfNotCached = false;

    /**
     * provides tokenization and sentence splitting to create a basic TextAnnotation object from
     * plain text String.
     */
    protected TextAnnotationBuilder textAnnotationBuilder;

    /**
     * A way to define externally provided views. Each view provider needs to implement
     * {@link Annotator}.
     */
    protected Map<String, Annotator> viewProviders;
    protected boolean forceUpdate;

    /**
     * constructor with ResourceManager properties for caching behavior
     *
     * @param textAnnotationBuilder tokenizes and sentence splits input text.
     * @param viewProviders Annotators that populate a View with the same name as the corresponding
     *        Annotator key.
     * @param rm A {@link ResourceManager} containing cache configuration options.
     * @throws edu.illinois.cs.cogcomp.annotation.AnnotatorException
     */
    public BasicAnnotatorService(TextAnnotationBuilder textAnnotationBuilder,
            Map<String, Annotator> viewProviders, ResourceManager rm) throws AnnotatorException {
        this(textAnnotationBuilder, viewProviders, rm
                .getString(AnnotatorServiceConfigurator.CACHE_DIR.key), rm
                .getBoolean(AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED.key), rm
                .getBoolean(AnnotatorServiceConfigurator.DISABLE_CACHE.key), rm
                .getBoolean(AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key));
    }

    /**
     * constructor that uses AnnotatorServiceConfigurator default configuration
     * 
     * @param textAnnotationBuilder object that will build basic TextAnnotation objects
     * @param viewProviders annotators that will populate views in new TextAnnotation objects
     * @throws AnnotatorException
     */
    public BasicAnnotatorService(TextAnnotationBuilder textAnnotationBuilder,
            Map<String, Annotator> viewProviders) throws AnnotatorException {
        this(textAnnotationBuilder, viewProviders, (new AnnotatorServiceConfigurator())
                .getDefaultConfig());

    }

    /**
     * Populates the AnnotatorService with {@link View} providers and initializes cache manager, if caching enabled.
     *
     * @param textAnnotationBuilder tokenizes and sentence splits input text.
     * @param viewProviders Annotators that populate a View with the same name as the corresponding
     *        Annotator key.
     * @param cacheFile Where the AnnotatorService should write the cache. <b>IMPORTANT</b>: multiple
     *        VMs may <b>NOT</b> share a single cache location.
     * @param throwExceptionIfNotCached if 'true', throw an exception if no cached value is found.
     * @throws AnnotatorException
     */
    public BasicAnnotatorService(TextAnnotationBuilder textAnnotationBuilder,
            Map<String, Annotator> viewProviders, String cacheFile,
            boolean throwExceptionIfNotCached, boolean disableCache,
            boolean forceUpdate) throws AnnotatorException {
        this.textAnnotationBuilder = textAnnotationBuilder;
        this.disableCache = disableCache;
        this.forceUpdate = forceUpdate;

//        if (setCacheShutdownHook)
//            System.setProperty("net.sf.ehcache.enableShutdownHook", "true");

//        openCache(cacheDir, cacheDiskSizeInMegabytes, cacheHeapSizeInMegabytes);

        if (!disableCache)
            this.annotationCache = new TextAnnotationMapDBHandler(cacheFile);

        // To avoid NullExceptions we need to create an empty map
        this.viewProviders = new HashMap<>();
        // keep own copy of viewProviders to prevent later addition via reference,
        // which will miss step for adding supported/required views
        if (viewProviders != null && !viewProviders.isEmpty()) {
            this.viewProviders.putAll(viewProviders);
        }

        this.throwExceptionIfNotCached = throwExceptionIfNotCached;
    }

    /**
     * Creates a basic {@link TextAnnotation} with sentence and token views with the pre-tokenized
     * text by using the {@link edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder}. Note that
     * this method works only with
     * {@link edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder}.
     * DOES NOT CACHE THIS BASIC TEXT ANNOTATION.
     *
     * @param corpusId The name of the data set to associate with this text
     * @param docId document id to associate with this text
     * @param text The raw text
     * @throws AnnotatorException
     */
    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text) throws AnnotatorException {
        return textAnnotationBuilder.createTextAnnotation(corpusId, docId, text);
    }

    /**
     * A convenience method for creating a
     * {@link TextAnnotation} while
     * respecting the pre-tokenization of text passed in the form of
     * {@link Tokenizer.Tokenization}.
     *
     * @param corpusId
     * @param docId
     * @param text         The raw text
     * @param tokenization An instance of
     *                     {@link Tokenizer.Tokenization} which contains
     *                     tokens, character offsets, and sentence boundaries to be used while constructing the
     *                     {@link TextAnnotation}.   @throws AnnotatorException If the service cannot create requested object
     */
    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text, Tokenizer.Tokenization tokenization) throws AnnotatorException {
        return textAnnotationBuilder.createTextAnnotation(corpusId, docId, text, tokenization);
    }

    /**
     * Creates a {@link TextAnnotation} with all annotators provided to this {@link AnnotatorService}.
     *
     * @param corpusId corpus name to associate with this text/TextAnnotation.
     * @param textId document id to associate with this text/TextAnnotation.
     * @param text The raw text used to build the
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}
     *        where all the {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View}s
     *        should be added.
     * @return a TextAnnotation with all possible Views populated.
     * @throws AnnotatorException
     */
    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId, String text)
            throws AnnotatorException {
        return createAnnotatedTextAnnotation(corpusId, textId, text, viewProviders.keySet());
    }

    /**
     * A convenience method for creating a
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation} and adding
     * all the {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View}s supported by
     * this {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService}. This amounts to calling
     * {@link #createBasicTextAnnotation(String, String, String, Tokenizer.Tokenization)} and
     * successive calls of
     * {@link #addView(edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation, String)}
     * . Note that this method works only with
     * {@link edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder}.
     *
     * @param text The raw text
     * @param tokenization An instance of
     *        {@link edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer.Tokenization} which contains
     *        tokens, character offsets, and sentence boundaries to be used while constructing the
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}.
     * @return
     * @throws AnnotatorException
     */
    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId,
            String text, Tokenizer.Tokenization tokenization) throws AnnotatorException {
        return createAnnotatedTextAnnotation(corpusId, textId, text, tokenization,
                viewProviders.keySet());
    }

    /**
     * An overloaded version of {@link #createAnnotatedTextAnnotation(String, String, String)} that
     * adds only the {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View}s
     * requested. Note that this method works only with
     * {@link edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder}.
     *
     * @param text The raw text
     * @param tokenization An instance of
     *        {@link edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer.Tokenization} which contains
     *        tokens, character offsets, and sentence boundaries to be used while constructing the
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}.
     * @param viewNames Views to add
     * @return
     * @throws AnnotatorException
     */
    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId,
            String text, Tokenizer.Tokenization tokenization, Set<String> viewNames)
            throws AnnotatorException {
        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text, tokenization);
        return addViewsAndCache(ta, viewNames, this.forceUpdate );
    }

    /**
     * Add a new {@link Annotator} to the service. All prerequisite views must already be provided by other annotators
     * known to this {@link AnnotatorService}.
     *
     * @param annotator the {@link Annotator} to be added.
     * @throws {@link AnnotatorException} if the annotator requires views that cannot be satisfied.
     */
    @Override
    public void addAnnotator(Annotator annotator) throws AnnotatorException {
        String[] prerequisites = annotator.getRequiredViews();
        for ( String pre : prerequisites )
            if ( !this.getAvailableViews().contains( pre ) )
                throw new AnnotatorException("Missing prerequisite: " + pre );

        this.viewProviders.put( annotator.getViewName(), annotator );
    }

    /**
     * Return a set containing the names of all {@link View}s
     * that this service can provide.
     *
     * @return a set of view names corresponding to annotators known to this AnnotatorService
     */
    @Override
    public Set<String> getAvailableViews() {
        return this.viewProviders.keySet();
    }

    /**
     * Add the specified views to the TextAnnotation argument. This is useful when TextAnnotation objects are
     * built independently of the service, perhaps by a different system component (e.g. a corpus reader).
     * If so specified, overwrite existing views.
     *
     * @param ta                   The {@link TextAnnotation} to annotate
     * @param replaceExistingViews if 'true', annotate a
     *                             {@link View} even if
     *                             it is already present in the ta argument, replacing the original corresponding View.
     * @return a reference to the updated TextAnnotation
     */
    @Override
    public TextAnnotation annotateTextAnnotation(TextAnnotation ta, boolean replaceExistingViews) throws AnnotatorException {
        return addViewsAndCache(ta, viewProviders.keySet(), replaceExistingViews);
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

        if ( !textAnnotation.hasView( viewName )  || forceUpdate ) {
            isUpdated = true;

            if ( !viewProviders.containsKey(viewName) )
                throw new AnnotatorException( "View '" + viewName + "' cannot be provided by this AnnotatorService." );

            Annotator annotator = viewProviders.get( viewName );

            for ( String prereqView : annotator.getRequiredViews() ) {
                addView( textAnnotation, prereqView );
            }

            View v = annotator.getView(textAnnotation);

            textAnnotation.addView( annotator.getViewName(), v );
        }

        if (isUpdated && throwExceptionIfNotCached)
            throwNotCachedException(textAnnotation.getCorpusId(), textAnnotation.getId(), textAnnotation.getText());
        return isUpdated;
    }

    @Override
    public boolean addView(TextAnnotation textAnnotation, String viewName, ResourceManager runtimeAttributes) throws AnnotatorException {
        boolean isUpdated = false;

        if (ViewNames.SENTENCE.equals(viewName) || ViewNames.TOKENS.equals(viewName))
            return false;

        if ( !textAnnotation.hasView( viewName )  || forceUpdate ) {
            isUpdated = true;

            if ( !viewProviders.containsKey(viewName) )
                throw new AnnotatorException( "View '" + viewName + "' cannot be provided by this AnnotatorService." );

            Annotator annotator = viewProviders.get( viewName );

            for ( String prereqView : annotator.getRequiredViews() ) {
                addView( textAnnotation, prereqView, runtimeAttributes);
            }

            View v = annotator.getView(textAnnotation, runtimeAttributes);

            textAnnotation.addView( annotator.getViewName(), v );
        }

        if (isUpdated && throwExceptionIfNotCached)
            throwNotCachedException(textAnnotation.getCorpusId(), textAnnotation.getId(), textAnnotation.getText());
        return isUpdated;
    }

    protected void throwNotCachedException(String corpusId, String docId, String text) throws AnnotatorException {
        throw new AnnotatorException("text with corpusid '" + corpusId + "', docId '" + docId +
                "', value '" + text + "' was not in cache, and the field 'throwExceptionIfNotCached' is 'true'.");
    }

    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId, String text, Set<String> viewsToAnnotate) throws AnnotatorException {

        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text);

        // if it already contains its, return it.
        if (!forceUpdate && !disableCache && annotationCache.contains(ta)) {
            TextAnnotation taFromCache = annotationCache.getTextAnnotation(ta);
            boolean containsAll = true;
            for(String vu : viewsToAnnotate) {
                if(!taFromCache.getAvailableViews().contains(vu)) {
                    containsAll = false;
                    break;
                }
            }
            if(containsAll) return taFromCache;
        }

        return addViewsAndCache(ta, viewsToAnnotate, false);
    }

    /**
     * add all the specified views to the specified {@link TextAnnotation} and cache it. Will overwrite if so configured.
     * IMPORTANT: if the corresponding TextAnnotation has already been cached, the argument
     *     is ignored. The client should ALWAYS use the returned TextAnnotation.
     *
     * @param ta
     * @param viewsToAnnotate
     * @return
     * @throws AnnotatorException
     */
    public TextAnnotation addViewsAndCache(TextAnnotation ta, Set<String> viewsToAnnotate, boolean clientForceUpdate) throws AnnotatorException {
        boolean isUpdated = false;

        if (!(forceUpdate || clientForceUpdate) && !disableCache)
            if (annotationCache.contains(ta))
                ta = annotationCache.getTextAnnotation(ta);

        for (String viewName : viewsToAnnotate) {
            try {
                isUpdated = addView(ta, viewName) || isUpdated;
            } catch (AnnotatorException e) {
                // the exception is handled here, because one single view failure should not resutl in loss of all the annotations
                logger.error("The annotator for view " + viewName + " failed. Skipping the view . . . ");
                e.printStackTrace();
            }
        }

        if (!disableCache && (isUpdated || forceUpdate) || clientForceUpdate) {
            try {
                annotationCache.addTextAnnotation(ta.getCorpusId(), ta);
            } catch (Exception e) {
                e.printStackTrace();
                throw new AnnotatorException(e.getMessage());
            }
        }
        return ta;
    }
}
