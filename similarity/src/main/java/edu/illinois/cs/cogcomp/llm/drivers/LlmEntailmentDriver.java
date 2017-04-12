package edu.illinois.cs.cogcomp.llm.drivers;

import edu.illinois.cs.cogcomp.llm.entailment.LlmEntailmentClassifier;

public class LlmEntailmentDriver {

	/**
	 * @param args
	 */
	public static void main( String[] args ) 
	{
		if ( args.length != 5 ) 
		{
			System.err.println( "USAGE: LlmEntailmentDriver trainCorp trainId testCorp testId llmConfigFile" );
			System.exit( 1 );
		}
		
		String trainFile = args[ 0 ];
		String trainId = args[ 1 ];
	    String testFile = args[ 2 ];
	    String testId = args[ 3 ];
	    String configFile = args[ 4 ];

		try
		{
		    LlmEntailmentClassifier cl = new LlmEntailmentClassifier( configFile );
	    
            cl.runLlmOnTeCorpus(trainFile, trainId, testFile, testId);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
	}
}
