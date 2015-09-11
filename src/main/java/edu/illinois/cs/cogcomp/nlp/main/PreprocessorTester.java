package edu.illinois.cs.cogcomp.nlp.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

/**
 * allows user to run a test driver from the command line that checks output of views is
 *   consistent.  Assumes config file specifies POS, Stanford parse, chunk, NER, and lemma views as active.
 * @author mssammon
 *
 */

public class PreprocessorTester
{
    
    private static final String NAME = PreprocessorTester.class.getCanonicalName();

    public static void main( String[] args_ )
    {
        if ( args_.length != 4 )
        {
            System.err.println( "Usage: " + NAME + " <configFile> <textInFile> <textOutFile> <serializedOutputFile>" );
            System.exit( -1 );
        }
        
        String configFile = args_[0];
        String textInFile = args_[1];
        String textOutFile = args_[ 2 ];
        String serOutFile = args_[3];
        
        ResourceManager rm = ( new PipelineConfigurator() ).getDefaultConfig();
//        try
//        {
//            rm = new ResourceManager( configFile );
//        }
//        catch ( IOException e )
//        {
//            e.printStackTrace();
//            System.exit( -1 );
//        }
       
        PrintStream out =  null;
        
        try
        {
	        out = new PrintStream( new File( textOutFile ) );
        }
        catch ( FileNotFoundException e2 )
        {
	        e2.printStackTrace();
	        System.exit( -1 );
        }
        
        AnnotatorService prep = null;
        
        try
        {
            prep = IllinoisPipelineFactory.buildPipeline( rm );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
        
        String text = null; //"The CIA thought Marin had left the building already." ;
        
        try
        {
            text = LineIO.slurp( textInFile );
        }
        catch ( FileNotFoundException e1 )
        {
            e1.printStackTrace();
        } 
        
        TextAnnotation rec = null;
        boolean forceUpdate = true;

        try
        {
            rec = prep.createAnnotatedTextAnnotation( text, forceUpdate );
        }
        catch (AnnotatorException e)
        {
            e.printStackTrace();
            System.exit( -1 );
        }


        View lab = rec.getView(ViewNames.POS);
        View chunk = rec.getView(ViewNames.SHALLOW_PARSE);
        View toks = rec.getView(ViewNames.TOKENS);
        View lemmaLab = rec.getView(ViewNames.LEMMA);
        
        List<Constituent> tokens = toks.getConstituents();
        List< Constituent >  posTags = lab.getConstituents();
//        List< Span > lemmas = lemmaLab.getLabels();

        if ( tokens.size() != posTags.size() )
        {
            System.err.println( "ERROR: tokens, pos have different sizes." );
            System.exit( -1 );
        }

        out.println(lab);
        out.println(chunk);
        out.println(lemmaLab);


        Set< String > viewList = rec.getAvailableViews();
        
        System.err.println( "## Views in record: " ); // doesn't contain lemma view...
        
        for ( String v : viewList )
            System.err.print( v + ", " );
        
        System.err.println();
        
        TreeView parse = (TreeView) rec.getView(ViewNames.PARSE_STANFORD);
        
        int numParseLeaves = 0;
        
        if ( rec.getNumberOfSentences() > 0 )
        {
            for ( int sentenceId = 0; sentenceId < rec.getNumberOfSentences(); sentenceId++ )
            {
                Tree<String> parseTree = parse.getTree(sentenceId);
                numParseLeaves += parseTree.getYield().size();
                out.println(parseTree);
                out.println( "-----------" );
            }
        }

        View ner = rec.getView(ViewNames.NER_CONLL);
        int numNer = 0;
        
        if ( null != ner )
        {
        	numNer = ner.getNumberOfConstituents();
        	
            if ( numNer > 0 )
            {
                out.println(ner);
                out.println( "-----------" );                
            }
        }
        if ( tokens.size() == 0 || posTags.size() == 0 || numParseLeaves == 0 ||  numNer == 0 )
        {
            System.err.println( "ERROR: tokens, pos, parse or NER view have zero size. " );
            System.exit( -1 );
        }
            
        out.println( "Number of tokens: " + tokens.size() );
        out.println( "Number of POS tags: " + posTags.size() );
        out.println( "Number of Parse leaves: " + numParseLeaves );
//        out.println( "Number of Dependency leaves: " + numDepLeaves );
        out.println( "Number of Named Entities: " + ner.getNumberOfConstituents() );
        
//        if ( numParseLeaves != tokens.size() || numParseLeaves != posTags.size() )
//        {
//            System.err.println( "ERROR: different number of parse leaves, tokens, and pos tags." );
//        }
//       
        out.close();
        boolean forceOverwrite = true;
        try
        {
	        SerializationHelper.serializeTextAnnotationToFile(rec, serOutFile, forceOverwrite);
        }
        catch ( IOException e )
        {
	        e.printStackTrace();
	        System.err.println("Couldn't serialize record to file '" + serOutFile + "': " +
	        		e.getMessage() );
        }
    }

}
