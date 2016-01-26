package edu.illinois.cs.cogcomp.comma.annotators;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

/**
 * A class that contains all the necessary pre-processing for each sentence.
 */
public class PreProcessor{
    private final AnnotatorService annotatorService;
    Tokenizer tokenizer = new IllinoisTokenizer();

    public PreProcessor() throws Exception  {
        // Initialise AnnotatorServices with default configurations
        Map<String, String> nonDefaultValues = new HashMap<>();
        if (CommaProperties.getInstance().useCurator()){
        	nonDefaultValues.put(CuratorConfigurator.RESPECT_TOKENIZATION.key, CuratorConfigurator.TRUE);
        	nonDefaultValues.put(CuratorConfigurator.CURATOR_FORCE_UPDATE.key, CuratorConfigurator.FALSE);
        	ResourceManager curatorConfig = (new CuratorConfigurator()).getConfig(nonDefaultValues);
            annotatorService = CuratorFactory.buildCuratorClient(curatorConfig);
        }
        else {
            nonDefaultValues.put(CuratorConfigurator.RESPECT_TOKENIZATION.key, CuratorConfigurator.TRUE);
            ResourceManager pipelineConfig = (new CuratorConfigurator()).getConfig(nonDefaultValues);
            annotatorService = IllinoisPipelineFactory.buildPipeline(pipelineConfig);
        }
    }

    public TextAnnotation preProcess(List<String[]> text) throws AnnotatorException{
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(text);
        addViewsFromAnnotatorService(ta);
        return ta;
    }
    
    public TextAnnotation preProcess(String text) throws AnnotatorException{
    	String[] tokens = tokenizer.tokenizeSentence(text).getFirst();
    	return preProcess(Collections.singletonList(tokens));
    }

    private void addViewsFromAnnotatorService(TextAnnotation ta) throws AnnotatorException {
        annotatorService.addView(ta, ViewNames.POS);
        annotatorService.addView(ta, ViewNames.NER_CONLL);
        annotatorService.addView(ta, ViewNames.SHALLOW_PARSE);
        annotatorService.addView(ta, ViewNames.PARSE_STANFORD);
        if (CommaProperties.getInstance().useCurator()) {
            annotatorService.addView(ta, ViewNames.PARSE_CHARNIAK);
            annotatorService.addView(ta, ViewNames.SRL_VERB);
            annotatorService.addView(ta, ViewNames.SRL_NOM);
            annotatorService.addView(ta, ViewNames.SRL_PREP);
        }
    }
}