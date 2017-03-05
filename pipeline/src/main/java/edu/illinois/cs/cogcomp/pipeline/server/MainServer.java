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

import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.internal.HelpScreenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

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
            logger.info("Starting to load the pipeline . . . ");
            printMemoryDetails();
            ResourceManager rm = new PipelineConfigurator().getDefaultConfig();
            pipeline = PipelineFactory.buildPipeline(rm);
            logger.info("Done with loading the pipeline  . . .");
            printMemoryDetails();
        } catch (IOException | AnnotatorException e) {
            e.printStackTrace();
        }

        AnnotatorService finalPipeline = pipeline;
        get("/annotate", "application/json", (request, response)->{
            logger.info("GET request . . . ");
            logger.info( "request.body(): " + request.body());
            String text = request.queryParams("text");
            String views = request.queryParams("views");
            return annotateText(finalPipeline, text, views);
        });

        post("/annotate", (request, response) -> {
                logger.info("POST request . . . ");
                logger.info( "request.body(): " + request.body());
                Map<String, String> map = splitQuery(request.body());
                System.out.println("POST body parameters parsed: " + map);
                String text = map.get("text");
                String views = map.get("views");
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
            logger.info("------------------------------");
            logger.info("Text: " + text);
            logger.info("Views to add: " + views);
            String[] viewsInArray = views.split(",");
            logger.info("Adding the basic annotations . . . ");
            TextAnnotation ta = finalPipeline.createBasicTextAnnotation("", "", text);
            for (String vuName : viewsInArray) {
                logger.info("Adding the view: ->" + vuName.trim() + "<-");
                try {
                    finalPipeline.addView(ta, vuName.trim());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                printMemoryDetails();
            }
            logger.info("Done adding the views. Deserializing the view now.");
            String output = SerializationHelper.serializeToJson(ta);
            logger.info("Done. Sending the result back. ");
            return output;
        }
    }

    public static void printMemoryDetails() {
        int mb = 1024 * 1024;

        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        // Print used memory
        logger.info("##### Used Memory[MB]:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        // Print free memory
        logger.info(" / Free Memory[MB]:" + runtime.freeMemory() / mb);

        // Print total available memory
        logger.info(" / Total Memory[MB]:" + runtime.totalMemory() / mb);

        // Print Maximum available memory
        logger.info(" / Max Memory[MB]:" + runtime.maxMemory() / mb);
    }

    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
