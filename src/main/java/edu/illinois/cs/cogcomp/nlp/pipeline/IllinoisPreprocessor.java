package edu.illinois.cs.cogcomp.nlp.pipeline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.annotation.handler.IllinoisChunkerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisLemmatizerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisNerExtHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisPOSHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisTokenizerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.StanfordToForest;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.curator.Whitespacer;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorDataStructureInterface;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorViewNames;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.nlp.common.AdditionalViewNames;
import edu.illinois.cs.cogcomp.nlp.common.PipelineVars;
import edu.illinois.cs.cogcomp.nlp.curator.Identifier;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Clustering;
import edu.illinois.cs.cogcomp.thrift.base.Forest;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.View;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
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
 * nerConfigFile   config/conll.config
 * lemmaCacheFile  data/lemmaCache.txt
 * updateLemmaCacheFile    true
 * maxLemmaCacheEntries    10000
 * wordnetPath data/WordNet
 * 
 * The first five flags indicate which annotations you want.
 * If useChunker is 'true', usePos must also be 'true'.
 * IllinoisPreprocessor to provide. 
 * The flag 'nerConfigFile' specifies one of the two config
 *    files provided from the Illinois NER tool. "conll.config"
 *    specifies the most standard 4-label model (PER, LOC, ORG, MISC),
 *    whereas "ontonotes.config" specifies a more complex 
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
    //    private boolean useSegmenter;
    private boolean usePos;
    private boolean useLemmatizer;
    private boolean useChunker;
    private boolean useStanfordParse;
    private boolean useNer;
    
    private IllinoisTokenizerHandler tokenizer;
    private IllinoisPOSHandler pos;
    private IllinoisLemmatizerHandler lemmatizer;
    private IllinoisChunkerHandler chunker;
    private IllinoisNerExtHandler ner;
    private StanfordCoreNLP stanfordPipeline;
    
//    private boolean respectTokenization;
    
    private Logger logger = LoggerFactory.getLogger( IllinoisPreprocessor.class );
    
    /**
     * ResourceManager is read from a config file that specifies which 
     *    annotation resources are active. 
     *    
     * @param rm_
     * @throws Exception
     */
    public IllinoisPreprocessor( ResourceManager rm_ ) throws Exception
    {
//        respectTokenization = rm_.getBoolean( PipelineVars.RESPECT_TOKENIZATION );
        usePos = rm_.getBoolean( PipelineVars.USE_POS );
        useStanfordParse = rm_.getBoolean( PipelineVars.USE_STANFORD_PARSE );
        useChunker = rm_.getBoolean( PipelineVars.USE_CHUNKER );
        useLemmatizer = rm_.getBoolean( PipelineVars.USE_LEMMA );
        useNer = rm_.getBoolean( PipelineVars.USE_NER );
        
        tokenizer = new IllinoisTokenizerHandler();

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
        	String nerConfig = rm_.getString( PipelineVars.NER_CONFIG );
        	ner = new IllinoisNerExtHandler( nerConfig );
        }
        if (useStanfordParse) {
        	Properties props = new Properties();
        	props.put("annotators", "tokenize, ssplit, pos, parse");
        	// Add options to avoid splitting and tokenization errors
        	// XXX Need to make sure sentence is given to parser with tokenization
        	props.put("tokenize.whitespace", "true");
        	props.put("ssplit.eolonly", "true");

        	stanfordPipeline = new StanfordCoreNLP(props);
        }
        	
    }
    
    /**
     * generates a TextAnnotation from the input raw text, setting the corpus and text ID fields in 
     *   the TextAnnotation with the specified values.
     *   
     * @param corpusId_
     * @param textId_
     * @param rawText_
     * @param isWhitespaced_
     * @return
     * @throws AnnotationFailedException
     * @throws TException
     */
    public TextAnnotation processTextToTextAnnotation( String corpusId_, String textId_, String rawText_, boolean isWhitespaced_ ) throws AnnotationFailedException, TException
    {
    	Record rec = processText( rawText_, isWhitespaced_ );
    	TextAnnotation ta = CuratorDataStructureInterface.getTextAnnotationViewsFromRecord( corpusId_, textId_, rec );
  
        if ( useLemmatizer )
        {
        	addLabelingToTextAnnotation( rec, AdditionalViewNames.ccgLemma, ta );
//        	addLabelingToTextAnnotation( rec, AdditionalViewNames.lemmaWn, ta );
//        	addLabelingToTextAnnotation( rec, AdditionalViewNames.lemmaPorter, ta );
        }
    	return ta;
    }
    

    /**
     * A complementary method to {@code processTextToTextAnnotation(String, String, String, boolean)}
     * that appends the newly created views to an existing TextAnnotation
     * @param ta
     * @param isWhitespaced_
     * @return The original TextAnnotation with the new views from the TextPrepocessor
     * @throws AnnotationFailedException
     * @throws TException
     */
	public TextAnnotation processTextAnnotation(TextAnnotation ta, boolean isWhitespaced_)
			throws AnnotationFailedException, TException {
		Record rec = processText( ta.getText(), isWhitespaced_ );
		TextAnnotation taTemp = CuratorDataStructureInterface.getTextAnnotationViewsFromRecord( "", "", rec );
		// Now taTemp should have only the new views created by the Preprocessor
		for (String view : taTemp.getAvailableViews()) {
			ta.addView(view, taTemp.getView(view));
		}
		return ta;
	}
    
    private void addLabelingToTextAnnotation(Record rec_, String viewName_, TextAnnotation ta_ ) 
    {
    	Labeling lemmas = rec_.getLabelViews().get( viewName_ );
    	TokenLabelView tlv = CuratorDataStructureInterface.alignLabelingToTokenLabelView( viewName_, ta_, lemmas);
    	ta_.addView(viewName_, tlv);
	}

	public Record processText( String rawText_, boolean isWhitespaced_ ) throws AnnotationFailedException, TException
    {
        Record record = createRecord( rawText_, isWhitespaced_ );
        Map<String, Labeling> labelViews = record.getLabelViews();
        Map<String, Forest> parseViews = record.getParseViews();
        
        if ( usePos )
        {
            Labeling posResult = pos.labelRecord( record );
            labelViews.put( CuratorViewNames.pos, posResult );
        }
        
        if ( useChunker )
        {
        	Labeling chunkerResult = chunker.labelRecord( record );
        	labelViews.put( CuratorViewNames.chunk, chunkerResult );
        }

        
        if ( useLemmatizer )
        {
            Labeling lemmaView = lemmatizer.labelRecord( record );
            
            labelViews.put( AdditionalViewNames.ccgLemma, lemmaView );
//            labelViews.put( AdditionalViewNames.lemmaWnPlus, lemmaViews.get( 1 ) );
//            labelViews.put( AdditionalViewNames.lemmaPorter, lemmaViews.get( 2 ) );
//            labelViews.put( CuratorViewNames.lemmaKp, lemmaViews.get( 3 ) );
            
        }
        
        if ( useNer )
        {
        	Labeling nerView  = ner.labelRecord( record );
        	labelViews.put( CuratorViewNames.ner, nerView );
        }
        
        // XXX We're assuming here that our tokenizer and the Stanford one will agree
        if ( useStanfordParse )
        {
        	Forest parseView = StanfordToForest.convert(stanfordPipeline, record);
        	parseViews.put(CuratorViewNames.stanfordParse, parseView);
        }
        
        return record;
    }
	
	

	/**
     * creates and tokenizes/sentence-splits input text. 
     * 
     * @param rawText_ the text to annotate
     * @param isWhitespaced_ if true, determines sentence boundaries using newlines and token boundaries from other
     *     whitespace
     * @return  a Record with rawText, sentence and token views set.
     * @throws TException 
     * @throws AnnotationFailedException 
     */
    
    public Record createRecord( String rawText_, boolean isWhitespaced_ ) throws AnnotationFailedException, TException
    {
        Record record = new Record();
        record.setRawText( rawText_ );
        record.setWhitespaced( isWhitespaced_ );
        record.setLabelViews(new HashMap<String, Labeling>());
        record.setClusterViews(new HashMap<String, Clustering>());
        record.setParseViews(new HashMap<String, Forest>());
        record.setViews(new HashMap<String, View>());
        record.setIdentifier(Identifier.getId( rawText_, isWhitespaced_ ) );
            
        if ( isWhitespaced_ )
        {
            List< String > inputs = new LinkedList< String >();

            String[] sentences = rawText_.split( System.getProperty( "line.separator" ) );
            
            for ( int i = 0; i < sentences.length; ++i )
                inputs.add( sentences[i] );
            
            Labeling sents = Whitespacer.sentences( inputs );
            record.getLabelViews().put( CuratorViewNames.sentences, sents );
            Labeling tokens = Whitespacer.tokenize( inputs );
            record.getLabelViews().put( CuratorViewNames.tokens, tokens );
        }
        else
        {
            List< Labeling > segmentedViews = tokenizer.labelRecord( record );

            record.getLabelViews().put( CuratorViewNames.sentences, segmentedViews.get( 0 ) );
            record.getLabelViews().put( CuratorViewNames.tokens, segmentedViews.get( 1 ) );
        }
        
        return record;
    }
}
