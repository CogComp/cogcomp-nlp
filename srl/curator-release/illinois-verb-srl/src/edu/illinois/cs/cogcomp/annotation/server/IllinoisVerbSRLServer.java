/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
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
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisVerbSRLHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IllinoisVerbSRLServer extends IllinoisAbstractServer{
    
    public IllinoisVerbSRLServer( Class c ){
        super( c );
    }
    
    public IllinoisVerbSRLServer( Class c, int threads, int port, String configFile){
         super(c, threads, port, configFile);
     }
    
    
    public static void main(String[] args) {

	
	
        IllinoisVerbSRLServer s = new IllinoisVerbSRLServer(IllinoisVerbSRLServer.class );

        Options options = createOptions();
        s.parseCommandLine(options, args, "9390", "2", "");
        
	
	Parser.Iface handler = new IllinoisVerbSRLHandler( s.configFile );
	Parser.Processor processor = new Parser.Processor(handler);

	s.runServer( processor );

    }
}