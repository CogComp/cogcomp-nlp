package edu.illinois.cs.cogcomp.nlp.main;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPreprocessor;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import org.apache.thrift.TException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * not fully implemented
 * presently just enough to help debug the system with arbitrary file, without blowing up unit tests
 */
public class RunPreprocessor
{
    private static final String NAME = RunPreprocessor.class.getCanonicalName();

    private IllinoisPreprocessor pipeline;

    public RunPreprocessor( String config ) throws Exception {
        ResourceManager rm = new ResourceManager( config );
        pipeline = new IllinoisPreprocessor( rm );
    }

	public TextAnnotation runPreprocessorOnFile( String fileName, String corpusId, String textId, boolean isWhitespaced ) throws FileNotFoundException, TException, AnnotationFailedException {
        String text = LineIO.slurp( fileName );
        TextAnnotation ta = pipeline.processTextToTextAnnotation( corpusId, textId, text, isWhitespaced );
        return ta;
	}

    public static void main( String[] args )
    {
        if ( args.length != 2 )
        {
            System.err.println( "Usage: "  + NAME + " config inputFile" );
            System.exit( -1 );
        }
        String config = args[ 0 ];
        String inFile = args[ 1 ];
        File file = new File( inFile );
        String corpusId = "test";
        String textId = file.getName();

        RunPreprocessor rp = null;
        try {
            rp = new RunPreprocessor( config );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit( -1 );
        }
        try {

            TextAnnotation ta = rp.runPreprocessorOnFile(inFile, corpusId, textId,  false);
            System.out.println( ta.toString() );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (AnnotationFailedException e) {
            e.printStackTrace();
        }


    }


	public void runPreprocessorOnDirectory( String dirName_, String outDir_ )
	{
		
	}
}
