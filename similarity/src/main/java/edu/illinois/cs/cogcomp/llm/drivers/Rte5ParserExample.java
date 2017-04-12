package edu.illinois.cs.cogcomp.llm.drivers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.io.FileReader;
import java.io.FileWriter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.mrcs.dataReaders.Rte5CorpusHandler;

/**
 * TODO: 
 *   flush output buffers on exception
 *   fill out exception behavior in getAnnotation()-- e.g. rethrow exceptions?
 *   
 * @author mssammon
 *
 */

public class Rte5ParserExample {

//	static Logger logger = Logger.getLogger( Rte5ParserExample.class.getName() );
	
	
	public static void main(String[] args) throws Exception
	{
		if ( 3 > args.length || 4 < args.length ) {
			System.out.println( "USAGE: Rte5Parser corpusId inFile outFile");

			String argString = "";
			for ( int i = 0; i < args.length; ++i ) 
				argString += args[i] + ",";
			
			System.out.println( "saw: " + argString );
			return;
		}
		String corpusId = args[0];
		String corpus = args[1];
		String outFile = args[2];
	
		Boolean DEBUG = false;
		
//		if ( args.length == 4 ) 
//		{
//			PropertyConfigurator.configure(args[3]);
//		}
//		else
//			BasicConfigurator.configure();
		
		
		FileReader inputStream = null;
		FileWriter outputStream = null;

		XMLReader reader = null;
		InputSource data = null;
		
		try 
		{
			inputStream = new FileReader( corpus );
			
//			if ( DEBUG ) {
//				String enc = inputStream.getEncoding();
////				logger.debug( "## input stream encoding is: " + enc );
//			}
			data = new InputSource( inputStream );
			reader  = XMLReaderFactory.createXMLReader();
		} 
		catch (SAXException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		Rte5CorpusHandler handler = new Rte5CorpusHandler( corpusId );
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
			
			//long start = System.currentTimeMillis();
		try 
		{
			reader.parse(data);
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		inputStream.close();
		HashMap< String, Pair< String, String > > eps = null;
		HashMap< String, HashMap< String, String > > epInfo = null;
		
		try	
		{
			outputStream = new FileWriter( outFile );
			eps = handler.getEntailmentCorpus();
			epInfo = handler.getEntailmentInfo();

//			if ( GlobalVars.DEBUG )
//				logger.debug( "## parser found " + eps.size() + " entailment pairs." );
			
			Set< String > ids = eps.keySet();
			Iterator<String> it_id = ids.iterator();
			
			while ( it_id.hasNext() ) 
			{
				String id = it_id.next();
				HashMap< String ,String > info = epInfo.get( id );
				Pair< String, String > ep = eps.get( id );

				String epString = getEpString( id, info, ep );
				
				
//				EntailmentPair ep = eps.get( id );
				outputStream.write( epString );
				
//				if ( GlobalVars.DEBUG )
//					logger.debug( "id: " + id + "; ep: \n" + epString );
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			outputStream.close();
		}

		if ( null == eps || null == epInfo ) 
		{
//			logger.fatal( "Rte5ParserExample::main(): "
//					+ "null reference to entailment pairs and/or info. ");
			System.err.println( "ERROR: failed to get entailment pairs and/or info." );
			System.exit( 1 );
		}

//		try
//		{
//			CuratorClient client = new CuratorClient("grandpa.cs.uiuc.edu", 9090);
//			boolean isStored = true;
//			boolean isForceUpdate = false;
//
//			Set< String > ids = eps.keySet();
//			Iterator< String > it_id = ids.iterator();
//
//			while ( it_id.hasNext() )
//			{
//				String pairId = it_id.next();
//				Pair< String, String > pairText = eps.get( pairId );
//				HashMap< String, String > pairInfo = epInfo.get( pairId );
//				String tId = pairInfo.get( XmlTags.TEXT_ID );
//				String hId = pairInfo.get( XmlTags.HYP_ID );
//				String tText = pairText.getFirst();
//				String hText = pairText.getSecond();
//
////				if ( GlobalVars.DEBUG )
////					logger.debug( "getting annotation from CuratorClient for "
////							+ "text '" + tId + "', hyp '" + hId + "'...\n" );
//
//				TextAnnotation hTa = getAnnotation( client,
//													corpusId,
//													hId,
//													hText,
//													isStored,
//													isForceUpdate
//												   );
//
//				TextAnnotation tTa = getAnnotation( client,
//													corpusId,
//													tId,
//													tText,
//													isStored,
//													isForceUpdate
//												  );
//
//				EntailmentPair ep = new EntailmentPair( tTa, hTa, pairInfo );
//
//				outputStream.write( ep.toString() );
////				outputStream.flush();
//			}
//		}
//		catch ( Exception e )
//		{
//			e.printStackTrace();
//			outputStream.flush();
//			outputStream.close();
//		}
//
        throw new Exception( "Removed dependency on CuratorClient from Edison.  Build a different app." );
//		outputStream.close();
	}


	static protected String getEpString(String id_,
										HashMap<String, String > epInfo_,
										Pair<String, String> ep_) 
	{
		
		String displayString = "Entailment Pair " + id_ + ":\n";
	
		Set< String > atts = epInfo_.keySet();
		Iterator< String > it_att = atts.iterator();
		
		while ( it_att.hasNext() ) 
		{
			String att = it_att.next();
			displayString += att + ": " + epInfo_.get( att ) + "\n";
		}
		
		displayString += "Text: " + ep_.getFirst() + "\n" + "Hyp: " 
		+ ep_.getSecond() + "\n";

		displayString += "---------------------------\n\n";	

		return displayString;
	}
	
	
	
	
//	static protected TextAnnotation getAnnotation( CuratorClient client_,
//												   String corpusId_,
//												   String textId_,
//												   String text_,
//												   final boolean isStore_,
//												   final boolean isForceUpdate_
//												  )
//	{
//		TextAnnotation ta = null;
//		boolean isFailed = false;
//		boolean isSkipParser = false;
//
//		if ( !isSkipParser )
//		{
//
//			try
//			{
//				ta = client_.getTextAnnotation( corpusId_,
//												textId_,
//												text_,
//												isForceUpdate_
//											   );
//
//				client_.addNamedEntityView( ta, isForceUpdate_ );
//
//			}
//			catch (ServiceUnavailableException e)
//			{
//				isFailed = true;
//				e.printStackTrace();
//
////				logger.warn( "Rte5ParserExample::getAnnotation(): " + e.reason
//				System.err.println( "Rte5ParserExample::getAnnotation(): " + e.reason
//									+ "; Generating minimal TextAnnotation for example "
//									+ textId_ + "..." );
//
//
//			}
//			catch (AnnotationFailedException e)
//			{
//				isFailed = true;
//				e.printStackTrace();
//
////				logger.warn( "Rte5ParserExample::getAnnotation(): " + e.reason
////							 + "; Generating minimal TextAnnotation for example "
////						     + textId_ + "..." );
//			}
//			catch (TException e)
//			{
////				logger.warn( "RteParserExample::getAnnotation(): Thrift exception: "
////							 + e.getMessage() +  "; Generating minimal TextAnnotation "
////							 + "for example " + textId_ + "..." );
//				System.err.println( "RteParserExample::getAnnotation(): Thrift exception: "
//						 + e.getMessage() +  "; Generating minimal TextAnnotation "
//						 + "for example " + textId_ + "..." );
//
//				e.printStackTrace();
////				System.exit(1);
//			}
//			catch ( Exception e )
//			{
////				logger.warn( "RteParserExample::getAnnotation(): Java Exception: "
//				System.err.println( "RteParserExample::getAnnotation(): Java Exception: "
//						     + e.getMessage() + "; Generating minimal TextAnnotation "
//							 + "for example " + textId_ + "..." );
//				e.printStackTrace();
////				System.exit( 1 );
//			}
//		}
//
//		boolean isRetryAllowed = true;
//
//		if ( ( isFailed && isRetryAllowed ) || isSkipParser )
//		{
////			if ( GlobalVars.DEBUG )
////				logger.debug( "Generating minimal TextAnnotation..." );
//
////			boolean isSingleSentence = false;
////			LBJTokenizer tokenizer = new LBJTokenizer();
//
//			ta = new TextAnnotation( corpusId_, textId_, text_, SentenceViewGenerators.LBJSentenceViewGenerator );
//
//			try
//			{
//				client_.addNamedEntityView( ta, isForceUpdate_ );
//			}
//			catch (ServiceUnavailableException e)
//			{
//				isFailed = true;
//				e.printStackTrace();
//
////				logger.warn( "Rte5ParserExample::getAnnotation(): " + e.reason
////						  	 + "; Failed to generate annotation for example " + textId_ );
//			}
//			catch (AnnotationFailedException e)
//			{
//				isFailed = true;
//				e.printStackTrace();
//
////				logger.warn( "Rte5ParserExample::getAnnotation(): " + e.reason
////				             + "; Failed to generate annotation for example " + textId_ );
//			}
//			catch (TException e)
//			{
////				logger.warn( ( "Rte5ParserExample::getAnnotation(): " + e.reason
//				System.err.println( "Rte5ParserExample::getAnnotation(): " + e.getMessage()
//									+ "; Thrift exception: couldn't generate TextAnnotation -- "
//									+ "giving up on example " + textId_ );
//
//
//				e.printStackTrace();
////				System.exit(1);
//			}
//			catch ( Exception e )
//			{
////				logger.warn( "Java Exception: couldn't generate TextAnnotation -- "
//				System.err.println( "Java Exception: couldn't generate TextAnnotation -- "
//					      + "giving up on example " + textId_ );
//				e.printStackTrace();
////				System.exit( 1 );
//			}
//		}
//
//		return ta;
//	}
	
	
}
