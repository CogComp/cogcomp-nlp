/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.nlp.utilities.PrintUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
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
 *         Created by mssammon on 4/13/15.
 */
public class BasicAnnotatorService implements AnnotatorService {

    private static Logger logger = LoggerFactory.getLogger(BasicAnnotatorService.class);

    protected Cache annotationCache = null;
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
    protected CacheManager cacheManager;
    private boolean forceUpdate;


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
                .getInt(AnnotatorServiceConfigurator.CACHE_HEAP_SIZE_MB.key), rm
                .getInt(AnnotatorServiceConfigurator.CACHE_DISK_SIZE_MB.key), rm
                .getBoolean(AnnotatorServiceConfigurator.SET_CACHE_SHUTDOWN_HOOK.key), rm
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
     * Populates the AnnotatorService with {@link View} providers and sets the {@link Cache}
     * properties.
     *
     * @param textAnnotationBuilder tokenizes and sentence splits input text.
     * @param viewProviders Annotators that populate a View with the same name as the corresponding
     *        Annotator key.
     * @param cacheDir Where the AnnotatorService should write the cache. <b>IMPORTANT</b>: multiple
     *        VMs may <b>NOT</b> share a single cache location.
     * @param throwExceptionIfNotCached if 'true', throw an exception if no cached value is found.
     * @param cacheHeapSizeInMegabytes cache maximum memory footprint.
     * @param cacheDiskSizeInMegabytes cache maximum disk footprint.
     * @throws AnnotatorException
     */
    public BasicAnnotatorService(TextAnnotationBuilder textAnnotationBuilder,
            Map<String, Annotator> viewProviders, String cacheDir,
            boolean throwExceptionIfNotCached, int cacheHeapSizeInMegabytes,
            int cacheDiskSizeInMegabytes, boolean setCacheShutdownHook, boolean disableCache,
            boolean forceUpdate) throws AnnotatorException {
        this.textAnnotationBuilder = textAnnotationBuilder;
        this.disableCache = disableCache;
        this.forceUpdate = forceUpdate;

        if (setCacheShutdownHook)
            System.setProperty("net.sf.ehcache.enableShutdownHook", "true");

        openCache(cacheDir, cacheDiskSizeInMegabytes, cacheHeapSizeInMegabytes);

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
     * Generates a hash key based on the basic {@link TextAnnotation} information (TextAnnotation's
     * own hash function) and the {@link View} name.
     *
     * @param ta The {@link TextAnnotation} to be hashed
     * @param viewName The name of the {@link View} to be cached
     * @return The cache key
     */
    public static String getCacheKey(TextAnnotation ta, String viewName) {
        return ta.hashCode() + ":" + viewName;
    }

    /**
     * get a hash key based on the text value provided
     *
     * @param text a text that has been used as the basis of a TextAnnotation object
     * @param taBuilderName name of TextAnnotationBuilder that created the basic TextAnnotation
     * @return a key for this TextAnnotation
     */
    public static String getTextAnnotationCacheKey(String text, String taBuilderName) {
        return text.hashCode() * 13 + ":" + taBuilderName;
    }

    /**
     * This opens a cache with the name cacheName, or adds it if it doesn't exist.
     */
    public void openCache(String cName, int cacheDiskSizeInMegabytes, int cacheHeapSizeInMegabytes) {

        if (disableCache)
            logger.error("Trying to open cache, but disableCache is set to 'true'.");

        else {
            cacheManager = CacheManager.getCacheManager(cName);

            if (null == cacheManager) {
                /**
                 * NOTE: caches are NOT USABLE until they have been added to a CacheManager.
                 */
                DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();

                diskStoreConfiguration.setPath(cName);
                Configuration cacheManagerConfig = new Configuration();
                cacheManagerConfig.setName(cName);
                cacheManagerConfig.addDiskStore(diskStoreConfiguration);
                cacheManagerConfig.setMaxBytesLocalDisk(1000000L * cacheDiskSizeInMegabytes);
                cacheManagerConfig.setMaxBytesLocalHeap(1000000L * cacheHeapSizeInMegabytes);
                cacheManager = new CacheManager(cacheManagerConfig);


                /**
                 * check to see if cache of same name already exists: if so, use it
                 */
                if (cacheManager.cacheExists(cName)) {
                    annotationCache = cacheManager.getCache(cName);
                } else {
                    CacheConfiguration cacheConfiguration =
                            new CacheConfiguration().eternal(true).name(cName).diskPersistent(true)
                                    .overflowToDisk(true);
                    // unfortunately, this next option appears to be enterprise-only
                    // .persistence(new
                    // PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALRESTARTABLE));
                    annotationCache = new Cache(cacheConfiguration);
                    cacheManager.addCache(annotationCache);
                }
            }
        }
    }

    /**
     * Create a basic {@link TextAnnotation} with sentence and token views using the
     * {@link edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder} provided to constructor.
     * <p>
     * Also, caches the created {@link TextAnnotation} using the
     * {@link #getTextAnnotationCacheKey(String, String)}
     */
    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text)
            throws AnnotatorException {
        TextAnnotation ta = getTextAnnotationFromCache(text);
        if (ta == null)
            ta = createTextAnnotationAndCache(corpusId, docId, text);
        return ta;
    }

    private TextAnnotation createTextAnnotationAndCache(String corpusId, String docId, String text)
            throws AnnotatorException {
        TextAnnotation ta = textAnnotationBuilder.createTextAnnotation(corpusId, docId, text);
        if (!disableCache) {
            putTextAnnotationInCache(text, ta);
        }
        return ta;
    }

    /**
     * Creates a basic {@link TextAnnotation} with sentence and token views with the pre-tokenized
     * text by using the {@link edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder}. Note that
     * this method works only with
     * {@link edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder}.
     *
     * @param text The raw text
     * @param tokenization An instance of
     *        {@link edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer.Tokenization} which contains
     *        tokens, character offsets, and sentence boundaries to be used while constructing the
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}.
     * @throws AnnotatorException
     */
    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text,
            Tokenizer.Tokenization tokenization) throws AnnotatorException {
        TextAnnotation ta = getTextAnnotationFromCache(text);
        if (ta == null)
            ta = createTextAnnotationAndCache(corpusId, docId, text, tokenization);
        return ta;
    }

    private TextAnnotation createTextAnnotationAndCache(String corpusId, String docId, String text,
            Tokenizer.Tokenization tokenization) throws AnnotatorException {
        TextAnnotation ta =
                textAnnotationBuilder.createTextAnnotation(corpusId, docId, text, tokenization);
        if (!disableCache) {
            putTextAnnotationInCache(text, ta);
        }
        return ta;
    }

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

    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId,
            String text, Set<String> viewNames) throws AnnotatorException {
        long startTime = System.currentTimeMillis();
        logger.debug("starting createAnnotatedTextAnnotation()...");
        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text);
        for (String view : viewNames)
            addView(ta, view);
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        logger.debug("Finished createAnnotatedTextAnnotation(), took: " + duration
                + " milliseconds");

        return ta;
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
        for (String view : viewNames)
            addView(ta, view);
        return ta;
    }

    @Override
    public boolean addView(TextAnnotation ta, String viewName) throws AnnotatorException {
        if (throwExceptionIfNotCached && !annotationCache.isKeyInCache(getCacheKey(ta, viewName)))
            throw new AnnotatorException(
                    "No cache entry for TextAnnotation, and throwExceptionIfNotCached is 'true'. "
                            + " Text is: '" + ta.getText());

        long startTime = System.currentTimeMillis();
        logger.debug("starting addView(" + viewName + ")...");

        boolean isUpdateNeeded = true;

        if (!forceUpdate) {
            if (ta.hasView(viewName))
                isUpdateNeeded = false;
            else if (!disableCache) {
                String key = getCacheKey(ta, viewName);
                if (annotationCache.isKeyInCache(key)) {
                    Element taElem = annotationCache.get(key);
                    View view =
                            (View) SerializationUtils.deserialize((byte[]) taElem.getObjectValue());
                    ta.addView(viewName, view);
                    long endTime = System.currentTimeMillis();
                    logger.debug(PrintUtils.printTimeTakenMs("Finished addView(" + viewName
                            + ") with cache hit.", startTime, endTime));
                    isUpdateNeeded = false;
                }
            }
        }

        if (isUpdateNeeded) {
            /**
             * at this point, we have to run the annotator b.c. view is not in cache, or cache is
             * disabled, or client forces update
             */
            addViewAndCache(ta, viewName);

            long endTime = System.currentTimeMillis();

            logger.debug(PrintUtils.printTimeTakenMs("Finished addView(" + viewName
                    + ") with cache miss.", startTime, endTime));
        }
        return isUpdateNeeded;
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
        for ( String key : getAvailableViews() )
            addView( ta, key );
        return ta;
    }

    /**
     * run the named annotator on the TextAnnotation provided
     *
     * @param ta TextAnnotation to update
     * @param viewName name of View to populate
     * @throws AnnotatorException
     */
    private void addViewAndCache(TextAnnotation ta, String viewName) throws AnnotatorException {
        // allow for TOKENS and SENTENCE View to be present independently of explicit annotator,
        // as TextAnnotationBuilder provides these
        if (ViewNames.SENTENCE.equals(viewName) || ViewNames.TOKENS.equals(viewName))
            return;

        if (!viewProviders.containsKey(viewName))
            throw new AnnotatorException("View '" + viewName
                    + "' is not supported by this AnnotatorService. ");

        Annotator annotator = viewProviders.get(viewName);

        // get inputs required by annotator
        // recursive call: MUST HAVE VERIFIED NO CYCLICAL DEPENDENCIES
        // TODO: add depth-first search: look for cycles
        for (String prereqViewName : annotator.getRequiredViews()) {
            addView(ta, prereqViewName);
        }
        View view = annotator.getView(ta);
        ta.addView(viewName, view);

        if (!disableCache) {
            String cacheKey = getCacheKey(ta, viewName);
            removeKeyFromCache(cacheKey);
            putInCache(cacheKey, SerializationUtils.serialize(view));
        }
    }

    /**
     * This removes the key from the cache. Used for testing.
     */
    public void removeKeyFromCache(String key) throws AnnotatorException {
        if (annotationCache == null) {
            throw new AnnotatorException("You need to open the cache before using it!");
        }

        if (isKeyInCache(key)) {
            annotationCache.remove(key);
            logger.debug("successfully removed key from cache...");
        }
    }

    /**
     * mainly here for debugging purposes
     * 
     * @param key The cache key
     * @return Whether the key is contained in the cache
     */
    public boolean isKeyInCache(String key) {
        if (!disableCache)
            return annotationCache.isKeyInCache(key);
        else {
            logger.error("client called isKeyInCache(), but disableCache is set to 'true'.");
            return false;
        }
    }


    private void putInCache(String key, Serializable value) {
        Element elem = new Element(key, value);
        annotationCache.put(elem);
        annotationCache.flush();
    }

    public void closeCache() {
        if (disableCache)
            logger.error("client called closeCache(), but disableCache is set to 'true'.");
        else {
            annotationCache.flush();
            cacheManager.shutdown();
        }
    }

    private TextAnnotation getTextAnnotationFromCache(String text) {
        String key = getTextAnnotationCacheKey(text, textAnnotationBuilder.getName());
        if (!disableCache && annotationCache.isKeyInCache(key)) {
            Element taElem = annotationCache.get(key);
            return SerializationHelper.deserializeTextAnnotationFromBytes((byte[]) taElem
                    .getObjectValue());
        }
        return null;
    }

    private void putTextAnnotationInCache(String text, TextAnnotation ta) throws AnnotatorException {
        String key = getTextAnnotationCacheKey(text, textAnnotationBuilder.getName());
        removeKeyFromCache(key);
        try {
            putInCache(key, SerializationHelper.serializeTextAnnotationToBytes(ta));
        } catch (IOException e) {
            throw new AnnotatorException(e.getMessage());
        }
    }



}
