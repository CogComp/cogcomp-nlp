package edu.illinois.cs.cogcomp.quant.driver;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorClient;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;

/**
 * An annotation preprocessor used by all the modules. Can use either the {@link CuratorClient}
 * or {@link PipelineAnnotator}. The configurations parameters are set in {@link PreprocessorConfigurator} and
 * should be merged with {@link ESRLConfigurator}.
 */
public class Preprocessor {

	public enum Type {pipeline, curator}
    private final ResourceManager rm;
    private AnnotatorService annotator;

    public Preprocessor(ResourceManager rm) {
    		Map<String, String> nonDefaultValues = new HashMap<String, String>();
    		nonDefaultValues.put(CuratorConfigurator.RESPECT_TOKENIZATION.key, Configurator.TRUE);
        this.rm = Configurator.mergeProperties(rm, new CuratorConfigurator().getConfig(nonDefaultValues));
        	try {
        		annotator = CuratorFactory.buildCuratorClient(this.rm);
        	} catch (Exception e) {
        		e.printStackTrace();
		}
    }

    /**
     * Add the required views to the {@link TextAnnotation}. The views are specified in
     * {@link PreprocessorConfigurator#VIEWS_TO_ADD}.
     *
     * @param ta The {@link TextAnnotation} to be annotated
     * @return Whether new views were added
     */
    public boolean annotate(TextAnnotation ta) throws AnnotatorException {
        boolean addedViews = false;
        for (String view : rm.getCommaSeparatedValues(PreprocessorConfigurator.VIEWS_TO_ADD)) {
            if (ta.hasView(view)) continue;
            annotator.addView(ta, view);
            addedViews = true;
        }
        return addedViews;
    }
}