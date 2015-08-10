package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.cachingcurator.CachingAnnotator;
import edu.illinois.cs.cogcomp.cachingcurator.CachingAnnotatorService;
import edu.illinois.cs.cogcomp.cachingcurator.CachingCuratorException;
import edu.illinois.cs.cogcomp.common.CuratorViewNames;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * version of illinois nlp pipeline that caches annotations locally on disk.
 * IMPORTANT: this produces TextAnnotations with Edison view names (different from
 *    the view names in Record data structures used within Curator)
 *
 * Created by mssammon on 4/13/15.
 */
public class IllinoisCachingPreprocessor  extends CachingAnnotatorService
{

    private static final java.lang.String RESPECT_TOKENIZATION = "respectTokenization";
    private static final java.lang.String THROW_EXCEPTION_IF_NOT_CACHED = "throwExceptionIfNotCached";
    private static IllinoisCachingPreprocessor THE_INSTANCE;
    private static Map<String, Boolean> activeViews;
    private static boolean useEdisonNames;

    private Logger logger = LoggerFactory.getLogger( IllinoisCachingPreprocessor.class );

    private IllinoisPreprocessor preprocessor;

    private String currentText;
    private TextAnnotation currentTextAnnotation;

    private static Map< String, String > mappedViewNames;

    static
    {
        mappedViewNames = new HashMap< String, String >();
        mappedViewNames.put( CuratorViewNames.charniak, ViewNames.PARSE_CHARNIAK );
        mappedViewNames.put( CuratorViewNames.chunk, ViewNames.SHALLOW_PARSE );
        mappedViewNames.put( CuratorViewNames.coref, ViewNames.COREF );
        mappedViewNames.put( CuratorViewNames.dependencies, ViewNames.DEPENDENCY );
        mappedViewNames.put( CuratorViewNames.lemma, ViewNames.LEMMA );
        mappedViewNames.put( CuratorViewNames.ner, ViewNames.NER );
        mappedViewNames.put( CuratorViewNames.pos, ViewNames.POS );
        mappedViewNames.put( CuratorViewNames.sentences, ViewNames.SENTENCE );
        mappedViewNames.put( CuratorViewNames.wikifier, ViewNames.WIKIFIER );
        mappedViewNames.put( CuratorViewNames.stanfordDep, ViewNames.DEPENDENCY_STANFORD );
        mappedViewNames.put( CuratorViewNames.stanfordParse, ViewNames.PARSE_STANFORD );
    }

    public static IllinoisCachingPreprocessor getInstance( String config, Map< String, CachingAnnotator > extraViewGenerators ) throws Exception {
        return getInstance(new ResourceManager(config), extraViewGenerators );

    }


    public static IllinoisCachingPreprocessor getInstance( ResourceManager rm, Map< String, CachingAnnotator > extraViewGenerators ) throws Exception {
        if ( null == THE_INSTANCE )
        {
            THE_INSTANCE = new IllinoisCachingPreprocessor( new IllinoisPreprocessor( rm ), rm, extraViewGenerators );
        }

        return THE_INSTANCE;
    }

    /**
     * private constructor as should be a singleton
     *
     * @param illinoisPreprocessor
     * @param rm
     * @param extraViewGenerators
     * @throws CachingCuratorException
     */
    private IllinoisCachingPreprocessor(IllinoisPreprocessor illinoisPreprocessor, ResourceManager rm, Map< String, CachingAnnotator > extraViewGenerators ) throws CachingCuratorException
    {
        initialize(getRequestedViews(illinoisPreprocessor),
                rm.getBoolean(THROW_EXCEPTION_IF_NOT_CACHED),
                rm.getBoolean(RESPECT_TOKENIZATION),
                extraViewGenerators
        );

        preprocessor = illinoisPreprocessor;
    }

    /**
     * returns a map whose keys are requested views,each of which is paired with
     * a flag that indicates whether the cache must be updated.
     * @param preprocessor
     * @return
     */
    private static Map< String, Boolean > getRequestedViews(IllinoisPreprocessor preprocessor )
    {
        activeViews = preprocessor.getSupportedViews();

//        if ( activeViews.containsKey(CuratorViewNames.stanfordParse ) ) {
//            activeViews.put(CuratorViewNames.stanfordDep, false ); // don't try to force cache update
//        }
        activeViews = mapCuratorViewNamesToEdisonNames(activeViews);

        return activeViews;
    }

    private static Map<String, Boolean> mapCuratorViewNamesToEdisonNames(Map<String, Boolean> origViews) {
        Map< String, Boolean > renamedViews = new HashMap< String, Boolean >();

        for ( String origName : origViews.keySet() )
        {
            String finalName = origName;

            if ( mappedViewNames.containsKey( origName ) )
                finalName = mappedViewNames.get( origName );

            renamedViews.put( finalName, origViews.get( origName ) );
        }

        return renamedViews;
    }

    /**
     * to be called only by buildInstance()
     *
     * @param requestedViews
     * @param throwExceptionIfNotCached
     * @param respectTokenization
     * @param extraViewProviders
     * @throws CachingCuratorException
     */
    private IllinoisCachingPreprocessor(Map<String, Boolean> requestedViews, boolean throwExceptionIfNotCached, boolean respectTokenization, Map<String, CachingAnnotator> extraViewProviders, IllinoisPreprocessor preprocessor ) throws CachingCuratorException {
        initialize(requestedViews, throwExceptionIfNotCached, respectTokenization, extraViewProviders);
        initializeCurrentStatus();
    }

    private void initializeCurrentStatus() {
        currentText = null;
        currentTextAnnotation = null;
    }

    /**
     * sets list of supported views to match those that are actively generated by preprocessor.
     * Requires that activeViews field has been set.
     */
    @Override
    protected void setSupportedViews() {
        supportedViews.addAll( activeViews.keySet() );
    }

    /**
     * ugly first cut at making this work. The current implementation generates a Record with ALL active
     *    annotations in the first call for a given text and builds/stores the corresponding TextAnnotation.
     *    the requests for uncached views then simply returns the corresponding view from the complete
     *    TextAnnotation.
     * This approach means there is inefficiency if you process a given text with some subset of available
     *    resources, then later process again with additional resources -- the initial steps are repeated.
     *
     * @param textAnnotation
     * @param viewName
     */
    @Override
    protected void requestUncachedView(TextAnnotation textAnnotation, String viewName ) {
        if ( !isProcessed( textAnnotation.getText(), viewName ) ) {
//            if ( textAnnotation != currentTextAnnotation)
                process(textAnnotation);
        }

        addViewFromTo(viewName, currentTextAnnotation, textAnnotation);
    }


    private void addViewFromTo(String viewName, TextAnnotation currentTa, TextAnnotation newTa ) {

        View v = currentTa.getView( viewName );
        newTa.addView( viewName, v );
    }



    private boolean isProcessed(String text, String viewName) {
        boolean isProcessed = false;

        if ( text != currentText && null != currentTextAnnotation)
        {
            if ( text.equals( currentText ) )
                isProcessed = true;
        }
        return isProcessed;
    }


    /**
     * processes text in given TextAnnotation
     * sets current state with text and resulting TextAnnotation for subsequent view requests
     * @param ta
     * @return
     */
    private TextAnnotation process( TextAnnotation ta )
    {
        TextAnnotation processedTa = null;

        try {
            processedTa = preprocessor.processTextAnnotation(ta, this.respectTokenization);
        } catch (AnnotationFailedException e) {
            e.printStackTrace();
            logger.error( "Couldn't process TextAnnotation with text '" + ta.getText() + "'." );
        } catch (TException e) {
            e.printStackTrace();
            logger.error("Couldn't process TextAnnotation with text '" + ta.getText() + "'.");
        }


        currentTextAnnotation = processedTa;
        currentText = processedTa.getText();

        return processedTa;
    }



    @Override
    protected TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text) {
        TextAnnotation ta = null;
        try {
            ta = preprocessor.createTextAnnotation( corpusId, docId, text, this.respectTokenization );
        } catch (TException e) {
            e.printStackTrace();
            logger.error( "Couldn't create TextAnnotation for text '" + text + "': " + e.getMessage() );
        } catch (AnnotationFailedException e) {
            e.printStackTrace();
            logger.error("Couldn't create TextAnnotation for text '" + text + "': " + e.getMessage());
        }

        return ta;
    }


}
