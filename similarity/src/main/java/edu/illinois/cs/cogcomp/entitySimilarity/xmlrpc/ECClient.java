package edu.illinois.cs.cogcomp.entitySimilarity.xmlrpc;

import java.net.URL;
import java.util.HashMap;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;


public class ECClient {
	
	public static void main(String [] args) {
		if (args.length != 2)
			System.err.println("Usage: ECClient host port");
		String host = new String(args[0]);
		int port = (new Integer(args[1])).intValue();

		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("http://" + host + ":" + port + "/xmlrpc"));
			config.setEnabledForExtensions(true);
			config.setConnectionTimeout(60*1000);
			config.setReplyTimeout(60*1000);
			XmlRpcClient client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			
			HashMap<String, String> mapNames = new HashMap<String, String>();
			mapNames.put("FIRST_STRING", "George Bush");
			mapNames.put("SECOND_STRING", "George W Bush");

			Object[] params = new Object[] {mapNames};

			@SuppressWarnings("unchecked")
			HashMap<String, String> result = (HashMap<String, String>)client.execute("EntityComparison.compare", params);
			
			System.out.println("Score: " + result.get("SCORE"));
			System.out.println("Reason: " + result.get("REASON"));
		}
		catch (Exception exception) {
			System.err.println("JavaClient: " + exception);
		}
	}
}
