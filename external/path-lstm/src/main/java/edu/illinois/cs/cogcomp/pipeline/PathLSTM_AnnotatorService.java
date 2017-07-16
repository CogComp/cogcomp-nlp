package edu.illinois.cs.cogcomp.pipeline;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.pipeline.handlers.PathLSTMHandler;
import edu.illinois.cs.cogcomp.pipeline.main.ExternalAnnotatorServiceFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PathLSTM_AnnotatorService {
    public static AnnotatorService service = null;
    public static void initialize() throws IOException, AnnotatorException {
        Map<String, Annotator> viewGenerators = new HashMap<>();
        PathLSTMHandler pathSRL = new PathLSTMHandler(true);
        viewGenerators.put(pathSRL.getViewName(), pathSRL);
        service = ExternalAnnotatorServiceFactory.buildPipeline(viewGenerators);
    }
}
