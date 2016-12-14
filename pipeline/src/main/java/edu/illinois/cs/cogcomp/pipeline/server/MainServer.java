package edu.illinois.cs.cogcomp.pipeline.server;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

import java.io.IOException;

import static spark.Spark.*;

public class MainServer {

    public static void main(String[] args) {
        AnnotatorService pipeline = null;
        try {
            ResourceManager rm = new PipelineConfigurator().getDefaultConfig();
            pipeline = PipelineFactory.buildPipeline(rm);
        } catch (IOException | AnnotatorException e) {
            e.printStackTrace();
        }

        AnnotatorService finalPipeline = pipeline;
        get("/annotate", "application/json", (request, response)->{
            String text = request.queryParams("text");
            String views = request.queryParams("views");
            String output = "";
            if(views == null || text == null) {
                output = "The parameters 'text' and/or 'views' are not specified. Here is a sample input:  \n ?text=\"This is a sample sentence. I'm happy.\"&views=\"pos,ner\"";
            }
            else {
                String[] viewsInArray = views.split(",");
                TextAnnotation ta = finalPipeline.createBasicTextAnnotation( "", "", text);
                output = SerializationHelper.serializeToJson(ta);
            }
            return output;
        });

        post("/annotate", (request, response) ->
                "Not implemented yet: " + request.body()
        );
    }
}
