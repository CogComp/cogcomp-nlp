package edu.illinois.cs.cogcomp.nlp.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * not fully implemented
 * presently just enough to help debug the system with arbitrary file, without blowing up unit tests
 */
public class RunPreprocessor
{
    private static final String NAME = RunPreprocessor.class.getCanonicalName();

    private AnnotatorService pipeline;

    public RunPreprocessor( String config ) throws Exception {
        ResourceManager rm = new ResourceManager( config );
        pipeline = IllinoisPipelineFactory.buildPipeline(rm);
    }

	public TextAnnotation runPreprocessorOnFile( String fileName ) throws FileNotFoundException, AnnotatorException {
        String text = LineIO.slurp( fileName );
        boolean forceUpdate = true; // in actual use, this will usually be 'false'
        return pipeline.createAnnotatedTextAnnotation(text, forceUpdate);
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

        RunPreprocessor rp = null;
        try {
            rp = new RunPreprocessor( config );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit( -1 );
        }
        try {

            TextAnnotation ta = rp.runPreprocessorOnFile(inFile);
            System.out.println( ta.toString() );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (AnnotatorException e) {
            e.printStackTrace();
        }


    }


//	public void runPreprocessorOnDirectory( String dirName_, String outDir_ )
//	{
//
//	}
}
