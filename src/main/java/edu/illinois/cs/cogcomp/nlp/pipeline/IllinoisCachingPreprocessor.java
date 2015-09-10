package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilderInterface;
import edu.illinois.cs.cogcomp.common.CuratorViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TextAnnotationBuilder;
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
@Deprecated
public class IllinoisCachingPreprocessor extends AnnotatorService
{
    public static final java.lang.String CACHE_DIR = "cacheDir";
    private static IllinoisCachingPreprocessor THE_INSTANCE;
    private static Map<String, Boolean> activeViews;

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


    public static IllinoisCachingPreprocessor getInstance( String config,  Map< String, Annotator> extraViewGenerators ) throws Exception {
        return getInstance(new ResourceManager(config), new TextAnnotationBuilder(new IllinoisTokenizer() ), extraViewGenerators );

    }

    public static IllinoisCachingPreprocessor getInstance( String config, TextAnnotationBuilderInterface taBuilder,  Map< String, Annotator> extraViewGenerators ) throws Exception {
        return getInstance(new ResourceManager(config), taBuilder, extraViewGenerators );

    }


    public static IllinoisCachingPreprocessor getInstance( ResourceManager rm, TextAnnotationBuilderInterface taBuilder, Map< String, Annotator > extraViewGenerators ) throws Exception {
        if ( null == THE_INSTANCE )
        {
            THE_INSTANCE = new IllinoisCachingPreprocessor( new IllinoisPreprocessor( rm, taBuilder ), rm, taBuilder, extraViewGenerators );
        }

        return THE_INSTANCE;
    }

    /**
     * private constructor as should be a singleton
     *
     * @param illinoisPreprocessor
     * @param rm
     * @param extraViewGenerators
     * @throws AnnotatorException
     */
    private IllinoisCachingPreprocessor(IllinoisPreprocessor illinoisPreprocessor, ResourceManager rm, TextAnnotationBuilderInterface taBuilder, Map<String, Annotator> extraViewGenerators) throws AnnotatorException {
        super(  taBuilder,
                extraViewGenerators,
                rm.getString( AnnotatorService.CACHE_DIR ),
                rm.getBoolean( AnnotatorService.THROW_EXCEPTION_IF_NOT_CACHED ),
                rm.getInt( AnnotatorService.CACHE_HEAP_SIZE ),
                rm.getInt( AnnotatorService.CACHE_DISK_SIZE ),
                rm.getBoolean( AnnotatorService.SET_CACHE_SHUTDOWN_HOOK )
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
     * uses default cache values; use IllinoisPipelineFactory instead
     *
     * @param requestedViews
     * @param throwExceptionIfNotCached
     * @param extraViewProviders
     * @throws edu.illinois.cs.cogcomp.annotation.AnnotatorException
     */
    private IllinoisCachingPreprocessor(Map<String, Boolean> requestedViews, TextAnnotationBuilderInterface taBuilder, boolean throwExceptionIfNotCached, Map<String, Annotator> extraViewProviders, IllinoisPreprocessor preprocessor) throws AnnotatorException {
        super(taBuilder,  extraViewProviders);
        initializeCurrentStatus();
    }

    private void initializeCurrentStatus() {
        currentText = null;
        currentTextAnnotation = null;
    }




    private void addViewFromTo(String viewName, TextAnnotation currentTa, TextAnnotation newTa ) {

        View v = currentTa.getView( viewName );
        newTa.addView( viewName, v );
    }



    private boolean isProcessed(String text, String viewName) {
        boolean isProcessed = false;

        if ( !text.equals(currentText) && null != currentTextAnnotation)
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
            processedTa = preprocessor.processTextAnnotation(ta );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            logger.error( "Couldn't process TextAnnotation with text '" + ta.getText() + "'." );
        }


        currentTextAnnotation = processedTa;
        if ( null != processedTa )
            currentText = processedTa.getText();

        return processedTa;
    }


//
//    @Override
//    protected TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text) {
//        TextAnnotation ta = null;
//        try {
//            ta = preprocessor.createTextAnnotation( corpusId, docId, text, this.respectTokenization );
//        } catch (TException e) {
//            e.printStackTrace();
//            logger.error( "Couldn't create TextAnnotation for text '" + text + "': " + e.getMessage() );
//        } catch (AnnotationFailedException e) {
//            e.printStackTrace();
//            logger.error("Couldn't create TextAnnotation for text '" + text + "': " + e.getMessage());
//        }
//
//        return ta;
//    }


}
