package edu.illinois.cs.cogcomp.nlp.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorDataStructureInterface;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorViewNames;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.io.RecordFileIO;
import edu.illinois.cs.cogcomp.nlp.common.AdditionalViewNames;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPreprocessor;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Forest;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.base.Tree;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.util.CuratorDataUtils;

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
        
        ResourceManager rm = null;
        try
        {
            rm = new ResourceManager( configFile );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
       
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
        
        IllinoisPreprocessor prep = null;
        
        try
        {
            prep = new IllinoisPreprocessor( rm );
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
        
        Record rec = null;
        try
        {
            rec = prep.processText( text, false );
        }
        catch ( AnnotationFailedException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
        catch ( TException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
        
        
        Labeling lab = rec.getLabelViews().get( CuratorViewNames.pos );
        Labeling chunk = rec.getLabelViews().get( CuratorViewNames.chunk );
        Labeling toks = rec.getLabelViews().get( CuratorViewNames.tokens );
        Labeling lemmaLab = rec.getLabelViews().get( AdditionalViewNames.ccgLemma );
        
        List< Span > tokens = toks.getLabels();
        List< Span >  posTags = lab.getLabels();
//        List< Span > lemmas = lemmaLab.getLabels();

        if ( tokens.size() != posTags.size() )
        {
            System.err.println( "ERROR: tokens, pos have different sizes." );
            System.exit( -1 );
        }
        
        CuratorDataUtils.printLabeling( out, lab, text );
        out.println();
        
        CuratorDataUtils.printLabeling( out, chunk, text );
        
        CuratorDataUtils.printLabeling( out, lemmaLab, text );
        
        
        TextAnnotation ta = CuratorDataStructureInterface.getTextAnnotationViewsFromRecord( "test", "test", rec );

        Set< String > viewList = ta.getAvailableViews();
        
        System.err.println( "## Views in record: " ); // doesn't contain lemma view...
        
        for ( String v : viewList )
            System.err.print( v + ", " );
        
        System.err.println();
        
        Forest parse = rec.getParseViews().get( CuratorViewNames.stanfordParse );
        
        int numParseLeaves = 0;
        
        if ( parse.getTrees().size() > 0 )
        {
            for ( Tree parseTree : parse.getTrees() )            
            {
                numParseLeaves += CuratorDataUtils.printTree( out, parseTree, text );
                out.println( "-----------" );
            }
        }

//        Forest dep = rec.getParseViews().get( CuratorViewNames.stanfordDep );
//        
//        int numDepLeaves = 0;
//        
//        if ( dep.getTrees().size() > 0 )
//        {
//            for ( Tree depTree : dep.getTrees() )            
//            {
//                numDepLeaves += CuratorDataUtils.printTree( out, depTree, text );
//                out.println( "-----------" );
//            }
//        }

        
        Labeling ccgLemmas = rec.getLabelViews().get( AdditionalViewNames.ccgLemma );
        
        if ( null != ccgLemmas )
            if ( ccgLemmas.getLabelsSize() > 0 )
            {
                CuratorDataUtils.printLabeling( out, ccgLemmas, text );
                out.println( "-----------" );                
            }
  
        Labeling ner = rec.getLabelViews().get( CuratorViewNames.ner );
        int numNer = 0;
        
        if ( null != ner )
        {
        	numNer = ner.getLabelsSize();
        	
            if ( numNer > 0 )
            {
                CuratorDataUtils.printLabeling( out, ner, text );
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
        out.println( "Number of CCG Lemmas: " + ccgLemmas.getLabelsSize() );
        out.println( "Number of Named Entities: " + ner.getLabelsSize() );
        
//        if ( numParseLeaves != tokens.size() || numParseLeaves != posTags.size() )
//        {
//            System.err.println( "ERROR: different number of parse leaves, tokens, and pos tags." );
//        }
//       
        out.close();
        boolean forceOverwrite = true;
        try
        {
	        RecordFileIO.serializeRecordToFile( rec, serOutFile, forceOverwrite );
        }
        catch ( IOException e )
        {
	        e.printStackTrace();
	        System.err.println("Couldn't serialize record to file '" + serOutFile + "': " +
	        		e.getMessage() );
        }
        catch ( TException e )
        {
	        e.printStackTrace();
	        System.err.println("Couldn't serialize record to file '" + serOutFile + "': " +
	        		e.getMessage() );
        }
    }

}
