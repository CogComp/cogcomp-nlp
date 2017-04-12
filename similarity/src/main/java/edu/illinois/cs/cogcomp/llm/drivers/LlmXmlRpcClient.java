package edu.illinois.cs.cogcomp.llm.drivers;

import edu.illinois.cs.cogcomp.mrcs.comparators.XmlRpcMetricClient;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.MetricResponse;


/**
 * sample client to test LlmXmlRpcServer
 * 
 * @author mssammon
 *
 */

public class LlmXmlRpcClient 
{

	private static String m_METHOD_NAME = "Llm.compare";

	public static void main(String[] args) 
	{

		if ( args.length != 2 ) {
			System.err.println( "Usage: LlmXmlRpcClient host port");
		}
		
		String host = new String( args[0] );
		int port = ( new Integer( args[1]) ).intValue();

		System.err.println( "establishing connection to client with: " );
		System.err.println( "host: " + host + "; port: " + port + "; name: " + m_METHOD_NAME );
		
		XmlRpcMetricClient client = new XmlRpcMetricClient( m_METHOD_NAME , host, port, 0.001);

		String source = "George Bush visited France, Germany, and England.";
		String target = "George Bush went to England last year.";
	
		System.err.println( "Calling client with source: '" + source + "', target '" +
				target + "'..." );

		MetricResponse mr = client.compareStrings( source, target );
		
		System.err.println( "Called client, got response: " );
		System.err.println( "score: " + mr.score + "; reason: " + mr.reason );
		
	}
		
//		try {
//
//			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
//			config.setServerURL( new URL("http://" + host + ":" + port + "/xmlrpc") );
//			config.setEnabledForExtensions(true);
//			config.setConnectionTimeout(60 * 1000);
//			config.setReplyTimeout(60 * 1000);
//
//			XmlRpcClient client = new XmlRpcClient();
//			client.setTransportFactory(new XmlRpcCommonsTransportFactory(
//							client));
//			client.setConfig(config);
//
//			HashMap<String, String> mapNames = new HashMap<String, String>();
//			mapNames.put("FIRST_STRING", "George Bush visited France, Germany, and England.");
//			mapNames.put("SECOND_STRING", "George Bush went to England last year.");
//
//			Object[] params = new Object[] { mapNames };
//
//			HashMap<String, String> result = (HashMap<String, String>) client.execute(
//					"EntityComparison.compare", params);
//			
//			System.out.println("Score: " + result.get("SCORE"));
//			System.out.println("Reason: " + result.get("REASON"));
//	
//		} catch (Exception exception) {
//			System.err.println("JavaClient: " + exception);
//		}
//	}
	
}
