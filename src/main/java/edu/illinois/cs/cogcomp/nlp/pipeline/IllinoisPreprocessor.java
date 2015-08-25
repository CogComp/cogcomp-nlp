package edu.illinois.cs.cogcomp.nlp.pipeline;

import java.util.*;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilderInterface;
import edu.illinois.cs.cogcomp.annotation.handler.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.common.PipelineVars;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * IllinoisPreprocessor is intended to provide a simple one-stop
 *    application for annotating plain text with a set of NLP tools.
 * Behavior (what annotations get added) is specified by a config file
 *    which is used to instantiate an edu.illinois.cs.cogcomp.core.utilities.ResourceManager 
 * 
 * Configuration flags and possible values are as follows:
 * 
 * usePos	[true|false]
 * useChunker	[true|false]
 * useLemmatizer	[true|false]
 * useNer	[true|false]
 * useStanfordParse [true|false]
 * nerConfigFile   config/ner-conll-config.properties
 * lemmaCacheFile  data/lemmaCache.txt
 * updateLemmaCacheFile    true
 * maxLemmaCacheEntries    10000
 * wordnetPath data/WordNet
 * 
 * The first five flags indicate which annotations you want.
 * If useChunker is 'true', usePos must also be 'true'.
 * IllinoisPreprocessor to provide. 
 * The flag 'nerConfigFile' specifies one of the two config
 *    files provided from the Illinois NER tool. "ner-conll-config.properties"
 *    specifies the most standard 4-label model (PER, LOC, ORG, MISC),
 *    whereas "ner-ontonotes-config.properties" specifies a more complex
 *    18-label model (see the documentation for Illinois NER for 
 *    details). 
 * The next three flags specify properties of the lemmatizer cache; it will speed
 *    up the lemmatizer considerably if you allocate a large number
 *    (say, 100000) to maxLemmaCacheEntries and set updateLemmaCacheFile
 *    to 'true'. 'lemmaCacheFile' indicates the plain text file that
 *    contains the cache.
 * 'wordnetPath' should point to the root directory of your copy
 *    of WordNet.
 * 
 * @author mssammon
 *
 */
public class IllinoisPreprocessor
{
	private static final String NAME = IllinoisPreprocessor.class.getCanonicalName();
    private final TextAnnotationBuilderInterface textAnnotationBuilder;
    private ParserAnnotator parseAnnotator;
    private POSTaggerAnnotator posAnnotator;
    //    private boolean useSegmenter;
    private boolean usePos;
    private boolean useLemmatizer;
    private boolean useChunker;
    private boolean useStanfordParse;
    private boolean useNer;
    private boolean useNerExt;

    private IllinoisPOSHandler pos;
    private IllinoisLemmatizerHandler lemmatizer;
    private IllinoisChunkerHandler chunker;
    private IllinoisNerExtHandler ner;
    private IllinoisNerExtHandler nerExt;
    private StanfordCoreNLP stanfordPipeline;

    private Map< String, Boolean > supportedViews;

//    private boolean respectTokenization;
    
    private Logger logger = LoggerFactory.getLogger( IllinoisPreprocessor.class );
    private Set< String > activeViews;
    private boolean forceCacheUpdate;


    /**
     * ResourceManager is read from a config file that specifies which 
     *    annotation resources are active. 
     *    
     * @param rm_ The ResourceManager object
     * @throws Exception
     */
    public IllinoisPreprocessor( ResourceManager rm_, TextAnnotationBuilderInterface taBuilder ) throws Exception
    {
        this.textAnnotationBuilder = taBuilder;
//        respectTokenization = rm_.getBoolean( PipelineVars.RESPECT_TOKENIZATION );
        usePos = rm_.getBoolean( PipelineVars.USE_POS );
        useStanfordParse = rm_.getBoolean( PipelineVars.USE_STANFORD_PARSE );
        useChunker = rm_.getBoolean( PipelineVars.USE_CHUNKER );
        useLemmatizer = rm_.getBoolean( PipelineVars.USE_LEMMA );
        useNer = rm_.getBoolean( PipelineVars.USE_NER );
        useNerExt = rm_.getBoolean( PipelineVars.USE_NEREXT );
        forceCacheUpdate = rm_.getBoolean( PipelineVars.FORCE_CACHE_UPDATE );
        setSupportedViews();



    	if ( !usePos )
    	{
    		if ( useChunker )
    		{
    			String errMsg = "ERROR: " + NAME + " constructor: useChunker requires usePos flag to be 'true'; " + 
            		"setting usePos to true.";
                logger.error( errMsg );
    			usePos = true;
    		}
    	    if ( useLemmatizer )
            {
                String errMsg = "ERROR: " + NAME + " constructor: useLemmatizer requires usePos flag to be 'true'";
                logger.error( errMsg );
               usePos = true;
            }
        
    	}
    	
        if ( usePos )
            pos = new IllinoisPOSHandler();
        
        if ( useChunker )
        	chunker = new IllinoisChunkerHandler();
                
        if ( useLemmatizer )
            lemmatizer = new IllinoisLemmatizerHandler( rm_ );
        
        if ( useNer )
        {
        	String nerConfig = rm_.getString( PipelineVars.NER_CONLL_CONFIG );
        	ner = new IllinoisNerExtHandler( nerConfig );
        }

        if ( useNerExt )
        {
            String nerConfig = rm_.getString( PipelineVars.NER_ONTONOTES_CONFIG );
            nerExt = new IllinoisNerExtHandler( nerConfig );
        }

        if (useStanfordParse) {
        	// Add options to avoid splitting and tokenization errors
        	// XXX Need to make sure sentence is given to parser with tokenization
            Properties props = new Properties();
//            props.put("annotators", "tokenize, ssplit, pos, parse");
//        	props.put("tokenize.whitespace", "true");
//        	props.put("ssplit.eolonly", "true");
            props.put( "annotators", "pos, parse") ;
            props.put("parse.originalDependencies", true);
            posAnnotator = new POSTaggerAnnotator("pos", props);
            parseAnnotator = new ParserAnnotator("parse", props);
//            stanfordPipeline = new StanfordCoreNLP(props);
        }
        	
    }
    
    /**
     * generates a TextAnnotation from the input raw text.
     *
     * @param corpusId_ The corpus ID
     * @param textId_ The text ID
     * @param rawText_ The raw string
     * @return The TextAnnotation object
     * @throws AnnotatorException
     */
    public TextAnnotation processTextToTextAnnotation( String corpusId_, String textId_, String rawText_ ) throws AnnotatorException {
        TextAnnotation record = this.textAnnotationBuilder.createTextAnnotation(corpusId_, textId_, rawText_);

        return processTextAnnotation(record);

    }

    /**
     * Annotates TextAnnotation argument with views according to annotators with which this object is instantiated.
     *
     * @param ta TextAnnotation object to annotate
     * @return The annotated TextAnnotation object
     * @throws AnnotatorException
     */

    public TextAnnotation processTextAnnotation( TextAnnotation ta ) throws AnnotatorException {
        if ( usePos )
            pos.labelRecord( ta );

        if ( useChunker )
            chunker.labelRecord( ta );


        if ( useLemmatizer )
            lemmatizer.labelRecord( ta );

        //TODO Add these as TextAnnotation-based handlers
//        if ( useNer )
//            ner.labelRecord( ta );
//
//        if ( useNerExt )
//            nerExt.labelRecord(ta);


        if ( useStanfordParse )
            if (null != stanfordPipeline || ( null != this.posAnnotator && null != this.parseAnnotator ) )
            {
                StanfordParseHandler.annotate(this.posAnnotator, this.parseAnnotator, ta);
            }
            else
            {
                String msg = "ERROR: " + NAME + ".processText(): stanford parse view(s) requested, but no stanford " +
                        "parse annotator instantiated.";
                logger.error( msg );
                throw new AnnotatorException( msg );
            }
        return ta;
    }
    



	public TextAnnotation processText( String rawText_ ) throws AnnotatorException
    {
        return processTextToTextAnnotation("dummyId", "dummyId", rawText_);
    }




    /**
     *  this indicates views that can be populated by the preprocessor.
     *  CachingAnnotationService expects each key to represent an available service,
     *     and the boolean flag associated with each value to indicate whether the
     *     cached version of that view (if any) should be replaced.
     */
    protected void setSupportedViews()
    {
        if ( null == this.supportedViews) {
            this.supportedViews = new HashMap<String, Boolean>();
        }
        if ( supportedViews.isEmpty() )
        {
            if ( usePos )
                supportedViews.put(ViewNames.POS, forceCacheUpdate );

            if ( useStanfordParse ) {
                supportedViews.put(ViewNames.PARSE_STANFORD, forceCacheUpdate);
                supportedViews.put( ViewNames.DEPENDENCY_STANFORD, forceCacheUpdate );
            }
            if ( useChunker )
                supportedViews.put(ViewNames.SHALLOW_PARSE, forceCacheUpdate);

            if( useLemmatizer )
                supportedViews.put(ViewNames.LEMMA, forceCacheUpdate);

            if ( useNer )
                supportedViews.put(ViewNames.NER_CONLL, forceCacheUpdate);

            if ( useNerExt )
                supportedViews.put(ViewNames.NER_ONTONOTES, forceCacheUpdate );
        }

        setActiveViews();
    }

    private void setActiveViews()
    {
        if ( null == activeViews )
            activeViews = new HashSet<String>();

        activeViews.addAll( supportedViews.keySet() );

    }

    protected Map< String, Boolean > getSupportedViews()
    {
        return supportedViews;
    }

}
