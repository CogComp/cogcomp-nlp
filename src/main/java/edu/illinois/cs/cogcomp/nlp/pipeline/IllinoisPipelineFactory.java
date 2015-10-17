package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.handler.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.nlp.util.SimpleCachingPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Builds an AnnotatorService object with all available CCG NLP components.
 *
 * Created by mssammon on 8/31/15.
 */
public class IllinoisPipelineFactory
{

//    private static final String NER_CONLL_CONFIG = "nerConllConfig";
//    private static final String LEMMA_CONFIG = "lemmaConfig";
//    private static final java.lang.String NER_ONTONOTES_CONFIG = "nerOntonotesConfig";

    /**
     * Builds a complete pipeline with all the tools currently available,
     *   though for now, only one of two possible NER models.
     *
     * TODO:
     *    allow a configuration to specify a subset of annotators to use.
     *      probably, a set of ViewName values to 'true'/'false' entries,
     *      which would allow more or less direct use of Annotator-specified
     *      dependencies to activate components.
     *      CachingAnnotatorService will check these dependencies at run-time,
     *      so if you don't provide a prerequisite for some component, you'll know.
     *
     * TODO:
     *    add a "addAnnotator( Annotator )" method that checks supported views and requirements of new
     *      annotator, and if they are compatible, adds the annotator to the pipeline.
     *
     * TODO:
     *    when NER is updated, set it up to allow multiple different NER components with different models
     * @param nonDefaultRm    ResourceManager with properties for config files, caching behavior
     * @return  an AnnotatorService object with a suite of NLP components.
     * @throws IOException
     */

    public static AnnotatorService buildPipeline( ResourceManager nonDefaultRm ) throws IOException, AnnotatorException
    {
        ResourceManager rm = ( new PipelineConfigurator().getConfig( nonDefaultRm ) );
//        Map< String, Annotator > annotators = buildAnnotators( rm );
//        IllinoisTokenizer tokenizer = new IllinoisTokenizer();
//        TextAnnotationBuilder taBuilder = new TextAnnotationBuilder( tokenizer );
//
//
//        Map< String, Boolean > requestedViews = new HashMap<String, Boolean>();
//        for ( String view : annotators.keySet() )
//            requestedViews.put( view, false );

        return new SimpleCachingPipeline( rm );
    }


    /**
     * instantiate a set of annotators for use in an AnnotatorService object
     * @param nonDefaultRm ResourceManager with all non-default values for Annotators
     */
    public static Map<String, Annotator> buildAnnotators(ResourceManager nonDefaultRm) throws IOException {
        ResourceManager rm = new PipelineConfigurator().getConfig(nonDefaultRm);
        String timePerSentence = rm.getString(PipelineConfigurator.STFRD_TIME_PER_SENTENCE );
        String maxParseSentenceLength = rm.getString( PipelineConfigurator.STFRD_MAX_SENTENCE_LENGTH );
        Map< String, Annotator> viewGenerators = new HashMap<>();

        if( rm.getBoolean( PipelineConfigurator.USE_POS ) ) {
            IllinoisPOSHandler pos = new IllinoisPOSHandler();
            viewGenerators.put(ViewNames.POS, pos);
        }
        if ( rm.getBoolean( PipelineConfigurator.USE_LEMMA ) )
        {
            IllinoisLemmatizerHandler lemma = new IllinoisLemmatizerHandler( rm );
            viewGenerators.put(ViewNames.LEMMA, lemma);
        }
        if ( rm.getBoolean( PipelineConfigurator.USE_SHALLOW_PARSE )) {
            IllinoisChunkerHandler chunk = new IllinoisChunkerHandler();
            viewGenerators.put(ViewNames.SHALLOW_PARSE, chunk);
        }
        if ( rm.getBoolean( PipelineConfigurator.USE_NER_CONLL ) ) {
            IllinoisNerHandler nerConll = new IllinoisNerHandler(rm, ViewNames.NER_CONLL);
            viewGenerators.put(ViewNames.NER_CONLL, nerConll);
        }
        if ( rm.getBoolean( PipelineConfigurator.USE_NER_ONTONOTES ) ) {

            IllinoisNerHandler nerOntonotes = new IllinoisNerHandler(rm, ViewNames.NER_ONTONOTES);
            viewGenerators.put(ViewNames.NER_ONTONOTES, nerOntonotes);
        }
        if ( rm.getBoolean( PipelineConfigurator.USE_STANFORD_DEP ) || rm.getBoolean( PipelineConfigurator.USE_STANFORD_PARSE ) )
        {
            Properties stanfordProps = new Properties();
            stanfordProps.put( "annotators", "pos, parse") ;
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put( "parse.maxlen", maxParseSentenceLength );
            stanfordProps.put( "parse.maxtime", timePerSentence ); // per sentence? could be per document but no idea from stanford javadoc
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator( "pos", stanfordProps );
            ParserAnnotator parseAnnotator = new ParserAnnotator( "parse", stanfordProps );

            if ( rm.getBoolean( PipelineConfigurator.USE_STANFORD_DEP ) )
            {
                StanfordDepHandler depParser = new StanfordDepHandler( posAnnotator, parseAnnotator );
                viewGenerators.put(ViewNames.DEPENDENCY_STANFORD, depParser);
            }
            if ( rm.getBoolean( PipelineConfigurator.USE_STANFORD_PARSE ) )
            {
                StanfordParseHandler parser = new StanfordParseHandler( posAnnotator, parseAnnotator );
                viewGenerators.put(ViewNames.PARSE_STANFORD, parser);
            }
        }

        return viewGenerators;
    }
}
