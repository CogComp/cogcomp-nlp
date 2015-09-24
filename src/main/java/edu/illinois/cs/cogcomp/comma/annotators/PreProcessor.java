package edu.illinois.cs.cogcomp.comma.annotators;

import java.util.List;
import java.util.Properties;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigNames;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.nlp.utilities.BasicTextAnnotationBuilder;

/**
 * A class that contains all the necessary pre-processing for each sentence.
 */
public class PreProcessor{
    private final AnnotatorService annotatorService;

    public PreProcessor() throws Exception {
    	Properties cachingcuratorProperties = new Properties();
    	cachingcuratorProperties.setProperty(CuratorConfigNames.CURATOR_HOST, "trollope.cs.illinois.edu");
    	cachingcuratorProperties.setProperty(CuratorConfigNames.CURATOR_PORT, "9010");
    	cachingcuratorProperties.setProperty(CuratorConfigNames.RESPECT_TOKENIZATION, "true");
    	cachingcuratorProperties.setProperty(CuratorConfigNames.CURATOR_FORCE_UPDATE, "false");
    	
    	cachingcuratorProperties.setProperty(AnnotatorService.CACHE_DIR, AnnotatorService.DEFAULT_CACHE_DIR);
    	cachingcuratorProperties.setProperty(AnnotatorService.CACHE_DISK_SIZE, "" + AnnotatorService.DEFAULT_CACHE_DISK_SIZE);
    	cachingcuratorProperties.setProperty(AnnotatorService.CACHE_HEAP_SIZE, "" + AnnotatorService.DEFAULT_CACHE_HEAP_SIZE);
    	cachingcuratorProperties.setProperty(AnnotatorService.SET_CACHE_SHUTDOWN_HOOK, "" + AnnotatorService.DEFAULT_SET_CACHE_SHUTDOWN_HOOK);
    	cachingcuratorProperties.setProperty(AnnotatorService.THROW_EXCEPTION_IF_NOT_CACHED, "" + AnnotatorService.DEFAULT_THROW_EXCEPTION_IF_UNCACHED);
    	
    	ResourceManager cachingCuratorConfig = new ResourceManager(cachingcuratorProperties);
        annotatorService = CuratorFactory.buildCuratorClient(cachingCuratorConfig);
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
        annotatorService.addView(ta, ViewNames.SRL_VERB);
        annotatorService.addView(ta, ViewNames.SRL_NOM);
        annotatorService.addView(ta, ViewNames.SRL_PREP);
    }
    
    public void closeCache(){
    	annotatorService.closeCache();
    }
}