package edu.illinois.cs.cogcomp.comma.annotators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;
import edu.illinois.cs.cogcomp.nlp.utilities.BasicTextAnnotationBuilder;

/**
 * A class that contains all the necessary pre-processing for each sentence.
 */
public class PreProcessor{
    private final AnnotatorService annotatorService;

    public PreProcessor() throws Exception {
        // Initialise AnnotatorServices with default configurations
        if (CommaProperties.getInstance().useCurator()){
        	Map<String, String> nonDefaultValues = new HashMap<>();
        	nonDefaultValues.put(CuratorConfigurator.RESPECT_TOKENIZATION.key, CuratorConfigurator.TRUE);
        	nonDefaultValues.put(CuratorConfigurator.CURATOR_FORCE_UPDATE.key, CuratorConfigurator.TRUE);
        	ResourceManager cachingCuratorConfig = (new CuratorConfigurator()).getConfig(nonDefaultValues);
            annotatorService = CuratorFactory.buildCuratorClient(cachingCuratorConfig);
        }
        else
            annotatorService = IllinoisPipelineFactory.buildPipeline();
    }

    public TextAnnotation preProcess(List<String[]> text) throws AnnotatorException{
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(text);
        addViewsFromAnnotatorService(ta);
        return ta;
    }

    private void addViewsFromAnnotatorService(TextAnnotation ta) throws AnnotatorException {
        annotatorService.addView(ta, ViewNames.NER_CONLL);
        annotatorService.addView(ta, ViewNames.SHALLOW_PARSE);
        annotatorService.addView(ta, ViewNames.PARSE_STANFORD);
        annotatorService.addView(ta, ViewNames.PARSE_CHARNIAK);
        annotatorService.addView(ta, ViewNames.POS);
        //annotatorService.addView(ta, ViewNames.SRL_VERB);
        //annotatorService.addView(ta, ViewNames.SRL_NOM);
        annotatorService.addView(ta, ViewNames.SRL_PREP);
    }
}