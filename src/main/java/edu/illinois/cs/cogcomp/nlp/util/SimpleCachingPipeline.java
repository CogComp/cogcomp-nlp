package edu.illinois.cs.cogcomp.nlp.util;

/**
 * Created by mssammon on 9/21/15.
 */

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TextAnnotationBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.util.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory.*;

/**
 * Caching Curator MakeShift Version 2.1
 * This is a substitute for ehcache, which has... unhelpful... behavior when
 * the cache reaches a moderate size (say, if you want to process a thousand documents
 * with multiple annotators).  This is extremely simple local-disk-based.
 * @author upadhya3
 * @author mssammon
 */
public class SimpleCachingPipeline implements AnnotatorService
{

    private final Map<String, Annotator> viewProviders;
    private final boolean throwExceptionIfNotCached;
    private List<String> activeViews = null;
    public AnnotatorService processor = null;
    public String pathToSaveCachedFiles = null;
    private TextAnnotationBuilder textAnnotationBuilder;

    public SimpleCachingPipeline( ResourceManager rm ) throws IOException, AnnotatorException {
        this(buildAnnotators(rm),
                rm.getString(AnnotatorServiceConfigurator.CACHE_DIR),
                rm.getBoolean(AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED)
        );
    }

    public SimpleCachingPipeline( Map< String, Annotator> annotators,
                                  String cacheDir,
                                  boolean throwExceptionIfNotCached ) throws IOException, AnnotatorException {
        this.viewProviders = annotators;
        this.textAnnotationBuilder = new TextAnnotationBuilder( new IllinoisTokenizer() );
        this.pathToSaveCachedFiles = cacheDir;
        this.throwExceptionIfNotCached = throwExceptionIfNotCached;
    }


    /**
     * gets a textAnnotation with the set of views specified at construction.
     * First checks for a local copy in a specific directory and if present,
     *   loads it. Otherwise, creates a basic TextAnnotation from the text.
     * Checks whether specified views are present, and if not, adds them.
     * If TextAnnotation is updated, writes it to the cache in place of any original.
     * If forceUpdate is 'true', ignores cached copy and always writes the new one into the cache.
     *
     * @param text  the text to annotate.
     * @return TextAnnotation populated with views specified at construction.
     * @throws Exception
     */
    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text, boolean forceUpdate) throws AnnotatorException
    {
        String savePath = null;
        try {
            savePath = getSavePath( this.pathToSaveCachedFiles, text );
        } catch (Exception e) {
            e.printStackTrace();
            throw new AnnotatorException( e.getMessage() );
        }

        TextAnnotation ta = null;

        if ( new File(savePath).exists()) {
            try {
                ta = SerializationHelper.deserializeTextAnnotationFromFile(savePath);
                return ta;
            } catch (IOException e) {
                e.printStackTrace();
                throw new AnnotatorException( e.getMessage() );
            }
        }
        else if ( throwExceptionIfNotCached )
            throwNotCachedException( corpusId, docId, text );

        return textAnnotationBuilder.createTextAnnotation(corpusId, docId, text);
    }

    private void throwNotCachedException(String corpusId, String docId, String text) throws AnnotatorException {
        throw new AnnotatorException( "text with corpusid '" + corpusId + "', docId '" + docId +
            "', value '" + text + "' was not in cache, and the field 'throwExceptionIfNotCached' is 'true'." );
    }


    private TextAnnotation createTextAnnotationAndCache(String corpusId, String docId, String text) throws AnnotatorException {
        TextAnnotation ta = textAnnotationBuilder.createTextAnnotation(corpusId, docId, text);
        boolean forceUpdate = true;
        String cacheFile = null;
        try {
            cacheFile = getSavePath( this.pathToSaveCachedFiles, text );
            SerializationHelper.serializeTextAnnotationToFile(ta, cacheFile, forceUpdate );
        } catch (Exception e) {
            e.printStackTrace();
            throw new AnnotatorException( e.getMessage() );
        }

        return ta;
    }

    /**
     * get the location of the file that corresponds to the given text and directory
     * @param dir
     * @param text
     * @return
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
        for (int i = 0; i < b.length; i++)
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        return result;
    }


    /**
     *
     * @param corpusId
     * @param textId
     * @param text
     * @param forceUpdate
     * @return
     * @throws Exception
     */
    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId, String text, boolean forceUpdate) throws AnnotatorException {

        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text, forceUpdate);

        Set<String> viewsToAnnotate = new HashSet<>();
        viewsToAnnotate.addAll(viewProviders.keySet());

        addViewsAndCache( ta, viewsToAnnotate, forceUpdate );

        return ta;
    }


    /**
     * DOES NOT CACHE THE ADDED VIEW!!!
     * @param textAnnotation
     * @param viewName
     * @param forceUpdate
     * @return
     * @throws AnnotatorException
     */
    @Override
    public boolean addView(TextAnnotation textAnnotation, String viewName, boolean forceUpdate) throws AnnotatorException
    {
        boolean isUpdated = false;

        if (ViewNames.SENTENCE.equals( viewName ) ||
                ViewNames.TOKENS.equals( viewName ) )
            return false;

        if ( !textAnnotation.hasView( viewName )  )
        {
            if ( !viewProviders.containsKey( viewName ) )
                throw new AnnotatorException( "View '" + viewName + "' cannot be provided by this AnnotatorService." );

            isUpdated = true;
            Annotator annotator = viewProviders.get( viewName );

            for ( String prereqView : annotator.getRequiredViews() )
            {
                isUpdated = addView( textAnnotation, prereqView, forceUpdate ) || isUpdated;
            }

            annotator.getView(textAnnotation);
        }

        if ( isUpdated && throwExceptionIfNotCached )
            throwNotCachedException( textAnnotation.getCorpusId(), textAnnotation.getId(), textAnnotation.getText() );
        return isUpdated;
    }

    @Override
    public boolean addViewsAndCache(TextAnnotation ta, Set<String> viewsToAnnotate, boolean forceUpdate) throws AnnotatorException {
        boolean isUpdated = false;

        String cacheFile = null;
        try {
            cacheFile = getSavePath( this.pathToSaveCachedFiles, ta.getText() );
            if (IOUtils.exists( cacheFile ) )
                ta = SerializationHelper.deserializeTextAnnotationFromFile(cacheFile );
        } catch (Exception e) {
            e.printStackTrace();
            throw new AnnotatorException( e.getMessage() );
        }

        for (String viewName : viewsToAnnotate) {
            isUpdated = addView(ta, viewName, forceUpdate) || isUpdated;
        }

        if ( isUpdated || forceUpdate )
        {
            String outFile = null;
            try {
                outFile = getSavePath( pathToSaveCachedFiles, ta.getText() );
                // must update file, so force overwrite
                SerializationHelper.serializeTextAnnotationToFile( ta, outFile, true );
            } catch (Exception e) {
                e.printStackTrace();
                throw new AnnotatorException( e.getMessage() );
            }
        }
        return isUpdated;
    }

}