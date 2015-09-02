package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.handler.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TextAnnotationBuilder;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by mssammon on 8/31/15.
 */
public class IllinoisPipelineFactory
{

    private static final String NER_CONFIG = "nerConfig";
    private static final String LEMMA_CONFIG = "lemmaConfig";
    private static final java.lang.String NER_VIEW = "nerViewName";

    /**
     * Builds a complete pipeline with all the tools currently available,
     *   though for now, only one of two possible NER models.
     *
     * TODO:
     *    allow a configuration to specify a subset of annotators to use.
     *      probably, a set of ViewName values to 'true'/'false' entries,
     *      which would allow more or less direct use of Annotator-specified
     *      dependencies to activate components.
     *    CachingAnnotatorService will check these dependencies at run-time,
     *      so if you don't provide a prerequisite for some component, you'll know.
     * @param rm
     * @return
     * @throws IOException
     */

    public static AnnotatorService buildPipeline( ResourceManager rm ) throws IOException, AnnotatorException {
        String nerConfig = rm.getString( NER_CONFIG );
        String nerView = rm.getString( NER_VIEW );
        String lemmaConfig = rm.getString( LEMMA_CONFIG );

        IllinoisTokenizer tokenizer = new IllinoisTokenizer();
        TextAnnotationBuilder taBuilder = new TextAnnotationBuilder( tokenizer );

        IllinoisPOSHandler pos = new IllinoisPOSHandler();
        IllinoisChunkerHandler chunk = new IllinoisChunkerHandler();
        IllinoisNerHandler ner = new IllinoisNerHandler( nerConfig, ViewNames.NER_CONLL );
        IllinoisLemmatizerHandler lemma = new IllinoisLemmatizerHandler( lemmaConfig );


        Properties stanfordProps = new Properties();
        stanfordProps.put( "annotators", "pos, parse") ;
        stanfordProps.put("parse.originalDependencies", true);

        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator( "pos", stanfordProps );
        ParserAnnotator parseAnnotator = new ParserAnnotator( "parse", stanfordProps );

        StanfordParseHandler parser = new StanfordParseHandler( posAnnotator, parseAnnotator );
        StanfordDepHandler depParser = new StanfordDepHandler( posAnnotator, parseAnnotator );

        Map< String, Annotator> extraViewGenerators = new HashMap<String, Annotator>();

        extraViewGenerators.put( ViewNames.POS, pos );
        extraViewGenerators.put( ViewNames.SHALLOW_PARSE, chunk );
        extraViewGenerators.put( ViewNames.LEMMA, lemma );
        extraViewGenerators.put( ViewNames.NER_CONLL, ner );
        extraViewGenerators.put( ViewNames.PARSE_STANFORD, parser );
        extraViewGenerators.put( ViewNames.DEPENDENCY_STANFORD, depParser );

        boolean throwExceptionIfUncached = false;

        Map< String, Boolean > requestedViews = new HashMap<String, Boolean>();
        for ( String view : extraViewGenerators.keySet() )
            requestedViews.put( view, false );

//        String cacheDir = rm.getString( IllinoisCachingPreprocessor.CACHE_DIR );
        AnnotatorService pipeline = new AnnotatorService(requestedViews, taBuilder, extraViewGenerators, rm);
        //prep = IllinoisCachingPreprocessor.getInstance(CONFIG, taBuilder, extraViewGenerators);

//        AnnotatorService.openCache(cacheDir);

        return pipeline;
    }
}
