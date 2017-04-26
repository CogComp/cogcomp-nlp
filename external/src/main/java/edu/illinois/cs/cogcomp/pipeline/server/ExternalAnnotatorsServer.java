/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.server;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.pipeline.main.ExternalAnnotatorServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExternalAnnotatorsServer {
    private static Logger logger = LoggerFactory.getLogger(ExternalAnnotatorsServer.class);

    public static void main(String[] args) throws IOException, AnnotatorException {
        logger.info("Starting create the externals annotators pipeline . . . ");
        AnnotatorService service = ExternalAnnotatorServiceFactory.buildPipeline();
        logger.info("Setting the service . . . ");
        MainServer.setAnnotatorSetvice(service, logger);
        logger.info("Start the server . . . ");
        MainServer.startServer(args, logger);
    }
}
