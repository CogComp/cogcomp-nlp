package edu.illinois.cs.cogcomp.nlp.util;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.CcgTextAnnotationBuilder;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory.buildAnnotators;

/**
 * Caching Pipeline
 * This is a substitute for ehcache, which has... unhelpful... behavior when
 * the cache reaches a moderate size (say, if you want to process a thousand documents
 * with multiple annotators).  This is extremely simple local-disk-based.
 *
 * @author upadhya3
 * @author mssammon
 */
public class SimpleCachingPipeline implements AnnotatorService {

    private final Map<String, Annotator> viewProviders;
    private final boolean throwExceptionIfNotCached;
    public String pathToSaveCachedFiles = null;
    private TextAnnotationBuilder textAnnotationBuilder;
    private boolean forceUpdate;

    public SimpleCachingPipeline(ResourceManager rm) throws IOException, AnnotatorException {
        this(buildAnnotators(rm),
                rm.getString(AnnotatorServiceConfigurator.CACHE_DIR),
                rm.getBoolean(AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED),
                rm.getBoolean(AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE)
        );
    }

    public SimpleCachingPipeline(TextAnnotationBuilder taBuilder, Map<String, Annotator> annotators,

                                 ResourceManager rm)
            throws IOException, AnnotatorException {
        this(taBuilder, annotators, rm.getString(AnnotatorServiceConfigurator.CACHE_DIR),
                rm.getBoolean(AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED),
                rm.getBoolean(AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE));
    }

    public SimpleCachingPipeline(TextAnnotationBuilder taBuilder,
                                 Map<String, Annotator> annotators,
                                 String cacheDir,
                                 boolean throwExceptionIfNotCached,
                                 boolean forceCacheUpdate)
            throws IOException, AnnotatorException {
        this.viewProviders = annotators;
        this.textAnnotationBuilder = taBuilder;
        this.pathToSaveCachedFiles = cacheDir;
        this.throwExceptionIfNotCached = throwExceptionIfNotCached;
        this.forceUpdate = forceCacheUpdate;
    }

    public SimpleCachingPipeline(Map<String, Annotator> annotators,
                                 String cacheDir,
                                 boolean throwExceptionIfNotCached,
                                 boolean forceCacheUpdate)
            throws IOException, AnnotatorException {
        this.viewProviders = annotators;
        this.textAnnotationBuilder = new CcgTextAnnotationBuilder(new IllinoisTokenizer());
        this.pathToSaveCachedFiles = cacheDir;
        this.throwExceptionIfNotCached = throwExceptionIfNotCached;
        this.forceUpdate = forceCacheUpdate;
    }


    /**
     * gets a textAnnotation with the set of views specified at construction.
     * First checks for a local copy in a specific directory and if present,
     * loads it. Otherwise, creates a basic TextAnnotation from the text.
     * Checks whether specified views are present, and if not, adds them.
     * If TextAnnotation is updated, writes it to the cache in place of any original.
     * If forceUpdate is 'true', ignores cached copy and always writes the new one into the cache.
     *
     * @param text the text to annotate.
     * @return TextAnnotation populated with views specified at construction.
     */
    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text) throws AnnotatorException {
        String savePath;
        try {
            savePath = getSavePath(this.pathToSaveCachedFiles, text);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AnnotatorException(e.getMessage());
        }

        TextAnnotation ta;

        if (new File(savePath).exists() && !forceUpdate) {
            try {
                ta = SerializationHelper.deserializeTextAnnotationFromFile(savePath);
                return ta;
            } catch (IOException e) {
                e.printStackTrace();
                throw new AnnotatorException(e.getMessage());
            }
        } else if (throwExceptionIfNotCached)
            throwNotCachedException(corpusId, docId, text);

        return textAnnotationBuilder.createTextAnnotation(corpusId, docId, text);
    }

    private void throwNotCachedException(String corpusId, String docId, String text) throws AnnotatorException {
        throw new AnnotatorException("text with corpusid '" + corpusId + "', docId '" + docId +
                "', value '" + text + "' was not in cache, and the field 'throwExceptionIfNotCached' is 'true'.");
    }


    /**
     * get the location of the file that corresponds to the given text and directory
     *
     * @param dir  cache root
     * @param text text used as basis of key
     * @return path to cached file location, whether it exists or not
     * @throws Exception
     */
    public static String getSavePath(String dir, String text) throws Exception {
        String md5sum = getMD5Checksum(text);
        return dir + "/" + md5sum + ".cached";
    }


    public static String getMD5Checksum(String text) throws Exception {
        MessageDigest complete = MessageDigest.getInstance("MD5");
        complete.update(text.getBytes(), 0, text.getBytes().length);
        byte[] b = complete.digest();
        String result = "";
        for (byte aB : b) result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        return result;
    }


    /**
     * @param corpusId a user-specified identifier for the collection the text comes from
     * @param textId   a user-specified identifier for the text
     * @param text     the text to annotate
     * @return a TextAnnotation with views populated using the Annotators held by this object
     */
    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId, String text) throws AnnotatorException {


        Set<String> viewsToAnnotate = new HashSet<>();
        viewsToAnnotate.addAll(viewProviders.keySet());
        return createAnnotatedTextAnnotation(corpusId, textId, text, viewsToAnnotate);
    }

    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId, String text, Set<String> viewsToAnnotate) throws AnnotatorException {

        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text);

        addViewsAndCache(ta, viewsToAnnotate);

        return ta;
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

        if ( !textAnnotation.hasView( viewName )  || forceUpdate )
        {
            isUpdated = true;

            if ( !viewProviders.containsKey( viewName ) )
                throw new AnnotatorException( "View '" + viewName + "' cannot be provided by this AnnotatorService." );

            Annotator annotator = viewProviders.get( viewName );

            for ( String prereqView : annotator.getRequiredViews() )
            {
                addView( textAnnotation, prereqView );
            }

            View v = annotator.getView(textAnnotation);

            textAnnotation.addView( annotator.getViewName(), v );
        }

        if (isUpdated && throwExceptionIfNotCached)
            throwNotCachedException(textAnnotation.getCorpusId(), textAnnotation.getId(), textAnnotation.getText());
        return isUpdated;
    }

    public boolean addViewsAndCache(TextAnnotation ta, Set<String> viewsToAnnotate) throws AnnotatorException {
        boolean isUpdated = false;

        String cacheFile;
        if (!forceUpdate)
            try {
                cacheFile = getSavePath(this.pathToSaveCachedFiles, ta.getText());
                if (IOUtils.exists(cacheFile) && !forceUpdate)
                    ta = SerializationHelper.deserializeTextAnnotationFromFile(cacheFile);
            } catch (Exception e) {
                e.printStackTrace();
                throw new AnnotatorException(e.getMessage());
            }

        for (String viewName : viewsToAnnotate) {
            isUpdated = addView(ta, viewName) || isUpdated;
        }

        if (isUpdated || forceUpdate) {
            String outFile;
            try {
                outFile = getSavePath(pathToSaveCachedFiles, ta.getText());
                // must update file, so force overwrite
                SerializationHelper.serializeTextAnnotationToFile(ta, outFile, true);
            } catch (Exception e) {
                e.printStackTrace();
                throw new AnnotatorException(e.getMessage());
            }
        }
        return isUpdated;
    }

    /**
     * change caching behavior. useful for e.g. testing
     */
    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}