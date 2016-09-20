package edu.illinois.cs.cogcomp.annotation.server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.illinois.cs.cogcomp.thrift.parser.Parser;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisNomSRLHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IllinoisNomSRLServer extends IllinoisAbstractServer{
    
    public IllinoisNomSRLServer( Class c ){
        super( c );
    }
    
    public IllinoisNomSRLServer( Class c, int threads, int port, String configFile){
         super(c, threads, port, configFile);
     }
    
    
    public static void main(String[] args) {

	
	
        IllinoisNomSRLServer s = new IllinoisNomSRLServer(IllinoisNomSRLServer.class );

        Options options = createOptions();
        s.parseCommandLine(options, args, "9390", "2", "");
        
	
	Parser.Iface handler = new IllinoisNomSRLHandler( s.configFile );
	Parser.Processor processor = new Parser.Processor(handler);

	s.runServer( processor );

    }
}