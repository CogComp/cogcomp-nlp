package edu.illinois.cs.cogcomp.entitySimilarity.xmlrpc;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import edu.illinois.cs.cogcomp.entitySimilarity.compare.*;

public class ECServer {
	
	public static void main(String[] args) {
		if (args.length != 1)
			System.err.println("Usage: ECServer port");
		int port = (new Integer(args[0])).intValue();
		
		try {
			System.out.println("Attempting to start XML-RPC Server...");
			WebServer server = new WebServer(port);
			XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.addHandler("EntityComparison", EntityComparison.class);
			xmlRpcServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl)xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);
			server.start();
			System.out.println("Started successfully.");
			System.out.println("Accepting requests. (Halt program to stop.)");
		} 
		catch (Exception exception) {
			System.err.println("ECServer: " + exception);
		}
	}
}
