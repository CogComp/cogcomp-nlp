/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.server.MainServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClausIEServer {
    private static Logger logger = LoggerFactory.getLogger(ClausIEServer.class);

    public static void main(String[] args) throws IOException, AnnotatorException {
        System.out.println("Starting create the externals annotators pipeline . . . ");
        AnnotatorService service = ClauseAnnotatorFactory.buildPipeline(new PipelineConfigurator().getDefaultConfig());
        System.out.println("Setting the service . . . ");
        MainServer.setAnnotatorService(service);
        System.out.println("Start the server . . . ");
        MainServer.startServer(args, logger);
    }
}
