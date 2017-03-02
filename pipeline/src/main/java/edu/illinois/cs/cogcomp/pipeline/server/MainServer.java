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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.internal.HelpScreenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static spark.Spark.*;

public class MainServer {

    private static Logger logger = LoggerFactory.getLogger(MainServer.class);

    private static ArgumentParser argumentParser;

    static {
        // Setup Argument Parser with options.
        argumentParser =
                ArgumentParsers.newArgumentParser("pipeline/scripts/runWebserver.sh").description(
                        "Pipeline Webserver.");
        argumentParser.addArgument("--port", "-P").type(Integer.class).setDefault(8080)
                .dest("port").help("Port to run the webserver.");
    }

    public static void main(String[] args) {
        Namespace parseResults;

        try {
             parseResults = argumentParser.parseArgs(args);
        } catch (HelpScreenException ex) {
            return;
        } catch (ArgumentParserException ex) {
            logger.error("Exception while parsing arguments", ex);
            return;
        }

        port(parseResults.getInt("port"));

        AnnotatorService pipeline = null;
        try {
            logger.debug("Starting to load the pipeline . . . ");
            printMemoryDetails();
            ResourceManager rm = new PipelineConfigurator().getDefaultConfig();
            pipeline = PipelineFactory.buildPipeline(rm);
            logger.debug("Done with loading the pipeline  . . .");
            printMemoryDetails();
        } catch (IOException | AnnotatorException e) {
            e.printStackTrace();
        }

        AnnotatorService finalPipeline = pipeline;
        get("/annotate", "application/json", (request, response)->{
            String text = request.queryParams("text");
            String views = request.queryParams("views");
            return annotateText(finalPipeline, text, views);
        });

        post("/annotate", (request, response) -> {
                String text = request.queryParams("text");
                String views = request.queryParams("views");
                return annotateText(finalPipeline, text, views);
            }
        );

        // api to get name of the available views
        String viewsString = "";
        for(String view : pipeline.getAvailableViews()) {
            viewsString += ", " + view;
        }
        String finalViewsString = viewsString;
        get("/viewNames", (req, res) -> finalViewsString);

        post("/viewNames", (req, res) -> finalViewsString);
    }

    private static String annotateText(AnnotatorService finalPipeline, String text, String views)
            throws AnnotatorException {
        if (views == null || text == null) {
            return "The parameters 'text' and/or 'views' are not specified. Here is a sample input:  \n ?text=\"This is a sample sentence. I'm happy.\"&views=POS,NER";
        } else {
            logger.trace("------------------------------");
            logger.trace("Text: " + text);
            logger.trace("Views to add: " + views);
            String[] viewsInArray = views.split(",");
            logger.trace("Adding the basic annotations . . . ");
            TextAnnotation ta = finalPipeline.createBasicTextAnnotation("", "", text);
            for (String vuName : viewsInArray) {
                logger.debug("Adding the view: ->" + vuName.trim() + "<-");
                finalPipeline.addView(ta, vuName.trim());
                printMemoryDetails();
            }
            logger.trace("Done adding the views. Deserializing the view now.");
            String output = SerializationHelper.serializeToJson(ta);
            logger.debug("Done. Sending the result back. ");
            return output;
        }
    }

    public static void printMemoryDetails() {
        int mb = 1024 * 1024;

        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        // Print used memory
        logger.debug("##### Used Memory[MB]:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        // Print free memory
        logger.debug(" / Free Memory[MB]:" + runtime.freeMemory() / mb);

        // Print total available memory
        logger.debug(" / Total Memory[MB]:" + runtime.totalMemory() / mb);

        // Print Maximum available memory
        logger.debug(" / Max Memory[MB]:" + runtime.maxMemory() / mb);
    }

}
