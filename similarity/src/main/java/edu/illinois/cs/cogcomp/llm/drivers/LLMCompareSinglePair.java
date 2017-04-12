/**
 * 
 */
package edu.illinois.cs.cogcomp.llm.drivers;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author mssammon
 *
 */
public class LLMCompareSinglePair
{

	/**
	 * @param args: none 
	 */
    
    private static final String config = "sampleConfig.txt";

	private static final String NAME = LLMCompareSinglePair.class.getCanonicalName();
	
	private static Logger logger = LoggerFactory.getLogger( LLMCompareSinglePair.class );
	
	private static boolean TEST = true;
	
	public static void main(String[] args) 
	{
//	    String source = "Of the three kings referred to by their last names, Atawanaba was the oldest.";
//		String target = "Three kings were named in the lawsuit.";
		
//		LlmComparatorOld llm = null;
		
		LlmStringComparator llmNew = null;
	
		String configFile = config;
		
		if ( args.length != 3 )
		{
			System.err.println( "USAGE: " + NAME + " configFile inputFile outputFile" );
			System.exit( -1 );
		}

		configFile = args[ 0 ];
		String inFile = args[ 1 ];
		String outFile = args[ 2 ];

		String first = null;
		String second = null;

		try {
			ArrayList<String> inputs = LineIO.read(inFile);
			if ( inputs.size() != 2 )
			{
				System.err.println( "ERROR: " + NAME + ": input file needs two sentences." );
				System.exit( -1 );
			}
			first = inputs.get( 0 );
			second = inputs.get( 1 );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit( -1 );
		}


		try 
		{
			PrintWriter out = new PrintWriter( new File( outFile ) );
			llmNew = new LlmStringComparator( new ResourceManager( configFile ) );
			String msg = "running LLM with config file '" + configFile + "'...";
			logger.debug( msg );
			out.println( msg );
			double resultNew = llmNew.compareStrings( first, second );


			out.println( "compared '" + first + "' with '" + second + "': ");
			out.println( "LLM score is: " + resultNew );
			out.flush();
			out.close();
		}
		catch ( Exception e ) 
		{
			e.printStackTrace();
		}
		
	}

}
